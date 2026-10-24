/* 顶部导航：用户协议下拉（悬停已由 CSS 处理，这里负责点击切换） */
(function () {
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
