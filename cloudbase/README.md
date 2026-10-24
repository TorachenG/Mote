# Mote 云同步 · 云函数 + 管理面板

`cloudbase/` 下两块内容：**云函数**（后端）和 **管理面板**（本地网页，用来管账号）。

```
cloudbase/
├── admin/
│   └── index.html       ← 账号管理面板（双击用浏览器打开）
└── functions/
    └── auth/
        ├── index.js       ← 账号 + 同步数据 + 管理接口
        ├── package.json
        └── scf_bootstrap
```

## 管理面板怎么用

管理面板**不在仓库里**（避免公开暴露入口），放在仓库之外的任意目录，
例如 `Documents/MoteAdmin/index.html`。

1. 双击用浏览器打开（不需要起服务器）；
2. 填两项：
   - **云函数地址**：`https://mote-sync-d9geb238o1c1ea40e-1515028654.ap-shanghai.app.tcloudbase.com/moteauth`
   - **管理员密钥**：云函数环境变量 `ADMIN_KEY` 的值
3. 点「连接并刷新」。

面板能做的事：查看账号列表与同步概况、重置密码、停用/启用、删除账号（连带清同步数据）。

## 云函数接口

### 用户侧

| action | 参数 | 说明 |
| --- | --- | --- |
| `register` | `username`, `password` | 注册 |
| `login` | `username`, `password` | 登录，返回 `token` + `refresh` |
| `refresh` | `refresh` | 用 refresh token 换新 access token |
| `pull` | `token` | 拉取云端密文与 `updated_at` |
| `push` | `token`, `content` | 上传密文 |
| `changePassword` | `token`, `oldPassword`, `newPassword` | 用户自助改密码 |
| `deleteAccount` | `token`, `password` | 用户自助注销（连带清数据） |

### 管理侧（需要 `key` = `ADMIN_KEY`）

| action | 参数 | 说明 |
| --- | --- | --- |
| `adminList` | `key` | 账号列表 + 同步数据概况 |
| `adminResetPassword` | `key`, `username`, `newPassword` | 重置密码 |
| `adminDisable` | `key`, `username`, `disabled` | 停用 / 启用 |
| `adminDelete` | `key`, `username` | 删账号 + 删数据 |

## 数据集合

- `Users`：`username`、`uid`、`salt`、`hash`（PBKDF2）、`created_at`、`disabled`
- `SyncData`：`user_id`（= Users.uid）、`content`（端到端加密密文）、`updated_at`

两个集合只由云函数访问（管理员权限），建议在控制台把权限设为「仅管理端可读写」。

## 部署

```bash
tcb fn deploy auth --force
```

> ⚠️ 部署前确认云函数环境变量里有 `MOTE_SECRET` 和 `ADMIN_KEY`。
> `MOTE_SECRET` 一旦更换，所有已登录用户的 token 立即失效，需要重新登录。

## 注意事项

- 云函数挂在**默认域名**上，仅适合开发测试（有限流、无 SLA）。正式发布前绑定已备案的自定义域名，并同步修改客户端 `CloudBaseConfig.FUNCTION_URL`。
- `public: true` 意味着 `/moteauth` 对公网开放，注册接口没有验证码保护。若担心被刷，可加频率限制或邀请码。
