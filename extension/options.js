const $ = (id) => document.getElementById(id);

function send(msg) {
  return new Promise((resolve) => {
    chrome.runtime.sendMessage(msg, (resp) => resolve(resp || {}));
  });
}

function fmtTime(ms) {
  if (!ms) return "从未同步";
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

async function load() {
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

  const parts = [];
  parts.push(s.uid ? "已登录：@" + (s.username || "未知") : "未登录");
  parts.push(s.hasKey ? "端到端密钥已就绪" : "仅普通加密");
  parts.push("上次同步：" + fmtTime(s.lastSyncAt));
  if (s.lastStatus === "error" && s.lastError) parts.push("最近错误：" + s.lastError);
  setStatus($("status"), parts.join(" · "), s.lastStatus === "error" ? "err" : "");
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
    setStatus($("saveStatus"), "已保存", "ok");
    setTimeout(() => setStatus($("saveStatus"), ""), 1800);
  } else {
    setStatus($("saveStatus"), "保存失败：" + (r.error || ""), "err");
  }
}

$("loginBtn").addEventListener("click", async () => {
  const username = $("username").value.trim();
  const password = $("password").value;
  if (!username || !password) {
    setStatus($("saveStatus"), "请填写用户名和密码", "err");
    return;
  }
  $("loginBtn").disabled = true;
  setStatus($("saveStatus"), "登录中…");
  try {
    const r = await send({ type: "login", username, password });
    if (r.ok) {
      setStatus($("saveStatus"), "登录成功", "ok");
      $("password").value = "";
      await load();
    } else {
      setStatus($("saveStatus"), "登录失败：" + (r.error || ""), "err");
    }
  } finally {
    $("loginBtn").disabled = false;
  }
});

$("saveBtn").addEventListener("click", saveSettings);

$("syncBtn").addEventListener("click", async () => {
  $("syncBtn").disabled = true;
  setStatus($("status"), "同步中…");
  await saveSettings();
  const r = await send({ type: "sync-now" });
  await load();
  $("syncBtn").disabled = false;
  if (!r.ok) setStatus($("status"), "同步失败", "err");
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
setInterval(load, 4000);
