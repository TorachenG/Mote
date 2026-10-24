/* 多语言辅助：优先用 chrome.i18n，取不到时退回内置词典（避免出现原始 key） */
(function () {
  var FALLBACK = {
    en: {
    "extName": "Mote Bookmark Sync",
    "extDesc": "Two-way bookmark sync between Mote and Edge / Chrome.",
    "actionTitle": "Mote Bookmark Sync",
    "optionsTitle": "Mote Bookmark Sync · Settings",
    "heading": "Mote Bookmark Sync",
    "lead": "Bookmarks live in your browser's “Bookmarks bar → Mote” folder. Other bookmarks are not touched.",
    "secMethod": "Sync method",
    "optCloud": "Default cloud",
    "optWebdav": "WebDAV",
    "labelUser": "Username",
    "labelPass": "Password",
    "labelDavUrl": "WebDAV URL",
    "btnLogin": "Sign in and save",
    "hintLogin": "The password is kept in local extension storage only, used for token refresh and for decrypting Advanced Data Protection data.",
    "secAuto": "Automatic sync",
    "autoLabel": "Sync automatically (push bookmark changes and check the cloud on an interval)",
    "labelInterval": "Interval (minutes)",
    "btnSyncNow": "Sync now",
    "btnSave": "Save",
    "secNotes": "Notes",
    "note1": "Supports both standard encryption and Advanced Data Protection (end-to-end); the mode is detected automatically.",
    "note2": "Deletions sync too: an item removed on one side is removed on the other.",
    "note3": "Only the “Mote” folder is synced; other bookmarks are never modified.",
    "note4": "If Advanced Data Protection is enabled, sign in with the same username and password you use in Mote.",
    "statusNever": "never synced",
    "statusLoggedIn": "Signed in: @$1$",
    "statusNotLogged": "Not signed in",
    "statusKeyReady": "end-to-end key ready",
    "statusKeyBasic": "standard encryption only",
    "statusLastSync": "Last sync: $1$",
    "statusLastError": "Last error: $1$",
    "msgSaved": "Saved",
    "msgSaveFailed": "Save failed: $1$",
    "msgNeedCreds": "Enter your username and password",
    "msgLoggingIn": "Signing in…",
    "msgLoginOk": "Signed in",
    "msgLoginFail": "Sign-in failed: $1$",
    "msgSyncing": "Syncing…",
    "msgSyncFail": "Sync failed",
    "msgLoading": "Loading…",
    "pBtnSync": "Sync now",
    "pBtnSettings": "Open settings",
    "pStatusSyncing": "Syncing…",
    "pStatusError": "Sync failed",
    "pStatusOk": "Up to date",
    "pStatusIdle": "Not synced yet",
    "pProviderCloud": "Mote account",
    "pProviderDav": "WebDAV",
    "pLastSync": "Last sync: $1$",
    "pAutoOn": "Auto sync: every $1$ min",
    "pAutoOff": "Auto sync: off",
    "pError": "Error: $1$",
    "bgNotLogged": "Not signed in to a Mote account",
    "bgNoKey": "Missing encryption key: sign in again in settings",
    "bgDecryptFail": "Could not decrypt cloud data: check whether Advanced Data Protection is enabled, or sign in again in settings",
    "bgNeedDav": "Fill in the WebDAV URL in settings first",
    "bgDavRead": "WebDAV read failed: HTTP $1$",
    "bgDavWrite": "WebDAV write failed: HTTP $1$",
    "bgBadResp": "Unexpected response: $1$",
    "bgReqFail": "Request failed"
},
    zh: {
    "extName": "Mote 书签同步",
    "extDesc": "在 Mote 与 Edge / Chrome 之间双向同步书签。",
    "actionTitle": "Mote 书签同步",
    "optionsTitle": "Mote 书签同步 · 设置",
    "heading": "Mote 书签同步",
    "lead": "书签会同步到浏览器的「书签栏 → Mote」文件夹里，其它书签不受影响。",
    "secMethod": "同步方式",
    "optCloud": "默认云同步",
    "optWebdav": "WebDAV",
    "labelUser": "用户名",
    "labelPass": "密码",
    "labelDavUrl": "WebDAV 地址",
    "btnLogin": "登录并保存",
    "hintLogin": "密码只保存在本机扩展存储里，用于自动续期和「高级数据保护」的端到端解密。",
    "secAuto": "自动同步",
    "autoLabel": "自动同步（书签改动后自动推送，并按间隔检查云端）",
    "labelInterval": "检查间隔（分钟）",
    "btnSyncNow": "立即同步",
    "btnSave": "保存设置",
    "secNotes": "说明",
    "note1": "支持普通加密与「高级数据保护」（端到端加密）两种模式，会自动识别。",
    "note2": "删除会同步：一端删除的书签，另一端同步后也会删除。",
    "note3": "同步范围仅限「Mote」文件夹，浏览器里其它书签不会被改动。",
    "note4": "若开启高级数据保护，请使用与 Mote 相同的账号密码。",
    "statusNever": "从未同步",
    "statusLoggedIn": "已登录：@$1$",
    "statusNotLogged": "未登录",
    "statusKeyReady": "端到端密钥已就绪",
    "statusKeyBasic": "仅普通加密",
    "statusLastSync": "上次同步：$1$",
    "statusLastError": "最近错误：$1$",
    "msgSaved": "已保存",
    "msgSaveFailed": "保存失败：$1$",
    "msgNeedCreds": "请填写用户名和密码",
    "msgLoggingIn": "登录中…",
    "msgLoginOk": "登录成功",
    "msgLoginFail": "登录失败：$1$",
    "msgSyncing": "同步中…",
    "msgSyncFail": "同步失败",
    "msgLoading": "加载中…",
    "pBtnSync": "立即同步",
    "pBtnSettings": "打开设置",
    "pStatusSyncing": "同步中…",
    "pStatusError": "同步失败",
    "pStatusOk": "同步正常",
    "pStatusIdle": "尚未同步",
    "pProviderCloud": "Mote 账号",
    "pProviderDav": "WebDAV",
    "pLastSync": "上次同步：$1$",
    "pAutoOn": "自动同步：每 $1$ 分钟",
    "pAutoOff": "自动同步：关闭",
    "pError": "错误：$1$",
    "bgNotLogged": "还没有登录 Mote 账号",
    "bgNoKey": "缺少加密密钥：请在设置里重新登录",
    "bgDecryptFail": "无法解密云端数据：请检查是否开启了高级数据保护，或在设置里重新登录",
    "bgNeedDav": "请先在设置里填写 WebDAV 地址",
    "bgDavRead": "WebDAV 读取失败：HTTP $1$",
    "bgDavWrite": "WebDAV 写入失败：HTTP $1$",
    "bgBadResp": "接口返回异常：$1$",
    "bgReqFail": "请求失败"
}
  };

  function pickLang() {
    var lang = "";
    try { lang = (chrome.i18n.getUILanguage && chrome.i18n.getUILanguage()) || ""; } catch (e) {}
    if (!lang) lang = navigator.language || "en";
    return lang.toLowerCase().indexOf("zh") === 0 ? "zh" : "en";
  }

  var current = pickLang();

  function substitute(text, subs) {
    if (!subs || !subs.length) return text;
    var out = text;
    for (var i = 0; i < subs.length; i++) {
      out = out.split("$" + (i + 1) + "$").join(subs[i]);
    }
    return out;
  }

  function t(key, subs) {
    var msg = "";
    try { msg = chrome.i18n.getMessage(key, subs) || ""; } catch (e) { msg = ""; }
    if (!msg) msg = (FALLBACK[current] && FALLBACK[current][key]) || (FALLBACK.en && FALLBACK.en[key]) || "";
    if (!msg) msg = key;
    return substitute(msg, subs);
  }

  function apply(root) {
    var scope = root || document;
    scope.querySelectorAll("[data-i18n]").forEach(function (el) {
      el.textContent = t(el.getAttribute("data-i18n"));
    });
    scope.querySelectorAll("[data-i18n-placeholder]").forEach(function (el) {
      el.placeholder = t(el.getAttribute("data-i18n-placeholder"));
    });
    scope.querySelectorAll("[data-i18n-title]").forEach(function (el) {
      el.title = t(el.getAttribute("data-i18n-title"));
    });
  }

  window.t = t;
  window.applyI18n = apply;
  document.addEventListener("DOMContentLoaded", function () {
    apply();
  });
})();
