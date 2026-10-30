package com.torachen.mote.ui.filemanager

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TextButton as M3TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import top.yukonga.miuix.kmp.icon.extended.Back
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.torachen.mote.ui.FileManagerPrefs
import com.torachen.mote.ui.LocalUiMode
import com.torachen.mote.ui.M3SegmentSwitchItem
import com.torachen.mote.ui.MoteSegmentedColumn
import com.torachen.mote.ui.SettingsBackButton
import com.torachen.mote.ui.UiMode
import com.torachen.mote.ui.buildBackCardMaterialModifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.window.WindowDialog

/** 文件管理设置页：目前只有「功能」卡片里的回收站开关 */
@Composable
internal fun FileManagerSettingsPage(
    onBack: () -> Unit,
    onOpenRecycleBin: () -> Unit,
) {
    if (LocalUiMode.current == UiMode.Material) {
        FileManagerSettingsMaterial(
            onBack = onBack,
            onOpenRecycleBin = onOpenRecycleBin,
        )
        return
    }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    FileManagerPrefs.ensure(context)

    var showNotEmptyDialog by remember { mutableStateOf(false) }
    var barBottomPx by remember { mutableIntStateOf(0) }
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val density = LocalDensity.current

    val surface = MiuixTheme.colorScheme.surface
    val onSurface = MiuixTheme.colorScheme.onSurface
    val scrollBehavior = MiuixScrollBehavior()
    val listState = rememberLazyListState()
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 &&
                listState.firstVisibleItemScrollOffset == 0 &&
                !listState.isScrollInProgress
        }
    }

    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surface)
            drawContent()
        }
    } else null
    val backCardAlpha by animateFloatAsState(
        targetValue = if (isAtTop || !blurSupported) 0f else 1f,
        animationSpec = tween(400, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)),
        label = "fmSettingsBackCardAlpha",
    )
    val backCardMaterialModifier = if (blurSupported && backdrop != null) {
        buildBackCardMaterialModifier(2, true, backdrop, surface.luminance() < 0.5f)
    } else Modifier
    val titleBarBlendColorEntries = remember(surface) {
        listOf(BlendColorEntry(color = surface.copy(alpha = 0.8f)))
    }
    val contentTopPadding = if (barBottomPx > 0) with(density) { barBottomPx.toDp() } else statusTop + 64.dp
    val navOwner = rememberNavigationEventDispatcherOwner()

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides navOwner) {
        Box(modifier = Modifier.fillMaxSize().background(surface)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (blurSupported && backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .scrollEndHaptic()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(top = contentTopPadding, bottom = navBottom + 24.dp),
                    overscrollEffect = null,
                ) {
                    item(key = "content") {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(Modifier.height(12.dp))
                            SmallTitle("功能")
                            Card(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                SwitchPreference(
                                    title = "回收站",
                                    summary = "删除的文件先移入回收站，保留 " +
                                        "${FileManagerPrefs.RECYCLE_RETENTION_DAYS} 天后自动清理",
                                    checked = FileManagerPrefs.recycleEnabled,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            FileManagerPrefs.setRecycleEnabled(context, true)
                                        } else {
                                            scope.launch {
                                                val hasContent = withContext(Dispatchers.IO) {
                                                    recycleBinHasContent(context)
                                                }
                                                if (hasContent) {
                                                    showNotEmptyDialog = true
                                                } else {
                                                    FileManagerPrefs.setRecycleEnabled(context, false)
                                                }
                                            }
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .then(
                        if (blurSupported && backdrop != null) {
                            Modifier.progressiveTextureBlur(
                                backdrop = backdrop,
                                shape = RectangleShape,
                                blurRadius = 10f,
                                gradient = ProgressiveBlur.Top.copy(
                                    endFraction = 0.75f,
                                    curve = 2.2f,
                                ),
                                colors = BlurDefaults.blurColors(
                                    blendColors = listOf(BlendColorEntry(color = surface.copy(alpha = 0.3f))),
                                ),
                            ).background(
                                Brush.verticalGradient(
                                    listOf(surface, Color.Transparent),
                                ),
                            )
                        } else {
                            Modifier.background(surface)
                        }
                    )
                    .statusBarsPadding()
                    .onGloballyPositioned { coords ->
                        val bottom = coords.boundsInWindow().bottom.toInt()
                        if (bottom != barBottomPx) barBottomPx = bottom
                    },
            ) {
                TopAppBar(
                    title = "文件管理设置",
                    largeTitle = "文件管理设置",
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        SettingsBackButton(
                            onBack = onBack,
                            progressiveTitleBlur = blurSupported,
                            backCardAlpha = backCardAlpha,
                            backCardMaterialModifier = backCardMaterialModifier,
                            iconTint = onSurface,
                        )
                    },
                )
            }
        }
    }

    WindowDialog(
        show = showNotEmptyDialog,
        title = "关闭回收站",
        onDismissRequest = { showNotEmptyDialog = false },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "请先将回收站里的内容清空再关闭功能",
                style = MiuixTheme.textStyles.body1,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { showNotEmptyDialog = false },
                    modifier = Modifier.weight(1f),
                    colors = MiuixButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("取消") }
                Button(
                    onClick = {
                        showNotEmptyDialog = false
                        onOpenRecycleBin()
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("前往") }
            }
        }
    }
}

