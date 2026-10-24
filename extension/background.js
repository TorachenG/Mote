// Mote 书签同步 · 后台服务
// 与 Mote App 使用同一套协议：云函数 auth + AES-256-GCM 整包加密

const t = (k, subs) => { try { return chrome.i18n.getMessage(k, subs) || k; } catch (e) { return k; } };

const CLOUD_DEFAULT_URL =
  "https://mote-sync-d9geb238o1c1ea40e-1515028654.ap-shanghai.app.tcloudbase.com/moteauth";

const FOLDER_NAME = "Mote";
const ALARM_NAME = "mote-sync";
const DEBOUNCE_MS = 6000;
const SALT_NORMAL = "MoteNormalSalt";
const SALT_E2EE = "MoteE2EESalt";
const PBKDF2_ITER = 10000;

const DEFAULTS = {
  provider: "cloud",            // cloud | webdav
  cloudUrl: CLOUD_DEFAULT_URL,
  username: "",
  password: "",
  uid: "",
  accessToken: "",
  refreshToken: "",
  e2eeKey: "",                  // 端到端原始密钥（base64）
  webdavUrl: "",
  webdavUser: "",
  webdavPass: "",
  autoSync: true,
  intervalMinutes: 5,
  snapshot: "",                 // 上次同步后的书签树（用于识别删除）
  lastSyncAt: 0,
  lastError: "",
  lastStatus: "idle",
  moteFolderId: ""
};

/* ---------------- 存储 ---------------- */

async function getSettings() {
  const stored = await chrome.storage.local.get(null);
  return Object.assign({}, DEFAULTS, stored);
}

async function setSettings(patch) {
  await chrome.storage.local.set(patch);
  return getSettings();
}

/* ---------------- 工具 ---------------- */

function uuid() {
  if (crypto.randomUUID) return crypto.randomUUID();
  const b = crypto.getRandomValues(new Uint8Array(16));
  b[6] = (b[6] & 0x0f) | 0x40;
  b[8] = (b[8] & 0x3f) | 0x80;
  const h = [...b].map((x) => x.toString(16).padStart(2, "0")).join("");
  return `${h.slice(0, 8)}-${h.slice(8, 12)}-${h.slice(12, 16)}-${h.slice(16, 20)}-${h.slice(20)}`;
}

function bytesToBase64(bytes) {
  let binary = "";
  const chunk = 0x8000;
  for (let i = 0; i < bytes.length; i += chunk) {
    binary += String.fromCharCode.apply(null, bytes.subarray(i, i + chunk));
  }
  return btoa(binary);
}

function base64ToBytes(b64) {
  const binary = atob(b64);
  const out = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) out[i] = binary.charCodeAt(i);
  return out;
}

/* ---------------- 加密 ---------------- */

async function deriveAesKey(material, salt) {
  const enc = new TextEncoder();
  const baseKey = await crypto.subtle.importKey(
    "raw", enc.encode(material), "PBKDF2", false, ["deriveBits"]
  );
  const bits = await crypto.subtle.deriveBits(
    { name: "PBKDF2", salt: enc.encode(salt), iterations: PBKDF2_ITER, hash: "SHA-256" },
    baseKey,
    256
  );
  return crypto.subtle.importKey("raw", bits, "AES-GCM", false, ["encrypt", "decrypt"]);
}

async function keyFromBase64(b64) {
  return crypto.subtle.importKey("raw", base64ToBytes(b64), "AES-GCM", false, ["encrypt", "decrypt"]);
}

async function encryptWith(key, plainText) {
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const data = new TextEncoder().encode(plainText);
  const cipher = new Uint8Array(await crypto.subtle.encrypt({ name: "AES-GCM", iv }, key, data));
  const combined = new Uint8Array(iv.length + cipher.length);
  combined.set(iv, 0);
  combined.set(cipher, iv.length);
  return bytesToBase64(combined);
}

async function decryptWith(key, cipherB64) {
  try {
    const combined = base64ToBytes(cipherB64);
    if (combined.length < 28) return null;
    const iv = combined.subarray(0, 12);
    const cipher = combined.subarray(12);
    const plain = await crypto.subtle.decrypt({ name: "AES-GCM", iv }, key, cipher);
    return new TextDecoder().decode(plain);
  } catch (e) {
    return null;
  }
}

