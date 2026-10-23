# Mote 云同步 · 云函数

`cloudbase/functions/auth/` 是**部署到腾讯云开发（CloudBase）的云函数**，App 里没有它。

```
cloudbase/
└── functions/
    └── auth/
        ├── index.js       ← 账号 + 同步数据服务（HTTP 云函数，监听 9000）
        ├── package.json   ← 依赖 @cloudbase/node-sdk
        └── scf_bootstrap  ← 启动脚本
```

## 它负责什么

CloudBase 的身份认证要求邮箱/手机验证注册，且个人版「组织成员」只有 2 个名额。
所以 Mote 不用它：账号、登录态、同步数据全部由这个云函数处理。

| 接口 | 作用 |
| --- | --- |
| `POST /moteauth {action:"register", username, password}` | 注册：Users 集合写入 salt + PBKDF2 哈希 |
| `POST /moteauth {action:"login", username, password}` | 登录：校验后签发 HMAC token（30 天） |
| `POST /moteauth {action:"pull", token}` | 拉取云端密文 |
| `POST /moteauth {action:"push", token, content}` | 上传密文 |
| `POST /moteauth {action:"ping"}` | 健康检查 |

数据集合：`Users`（username / salt / hash / uid）、`SyncData`（user_id / content / updated_at）。

## 部署

```bash
tcb fn deploy auth --force
```

`cloudbaserc.json` 已配置：HTTP 函数、`public: true`（匿名可访问）、路由 `/moteauth`、
云端安装依赖。

## 环境变量

| 变量 | 说明 |
| --- | --- |
| `MOTE_SECRET` | token 签名密钥。**必须在控制台配置，不要写进仓库** |

设置位置：云函数 → auth → 配置 → 环境变量。
该值一旦更换，所有已签发的 token 立即失效（用户需要重新登录）。

## 注意事项

- 云函数挂在**默认域名**（`mote-sync-…-1515028654.ap-shanghai.app.tcloudbase.com`）上，
  只适合开发测试，有访问频率限制且无 SLA。正式发布前请绑定已备案的自定义域名，
  并同步修改客户端 `CloudBaseConfig.FUNCTION_URL`。
- 云函数的 `public: true` 意味着任何人只要知道 URL 就能调用 `/moteauth`，
  注册接口因此暴露在公网。若担心被刷，可加频率限制或邀请码校验。
