// Mote 云同步账号 + 数据云函数（HTTP 云函数，公开路由 /moteauth）
//
// 不用 CloudBase 身份认证：账号存在 Users 集合，登录由本函数签发 HMAC token，
// 同步数据读写也走这里（SyncData 集合），因此不受身份认证「组织成员」配额限制。
//
// 部署：tcb fn deploy auth --force
// 环境变量：MOTE_SECRET（token 签名密钥，云函数配置里设置）

const http = require('http')
const crypto = require('crypto')
const tcb = require('@cloudbase/node-sdk')

const SECRET = process.env.MOTE_SECRET || 'mote-default-secret-please-change'
const USERNAME_RE = /^[A-Za-z0-9][A-Za-z0-9._-]{2,47}$/
const TOKEN_TTL_MS = 30 * 24 * 3600 * 1000
const REFRESH_TTL_MS = 365 * 24 * 3600 * 1000

const app = tcb.init({ env: process.env.TCB_ENV || process.env.SCF_NAMESPACE })
const db = app.database()

function send(res, status, payload) {
  const body = JSON.stringify(payload)
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(body),
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    'Access-Control-Allow-Methods': 'POST, GET, OPTIONS',
  })
  res.end(body)
}

const ok = (res, data) => send(res, 200, { code: 0, data })
const fail = (res, message, code) => send(res, 200, { code: code || -1, message: String(message) })

async function readBody(req) {
  const chunks = []
  for await (const chunk of req) chunks.push(chunk)
  const raw = Buffer.concat(chunks).toString('utf8')
  if (!raw) return {}
  try {
    return JSON.parse(raw)
  } catch (e) {
    return {}
  }
}

// ---------- token ----------
function b64u(input) {
  return Buffer.from(input).toString('base64url')
}

function issueToken(payload) {
  const header = b64u(JSON.stringify({ alg: 'HS256', typ: 'JWT' }))
  const body = b64u(JSON.stringify(payload))
  const data = `${header}.${body}`
  const sig = crypto.createHmac('sha256', SECRET).update(data).digest('base64url')
  return `${data}.${sig}`
}

function verifyToken(token) {
  const parts = String(token || '').split('.')
  if (parts.length !== 3) return null
  const data = `${parts[0]}.${parts[1]}`
  const expect = crypto.createHmac('sha256', SECRET).update(data).digest('base64url')
  if (parts[2] !== expect) return null
  try {
    const payload = JSON.parse(Buffer.from(parts[1], 'base64url').toString('utf8'))
    if (payload.exp && Date.now() > payload.exp) return null
    return payload
  } catch (e) {
    return null
  }
}

function verifyAccess(token) {
  const payload = verifyToken(token)
  if (!payload) return null
  if (payload.typ && payload.typ !== 'access') return null
  return payload
}

// ---------- 密码 ----------
function hashPassword(password, salt) {
  return crypto.pbkdf2Sync(String(password), salt, 120000, 64, 'sha256').toString('hex')
}

function newSalt() {
  return crypto.randomBytes(16).toString('hex')
}

function newUid() {
  return crypto.randomBytes(12).toString('hex')
}

async function findUser(username) {
  const res = await db.collection('Users').where({ username }).limit(1).get()
  const list = (res && res.data) || []
  return list.length ? list[0] : null
}

async function handleRegister(body) {
  const username = String(body.username || '').trim().toLowerCase()
  const password = String(body.password || '')
  if (!USERNAME_RE.test(username)) {
    return { error: '用户名需 3~48 位，字母或数字开头，只能包含字母、数字、点、下划线、短横线' }
  }
  if (password.length < 8 || password.length > 32) {
    return { error: '密码长度需 8~32 位' }
  }
  const existing = await findUser(username)
  if (existing) return { error: '用户名已被占用', code: 409 }

  const salt = newSalt()
  const uid = newUid()
  const res = await db.collection('Users').add({
    username,
    uid,
    salt,
    hash: hashPassword(password, salt),
    created_at: Date.now(),
  })
  return { uid, username, id: (res && res.id) || null }
}

