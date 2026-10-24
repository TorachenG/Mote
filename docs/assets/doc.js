/* 极简 Markdown 渲染（够用于协议、隐私政策、更新日志） */
(function () {
  function esc(s) {
    return s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
  }

  function inline(text) {
    let s = esc(text);
    s = s.replace(/`([^`]+)`/g, "<code>$1</code>");
    s = s.replace(/\*\*([^*]+)\*\*/g, "<strong>$1</strong>");
    s = s.replace(/\[([^\]]+)\]\(([^)\s]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>');
    s = s.replace(/(^|[\s(])(https?:\/\/[^\s<)]+)/g, '$1<a href="$2" target="_blank" rel="noopener">$2</a>');
    return s;
  }

  function render(md) {
    const lines = md.replace(/\r\n/g, "\n").split("\n");
    const out = [];
    let i = 0;
    let listType = null;

    function closeList() {
      if (listType) {
        out.push(listType === "ul" ? "</ul>" : "</ol>");
        listType = null;
      }
    }

    while (i < lines.length) {
      const line = lines[i];
      const trimmed = line.trim();

      if (!trimmed) { closeList(); i++; continue; }

      if (/^---+$/.test(trimmed)) { closeList(); out.push("<hr>"); i++; continue; }

      let m = /^(#{1,4})\s+(.*)$/.exec(trimmed);
      if (m) {
        closeList();
        const level = Math.min(m[1].length, 3);
        out.push("<h" + level + ">" + inline(m[2]) + "</h" + level + ">");
        i++;
        continue;
      }

      m = /^[-*]\s+(.*)$/.exec(trimmed);
      if (m) {
        if (listType !== "ul") { closeList(); out.push("<ul>"); listType = "ul"; }
        out.push("<li>" + inline(m[1]) + "</li>");
        i++;
        continue;
      }

      m = /^\d+[.)]\s+(.*)$/.exec(trimmed);
      if (m) {
        if (listType !== "ol") { closeList(); out.push("<ol>"); listType = "ol"; }
        out.push("<li>" + inline(m[1]) + "</li>");
        i++;
        continue;
      }

      if (/^>\s?/.test(trimmed)) {
        closeList();
        const buf = [];
        while (i < lines.length && /^>\s?/.test(lines[i].trim())) {
          buf.push(inline(lines[i].trim().replace(/^>\s?/, "")));
          i++;
        }
        out.push("<blockquote>" + buf.join("<br>") + "</blockquote>");
        continue;
      }

      closeList();
      const para = [];
      while (i < lines.length) {
        const t = lines[i].trim();
        if (!t) break;
        if (/^(#{1,4})\s+/.test(t)) break;
        if (/^[-*]\s+/.test(t)) break;
        if (/^\d+[.)]\s+/.test(t)) break;
        if (/^---+$/.test(t)) break;
        if (/^>\s?/.test(t)) break;
        para.push(inline(t));
        i++;
      }
      if (para.length) out.push("<p>" + para.join("<br>") + "</p>");
      else i++;
    }
    closeList();
    return out.join("\n");
  }

  window.renderMarkdown = render;

  document.addEventListener("DOMContentLoaded", function () {
    const el = document.getElementById("doc");
    if (!el) return;
    const src = el.getAttribute("data-src");
    if (!src) return;
    fetch(src, { cache: "no-cache" })
      .then(function (r) {
        if (!r.ok) throw new Error(String(r.status));
        return r.text();
      })
      .then(function (md) {
        el.innerHTML = render(md);
      })
      .catch(function () {
        el.innerHTML = '<p>文档加载失败。可以 <a href="' + src + '" target="_blank" rel="noopener">直接查看原文</a>。</p>';
      });
  });
})();