async function buildKeys(s) {
  const keys = [];
  if (s.e2eeKey) {
    try {
      keys.push({ kind: "e2ee", key: await keyFromBase64(s.e2eeKey) });
    } catch (e) { /* ignore */ }
  }
  if (s.uid) {
    try {
      keys.push({ kind: "normal", key: await deriveAesKey(s.uid, SALT_NORMAL) });
    } catch (e) { /* ignore */ }
  }
  return keys;
}

async function decryptAny(cipherB64) {
  const s = await getSettings();
  const keys = await buildKeys(s);
  for (const item of keys) {
    const plain = await decryptWith(item.key, cipherB64);
    if (plain != null) return { plain, kind: item.kind };
  }
  return null;
}

async function encryptAny(plainText) {
  const s = await getSettings();
  const keys = await buildKeys(s);
  const preferred = keys.find((k) => k.kind === "e2ee") || keys.find((k) => k.kind === "normal");
  if (!preferred) throw new Error(t("bgNoKey"));
  return encryptWith(preferred.key, plainText);
}

/* ---------------- 云函数接口 ---------------- */

async function apiCall(body) {
  const s = await getSettings();
  const url = (s.cloudUrl || CLOUD_DEFAULT_URL).trim();
  const resp = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });
  const text = await resp.text();
  let json;
  try {
    json = JSON.parse(text);
  } catch (e) {
    throw new Error(t("bgBadResp", [text.slice(0, 120)]));
  }
  if (json.code !== 0) throw new Error(json.message || t("bgReqFail"));
  return json.data || {};
}

async function loginCloud(username, password) {
  const data = await apiCall({ action: "login", username, password });
  const token = data.token || data.access_token || data.accessToken || "";
  const refresh = data.refresh || "";
  const uid = data.uid || "";

  let e2eeKey = "";
  try {
    const raw = await crypto.subtle.deriveBits(
      {
        name: "PBKDF2",
        salt: new TextEncoder().encode(SALT_E2EE),
        iterations: PBKDF2_ITER,
        hash: "SHA-256"
      },
      await crypto.subtle.importKey(
        "raw", new TextEncoder().encode(username + password), "PBKDF2", false, ["deriveBits"]
      ),
      256
    );
    e2eeKey = bytesToBase64(new Uint8Array(raw));
  } catch (e) { /* ignore */ }

  await setSettings({ username, password, uid, accessToken: token, refreshToken: refresh, e2eeKey });
  return { ok: true };
}

async function refreshAccessToken() {
  const s = await getSettings();
  if (!s.refreshToken) return "";
  try {
    const data = await apiCall({ action: "refresh", refresh: s.refreshToken });
    const token = data.token || "";
    if (token) await setSettings({ accessToken: token });
    return token;
  } catch (e) {
    return "";
  }
}

async function callWithAuth(body) {
  const s = await getSettings();
  body.token = s.accessToken || "";
  try {
    return await apiCall(body);
  } catch (e) {
    if (!/登录|令牌|token|过期|未授权|401|权限/i.test(e.message)) throw e;
    const token = await refreshAccessToken();
    if (!token) throw e;
    body.token = token;
    return await apiCall(body);
  }
}

/* ---------------- 远端读写 ---------------- */

async function readRemote() {
  const s = await getSettings();
  if (s.provider === "webdav") {
    if (!s.webdavUrl) throw new Error(t("bgNeedDav"));
    const resp = await fetch(s.webdavUrl, {
      method: "GET",
      headers: { Authorization: "Basic " + btoa(`${s.webdavUser}:${s.webdavPass}`) }
    });
    if (resp.status === 404) return { content: "", updatedAt: 0 };
    if (!resp.ok) throw new Error(t("bgDavRead", [String(resp.status)]));
    return { content: (await resp.text()).trim(), updatedAt: 0 };
  }
  const data = await callWithAuth({ action: "pull" });
  return { content: data.content || "", updatedAt: data.updated_at || 0 };
}

async function writeRemote(cipher) {
  const s = await getSettings();
  if (s.provider === "webdav") {
    const resp = await fetch(s.webdavUrl, {
      method: "PUT",
      headers: {
        Authorization: "Basic " + btoa(`${s.webdavUser}:${s.webdavPass}`),
        "Content-Type": "text/plain"
      },
      body: cipher
    });
    if (!resp.ok) throw new Error(t("bgDavWrite", [String(resp.status)]));
    return;
  }
  await callWithAuth({ action: "push", content: cipher });
}