async function handleLogin(body) {
  const username = String(body.username || '').trim().toLowerCase()
  const password = String(body.password || '')
  if (!username || !password) return { error: '请输入用户名和密码' }

  const user = await findUser(username)
  if (!user) return { error: '账号不存在或密码错误', code: 401 }
  if (user.disabled) return { error: '账号已被停用', code: 403 }

  const hash = hashPassword(password, user.salt)
  if (hash !== user.hash) return { error: '账号不存在或密码错误', code: 401 }

  const token = issueToken({
    uid: user.uid,
    username: user.username,
    typ: 'access',
    exp: Date.now() + TOKEN_TTL_MS,
  })
  const refresh = issueToken({
    uid: user.uid,
    username: user.username,
    typ: 'refresh',
    exp: Date.now() + REFRESH_TTL_MS,
  })
  return { token, refresh, uid: user.uid, username: user.username }
}

async function handlePull(body) {
  const payload = verifyAccess(body.token)
  if (!payload) return { error: '登录已过期，请重新登录', code: 401 }

  const res = await db.collection('SyncData').where({ user_id: payload.uid }).limit(1).get()
  const list = (res && res.data) || []
  if (!list.length) return { content: '', updated_at: 0 }
  return { content: list[0].content || '', updated_at: list[0].updated_at || 0 }
}

async function handlePush(body) {
  const payload = verifyAccess(body.token)
  if (!payload) return { error: '登录已过期，请重新登录', code: 401 }
  const content = String(body.content || '')
  if (!content) return { error: '内容为空' }

  const res = await db.collection('SyncData').where({ user_id: payload.uid }).limit(1).get()
  const list = (res && res.data) || []
  const now = Date.now()
  if (list.length) {
    await db.collection('SyncData').doc(list[0]._id).update({ content, updated_at: now })
  } else {
    await db.collection('SyncData').add({ user_id: payload.uid, content, updated_at: now })
  }
  return { ok: true, updated_at: now }
}

async function handleRefresh(body) {
  const payload = verifyToken(body.refresh)
  if (!payload || payload.typ !== 'refresh') {
    return { error: '登录已过期，请重新登录', code: 401 }
  }
  const token = issueToken({
    uid: payload.uid,
    username: payload.username,
    typ: 'access',
    exp: Date.now() + TOKEN_TTL_MS,
  })
  return { token, uid: payload.uid, username: payload.username }
}

// ---------- 管理端 ----------
function requireAdmin(body) {
  const adminKey = process.env.ADMIN_KEY || ''
  if (!adminKey) return '服务端未配置 ADMIN_KEY 环境变量'
  if (String(body.key || '') !== adminKey) return '管理员密钥错误'
  return null
}

async function handleAdminList(body) {
  const authError = requireAdmin(body)
  if (authError) return { error: authError, code: 401 }

  const res = await db.collection('Users').limit(1000).get()
  const users = (res && res.data) || []
  const out = []
  for (const u of users) {
    let syncAt = 0
    let syncSize = 0
    try {
      const s = await db.collection('SyncData').where({ user_id: u.uid }).limit(1).get()
      const doc = ((s && s.data) || [])[0]
      if (doc) {
        syncAt = doc.updated_at || 0
        syncSize = String(doc.content || '').length
      }
    } catch (e) {}
    out.push({
      username: u.username,
      uid: u.uid,
      created_at: u.created_at || 0,
      disabled: !!u.disabled,
      sync_at: syncAt,
      sync_size: syncSize,
    })
  }
  out.sort((a, b) => (b.created_at || 0) - (a.created_at || 0))
  return { total: out.length, users: out }
}

async function handleAdminResetPassword(body) {
  const authError = requireAdmin(body)
  if (authError) return { error: authError, code: 401 }
  const username = String(body.username || '').trim().toLowerCase()
  const newPassword = String(body.newPassword || '')
  if (newPassword.length < 8 || newPassword.length > 32) return { error: '密码长度需 8~32 位' }
  const user = await findUser(username)
  if (!user) return { error: '账号不存在', code: 404 }
  const salt = newSalt()
  await db.collection('Users').doc(user._id).update({ salt, hash: hashPassword(newPassword, salt) })
  return { ok: true, username, uid: user.uid }
}

