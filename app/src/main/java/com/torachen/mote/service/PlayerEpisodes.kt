@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.service

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import org.json.JSONObject
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet

// ============================================================
// 剧集数据模型
// ============================================================

internal data class EpisodeItem(
    val title: String,
    val url: String,
    val episodeNumber: Int = 0,
    val line: Int = 0,
)

internal data class EpisodeListResult(
    val episodes: List<EpisodeItem> = emptyList(),
    val currentIndex: Int = -1,
    val total: Int = 0,
)

// ============================================================
// 从 URL 反推剧集列表
//
// ★ 保守兜底：只生成 1..currentEp，不猜后面的
//   - currentEp == 1 → total == 1 → 按钮隐藏（等 DOM 抓到后覆盖）
//   - currentEp >= 2 → 至少能往回看
//   - DOM 抓到真实列表（>1 集）后会完全覆盖这里
// ============================================================

internal fun generateEpisodesFromUrl(pageUrl: String): EpisodeListResult {
    // ★ 适配 vidhub.tv: /vodplay/{vod_id}-{sid}-{nid}.html
    //   例：/vodplay/112578-1-49.html → vodId=112578, sid=1, nid=49
    val vidhubRegex = Regex("/vodplay/(\\d+)-(\\d+)-(\\d+)\\.html")
    val vidhubMatch = vidhubRegex.find(pageUrl)
    if (vidhubMatch != null) {
        val vodId = vidhubMatch.groupValues[1]
        val sid = vidhubMatch.groupValues[2]
        val currentNid = vidhubMatch.groupValues[3].toIntOrNull() ?: return EpisodeListResult()
        // 只生成 1..currentNid，后续由 DOM 抓取覆盖为完整列表
        val episodes = (1..currentNid).map { i ->
            EpisodeItem(
                title = "第${i.toString().padStart(2, '0')}集",
                url = "/vodplay/$vodId-$sid-$i.html",
                episodeNumber = i,
                line = sid.toIntOrNull() ?: 1,
            )
        }
        return EpisodeListResult(
            episodes = episodes,
            currentIndex = currentNid - 1,
            total = episodes.size,
        )
    }

    // 原有 maccms 格式（保留兼容）
    return try {
        val regex = Regex("#?/play/(\\d+)/(\\d+)/(\\d+)")
        val match = regex.find(pageUrl) ?: return EpisodeListResult()
        val animeId = match.groupValues[1]
        val line = match.groupValues[2]
        val currentEp = match.groupValues[3].toIntOrNull() ?: return EpisodeListResult()

        val episodes = (1..currentEp).map { i ->
            EpisodeItem(
                title = "第 $i 集",
                url = "#/play/$animeId/$line/$i",
                episodeNumber = i,
                line = line.toIntOrNull() ?: 1,
            )
        }
        EpisodeListResult(
            episodes = episodes,
            currentIndex = currentEp - 1,
            total = episodes.size,
        )
    } catch (_: Exception) {
        EpisodeListResult()
    }
}

// ============================================================
// JS 脚本：提取剧集列表
//
// ★ 通用方案：遍历所有 <a>，找所有同 animeId 的播放链接
//   不再依赖特定的 class 名（不同网站结构不同）
// ============================================================

internal val JS_EXTRACT_EPISODES = """
(function(){
    try {
        // ★ 适配 vidhub.tv：遍历 a.player-ep-link
        var vidhubLinks = document.querySelectorAll('a.player-ep-link');
        if (vidhubLinks.length > 0) {
            var episodes = [];
            var seen = {};
            for (var i = 0; i < vidhubLinks.length; i++) {
                var a = vidhubLinks[i];
                var href = a.getAttribute('href') || a.href || '';
                var nid = a.getAttribute('data-nid');
                var name = a.getAttribute('data-name') || (a.textContent || '').trim();
                if (!nid) continue;
                var epNum = parseInt(nid);
                if (isNaN(epNum) || seen[epNum]) continue;
                seen[epNum] = true;
                episodes.push({
                    title: name || ('第' + epNum + '集'),
                    url: href,
                    episodeNumber: epNum,
                    line: parseInt(a.getAttribute('data-sid') || '1')
                });
            }
            episodes.sort(function(a, b){ return a.episodeNumber - b.episodeNumber; });

            // 从当前 URL 解析当前集
            var currentNid = 0;
            var m = location.pathname.match(/\/vodplay\/\d+-(\d+)-(\d+)\.html/);
            if (m) currentNid = parseInt(m[2]);

            var currentIndex = -1;
            for (var q = 0; q < episodes.length; q++) {
                if (episodes[q].episodeNumber === currentNid) {
                    currentIndex = q;
                    break;
                }
            }
            return JSON.stringify({
                episodes: episodes,
                currentIndex: currentIndex,
                total: episodes.length
            });
        }

        // 原有通用逻辑（maccms 等）
        var hash = (location.hash || '') + ' ' + (location.href || '');
        var m = hash.match(/#?\/play\/(\d+)\/(\d+)\/(\d+)/);
        if (!m) {
            return JSON.stringify({episodes: [], currentIndex: -1, total: 0});
        }
        var animeId = m[1];
        var line = m[2];
        var currentEp = parseInt(m[3]);

        var episodes = [];
        var seen = {};

        var allLinks = document.querySelectorAll('a');
        for (var i = 0; i < allLinks.length; i++) {
            var href = allLinks[i].getAttribute('href') || allLinks[i].href || '';
            var mm = href.match(/#?\/play\/(\d+)\/(\d+)\/(\d+)/);
            if (!mm) continue;
            if (mm[1] !== animeId) continue;

            var epNum = parseInt(mm[3]);
            if (seen[epNum]) continue;
            seen[epNum] = true;

            var text = (allLinks[i].textContent || '').trim();
            if (!text || text.length > 20 ||
                text === '播放' || text === '▶' || text === '▶︎') {
                text = '第 ' + epNum + ' 集';
            }

            episodes.push({
                title: text,
                url: href,
                episodeNumber: epNum,
                line: parseInt(mm[2])
            });
        }

        if (episodes.length === 0) {
            episodes.push({
                title: '第 ' + currentEp + ' 集',
                url: '#/play/' + animeId + '/' + line + '/' + currentEp,
                episodeNumber: currentEp,
                line: parseInt(line)
            });
        }

        episodes.sort(function(a, b){ return a.episodeNumber - b.episodeNumber; });

        var currentIndex = -1;
        for (var q = 0; q < episodes.length; q++) {
            if (episodes[q].episodeNumber === currentEp) {
                currentIndex = q;
                break;
            }
        }

        return JSON.stringify({
            episodes: episodes,
            currentIndex: currentIndex,
            total: episodes.length
        });
    } catch(e) {
        return JSON.stringify({episodes: [], currentIndex: -1, total: 0});
    }
})();
""".trimIndent()

