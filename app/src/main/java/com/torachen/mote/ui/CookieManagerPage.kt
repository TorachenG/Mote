package com.torachen.mote.ui

import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** Cookie 详情页参数 */
internal object CookieDetailNav {
    var origin: String? = null
    var host: String? = null
}

/** 单条站点数据 */
internal data class CookieSite(
    val host: String,
    val origin: String,
    val usageBytes: Long,
    val cookieCount: Int,
    val cookieBytes: Long,
) {
    val totalBytes: Long get() = usageBytes + cookieBytes
}

internal data class CookieGroup(
    val domain: String,
    val sites: List<CookieSite>,
) {
    val totalBytes: Long get() = sites.sumOf { it.totalBytes }
    val cookieCount: Int get() = sites.sumOf { it.cookieCount }
}

private fun baseDomainOf(host: String): String {
    val parts = host.split('.').filter { it.isNotBlank() }
    if (parts.size <= 2) return host
    return parts.takeLast(2).joinToString(".")
}

private fun formatBytes(bytes: Long): String = when {
    bytes <= 0 -> "0 字节"
    bytes < 1024 -> "$bytes 字节"
    bytes < 1024 * 1024 -> String.format("%.0f KB", bytes / 1024.0)
    bytes < 1024L * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
    else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
}

/** 读取所有站点（WebStorage 的 localStorage + Cookie） */
internal fun loadCookieGroups(onLoaded: (List<CookieGroup>) -> Unit) {
    val cm = CookieManager.getInstance()
    WebStorage.getInstance().getOrigins { origins ->
        val entries = (origins as? Map<*, *>)?.entries.orEmpty()
        val sites = entries.mapNotNull { entry ->
            val originStr = entry.key as? String ?: return@mapNotNull null
            val host = try { android.net.Uri.parse(originStr).host } catch (_: Exception) { null }
                ?: return@mapNotNull null
            val storage = entry.value as? WebStorage.Origin
            val cookie = try { cm.getCookie(originStr) } catch (_: Exception) { null }
            CookieSite(
                host = host,
                origin = originStr,
                usageBytes = try { storage?.usage ?: 0L } catch (_: Exception) { 0L },
                cookieCount = cookie?.split(';')?.count { it.isNotBlank() } ?: 0,
                cookieBytes = cookie?.length?.toLong() ?: 0L,
            )
        }

        val grouped = sites.groupBy { baseDomainOf(it.host) }
            .map { (domain, list) -> CookieGroup(domain, list.sortedByDescending { it.totalBytes }) }
            .sortedByDescending { it.totalBytes }
        onLoaded(grouped)
    }
}

internal fun clearCookieSite(site: CookieSite) {
    runCatching {
        val cm = CookieManager.getInstance()
        cm.getCookie(site.origin)?.split(';')?.forEach { pair ->
            val name = pair.substringBefore('=').trim()
            if (name.isNotBlank()) {
                cm.setCookie(site.origin, "$name=; Max-Age=0; Path=/")
            }
        }
        WebStorage.getInstance().deleteOrigin(site.origin)
        cm.flush()
    }
}

@Composable
fun CookieManagerPage(onBack: () -> Unit, onOpenOrigin: (origin: String, host: String) -> Unit = { _, _ -> }) {
    if (LocalUiMode.current == UiMode.Miuix) CookieManagerPageMiuix(onBack, onOpenOrigin)
    else CookieManagerPageMaterial(onBack, onOpenOrigin)
}

