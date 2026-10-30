/*
 * Mote 站点统一数据源。
 *
 * - 依据访问者 IP 归属选择数据源：中国大陆优先 Gitee，其它地区优先 GitHub。
 * - 任一侧在加载过程中失败（网络错误 / 非 2xx / 空数据），自动切换到另一侧。
 * - 结果在本次会话内缓存，避免重复探测。
 */
(function (global) {
  'use strict';

  var REPO = 'TorachenG/Mote';
  var GITHUB_REPO = 'https://github.com/' + REPO;
  var GITEE_REPO = 'https://gitee.com/' + REPO;
  var GITHUB_API = 'https://api.github.com/repos/' + REPO;
  var GITEE_API = 'https://gitee.com/api/v5/repos/' + REPO;

  var REGION_CACHE_KEY = 'mote_source_region';

  function heuristicRegion() {
    try {
      var tz = (Intl && Intl.DateTimeFormat)
        ? (Intl.DateTimeFormat().resolvedOptions().timeZone || '')
        : '';
      var lang = navigator.language || '';
      if (tz === 'Asia/Shanghai' || tz === 'Asia/Chongqing' ||
          tz === 'Asia/Harbin' || tz === 'Asia/Urumqi') {
        return 'cn';
      }
      if (/^zh/i.test(lang) && /^Asia\//.test(tz)) return 'cn';
    } catch (e) {}
    return 'global';
  }

  function fetchJson(url, timeoutMs) {
    var controller = ('AbortController' in global) ? new AbortController() : null;
    var timer = null;
    var options = { headers: { Accept: 'application/json' }, cache: 'no-store' };
    if (controller) options.signal = controller.signal;
    return new Promise(function (resolve, reject) {
      if (controller) {
        timer = setTimeout(function () { controller.abort(); }, timeoutMs || 3000);
      }
      fetch(url, options)
        .then(function (res) {
          if (!res.ok) throw new Error(String(res.status));
          return res.json();
        })
        .then(function (data) {
          if (timer) clearTimeout(timer);
          resolve(data);
        })
        .catch(function (err) {
          if (timer) clearTimeout(timer);
          reject(err);
        });
    });
  }

  function detectRegion() {
    try {
      var cached = sessionStorage.getItem(REGION_CACHE_KEY);
      if (cached === 'cn' || cached === 'global') return Promise.resolve(cached);
    } catch (e) {}

    var endpoints = [
      'https://ipapi.co/json/',
      'https://ipwho.is/',
      'https://ipinfo.io/json'
    ];

    function tryNext(index) {
      if (index >= endpoints.length) {
        var fallback = heuristicRegion();
        rememberRegion(fallback);
        return Promise.resolve(fallback);
      }
      return fetchJson(endpoints[index], 2500)
        .then(function (data) {
          var code = String(
            (data && (data.country_code || data.country || data.countryCode)) || ''
          ).toUpperCase();
          if (!code) throw new Error('no country');
          var region = code === 'CN' ? 'cn' : 'global';
          rememberRegion(region);
          return region;
        })
        .catch(function () { return tryNext(index + 1); });
    }

    return tryNext(0);
  }

  function rememberRegion(region) {
    try { sessionStorage.setItem(REGION_CACHE_KEY, region); } catch (e) {}
  }

  var regionPromise = detectRegion();

  function platformOrder() {
    return regionPromise.then(function (region) {
      return region === 'cn' ? ['gitee', 'github'] : ['github', 'gitee'];
    });
  }

  /** 依次尝试各平台加载器，第一个成功（非空）的结果胜出 */
  function withFallback(loaders) {
    return platformOrder().then(function (order) {
      function step(i) {
        if (i >= order.length) return Promise.reject(new Error('all sources failed'));
        var platform = order[i];
        var loader = loaders[platform];
        if (!loader) return step(i + 1);
        return Promise.resolve()
          .then(loader)
          .then(function (value) {
            if (value == null) return step(i + 1);
            if (Array.isArray(value) && value.length === 0) return step(i + 1);
            return value;
          })
          .catch(function () { return step(i + 1); });
      }
      return step(0);
    });
  }

  function normalizeGithubRelease(raw) {
    var assets = (raw.assets || []).map(function (a) {
      return { name: a.name, url: a.browser_download_url, size: a.size };
    });
    return {
      platform: 'github',
      tag: String(raw.tag_name || raw.name || '').replace(/^v/i, ''),
      name: raw.name || raw.tag_name || '',
      body: raw.body || '',
      publishedAt: raw.published_at || raw.created_at || null,
      prerelease: !!raw.prerelease,
      assets: assets,
      pageUrl: (raw.html_url || (GITHUB_REPO + '/releases'))
    };
  }

  function normalizeGiteeRelease(raw) {
    var assets = (raw.assets || []).map(function (a) {
      return { name: a.name, url: a.browser_download_url, size: a.size };
    });
    var tag = String(raw.tag_name || raw.name || '').replace(/^v/i, '');
    return {
      platform: 'gitee',
      tag: tag,
      name: raw.name || raw.tag_name || '',
      body: raw.body || '',
      publishedAt: raw.created_at || null,
      prerelease: !!raw.prerelease,
      assets: assets,
      pageUrl: GITEE_REPO + '/releases'
    };
  }

  function fetchGithubReleases(count) {
    return fetchJson(
      GITHUB_API + '/releases?per_page=' + (count || 30) + '&page=1',
      4500
    ).then(function (items) {
      if (!Array.isArray(items)) return [];
      return items.map(normalizeGithubRelease);
    });
  }

  function sortNewestFirst(list) {
    return list.slice().sort(function (a, b) {
      var ta = a.publishedAt ? Date.parse(a.publishedAt) : 0;
      var tb = b.publishedAt ? Date.parse(b.publishedAt) : 0;
      return tb - ta;
    });
  }

  function fetchGiteeReleases(count) {
    // 注意：Gitee releases 接口按创建时间升序返回（最旧在前），必须自己按时间倒序
    return fetchJson(
      GITEE_API + '/releases?per_page=100&page=1',
      5000
    ).then(function (items) {
      if (!Array.isArray(items)) return [];
      return sortNewestFirst(items.map(normalizeGiteeRelease)).slice(0, count || 30);
    });
  }

  function latestRelease() {
    return withFallback({
      github: function () {
        return fetchJson(GITHUB_API + '/releases/latest', 4500)
          .then(normalizeGithubRelease);
      },
      gitee: function () {
        return fetchGiteeReleases(100).then(function (list) {
          if (!list.length) return null;
          var stable = list.filter(function (r) { return !r.prerelease; });
          return stable[0] || list[0];
        });
      }
    });
  }

  function releases(count) {
    return withFallback({
      github: function () { return fetchGithubReleases(count); },
      gitee: function () { return fetchGiteeReleases(count); }
    });
  }

  function releasesPageUrl() {
    return regionPromise.then(function (region) {
      return region === 'cn' ? (GITEE_REPO + '/releases') : (GITHUB_REPO + '/releases');
    });
  }

  /** 仓库内文件的原始地址（用户协议、隐私政策、LICENSE、NOTICE 等） */
  function rawUrl(path) {
    return regionPromise.then(function (region) {
      var clean = String(path || '').replace(/^\/+/, '');
      if (region === 'cn') {
        return 'https://gitee.com/' + REPO + '/raw/main/' + clean;
      }
      return 'https://raw.githubusercontent.com/' + REPO + '/main/' + clean;
    });
  }

  function repoUrl() {
    return regionPromise.then(function (region) {
      return region === 'cn' ? GITEE_REPO : GITHUB_REPO;
    });
  }

  var LINK_TARGETS = {
    license: 'LICENSE',
    notice: 'NOTICE'
  };

  /** 把页面上标记了 data-mote-src 的链接改成当前区域的数据源地址 */
  function applyLinks(root) {
    var scope = root || document;
    var nodes = scope.querySelectorAll('[data-mote-src]');
    Array.prototype.forEach.call(nodes, function (node) {
      var key = node.getAttribute('data-mote-src');
      if (key === 'releases') {
        releasesPageUrl().then(function (url) { node.href = url; });
        return;
      }
      if (key === 'repo') {
        repoUrl().then(function (url) { node.href = url; });
        return;
      }
      if (LINK_TARGETS[key]) {
        rawUrl(LINK_TARGETS[key]).then(function (url) { node.href = url; });
      }
    });
  }

  global.MoteSource = {
    region: function () { return regionPromise; },
    withFallback: withFallback,
    latestRelease: latestRelease,
    releases: releases,
    releasesPageUrl: releasesPageUrl,
    rawUrl: rawUrl,
    repoUrl: repoUrl,
    applyLinks: applyLinks
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', function () { applyLinks(); });
  } else {
    applyLinks();
  }
})(window);