// ============================================================
// 解析 JSON
// ============================================================

internal fun parseEpisodeListJson(json: String): EpisodeListResult {
    return try {
        val obj = JSONObject(json)
        val arr = obj.optJSONArray("episodes") ?: return EpisodeListResult()
        val list = mutableListOf<EpisodeItem>()
        for (i in 0 until arr.length()) {
            val item = arr.optJSONObject(i) ?: continue
            list.add(
                EpisodeItem(
                    title = item.optString("title", ""),
                    url = item.optString("url", ""),
                    episodeNumber = item.optInt("episodeNumber", 0),
                    line = item.optInt("line", 0),
                )
            )
        }
        EpisodeListResult(
            episodes = list,
            currentIndex = obj.optInt("currentIndex", -1),
            total = obj.optInt("total", 0),
        )
    } catch (_: Exception) {
        EpisodeListResult()
    }
}

// ============================================================
// 隐藏 WebView：加载网页，提取剧集列表 + 拦截新视频 URL
//
// ★★★ 关键修复 ★★★
// 1. WebView 只创建一次，切集时只调 hash 变化（触发 SPA 路由）
//    → 内存峰值从「3 个 WebView」降到「2 个 WebView」，避免 OOM
// 2. SPA 的 hash 路由（#/play/xxx/1/3），直接 loadUrl 不会触发重新加载
//    （Chrome 认为同一文档），必须用 JS 改 location.hash，
//    让 Vue Router 响应路由变化。
// 3. onRenderProcessGone 返回 true，防止渲染进程崩溃连带杀死应用
// 4. DisposableEffect 完整销毁 WebView，释放所有资源
// ============================================================

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun HiddenEpisodeWebView(
    context: Context,
    pageUrl: String,
    onEpisodeListExtracted: (EpisodeListResult) -> Unit,
    onVideoUrlExtracted: (String) -> Unit,
) {
    val webViewRef = remember { mutableStateOf<WebView?>(null) }
    val isFirstLoad = remember { mutableStateOf(true) }
    val lastLoadedUrl = remember { mutableStateOf("") }

    LaunchedEffect(pageUrl) {
        if (isFirstLoad.value) {
            isFirstLoad.value = false
            lastLoadedUrl.value = pageUrl
            return@LaunchedEffect
        }
        if (pageUrl == lastLoadedUrl.value) return@LaunchedEffect
        lastLoadedUrl.value = pageUrl

        val wv = webViewRef.value ?: return@LaunchedEffect
        try {
            wv.stopLoading()
            val newHash = pageUrl.substringAfter("#", "")
            if (newHash.isNotBlank()) {
                // ★ 修复：改 hash 后主动派发 hashchange / popstate 事件，
                //   兼容 hash 模式和历史模式的 SPA。
                //   仅 location.hash = ... 不足以让某些 SPA（如带内部状态机、
                //   或 Vue Router history 模式）重新加载视频。
                val hashWithPound = "#$newHash"
                val js = """
                    (function(){
                        try {
                            var targetHash = '$hashWithPound';
                            if (location.hash !== targetHash) {
                                location.hash = targetHash;
                            }
                        } catch(e) {}

                        // 派发 hashchange
                        try {
                            var evt = document.createEvent('HTMLEvents');
                            evt.initEvent('hashchange', true, false);
                            window.dispatchEvent(evt);
                        } catch(e) {}
                        try {
                            window.dispatchEvent(new HashChangeEvent('hashchange'));
                        } catch(e) {}

                        // 兜底：尝试触发 history 模式（Vue Router history 模式）
                        try {
                            if (window.history && window.history.pushState) {
                                window.history.replaceState(null, '', targetHash);
                                var popEvt = document.createEvent('HTMLEvents');
                                popEvt.initEvent('popstate', true, false);
                                window.dispatchEvent(popEvt);
                            }
                        } catch(e) {}
                    })();
                """.trimIndent()
                wv.evaluateJavascript(js, null)
            } else {
                wv.loadUrl(pageUrl)
            }
            // ★ 延迟后重新抓一次 DOM（等 SPA 路由渲染完成）
            delay(2500)
            try {
                wv.evaluateJavascript(JS_EXTRACT_EPISODES, null)
            } catch (_: Exception) {}
        } catch (_: Exception) {}
    }

    // ★ 只在 Composable 真正离开时销毁 WebView
    DisposableEffect(Unit) {
        onDispose {
            webViewRef.value?.let { wv ->
                try {
                    (wv.parent as? ViewGroup)?.removeView(wv)
                    wv.stopLoading()
                    wv.loadUrl("about:blank")
                    wv.clearHistory()
                    wv.removeAllViews()
                    wv.removeJavascriptInterface("moteEpisodes")
                    wv.destroy()
                } catch (_: Exception) {}
            }
            webViewRef.value = null
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString =
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"
                settings.loadsImagesAutomatically = false
                settings.blockNetworkImage = true

                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onEpisodeList(json: String) {
                        post {
                            val result = parseEpisodeListJson(json)
                            onEpisodeListExtracted(result)
                        }
                    }
                }, "moteEpisodes")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // ★ 改进：SPA 的剧集列表可能懒渲染（视频加载后才显示），
                        //   单次 1.5s 延迟可能太早，改为多次尝试
                        val delays = listOf(1500L, 3500L, 6500L, 10000L)
                        for (d in delays) {
                            view?.postDelayed({
                                try {
                                    view.evaluateJavascript(JS_EXTRACT_EPISODES, null)
                                } catch (_: Exception) {}
                            }, d)
                        }
                    }

                    override fun shouldInterceptRequest(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): WebResourceResponse? {
                        try {
                            val u = request?.url?.toString()
                            if (!u.isNullOrBlank()) {
                                val isM3u8 = u.contains(".m3u8", true) ||
                                        u.contains(".mpd", true)
                                val isMp4WithRange = u.contains(".mp4", true) &&
                                        request.requestHeaders?.keys?.any {
                                            it.equals("Range", true)
                                        } == true
                                if (isM3u8 || isMp4WithRange) {
                                    view?.post { onVideoUrlExtracted(u) }
                                }
                            }
                        } catch (_: Exception) {}
                        return super.shouldInterceptRequest(view, request)
                    }

                    override fun onRenderProcessGone(
                        view: WebView?,
                        detail: RenderProcessGoneDetail?
                    ): Boolean {
                        try {
                            (view?.parent as? ViewGroup)?.removeView(view)
                            view?.destroy()
                        } catch (_: Exception) {}
                        webViewRef.value = null
                        return true
                    }
                }

                webViewRef.value = this
                loadUrl(pageUrl)
            }
        },
        modifier = Modifier
            .size(1.dp)
            .alpha(0f)
    )
}