private fun recycleBinHasContent(context: android.content.Context): Boolean {
    val root = java.io.File(context.filesDir, "recycle")
    return runCatching { root.walkTopDown().any { it.isFile } }.getOrDefault(false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FileManagerSettingsMaterial(
    onBack: () -> Unit,
    onOpenRecycleBin: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    FileManagerPrefs.ensure(context)
    var showNotEmptyDialog by remember { mutableStateOf(false) }
    val surface = M3MaterialTheme.colorScheme.surface

    Scaffold(
        containerColor = surface,
        topBar = {
            TopAppBar(
                title = { M3Text("文件管理设置") },
                navigationIcon = {
                    M3IconButton(onClick = onBack) {
                        M3Icon(
                            top.yukonga.miuix.kmp.icon.MiuixIcons.Back,
                            contentDescription = "返回",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surface,
                ),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item(key = "function_header") {
                M3Text(
                    text = "功能",
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                    style = M3MaterialTheme.typography.titleSmall,
                    color = M3MaterialTheme.colorScheme.primary,
                )
            }
            item(key = "function_card") {
                MoteSegmentedColumn(
                    content = listOf {
                        M3SegmentSwitchItem(
                            title = "回收站",
                            summary = "删除的文件先移入回收站，保留 " +
                                "${FileManagerPrefs.RECYCLE_RETENTION_DAYS} 天后自动清理",
                            checked = FileManagerPrefs.recycleEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    FileManagerPrefs.setRecycleEnabled(context, true)
                                } else {
                                    scope.launch {
                                        val hasContent = withContext(Dispatchers.IO) {
                                            recycleBinHasContent(context)
                                        }
                                        if (hasContent) {
                                            showNotEmptyDialog = true
                                        } else {
                                            FileManagerPrefs.setRecycleEnabled(context, false)
                                        }
                                    }
                                }
                            },
                        )
                    },
                )
            }
        }
    }

    if (showNotEmptyDialog) {
        AlertDialog(
            onDismissRequest = { showNotEmptyDialog = false },
            title = { M3Text("关闭回收站") },
            text = { M3Text("请先将回收站里的内容清空再关闭功能") },
            confirmButton = {
                M3TextButton(
                    onClick = {
                        showNotEmptyDialog = false
                        onOpenRecycleBin()
                    },
                ) { M3Text("前往") }
            },
            dismissButton = {
                M3TextButton(onClick = { showNotEmptyDialog = false }) {
                    M3Text("取消")
                }
            },
        )
    }
}
