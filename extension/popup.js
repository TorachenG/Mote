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
  return `${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
}

async function refresh() {
  const s = await send({ type: "status" });
  const dot = $("dot");
  const text = $("stateText");

  dot.className = "dot";
  if (s.lastStatus === "syncing") {
    dot.classList.add("syncing");
    text.textContent = "同步中…";
  } else if (s.lastStatus === "error") {
    dot.classList.add("err");
    text.textContent = "同步失败";
  } else if (s.lastStatus === "ok") {
    dot.classList.add("ok");
    text.textContent = "同步正常";
  } else {
    text.textContent = "尚未同步";
  }

  const lines = [];
  lines.push(s.provider === "webdav" ? "WebDAV" : "Mote 账号" + (s.username ? " · @" + s.username : ""));
  lines.push("上次同步：" + fmtTime(s.lastSyncAt));
  lines.push(s.autoSync ? "自动同步：每 " + (s.intervalMinutes || 5) + " 分钟" : "自动同步：关闭");
  if (s.lastStatus === "error" && s.lastError) lines.push("错误：" + s.lastError);
  $("meta").textContent = lines.join("\n");
}

$("sync").addEventListener("click", async () => {
  $("sync").disabled = true;
  $("stateText").textContent = "同步中…";
  $("dot").className = "dot syncing";
  await send({ type: "sync-now" });
  await refresh();
  $("sync").disabled = false;
});

$("options").addEventListener("click", () => {
  chrome.runtime.openOptionsPage();
});

refresh();