// ============================================================
// 选集面板
// ============================================================

@Composable
internal fun EpisodeSheet(
    show: Boolean,
    episodes: List<EpisodeItem>,
    currentIndex: Int,
    onSelect: (EpisodeItem) -> Unit,
    onDismiss: () -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(show, currentIndex) {
        if (show && currentIndex >= 0) {
            listState.scrollToItem(maxOf(0, currentIndex - 3))
        }
    }

    WindowBottomSheet(
        show = show,
        onDismissRequest = onDismiss,
        title = "选集 (共 ${episodes.size} 集)",
        backgroundColor = MiuixTheme.colorScheme.surface,
        allowDismiss = true,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                items(
                    count = episodes.size,
                    key = { idx -> "ep_${episodes[idx].url}_$idx" },
                ) { idx ->
                    val ep = episodes[idx]
                    val isCurrent = idx == currentIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isCurrent)
                                    MiuixTheme.colorScheme.primary.copy(alpha = 0.1f)
                                else Color.Transparent
                            )
                            .clickable {
                                if (!isCurrent) {
                                    onSelect(ep)
                                }
                                onDismiss()
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MiuixTheme.colorScheme.primary),
                            )
                            Spacer(Modifier.width(10.dp))
                        }
                        Text(
                            text = ep.title.ifBlank { "第 ${ep.episodeNumber} 集" },
                            fontSize = 15.sp,
                            color = if (isCurrent) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.onSurface,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                        )
                        if (isCurrent) {
                            Text(
                                text = "当前",
                                fontSize = 12.sp,
                                color = MiuixTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}