/* ---------------- 书签树 ---------------- */

async function findFolder(parentId, title) {
  const children = await chrome.bookmarks.getChildren(parentId);
  return children.find((c) => !c.url && c.title === title) || null;
}

async function ensureMoteFolder() {
  const s = await getSettings();
  if (s.moteFolderId) {
    try {
      const node = await chrome.bookmarks.get(s.moteFolderId);
      if (node && node.length) return { id: s.moteFolderId, created: false };
    } catch (e) { /* 不存在，重建 */ }
  }
  let parentId = "1";
  try {
    await chrome.bookmarks.get("1");
  } catch (e) {
    parentId = "2";
  }
  let folder = await findFolder(parentId, FOLDER_NAME);
  let created = false;
  if (!folder) {
    folder = await chrome.bookmarks.create({ parentId, title: FOLDER_NAME });
    created = true;
  }
  await setSettings({ moteFolderId: folder.id });
  return { id: folder.id, created };
}

function chromeNodesToTree(nodes, prevIds) {
  const out = [];
  for (const node of nodes) {
    if (node.url) {
      const key = (node.url || "") + "|" + (node.title || "");
      out.push({
        id: prevIds.get(key) || uuid(),
        title: node.title || node.url,
        url: node.url,
        time: node.dateAdded || Date.now()
      });
    } else {
      out.push({
        id: prevIds.get("[folder]" + (node.title || "")) || uuid(),
        title: node.title || "未命名文件夹",
        folder: true,
        children: chromeNodesToTree(node.children || [], prevIds),
        time: node.dateAdded || Date.now()
      });
    }
  }
  return out;
}

function flattenTree(tree, prefix, out) {
  out = out || { links: new Map(), folders: new Map() };
  for (const node of tree || []) {
    if (node.folder) {
      const path = prefix + "/" + (node.title || "");
      out.folders.set(path, { title: node.title || "未命名文件夹", path, time: node.time || 0 });
      flattenTree(node.children || [], path, out);
    } else {
      const key = prefix + "|" + (node.url || "");
      out.links.set(key, { url: node.url, title: node.title || node.url, time: node.time || 0, path: prefix });
    }
  }
  return out;
}

function buildTree(links, folders) {
  const root = { children: [] };
  const dirs = new Map();
  dirs.set("", root);

  function ensure(path) {
    if (dirs.has(path)) return dirs.get(path);
    const idx = path.lastIndexOf("/");
    const parentPath = idx <= 0 ? "" : path.slice(0, idx);
    const title = path.slice(idx + 1);
    const parent = ensure(parentPath);
    const node = { id: uuid(), title, folder: true, children: [], time: Date.now() };
    parent.children.push(node);
    dirs.set(path, node);
    return node;
  }

  for (const folder of folders.values()) ensure(folder.path);
  for (const link of links.values()) {
    const parent = ensure(link.path || "");
    parent.children.push({
      id: uuid(),
      title: link.title,
      url: link.url,
      time: link.time || Date.now()
    });
  }
  return root.children;
}

function mergeTrees(prev, local, remote) {
  const P = flattenTree(prev, "");
  const L = flattenTree(local, "");
  const R = flattenTree(remote, "");

  const deletedLinks = new Set();
  for (const key of P.links.keys()) {
    if (!L.links.has(key) || !R.links.has(key)) deletedLinks.add(key);
  }
  const deletedFolders = new Set();
  for (const path of P.folders.keys()) {
    if (!L.folders.has(path) || !R.folders.has(path)) deletedFolders.add(path);
  }

  const links = new Map();
  for (const [key, value] of L.links) if (!deletedLinks.has(key)) links.set(key, value);
  for (const [key, value] of R.links) {
    if (!deletedLinks.has(key) && !links.has(key)) links.set(key, value);
  }

  const folders = new Map();
  for (const [path, value] of L.folders) if (!deletedFolders.has(path)) folders.set(path, value);
  for (const [path, value] of R.folders) {
    if (!deletedFolders.has(path) && !folders.has(path)) folders.set(path, value);
  }

  for (const link of links.values()) {
    if (!link.path) continue;
    const parts = link.path.split("/").filter(Boolean);
    let current = "";
    for (const part of parts) {
      current += "/" + part;
      if (!folders.has(current)) folders.set(current, { title: part, path: current, time: 0 });
    }
  }

  return buildTree(links, folders);
}

