/*
 * 离线优先的发布数据源。
 * 站点构建时会在 assets/release.json 生成一份快照，
 * 这里优先读本地文件，读取失败再退回 GitHub / Gitee API。
 */
(function (global) {
  'use strict';
  var cache = null;

  function load() {
    if (cache) return Promise.resolve(cache);
    return fetch('assets/release.json', { cache: 'no-store' })
      .then(function (res) {
        if (!res.ok) throw new Error(String(res.status));
        return res.json();
      })
      .then(function (data) {
        cache = data || {};
        return cache;
      })
      .catch(function () { return null; });
  }

  function toItem(r) {
    var apk = r.apk || null;
    return {
      platform: 'local',
      tag: String(r.tag || '').replace(/^v/i, ''),
      name: r.name || '',
      body: r.body || '',
      publishedAt: r.publishedAt || null,
      prerelease: !!r.prerelease,
      assets: apk ? [{ name: apk.name, url: apk.url, size: apk.size }] : [],
      pageUrl: r.pageUrl || 'https://github.com/TorachenG/Mote/releases'
    };
  }

  global.MoteLocal = {
    latest: function () {
      return load().then(function (data) {
        if (!data || !data.latest) return null;
        return toItem(data.latest);
      });
    },
    releases: function (count) {
      return load().then(function (data) {
        if (!data || !Array.isArray(data.releases) || !data.releases.length) return null;
        var list = data.releases.map(toItem);
        return count ? list.slice(0, count) : list;
      });
    }
  };
})(window);
