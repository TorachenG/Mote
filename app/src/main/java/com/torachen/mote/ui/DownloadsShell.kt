package com.torachen.mote.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
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
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationBarDisplayMode
import com.torachen.mote.ui.floating.FloatingBottomBar
import com.torachen.mote.ui.floating.FloatingBottomBarItem
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
    val tabState = remember { mutableIntStateOf(initialTab) }
    val tab = tabState.intValue
    var downloadsSelecting by remember { mutableStateOf(false) }
    var filesSelecting by remember { mutableStateOf(false) }

    when (LocalUiMode.current) {
        UiMode.Miuix -> DownloadsShellMiuix(
            tab = tab,
            onTabChange = { tabState.intValue = it },
            onBack = onBack,
            onOpenDownloadSettings = onOpenDownloadSettings,
            onOpenTaskDetail = onOpenTaskDetail,
            onOpenInBrowser = onOpenInBrowser,
            navBarVisible = !downloadsSelecting && !filesSelecting,
            onDownloadsSelectingChange = { downloadsSelecting = it },
            onFilesSelectingChange = { filesSelecting = it },
            selectedIndex = { tabState.intValue },
        )
        UiMode.Material -> DownloadsShellMaterial(
            tab = tab,
            onTabChange = { tabState.intValue = it },
            onBack = onBack,
            onOpenDownloadSettings = onOpenDownloadSettings,
            onOpenTaskDetail = onOpenTaskDetail,
            onOpenInBrowser = onOpenInBrowser,
            navBarVisible = !downloadsSelecting && !filesSelecting,
            onDownloadsSelectingChange = { downloadsSelecting = it },
            onFilesSelectingChange = { filesSelecting = it },
            selectedIndex = { tabState.intValue },
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
    selectedIndex: () -> Int,
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
            AnimatedContent(
                targetState = tab,
                transitionSpec = {
                    val forward = targetState > initialState
                    val slide = tween<androidx.compose.ui.unit.IntOffset>(durationMillis = 300)
                    val fade = tween<Float>(durationMillis = 220)
                    if (forward) {
                        (slideInHorizontally(slide) { it / 4 } + fadeIn(fade)) togetherWith
                            (slideOutHorizontally(slide) { -it / 4 } + fadeOut(fade))
                    } else {
                        (slideInHorizontally(slide) { -it / 4 } + fadeIn(fade)) togetherWith
                            (slideOutHorizontally(slide) { it / 4 } + fadeOut(fade))
                    }
                },
                label = "sheetTab",
            ) { current ->
                if (current == 0) {
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
        }

        AnimatedVisibility(
            visible = navBarVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            if (backdrop != null) {
                FloatingBottomBar(
                    modifier = Modifier.padding(
                        bottom = 12.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    ),
                    selectedIndex = selectedIndex,
                    onSelected = { onTabChange(it) },
                    backdrop = backdrop,
                    tabsCount = 2,
                    isBlurEnabled = true,
                ) {
                    FloatingBottomBarItem(
                        onClick = { onTabChange(0) },
                        modifier = Modifier.defaultMinSize(minWidth = 76.dp),
                    ) {
                        top.yukonga.miuix.kmp.basic.Icon(
                            imageVector = MiuixIcons.Download,
                            contentDescription = "下载",
                            tint = if (tab == 0) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(22.dp),
                        )
                        top.yukonga.miuix.kmp.basic.Text(
                            "下载",
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = if (tab == 0) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Visible,
                        )
                    }
                    FloatingBottomBarItem(
                        onClick = { onTabChange(1) },
                        modifier = Modifier.defaultMinSize(minWidth = 76.dp),
                    ) {
                        top.yukonga.miuix.kmp.basic.Icon(
                            imageVector = MiuixIcons.Folder,
                            contentDescription = "文件",
                            tint = if (tab == 1) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(22.dp),
                        )
                        top.yukonga.miuix.kmp.basic.Text(
                            "文件",
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = if (tab == 1) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Visible,
                        )
                    }
                }
            } else {
                NavigationBar(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    showDivider = false,
                    mode = NavigationBarDisplayMode.IconAndText,
                ) {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { onTabChange(0) },
                        icon = MiuixIcons.Download,
                        label = "下载",
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { onTabChange(1) },
                        icon = MiuixIcons.Folder,
                        label = "文件",
                    )
                }
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
    selectedIndex: () -> Int,
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