/** Mote 的书签 JSON 用 isFolder 表示文件夹，内部统一用 folder */
function fromMoteTree(nodes) {
  return (nodes || []).map(function (n) {
    var isFolder = n.isFolder === true || n.folder === true;
    return {
      id: n.id,
      title: n.title || "",
      url: isFolder ? undefined : n.url,
      folder: isFolder,
      time: n.time || 0,
      children: isFolder ? fromMoteTree(n.children || []) : undefined,
    };
  });
}

/** 写回云端时转成 Mote 认识的字段 */
function treeToMoteJson(nodes) {
  return (nodes || []).map(function (n) {
    var isFolder = n.folder === true;
    var out = {
      id: n.id,
      title: n.title || "",
      isFolder: isFolder,
      time: n.time || Date.now(),
    };
    if (isFolder) out.children = treeToMoteJson(n.children || []);
    else out.url = n.url || "";
    return out;
  });
}

function normalizeTree(tree) {
  return JSON.stringify(
    (function strip(nodes) {
      return (nodes || []).map((node) => {
        if (node.folder) {
          return { f: node.title, c: strip(node.children) };
        }
        return { t: node.title, u: node.url };
      });
    })(tree)
  );
}

function idMapFromTree(tree, map) {
  map = map || new Map();
  for (const node of tree || []) {
    if (node.folder) {
      map.set("[folder]" + (node.title || ""), node.id);
      idMapFromTree(node.children || [], map);
    } else {
      map.set((node.url || "") + "|" + (node.title || ""), node.id);
    }
  }
  return map;
}

async function readChromeTree(rootId) {
  const s = await getSettings();
  let prevMap = new Map();
  try {
    if (s.snapshot) prevMap = idMapFromTree(JSON.parse(s.snapshot));
  } catch (e) { /* ignore */ }
  const id = rootId || (await ensureMoteFolder()).id;
  const [rootNode] = await chrome.bookmarks.getSubTree(id);
  return chromeNodesToTree(rootNode.children || [], prevMap);
}

let applying = false;

async function applyChromeTree(tree) {
  applying = true;
  try {
    const rootId = (await ensureMoteFolder()).id;
    const children = await chrome.bookmarks.getChildren(rootId);
    for (const child of children) {
      await chrome.bookmarks.removeTree(child.id);
    }
    await createNodes(rootId, tree);
  } finally {
    setTimeout(() => { applying = false; }, 1200);
  }
}

async function createNodes(parentId, nodes) {
  for (const node of nodes || []) {
    if (node.folder) {
      const created = await chrome.bookmarks.create({ parentId, title: node.title });
      await createNodes(created.id, node.children || []);
    } else if (node.url) {
      await chrome.bookmarks.create({ parentId, title: node.title || node.url, url: node.url });
    }
  }
}

/* ---------------- 同步主流程 ---------------- */

let syncing = false;
let queued = false;
let debounceTimer = null;

async function sync(reason) {
  if (syncing) {
    queued = true;
    return;
  }
  syncing = true;
  await setSettings({ lastStatus: "syncing", lastError: "" });
  try {
    const s = await getSettings();
    if (s.provider === "cloud" && (!s.accessToken || !s.uid)) {
      throw new Error(t("bgNotLogged"));
    }

    const remote = await readRemote();
    const folder = await ensureMoteFolder();
    const localTree = await readChromeTree(folder.id);

    let remoteData = null;
    let remoteTree = [];
    if (remote.content) {
      const decoded = await decryptAny(remote.content);
      if (!decoded) {
        throw new Error(t("bgDecryptFail"));
      }
      remoteData = JSON.parse(decoded.plain);
      try {
        remoteTree = fromMoteTree(JSON.parse(remoteData.bookmarks_v2 || "[]"));
      } catch (e) {
        remoteTree = [];
      }
    }

    let prevTree = [];
    try {
      if (s.snapshot) prevTree = JSON.parse(s.snapshot);
    } catch (e) { /* ignore */ }

    let merged;
    if (!remote.content) {
      merged = localTree;
    } else if (folder.created) {
      // 「Mote」文件夹被删掉后又重建：以云端为准恢复，避免把删除回推上去
      merged = remoteTree;
    } else {
      merged = mergeTrees(prevTree, localTree, remoteTree);
    }

    const mergedNorm = normalizeTree(merged);
    const localChanged = normalizeTree(localTree) !== mergedNorm;
    const remoteChanged = !remote.content || normalizeTree(remoteTree) !== mergedNorm;

    if (localChanged) {
      await applyChromeTree(merged);
    }

    if (remoteChanged) {
      const bag = remoteData || { fallback_key: "" };
      bag.bookmarks_v2 = JSON.stringify(treeToMoteJson(merged));
      const cipher = await encryptAny(JSON.stringify(bag));
      await writeRemote(cipher);
    }

    await setSettings({
      snapshot: JSON.stringify(merged),
      lastSyncAt: Date.now(),
      lastStatus: "ok",
      lastError: ""
    });
  } catch (e) {
    await setSettings({ lastStatus: "error", lastError: e.message || String(e) });
  } finally {
    syncing = false;
    if (queued) {
      queued = false;
      setTimeout(() => sync("queued"), 1500);
    }
  }
}

