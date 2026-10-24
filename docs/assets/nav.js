/* 顶部导航：品牌字距 + 下拉菜单（悬停已由 CSS 处理，这里负责点击切换） */
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

  const menus = Array.prototype.slice.call(document.querySelectorAll('.nav-item'));
  if (!menus.length) return;

  function closeAll(except) {
    menus.forEach(function (menu) {
      if (menu !== except) menu.classList.remove('open');
    });
  }

  menus.forEach(function (menu) {
    const link = menu.querySelector('.nav-link');
    if (!link) return;
    link.addEventListener('click', function (e) {
      e.preventDefault();
      e.stopPropagation();
      const willOpen = !menu.classList.contains('open');
      closeAll(menu);
      menu.classList.toggle('open', willOpen);
    });
  });

  document.addEventListener('click', function (e) {
    menus.forEach(function (menu) {
      if (!menu.contains(e.target)) menu.classList.remove('open');
    });
  });

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') closeAll(null);
  });
})();
