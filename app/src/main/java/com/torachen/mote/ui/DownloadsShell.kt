package com.torachen.mote.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationBarDisplayMode
import com.torachen.mote.ui.floating.FloatingBottomBar
import com.torachen.mote.ui.floating.FloatingBottomBarItem
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.FileDownloads
import top.yukonga.miuix.kmp.icon.extended.FolderFill
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
    onOpenScanner: () -> Unit = {},
) {
    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    val tab = pagerState.currentPage
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    // 复用同一个 controller，避免每次切到文件页都重新扫描目录导致掉帧
    val fileController = remember {
        FileManagerController(context, onOpenInBrowser) { scope.launch { pagerState.animateScrollToPage(0) } }
    }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) { fileController.reload() }
    }
    val openDownloadsFolder: () -> Unit = {
        val target = java.io.File(
            android.os.Environment.getExternalStorageDirectory(),
            "Download/Mote",
        )
        if (!target.exists()) target.mkdirs()
        fileController.enter(target)
        scope.launch { pagerState.animateScrollToPage(1) }
    }
    var downloadsSelecting by remember { mutableStateOf(false) }
    var filesSelecting by remember { mutableStateOf(false) }
    var filesSubPage by remember { mutableStateOf(false) }

    when (LocalUiMode.current) {
        UiMode.Miuix -> DownloadsShellMiuix(
            fileController = fileController,
            tab = tab,
            onTabChange = { scope.launch { pagerState.animateScrollToPage(it) } },
            pagerState = pagerState,
            swipeEnabled = !downloadsSelecting && !filesSelecting,
            onBack = onBack,
            onOpenDownloadSettings = onOpenDownloadSettings,
            onOpenTaskDetail = onOpenTaskDetail,
            onOpenInBrowser = onOpenInBrowser,
            navBarVisible = !downloadsSelecting && !filesSelecting && !filesSubPage,
            onDownloadsSelectingChange = { downloadsSelecting = it },
            onFilesSelectingChange = { filesSelecting = it },
            onFilesSubPageChange = { filesSubPage = it },
            selectedIndex = { pagerState.currentPage },
            onOpenDownloadsFolder = openDownloadsFolder,
            onOpenScanner = onOpenScanner,
        )
        UiMode.Material -> DownloadsShellMaterial(
            fileController = fileController,
            tab = tab,
            onTabChange = { scope.launch { pagerState.animateScrollToPage(it) } },
            pagerState = pagerState,
            swipeEnabled = !downloadsSelecting && !filesSelecting,
            onBack = onBack,
            onOpenDownloadSettings = onOpenDownloadSettings,
            onOpenTaskDetail = onOpenTaskDetail,
            onOpenInBrowser = onOpenInBrowser,
            navBarVisible = !downloadsSelecting && !filesSelecting && !filesSubPage,
            onDownloadsSelectingChange = { downloadsSelecting = it },
            onFilesSelectingChange = { filesSelecting = it },
            onFilesSubPageChange = { filesSubPage = it },
            selectedIndex = { pagerState.currentPage },
            onOpenDownloadsFolder = openDownloadsFolder,
            onOpenScanner = onOpenScanner,
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
    onFilesSubPageChange: (Boolean) -> Unit,
    selectedIndex: () -> Int,
    fileController: FileManagerController,
    pagerState: androidx.compose.foundation.pager.PagerState,
    swipeEnabled: Boolean,
    onOpenDownloadsFolder: () -> Unit,
    onOpenScanner: () -> Unit,
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
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1,
                userScrollEnabled = swipeEnabled,
            ) { page ->
                if (page == 0) {
                    DownloadManagerScreen(
                        onBack = onBack,
                        onOpenDownloadSettings = onOpenDownloadSettings,
                        onOpenTaskDetail = onOpenTaskDetail,
                        onSelectionModeChange = onDownloadsSelectingChange,
                        onOpenDownloadsFolder = onOpenDownloadsFolder,
                    )
                } else {
                    FileManagerPage(
                        onBack = { onTabChange(0) },
                        onOpenInBrowser = onOpenInBrowser,
                        onSelectionModeChange = onFilesSelectingChange,
                        controller = fileController,
                        onOpenScanner = onOpenScanner,
                        onSubPageChange = onFilesSubPageChange,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = navBarVisible,
            enter = fadeIn(tween(220)) +
                slideInVertically(tween(320, easing = androidx.compose.animation.core.CubicBezierEasing(0.32f, 0.72f, 0f, 1f))) { it / 2 } +
                scaleIn(tween(320, easing = androidx.compose.animation.core.CubicBezierEasing(0.32f, 0.72f, 0f, 1f)), initialScale = 0.86f),
            exit = fadeOut(tween(160)) +
                slideOutVertically(tween(220, easing = androidx.compose.animation.core.CubicBezierEasing(0.4f, 0f, 1f, 1f))) { it / 3 } +
                scaleOut(tween(220), targetScale = 0.9f),
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
                    // 与液体指示器位置同步：指示器靠近多少，图标就蓝多少
                    val pillPos = com.torachen.mote.ui.floating.LocalFloatingBottomBarPillPosition.current
                    val idleColor = MiuixTheme.colorScheme.onSurfaceVariantSummary
                    val activeColor = MiuixTheme.colorScheme.primary
                    val downloadCoverage =
                        (1f - kotlin.math.abs(pillPos() - 0f)).coerceIn(0f, 1f)
                    val fileCoverage =
                        (1f - kotlin.math.abs(pillPos() - 1f)).coerceIn(0f, 1f)
                    val downloadTint = androidx.compose.ui.graphics.lerp(idleColor, activeColor, downloadCoverage)
                    val fileTint = androidx.compose.ui.graphics.lerp(idleColor, activeColor, fileCoverage)

                    FloatingBottomBarItem(
                        onClick = { onTabChange(0) },
                        modifier = Modifier.defaultMinSize(minWidth = 76.dp),
                    ) {
                        top.yukonga.miuix.kmp.basic.Icon(
                            imageVector = MiuixIcons.FileDownloads,
                            contentDescription = "下载",
                            tint = downloadTint,
                            modifier = Modifier.size(22.dp),
                        )
                        top.yukonga.miuix.kmp.basic.Text(
                            "下载",
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = downloadTint,
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
                            imageVector = MiuixIcons.FolderFill,
                            contentDescription = "文件",
                            tint = fileTint,
                            modifier = Modifier.size(22.dp),
                        )
                        top.yukonga.miuix.kmp.basic.Text(
                            "文件",
                            fontSize = 11.sp,
                            lineHeight = 14.sp,
                            color = fileTint,
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
                        icon = MiuixIcons.FileDownloads,
                        label = "下载",
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { onTabChange(1) },
                        icon = MiuixIcons.FolderFill,
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
    onFilesSubPageChange: (Boolean) -> Unit,
    selectedIndex: () -> Int,
    fileController: FileManagerController,
    pagerState: androidx.compose.foundation.pager.PagerState,
    swipeEnabled: Boolean,
    onOpenDownloadsFolder: () -> Unit,
    onOpenScanner: () -> Unit,
) {
    val bg = M3MaterialTheme.colorScheme.surface

    Box(modifier = Modifier.fillMaxSize().background(bg)) {
        if (tab == 0) {
            DownloadManagerScreen(
                onBack = onBack,
                onOpenDownloadSettings = onOpenDownloadSettings,
                onOpenTaskDetail = onOpenTaskDetail,
                onSelectionModeChange = onDownloadsSelectingChange,
                onOpenDownloadsFolder = onOpenDownloadsFolder,
            )
        } else {
            FileManagerPage(
                onBack = { onTabChange(0) },
                onOpenInBrowser = onOpenInBrowser,
                onSelectionModeChange = onFilesSelectingChange,
                controller = fileController,
                onOpenScanner = onOpenScanner,
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
                    icon = { M3Icon(MiuixIcons.FileDownloads, contentDescription = "下载") },
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
