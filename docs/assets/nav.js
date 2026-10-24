/* 顶部导航：品牌字距 + 用户协议下拉（悬停已由 CSS 处理，这里负责点击切换） */
(function () {
  /* [ TORACHEN ] 拆成单字，配合 flex 让它与 MOTE 左右对齐 */
  document.querySelectorAll('.brand .org').forEach(function (el) {
    var text = el.textContent || '';
    el.textContent = '';
    Array.prototype.forEach.call(text, function (ch) {
      var span = document.createElement('span');
      span.textContent = ch === ' ' ? '\u00A0' : ch;
      el.appendChild(span);
    });
  });

  const menu = document.querySelector('.nav-item');
  if (!menu) return;
  const link = menu.querySelector('.nav-link');
  if (link) {
    link.addEventListener('click', function (e) {
      e.preventDefault();
      menu.classList.toggle('open');
    });
  }
  document.addEventListener('click', function (e) {
    if (!menu.contains(e.target)) menu.classList.remove('open');
  });
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') menu.classList.remove('open');
  });
})();
