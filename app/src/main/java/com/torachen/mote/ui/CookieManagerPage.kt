package com.torachen.mote.ui

import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.compose.animation.AnimatedVisibility
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
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
fun CookieManagerPage(onBack: () -> Unit) {
    if (LocalUiMode.current == UiMode.Miuix) CookieManagerPageMiuix(onBack) else CookieManagerPageMaterial(onBack)
}

@Composable
private fun CookieManagerPageMiuix(onBack: () -> Unit) {
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
        TopAppBar(
            title = "Cookie 与数据",
            largeTitle = "Cookie 与数据",
            color = Color.Transparent,
            defaultWindowInsetsPadding = true,
            navigationIcon = {
                IconButton(onClick = { if (selecting) exitSelect() else onBack() }) {
                    Icon(
                        imageVector = if (selecting) MiuixIcons.Delete else MiuixIcons.ChevronBackward,
                        contentDescription = if (selecting) "删除" else "返回",
                        tint = if (selecting) Color(0xFFFF3B30) else MiuixTheme.colorScheme.onSurface,
                    )
                }
            },
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
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
                            .padding(horizontal = 16.dp, vertical = 10.dp),
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
                                    if (group.cookieCount > 0) append(" • ${group.cookieCount} 个 Cookie")
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
                                            onClick = { clearCookieSite(site) },
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
                                                if (site.cookieCount > 0) append(" • ${site.cookieCount} 个 Cookie")
                                            },
                                            fontSize = 12.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        )
                                    }
                                    Icon(
                                        imageVector = MiuixIcons.Delete,
                                        contentDescription = "清除",
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
private fun CookieManagerPageMaterial(onBack: () -> Unit) {
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
                title = { M3Text("Cookie 与数据") },
                navigationIcon = {
                    M3IconButton(onClick = { if (selecting) { selecting = false; selected = emptySet() } else onBack() }) {
                        M3Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "返回")
                    }
                },
                colors = M3TopAppBarDefaults.topAppBarColors(containerColor = bg),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
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
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    M3Text(group.domain, style = M3MaterialTheme.typography.bodyLarge)
                                    Spacer(Modifier.height(2.dp))
                                    M3Text(
                                        buildString {
                                            append(formatBytes(group.totalBytes))
                                            if (group.cookieCount > 0) append(" • ${group.cookieCount} 个 Cookie")
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
                                                .clickable { clearCookieSite(site) }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                M3Text(site.host, style = M3MaterialTheme.typography.bodyMedium)
                                                M3Text(
                                                    buildString {
                                                        append(formatBytes(site.totalBytes))
                                                        if (site.cookieCount > 0) append(" • ${site.cookieCount} 个 Cookie")
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
    }
}
