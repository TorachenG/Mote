const $ = (id) => document.getElementById(id);
const t = (k, subs) => (window.t ? window.t(k, subs) : k);

function send(msg) {
  return new Promise((resolve) => {
    chrome.runtime.sendMessage(msg, (resp) => resolve(resp || {}));
  });
}

function fmtTime(ms) {
  if (!ms) return t("statusNever");
  const d = new Date(ms);
  const p = (n) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
}

function setStatus(el, text, kind) {
  el.textContent = text || "";
  el.className = "status" + (kind ? " " + kind : "");
}

function currentProvider() {
  const checked = document.querySelector('input[name="provider"]:checked');
  return checked ? checked.value : "cloud";
}

function updateProviderUI() {
  const isCloud = currentProvider() === "cloud";
  $("cloudBox").classList.toggle("hidden", !isCloud);
  $("webdavBox").classList.toggle("hidden", isCloud);
}

function renderStatus(s) {
  const parts = [];
  parts.push(s.uid ? t("statusLoggedIn", [s.username || "-"]) : t("statusNotLogged"));
  parts.push(s.hasKey ? t("statusKeyReady") : t("statusKeyBasic"));
  parts.push(t("statusLastSync", [fmtTime(s.lastSyncAt)]));
  if (s.lastStatus === "error" && s.lastError) parts.push(t("statusLastError", [s.lastError]));
  setStatus($("status"), parts.join(" · "), s.lastStatus === "error" ? "err" : "");
}

async function load() {
  document.title = t("optionsTitle");
  const s = await send({ type: "get-settings" });
  $("username").value = s.username || "";
  $("webdavUrl").value = s.webdavUrl || "";
  $("webdavUser").value = s.webdavUser || "";
  $("webdavPass").value = s.webdavPass || "";
  $("autoSync").checked = s.autoSync !== false;
  $("intervalMinutes").value = s.intervalMinutes || 5;

  document.querySelectorAll('input[name="provider"]').forEach((input) => {
    input.checked = input.value === (s.provider || "cloud");
  });
  updateProviderUI();
  renderStatus(s);
}

/** 定时只刷新状态文字，不回填输入框，避免打断正在输入的内容 */
async function refreshStatusOnly() {
  const s = await send({ type: "get-settings" });
  renderStatus(s);
}

async function saveSettings() {
  const patch = {
    provider: currentProvider(),
    webdavUrl: $("webdavUrl").value.trim(),
    webdavUser: $("webdavUser").value.trim(),
    webdavPass: $("webdavPass").value,
    autoSync: $("autoSync").checked,
    intervalMinutes: Math.max(1, Number($("intervalMinutes").value) || 5)
  };
  const r = await send({ type: "save-settings", patch });
  if (r.ok) {
    setStatus($("saveStatus"), t("msgSaved"), "ok");
    setTimeout(() => setStatus($("saveStatus"), ""), 1800);
  } else {
    setStatus($("saveStatus"), t("msgSaveFailed", [r.error || ""]), "err");
  }
}

$("loginBtn").addEventListener("click", async () => {
  const username = $("username").value.trim();
  const password = $("password").value;
  if (!username || !password) {
    setStatus($("saveStatus"), t("msgNeedCreds"), "err");
    return;
  }
  $("loginBtn").disabled = true;
  setStatus($("saveStatus"), t("msgLoggingIn"));
  try {
    const r = await send({ type: "login", username, password });
    if (r.ok) {
      setStatus($("saveStatus"), t("msgLoginOk"), "ok");
      $("password").value = "";
      await load();
    } else {
      setStatus($("saveStatus"), t("msgLoginFail", [r.error || ""]), "err");
    }
  } finally {
    $("loginBtn").disabled = false;
  }
});

$("saveBtn").addEventListener("click", saveSettings);

$("syncBtn").addEventListener("click", async () => {
  $("syncBtn").disabled = true;
  setStatus($("status"), t("msgSyncing"));
  await saveSettings();
  const r = await send({ type: "sync-now" });
  await load();
  $("syncBtn").disabled = false;
  if (!r.ok) setStatus($("status"), t("msgSyncFail"), "err");
});

document.querySelectorAll('input[name="provider"]').forEach((input) => {
  input.addEventListener("change", () => {
    updateProviderUI();
    saveSettings();
  });
});

$("autoSync").addEventListener("change", saveSettings);
$("intervalMinutes").addEventListener("change", saveSettings);

load();
setInterval(refreshStatusOnly, 5000);
