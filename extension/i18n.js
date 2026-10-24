/* 简单的多语言辅助：给带 data-i18n 属性的元素套上 chrome.i18n 文案 */
(function () {
  function t(key, subs) {
    try {
      return chrome.i18n.getMessage(key, subs) || key;
    } catch (e) {
      return key;
    }
  }

  function apply(root) {
    const scope = root || document;
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
