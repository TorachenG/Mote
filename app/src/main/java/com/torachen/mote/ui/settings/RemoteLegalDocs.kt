@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

// ============================================================
// 云端法律文档：内置 assets 兜底 → 磁盘缓存 → Gitee / GitHub / jsDelivr
// ============================================================

/**
 * 三份需要"随时可更新、但不发版也能改"的法律文档。
 *
 * 加载顺序（用户永远先看到内容，不会白屏）：
 *   1. 磁盘缓存（上次成功拉取的版本）
 *   2. 应用内置 assets（随包发布，保证离线可看）
 *   3. 后台静默拉取远端，成功则替换并写入缓存
 */
internal enum class RemoteLegalDoc(
    val route: String,
    val title: String,
    val fileName: String,
) {
    PRIVACY_POLICY(LEGAL_ROUTE_PRIVACY_POLICY, "隐私政策", "privacy-policy.md"),
    USER_AGREEMENT(LEGAL_ROUTE_USER_AGREEMENT, "用户协议", "user-agreement.md"),
    UX_IMPROVEMENT(LEGAL_ROUTE_UX_IMPROVEMENT, "用户体验改进计划", "ux-improvement.md"),
}

internal object RemoteLegalDocs {

    private const val TAG = "RemoteLegalDocs"

    /** 缓存有效期：6 小时。期内直接读本地，避免每次进页面都发请求。 */
    private const val CACHE_TTL_MS = 6L * 60 * 60 * 1000

    /** 小于这个长度基本就是错误页 / 半截内容，当成失败。 */
    private const val MIN_VALID_LENGTH = 64

    /**
     * 依次尝试。Gitee 在国内最稳，GitHub raw 常被墙，jsDelivr 兜底。
     * 分支统一为 main（与仓库当前默认分支一致）。
     */
    private val URL_TEMPLATES = listOf(
        "https://gitee.com/TorachenG/Mote/raw/main/docs/legal/%s",
        "https://raw.githubusercontent.com/TorachenG/Mote/main/docs/legal/%s",
        "https://cdn.jsdelivr.net/gh/TorachenG/Mote@main/docs/legal/%s",
    )

    // ---------------------------------------------------------- 本地

    fun bundled(context: Context, doc: RemoteLegalDoc): String =
        runCatching {
            context.assets.open("legal/${doc.fileName}")
                .bufferedReader(Charsets.UTF_8)
                .use { it.readText() }
        }.getOrDefault("")

    private fun cacheFile(context: Context, doc: RemoteLegalDoc): File =
        File(File(context.filesDir, "legal"), doc.fileName)

    private fun readCache(context: Context, doc: RemoteLegalDoc): String? {
        val file = cacheFile(context, doc)
        if (!file.isFile) return null
        return runCatching { file.readText(Charsets.UTF_8) }
            .getOrNull()
            ?.takeIf { it.length > MIN_VALID_LENGTH }
    }

    private fun isCacheFresh(context: Context, doc: RemoteLegalDoc): Boolean {
        val file = cacheFile(context, doc)
        if (!file.isFile) return false
        return System.currentTimeMillis() - file.lastModified() < CACHE_TTL_MS
    }

    private fun writeCache(context: Context, doc: RemoteLegalDoc, text: String) {
        runCatching {
            val file = cacheFile(context, doc)
            file.parentFile?.mkdirs()
            file.writeText(text, Charsets.UTF_8)
        }
    }

    // ---------------------------------------------------------- 远端

    private fun fetchSync(doc: RemoteLegalDoc): String? {
        for (template in URL_TEMPLATES) {
            val url = template.replace("%s", doc.fileName)
            val body = try {
                fetchOnce(url)
            } catch (e: Exception) {
                Log.d(TAG, "fetch failed: $url (${e.javaClass.simpleName})")
                null
            }
            if (!body.isNullOrBlank() && body.length > MIN_VALID_LENGTH) {
                Log.d(TAG, "fetched ${doc.fileName} from $url (${body.length} chars)")
                return body
            }
        }
        return null
    }

    private fun fetchOnce(url: String): String? {
        val conn = URL(url).openConnection() as HttpURLConnection
        return try {
            conn.connectTimeout = 4000
            conn.readTimeout = 4000
            conn.instanceFollowRedirects = true
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Mote-Android")
            if (conn.responseCode != HttpURLConnection.HTTP_OK) return null
            conn.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }

    // ---------------------------------------------------------- 对外

    /** 首帧内容：缓存优先，其次内置。永远不返回空串（除非打包缺失）。 */
    fun loadInitial(context: Context, doc: RemoteLegalDoc): String =
        readCache(context, doc) ?: bundled(context, doc)

    /**
     * 后台刷新：缓存未过期直接返回 null（无需更新）；
     * 否则拉取远端并写缓存，失败返回 null，调用方保持现有内容。
     */
    fun refreshIfNeeded(context: Context, doc: RemoteLegalDoc): String? {
        if (isCacheFresh(context, doc)) return null
        val remote = fetchSync(doc) ?: return null
        writeCache(context, doc, remote)
        return remote
    }
}

// ============================================================
// 页面
// ============================================================

@Composable
internal fun rememberLegalDocText(doc: RemoteLegalDoc): String {
    val context = LocalContext.current
    var text by remember(doc) { mutableStateOf(RemoteLegalDocs.loadInitial(context, doc)) }

    LaunchedEffect(doc) {
        val fresh = withContext(Dispatchers.IO) { RemoteLegalDocs.refreshIfNeeded(context, doc) }
        if (!fresh.isNullOrBlank()) {
            text = fresh
        }
    }
    return text
}

@Composable
internal fun RemoteLegalDocPage(
    doc: RemoteLegalDoc,
    onBack: () -> Unit,
) {
    val text = rememberLegalDocText(doc)

    LegalDocumentScaffold(title = doc.title, onBack = onBack) {
        item(key = "doc_body") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    // 与「下载管理」首张卡片一致：顶部留 12dp，避免卡片直接贴住模糊标题栏
                    .padding(top = 12.dp, bottom = 12.dp),
            ) {
                MarkdownText(
                    text = text,
                    baseColor = MiuixTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                )
            }
        }
        item(key = "bottom_spacer") {
            Spacer(
                Modifier.height(
                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                        WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                )
            )
        }
    }
}
