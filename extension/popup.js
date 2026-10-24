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
  return `${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`;
}

async function refresh() {
  const s = await send({ type: "status" });
  const dot = $("dot");
  const text = $("stateText");

  dot.className = "dot";
  if (s.lastStatus === "syncing") {
    dot.classList.add("syncing");
    text.textContent = t("pStatusSyncing");
  } else if (s.lastStatus === "error") {
    dot.classList.add("err");
    text.textContent = t("pStatusError");
  } else if (s.lastStatus === "ok") {
    dot.classList.add("ok");
    text.textContent = t("pStatusOk");
  } else {
    text.textContent = t("pStatusIdle");
  }

  const lines = [];
  lines.push(s.provider === "webdav" ? t("pProviderDav") : t("pProviderCloud") + (s.username ? " · @" + s.username : ""));
  lines.push(t("pLastSync", [fmtTime(s.lastSyncAt)]));
  lines.push(s.autoSync ? t("pAutoOn", [String(s.intervalMinutes || 5)]) : t("pAutoOff"));
  if (s.lastStatus === "error" && s.lastError) lines.push(t("pError", [s.lastError]));
  $("meta").textContent = lines.join("\n");
}

$("sync").addEventListener("click", async () => {
  $("sync").disabled = true;
  $("stateText").textContent = t("pStatusSyncing");
  $("dot").className = "dot syncing";
  await send({ type: "sync-now" });
  await refresh();
  $("sync").disabled = false;
});

$("options").addEventListener("click", () => {
  chrome.runtime.openOptionsPage();
});

refresh();