async function handleAdminDisable(body) {
  const authError = requireAdmin(body)
  if (authError) return { error: authError, code: 401 }
  const username = String(body.username || '').trim().toLowerCase()
  const disabled = !!body.disabled
  const user = await findUser(username)
  if (!user) return { error: '账号不存在', code: 404 }
  await db.collection('Users').doc(user._id).update({ disabled })
  return { ok: true, username, disabled }
}

async function handleAdminDelete(body) {
  const authError = requireAdmin(body)
  if (authError) return { error: authError, code: 401 }
  const username = String(body.username || '').trim().toLowerCase()
  const user = await findUser(username)
  if (!user) return { error: '账号不存在', code: 404 }
  await deleteUserData(user)
  return { ok: true, username, uid: user.uid }
}

async function deleteUserData(user) {
  try {
    const s = await db.collection('SyncData').where({ user_id: user.uid }).limit(1).get()
    const doc = ((s && s.data) || [])[0]
    if (doc) await db.collection('SyncData').doc(doc._id).remove()
  } catch (e) {}
  await db.collection('Users').doc(user._id).remove()
}

// ---------- 用户自助 ----------
async function handleChangePassword(body) {
  const payload = verifyAccess(body.token)
  if (!payload) return { error: '登录已过期，请重新登录', code: 401 }
  const user = await findUser(payload.username)
  if (!user) return { error: '账号不存在', code: 404 }
  const oldPassword = String(body.oldPassword || '')
  if (hashPassword(oldPassword, user.salt) !== user.hash) return { error: '旧密码错误', code: 403 }
  const newPassword = String(body.newPassword || '')
  if (newPassword.length < 8 || newPassword.length > 32) return { error: '密码长度需 8~32 位' }
  const salt = newSalt()
  await db.collection('Users').doc(user._id).update({ salt, hash: hashPassword(newPassword, salt) })
  return { ok: true }
}

async function handleDeleteAccount(body) {
  const payload = verifyAccess(body.token)
  if (!payload) return { error: '登录已过期，请重新登录', code: 401 }
  const user = await findUser(payload.username)
  if (!user) return { error: '账号不存在', code: 404 }
  if (hashPassword(String(body.password || ''), user.salt) !== user.hash) {
    return { error: '密码错误', code: 403 }
  }
  await deleteUserData(user)
  return { ok: true }
}

const handlers = {
  register: handleRegister,
  login: handleLogin,
  refresh: handleRefresh,
  pull: handlePull,
  push: handlePush,
  changePassword: handleChangePassword,
  deleteAccount: handleDeleteAccount,
  adminList: handleAdminList,
  adminResetPassword: handleAdminResetPassword,
  adminDisable: handleAdminDisable,
  adminDelete: handleAdminDelete,
}

const server = http.createServer(async (req, res) => {
  try {
    if (req.method === 'OPTIONS') return send(res, 204, {})
    if (req.method === 'GET') {
      return ok(res, { pong: true, env: process.env.TCB_ENV || process.env.SCF_NAMESPACE })
    }
    if (req.method !== 'POST') return send(res, 405, { code: -1, message: '只支持 POST' })

    const body = await readBody(req)
    const action = body.action || 'ping'
    if (action === 'ping') return ok(res, { pong: true })

    const handler = handlers[action]
    if (!handler) return fail(res, '不支持的 action: ' + action)

    const result = await handler(body)
    if (result.error) return fail(res, result.error, result.code)
    return ok(res, result)
  } catch (err) {
    const text = (err && (err.message || err.errorMessage)) || String(err)
    console.error('[mote-auth] failed:', text)
    if (/collection not exist/i.test(text)) {
      return fail(res, '数据库集合不存在，请在控制台创建 Users 和 SyncData 集合')
    }
    return fail(res, text)
  }
})

server.listen(9000, '0.0.0.0', () => {
  console.log('[mote-auth] listening on 9000')
})