@Composable
private fun CookieManagerPageMiuix(onBack: () -> Unit, onOpenOrigin: (String, String) -> Unit) {
    val context = LocalContext.current
    var groups by remember { mutableStateOf<List<CookieGroup>>(emptyList()) }
    var expanded by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) { loadCookieGroups { groups = it } }

    fun exitSelect() {
        selecting = false
        selected = emptySet()
    }

    Column(modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface)) {
        val barTitle = if (selecting) "已选择 ${selected.size} 个网站" else "Cookie 与数据"
        TopAppBar(
            title = barTitle,
            largeTitle = barTitle,
            color = Color.Transparent,
            defaultWindowInsetsPadding = true,
            navigationIcon = {
                IconButton(onClick = { if (selecting) exitSelect() else onBack() }) {
                    Icon(
                        imageVector = if (selecting) MiuixIcons.Close else MiuixIcons.ChevronBackward,
                        contentDescription = if (selecting) "关闭" else "返回",
                        tint = MiuixTheme.colorScheme.onSurface,
                    )
                }
            },
            actions = {
                if (selecting) {
                    val all = groups.map { it.domain }.toSet()
                    IconButton(onClick = { selected = if (selected.size == all.size) emptySet() else all }) {
                        Icon(
                            imageVector = MiuixIcons.SelectAll,
                            contentDescription = "全选",
                            tint = if (selected.size == all.size) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurface,
                        )
                    }
                }
            },
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize().overScrollVertical(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            overscrollEffect = null,
        ) {
            if (groups.isEmpty()) {
                item(key = "empty") {
                    Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp), contentAlignment = Alignment.Center) {
                            Text("暂无 Cookie 与网站数据", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        }
                    }
                }
            }
            items(groups, key = { it.domain }) { group ->
                val isOpen = group.domain in expanded
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .combinedClickable(
                                onClick = {
                                    if (selecting) {
                                        selected = if (group.domain in selected) selected - group.domain else selected + group.domain
                                    } else {
                                        expanded = if (isOpen) expanded - group.domain else expanded + group.domain
                                    }
                                },
                                onLongClick = {
                                    selecting = true
                                    selected = selected + group.domain
                                },
                            )
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = group.domain,
                                style = MiuixTheme.textStyles.headline1,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                color = MiuixTheme.colorScheme.onSurface,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = buildString {
                                    append(formatBytes(group.totalBytes))
                                    if (group.cookieCount > 0) append(" | ${group.cookieCount} 个 Cookie")
                                },
                                fontSize = 12.sp,
                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            )
                        }
                        if (selecting) {
                            Checkbox(
                                state = if (group.domain in selected) ToggleableState.On else ToggleableState.Off,
                                onClick = null,
                            )
                        } else {
                            Icon(
                                imageVector = MiuixIcons.ChevronBackward,
                                contentDescription = if (isOpen) "收起" else "展开",
                                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = if (isOpen) 90f else -90f },
                            )
                        }
                    }
                    AnimatedVisibility(visible = isOpen) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            group.sites.forEach { site ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { onOpenOrigin(site.origin, site.host) },
                                        )
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = site.host, fontSize = 15.sp, color = MiuixTheme.colorScheme.onSurface)
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            text = buildString {
                                                append(formatBytes(site.totalBytes))
                                                if (site.cookieCount > 0) append(" | ${site.cookieCount} 个 Cookie")
                                            },
                                            fontSize = 12.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        )
                                    }
                                    IconButton(onClick = { clearCookieSite(site) }) {
                                        Icon(
                                            imageVector = MiuixIcons.Delete,
                                            contentDescription = "清除",
                                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                            modifier = Modifier.size(18.dp),
                                        )
                                    }
                                    Icon(
                                        imageVector = MiuixIcons.ChevronForward,
                                        contentDescription = null,
                                        tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CookieManagerPageMaterial(onBack: () -> Unit, onOpenOrigin: (String, String) -> Unit) {
    val context = LocalContext.current
    var groups by remember { mutableStateOf<List<CookieGroup>>(emptyList()) }
    var expanded by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    val bg = M3MaterialTheme.colorScheme.surface

    LaunchedEffect(Unit) { loadCookieGroups { groups = it } }

    M3Scaffold(
        containerColor = bg,
        topBar = {
            M3TopAppBar(
                title = {
                    M3Text(if (selecting) "已选择 ${selected.size} 个网站" else "Cookie 与数据")
                },
                navigationIcon = {
                    M3IconButton(onClick = { if (selecting) { selecting = false; selected = emptySet() } else onBack() }) {
                        M3Icon(
                            if (selecting) Icons.Filled.Close else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = if (selecting) "关闭" else "返回",
                        )
                    }
                },
                actions = {
                    if (selecting) {
                        val all = groups.map { it.domain }.toSet()
                        M3IconButton(onClick = { selected = if (selected.size == all.size) emptySet() else all }) {
                            M3Icon(Icons.Filled.SelectAll, contentDescription = "全选")
                        }
                    }
                },
                colors = M3TopAppBarDefaults.topAppBarColors(containerColor = bg),
            )
        },
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).overScrollVertical(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            overscrollEffect = null,
        ) {
            if (groups.isEmpty()) {
                item(key = "empty") {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp), contentAlignment = Alignment.Center) {
                        M3Text("暂无 Cookie 与网站数据", style = M3MaterialTheme.typography.bodyMedium, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(groups, key = { it.domain }) { group ->
                val isOpen = group.domain in expanded
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    M3Surface(shape = RoundedCornerShape(16.dp), color = M3MaterialTheme.colorScheme.surface) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .combinedClickable(
                                        onClick = {
                                            if (selecting) {
                                                selected = if (group.domain in selected) selected - group.domain else selected + group.domain
                                            } else {
                                                expanded = if (isOpen) expanded - group.domain else expanded + group.domain
                                            }
                                        },
                                        onLongClick = { selecting = true; selected = selected + group.domain },
                                    )
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    M3Text(group.domain, style = M3MaterialTheme.typography.bodyLarge)
                                    Spacer(Modifier.height(2.dp))
                                    M3Text(
                                        buildString {
                                            append(formatBytes(group.totalBytes))
                                            if (group.cookieCount > 0) append(" | ${group.cookieCount} 个 Cookie")
                                        },
                                        style = M3MaterialTheme.typography.bodySmall,
                                        color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                if (selecting) {
                                    androidx.compose.material3.Checkbox(checked = group.domain in selected, onCheckedChange = null)
                                } else {
                                    M3Text(if (isOpen) "收起" else "展开", style = M3MaterialTheme.typography.bodySmall)
                                }
                            }
                            AnimatedVisibility(visible = isOpen) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    group.sites.forEach { site ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onOpenOrigin(site.origin, site.host) }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                M3Text(site.host, style = M3MaterialTheme.typography.bodyMedium)
                                                M3Text(
                                                    buildString {
                                                        append(formatBytes(site.totalBytes))
                                                        if (site.cookieCount > 0) append(" | ${site.cookieCount} 个 Cookie")
                                                    },
                                                    style = M3MaterialTheme.typography.bodySmall,
                                                    color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                                )
                                            }
                                            M3Icon(Icons.Filled.Delete, contentDescription = "清除", tint = M3MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (selecting) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp)
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(M3MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                M3IconButton(onClick = {
                    groups.filter { it.domain in selected }.forEach { g -> g.sites.forEach { clearCookieSite(it) } }
                    selecting = false
                    selected = emptySet()
                    loadCookieGroups { groups = it }
                }) {
                    M3Icon(Icons.Filled.Delete, contentDescription = "删除", tint = M3MaterialTheme.colorScheme.error)
                }
            }
        }
        }
    }
}


// ============================================================
// 单个站点的 Cookie 列表
// ============================================================

internal data class CookieItem(val name: String, val value: String)

internal fun loadCookies(origin: String): List<CookieItem> {
    val raw = runCatching { CookieManager.getInstance().getCookie(origin) }.getOrNull().orEmpty()
    return raw.split(';').mapNotNull { part ->
        val trimmed = part.trim()
        if (trimmed.isBlank()) return@mapNotNull null
        CookieItem(trimmed.substringBefore('='), trimmed.substringAfter('=', ""))
    }
}

@Composable
fun CookieDetailPage(onBack: () -> Unit) {
    if (LocalUiMode.current == UiMode.Miuix) CookieDetailPageMiuix(onBack) else CookieDetailPageMaterial(onBack)
}

@Composable
private fun CookieDetailPageMiuix(onBack: () -> Unit) {
    val origin = CookieDetailNav.origin.orEmpty()
    val host = CookieDetailNav.host ?: origin
    var cookies by remember { mutableStateOf(loadCookies(origin)) }
    var expanded by remember { mutableStateOf<Set<String>>(emptySet()) }

    Column(modifier = Modifier.fillMaxSize().background(MiuixTheme.colorScheme.surface)) {
        TopAppBar(
            title = host,
            largeTitle = host,
            color = Color.Transparent,
            defaultWindowInsetsPadding = true,
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(MiuixIcons.ChevronBackward, contentDescription = "返回", tint = MiuixTheme.colorScheme.onSurface)
                }
            },
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize().overScrollVertical(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            overscrollEffect = null,
        ) {
            if (cookies.isEmpty()) {
                item(key = "empty") {
                    Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp), contentAlignment = Alignment.Center) {
                            Text("该站点没有 Cookie", fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
                        }
                    }
                }
            }
            items(cookies, key = { it.name }) { cookie ->
                val open = cookie.name in expanded
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .clickable { expanded = if (open) expanded - cookie.name else expanded + cookie.name }
                            .padding(horizontal = 16.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = cookie.name,
                            style = MiuixTheme.textStyles.headline1,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                            color = MiuixTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(onClick = {
                            runCatching {
                                val cm = CookieManager.getInstance()
                                cm.setCookie(origin, "${cookie.name}=; Max-Age=0; Path=/")
                                cm.flush()
                            }
                            cookies = loadCookies(origin)
                        }) {
                            Icon(MiuixIcons.Delete, contentDescription = "删除", tint = MiuixTheme.colorScheme.onSurfaceVariantSummary, modifier = Modifier.size(18.dp))
                        }
                        Icon(
                            imageVector = MiuixIcons.ChevronForward,
                            contentDescription = null,
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = if (open) 90f else -90f },
                        )
                    }
                    AnimatedVisibility(visible = open) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
                            CookieFieldMiuix("名称", cookie.name)
                            CookieFieldMiuix("内容", cookie.value)
                            CookieFieldMiuix("域", host)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CookieFieldMiuix(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text(label, fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurface)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 12.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun CookieDetailPageMaterial(onBack: () -> Unit) {
    val origin = CookieDetailNav.origin.orEmpty()
    val host = CookieDetailNav.host ?: origin
    var cookies by remember { mutableStateOf(loadCookies(origin)) }
    var expanded by remember { mutableStateOf<Set<String>>(emptySet()) }
    val bg = M3MaterialTheme.colorScheme.surface

    M3Scaffold(
        containerColor = bg,
        topBar = {
            M3TopAppBar(
                title = { M3Text(host) },
                navigationIcon = {
                    M3IconButton(onClick = onBack) {
                        M3Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "返回")
                    }
                },
                colors = M3TopAppBarDefaults.topAppBarColors(containerColor = bg),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).overScrollVertical(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            overscrollEffect = null,
        ) {
            if (cookies.isEmpty()) {
                item(key = "empty") {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp), contentAlignment = Alignment.Center) {
                        M3Text("该站点没有 Cookie", style = M3MaterialTheme.typography.bodyMedium, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(cookies, key = { it.name }) { cookie ->
                val open = cookie.name in expanded
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    M3Surface(shape = RoundedCornerShape(16.dp), color = M3MaterialTheme.colorScheme.surface) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 56.dp)
                                    .clickable { expanded = if (open) expanded - cookie.name else expanded + cookie.name }
                                    .padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                M3Text(cookie.name, modifier = Modifier.weight(1f), style = M3MaterialTheme.typography.bodyLarge)
                                M3IconButton(onClick = {
                                    runCatching {
                                        val cm = CookieManager.getInstance()
                                        cm.setCookie(origin, "${cookie.name}=; Max-Age=0; Path=/")
                                        cm.flush()
                                    }
                                    cookies = loadCookies(origin)
                                }) { M3Icon(Icons.Filled.Delete, contentDescription = "删除") }
                                M3Text(if (open) "收起" else "展开", style = M3MaterialTheme.typography.bodySmall)
                            }
                            AnimatedVisibility(visible = open) {
                                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 12.dp)) {
                                    M3Text("名称", style = M3MaterialTheme.typography.bodyMedium)
                                    M3Text(cookie.name, style = M3MaterialTheme.typography.bodySmall, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(8.dp))
                                    M3Text("内容", style = M3MaterialTheme.typography.bodyMedium)
                                    M3Text(cookie.value, style = M3MaterialTheme.typography.bodySmall, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.height(8.dp))
                                    M3Text("域", style = M3MaterialTheme.typography.bodyMedium)
                                    M3Text(host, style = M3MaterialTheme.typography.bodySmall, color = M3MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
