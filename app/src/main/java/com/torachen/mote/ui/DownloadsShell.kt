package com.torachen.mote.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.NavigationBar as M3NavigationBar
import androidx.compose.material3.NavigationBarItem as M3NavigationBarItem
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 下载管理与文件管理的合并页面：底部悬浮导航栏切换两个页签，
 * 任一页签进入多选时隐藏导航栏，由各自的悬浮胶囊工具栏接管。
 */
@Composable
fun DownloadsShell(
    onBack: () -> Unit,
    onOpenDownloadSettings: () -> Unit,
    onOpenTaskDetail: () -> Unit,
    onOpenInBrowser: (String) -> Unit,
    initialTab: Int = 0,
) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    var downloadsSelecting by remember { mutableStateOf(false) }
    var filesSelecting by remember { mutableStateOf(false) }

    when (LocalUiMode.current) {
        UiMode.Miuix -> DownloadsShellMiuix(
            tab = tab,
            onTabChange = { tab = it },
            onBack = onBack,
            onOpenDownloadSettings = onOpenDownloadSettings,
            onOpenTaskDetail = onOpenTaskDetail,
            onOpenInBrowser = onOpenInBrowser,
            navBarVisible = !downloadsSelecting && !filesSelecting,
            onDownloadsSelectingChange = { downloadsSelecting = it },
            onFilesSelectingChange = { filesSelecting = it },
        )
        UiMode.Material -> DownloadsShellMaterial(
            tab = tab,
            onTabChange = { tab = it },
            onBack = onBack,
            onOpenDownloadSettings = onOpenDownloadSettings,
            onOpenTaskDetail = onOpenTaskDetail,
            onOpenInBrowser = onOpenInBrowser,
            navBarVisible = !downloadsSelecting && !filesSelecting,
            onDownloadsSelectingChange = { downloadsSelecting = it },
            onFilesSelectingChange = { filesSelecting = it },
        )
    }
}

@Composable
private fun DownloadsShellMiuix(
    tab: Int,
    onTabChange: (Int) -> Unit,
    onBack: () -> Unit,
    onOpenDownloadSettings: () -> Unit,
    onOpenTaskDetail: () -> Unit,
    onOpenInBrowser: (String) -> Unit,
    navBarVisible: Boolean,
    onDownloadsSelectingChange: (Boolean) -> Unit,
    onFilesSelectingChange: (Boolean) -> Unit,
) {
    val surface = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surface)
            drawContent()
        }
    } else null
    val shape = remember { RoundedCornerShape(28.dp) }

    Box(modifier = Modifier.fillMaxSize().background(surface)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
        ) {
            if (tab == 0) {
                DownloadManagerScreen(
                    onBack = onBack,
                    onOpenDownloadSettings = onOpenDownloadSettings,
                    onOpenTaskDetail = onOpenTaskDetail,
                    onSelectionModeChange = onDownloadsSelectingChange,
                )
            } else {
                FileManagerPage(
                    onBack = { onTabChange(0) },
                    onOpenInBrowser = onOpenInBrowser,
                    onSelectionModeChange = onFilesSelectingChange,
                )
            }
        }

        AnimatedVisibility(
            visible = navBarVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            FloatingNavigationBar(
                modifier = if (backdrop != null) {
                    Modifier.textureBlur(
                        backdrop = backdrop,
                        shape = shape,
                        blurRadius = 25f,
                        colors = BlurDefaults.blurColors(
                            blendColors = listOf(
                                BlendColorEntry(color = MiuixTheme.colorScheme.surfaceContainer.copy(0.6f)),
                            ),
                        ),
                    )
                } else {
                    Modifier
                },
                color = MiuixTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f),
                cornerRadius = 28.dp,
            ) {
                NavItem(selected = tab == 0, icon = MiuixIcons.Download, label = "下载") { onTabChange(0) }
                NavItem(selected = tab == 1, icon = MiuixIcons.Folder, label = "文件") { onTabChange(1) }
            }
        }
    }
}

@Composable
private fun DownloadsShellMaterial(
    tab: Int,
    onTabChange: (Int) -> Unit,
    onBack: () -> Unit,
    onOpenDownloadSettings: () -> Unit,
    onOpenTaskDetail: () -> Unit,
    onOpenInBrowser: (String) -> Unit,
    navBarVisible: Boolean,
    onDownloadsSelectingChange: (Boolean) -> Unit,
    onFilesSelectingChange: (Boolean) -> Unit,
) {
    val bg = M3MaterialTheme.colorScheme.surface

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        if (tab == 0) {
            DownloadManagerScreen(
                onBack = onBack,
                onOpenDownloadSettings = onOpenDownloadSettings,
                onOpenTaskDetail = onOpenTaskDetail,
                onSelectionModeChange = onDownloadsSelectingChange,
            )
        } else {
            FileManagerPage(
                onBack = { onTabChange(0) },
                onOpenInBrowser = onOpenInBrowser,
                onSelectionModeChange = onFilesSelectingChange,
            )
        }

        AnimatedVisibility(
            visible = navBarVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            M3NavigationBar(containerColor = M3MaterialTheme.colorScheme.surfaceContainer) {
                M3NavigationBarItem(
                    selected = tab == 0,
                    onClick = { onTabChange(0) },
                    icon = { M3Icon(MiuixIcons.Download, contentDescription = "下载") },
                    label = { M3Text("下载") },
                )
                M3NavigationBarItem(
                    selected = tab == 1,
                    onClick = { onTabChange(1) },
                    icon = { M3Icon(Icons.Filled.Folder, contentDescription = "文件") },
                    label = { M3Text("文件") },
                )
            }
        }
    }
}

/** 图标在上、文字在下的导航项，容器仍使用原生 FloatingNavigationBar */
@Composable
private fun NavItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    val tint = if (selected) MiuixTheme.colorScheme.primary else MiuixTheme.colorScheme.onSurfaceVariantSummary
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        top.yukonga.miuix.kmp.basic.Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(2.dp))
        top.yukonga.miuix.kmp.basic.Text(
            label,
            fontSize = 11.sp,
            color = tint,
        )
    }
}