function scheduleSync() {
  if (debounceTimer) clearTimeout(debounceTimer);
  debounceTimer = setTimeout(() => {
    debounceTimer = null;
    sync("bookmark-change");
  }, DEBOUNCE_MS);
}

async function reconfigure() {
  const s = await getSettings();
  await chrome.alarms.clear(ALARM_NAME);
  if (s.autoSync) {
    const minutes = Math.max(1, Number(s.intervalMinutes) || 5);
    chrome.alarms.create(ALARM_NAME, { periodInMinutes: minutes });
  }
}

/* ---------------- 事件 ---------------- */

chrome.bookmarks.onCreated.addListener(() => { if (!applying) scheduleSync(); });
chrome.bookmarks.onChanged.addListener(() => { if (!applying) scheduleSync(); });
chrome.bookmarks.onRemoved.addListener(() => { if (!applying) scheduleSync(); });
chrome.bookmarks.onMoved.addListener(() => { if (!applying) scheduleSync(); });

chrome.alarms.onAlarm.addListener((alarm) => {
  if (alarm.name === ALARM_NAME) sync("alarm");
});

chrome.runtime.onInstalled.addListener(() => { reconfigure(); });
chrome.runtime.onStartup.addListener(() => { reconfigure(); });

chrome.runtime.onMessage.addListener((msg, sender, sendResponse) => {
  if (msg && msg.type === "sync-now") {
    sync("manual").then(() => sendResponse({ ok: true })).catch(() => sendResponse({ ok: false }));
    return true;
  }
  if (msg && msg.type === "reconfigure") {
    reconfigure().then(() => sendResponse({ ok: true }));
    return true;
  }
  if (msg && msg.type === "login") {
    loginCloud(msg.username || "", msg.password || "")
      .then((r) => sendResponse({ ok: true, result: r }))
      .catch((e) => sendResponse({ ok: false, error: e.message || String(e) }));
    return true;
  }
  if (msg && msg.type === "logout") {
    setSettings({
      accessToken: "",
      refreshToken: "",
      uid: "",
      username: "",
      password: "",
      e2eeKey: "",
      snapshot: "",
      lastStatus: "idle",
      lastError: ""
    })
      .then(() => sendResponse({ ok: true }))
      .catch((e) => sendResponse({ ok: false, error: e.message || String(e) }));
    return true;
  }
  if (msg && msg.type === "save-settings") {
    setSettings(msg.patch || {})
      .then(() => reconfigure())
      .then(() => sendResponse({ ok: true }))
      .catch((e) => sendResponse({ ok: false, error: e.message || String(e) }));
    return true;
  }
  if (msg && msg.type === "get-settings") {
    getSettings().then((s) => sendResponse({
      provider: s.provider,
      cloudUrl: s.cloudUrl,
      username: s.username,
      uid: s.uid,
      hasKey: !!s.e2eeKey,
      webdavUrl: s.webdavUrl,
      webdavUser: s.webdavUser,
      webdavPass: s.webdavPass,
      autoSync: s.autoSync,
      intervalMinutes: s.intervalMinutes,
      lastSyncAt: s.lastSyncAt,
      lastStatus: s.lastStatus,
      lastError: s.lastError
    }));
    return true;
  }
  if (msg && msg.type === "status") {
    getSettings().then((s) => sendResponse({
      provider: s.provider,
      lastStatus: s.lastStatus,
      lastError: s.lastError,
      lastSyncAt: s.lastSyncAt,
      autoSync: s.autoSync,
      intervalMinutes: s.intervalMinutes,
      username: s.username
    }));
    return true;
  }
});
