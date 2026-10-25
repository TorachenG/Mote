package com.torachen.mote.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.FolderZip
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastCoerceIn
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun FileManagerHomePage(
    controller: FileManagerController,
    onOpenScanner: () -> Unit,
) {
    val surface = MiuixTheme.colorScheme.surface
    val onSurface = MiuixTheme.colorScheme.onSurface
    val summary = MiuixTheme.colorScheme.onSurfaceVariantSummary
    val density = androidx.compose.ui.platform.LocalDensity.current
    val scrollBehavior = MiuixScrollBehavior()
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val storage = remember { fmStorageUsage() }
    val apps = remember { fmDetectedApps(controller.context) }

    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surface)
            drawContent()
        }
    } else null
    var barBottomPx by remember { mutableIntStateOf(0) }
    val contentTopPadding = if (barBottomPx > 0) with(density) { barBottomPx.toDp() } else statusTop + 64.dp
    val listState = rememberLazyListState()
    val isAtTop by remember {
        androidx.compose.runtime.derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }
    val backCardAlpha by animateFloatAsState(
        targetValue = if (isAtTop || !blurSupported) 0f else 1f,
        animationSpec = tween(400, easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)),
        label = "homeBackCardAlpha",
    )
    val backCardModifier = if (blurSupported && backdrop != null) {
        buildBackCardMaterialModifier(2, true, backdrop, surface.luminance() < 0.5f)
    } else Modifier

    Box(modifier = Modifier.fillMaxSize().background(surface)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .overScrollVertical(),
                contentPadding = PaddingValues(top = contentTopPadding, bottom = navBottom + 96.dp),
                overscrollEffect = null,
            ) {
                item(key = "home") {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Spacer(Modifier.height(12.dp))
                        if (controller.needsAllFiles) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            ) {
                                ArrowPreference(
                                    title = "受系统限制，Mote 需要「所有文件访问权限」才能显示全部文件",
                                    onClick = { requestAllFiles(controller.context) },
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                        // 5×2 功能区
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            insideMargin = PaddingValues(0.dp),
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
                            ) {
                                fmHomeTools.chunked(5).forEachIndexed { rowIndex, row ->
                                    if (rowIndex > 0) Spacer(Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        row.forEach { tool ->
                                            HomeToolCell(tool, onSurface) {
                                                when (tool.tool) {
                                                    FmTool.SCAN -> onOpenScanner()
                                                    FmTool.TRASH -> controller.openRecycleBin()
                                                    else -> controller.enterList(controller.root)
                                                }
                                            }
                                        }
                                        repeat(5 - row.size) { Spacer(Modifier.width(40.dp)) }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // 存储环
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            insideMargin = PaddingValues(0.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val used = storage.first
                                val total = storage.second
                                val fraction = if (total > 0) (used.toFloat() / total).coerceIn(0f, 1f) else 0f
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        progress = fraction,
                                        size = 56.dp,
                                        strokeWidth = 5.dp,
                                    )
                                    Text("${(fraction * 100).toInt()}%", fontSize = 13.sp, color = onSurface)
                                }
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text(fmStorageHint(fraction), fontSize = 16.sp, color = onSurface)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "已占用 ${fmFormatGb(used)} GB / ${fmFormatGb(total)} GB" +
                                            "（可用 ${fmFormatGb((total - used).coerceAtLeast(0))} GB）",
                                        fontSize = 12.sp,
                                        color = summary,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // 手机存储目录
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        ) {
                            ArrowPreference(
                                title = "手机存储目录",
                                startAction = {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(
                                                MiuixTheme.colorScheme.surfaceContainer,
                                                RoundedCornerShape(9.dp),
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.PhoneAndroid,
                                            contentDescription = null,
                                            tint = onSurface,
                                            modifier = Modifier.size(22.dp),
                                        )
                                    }
                                },
                                onClick = { controller.enterList(controller.root) },
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // 2 列分类
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                            fmCategories.chunked(2).forEachIndexed { index, pair ->
                                if (index > 0) Spacer(Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    pair.forEach { entry ->
                                        Card(modifier = Modifier.weight(1f)) {
                                            ArrowPreference(
                                                title = entry.category.label,
                                                startAction = {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(34.dp)
                                                            .background(
                                                                if (entry.usePrimary) MiuixTheme.colorScheme.primary else entry.color,
                                                                RoundedCornerShape(9.dp),
                                                            ),
                                                        contentAlignment = Alignment.Center,
                                                    ) {
                                                        Icon(
                                                            imageVector = entry.icon,
                                                            contentDescription = entry.category.label,
                                                            tint = Color.White,
                                                            modifier = Modifier.size(22.dp),
                                                        )
                                                    }
                                                },
                                                onClick = { controller.openCategory(entry.category) },
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (apps.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    apps.forEach { app ->
                                        val exists = java.io.File(app.dirPath).exists()
                                        ArrowPreference(
                                            title = app.name,
                                            summary = app.dirPath.removePrefix(
                                                android.os.Environment.getExternalStorageDirectory().absolutePath + "/"
                                            ),
                                            startAction = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(34.dp)
                                                        .background(app.color, RoundedCornerShape(9.dp)),
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Text(
                                                        text = app.name.take(1),
                                                        fontSize = 15.sp,
                                                        color = Color.White,
                                                    )
                                                }
                                            },
                                            onClick = {
                                                if (exists) {
                                                    controller.enterList(java.io.File(app.dirPath))
                                                } else {
                                                    android.widget.Toast.makeText(
                                                        controller.context,
                                                        "${app.name} 的下载目录不存在",
                                                        android.widget.Toast.LENGTH_SHORT,
                                                    ).show()
                                                }
                                            },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 顶部模糊 + 标题栏（与下载管理页一致）
        if (blurSupported && backdrop != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(contentTopPadding)
                    .align(Alignment.TopCenter)
                    .progressiveTextureBlur(
                        backdrop = backdrop,
                        shape = RectangleShape,
                        blurRadius = 25f,
                        gradient = ProgressiveBlur(
                            angle = 90f,
                            startFraction = 0.6f,
                            endFraction = 1f,
                        ),
                        colors = BlurDefaults.blurColors(
                            blendColors = listOf(BlendColorEntry(color = surface.copy(alpha = 0.8f))),
                        ),
                    ),
            )
        }

        Box(
            modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).statusBarsPadding()
                .onGloballyPositioned { coords ->
                    val bottom = coords.boundsInWindow().bottom.toInt()
                    if (bottom != barBottomPx) barBottomPx = bottom
                },
        ) {
            TopAppBar(
                title = "文件管理",
                largeTitle = "文件管理",
                color = Color.Transparent,
                defaultWindowInsetsPadding = false,
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    SettingsBackButton(
                        onBack = { if (!controller.onBackPressed()) controller.onBack() },
                        progressiveTitleBlur = blurSupported,
                        backCardAlpha = backCardAlpha,
                        backCardMaterialModifier = backCardModifier,
                        iconTint = onSurface,
                    )
                },
            )
        }
    }
}

@Composable
private fun HomeToolCell(tool: FmToolEntry, onSurface: Color, onClick: () -> Unit) {
    val remote = tool.remoteIcon?.let { rememberRemoteIcon(it) }
    Column(
        modifier = Modifier.width(40.dp).clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (tool.usePrimary) MiuixTheme.colorScheme.primary else tool.color,
                    RoundedCornerShape(10.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (remote != null) {
                Image(
                    bitmap = remote,
                    contentDescription = tool.label,
                    colorFilter = if (tool.whiteMask) {
                        androidx.compose.ui.graphics.ColorFilter.tint(
                            Color.White,
                            androidx.compose.ui.graphics.BlendMode.SrcIn,
                        )
                    } else null,
                    modifier = Modifier.size(24.dp),
                )
            } else {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.label,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(Modifier.height(5.dp))
        Text(tool.label, fontSize = 9.sp, color = onSurface, maxLines = 1, softWrap = false)
    }
}

internal val fmHomeTools: List<FmToolEntry> = listOf(
    FmToolEntry(FmTool.COMPRESS, "文件压缩", Icons.Rounded.FolderZip, Color(0xFFFFFFFF), usePrimary = true),
    FmToolEntry(FmTool.EXTRACT, "文件解压", Icons.Rounded.FolderOpen, Color(0xFFFFFFFF), usePrimary = true),
    FmToolEntry(FmTool.CONVERT, "格式转换", Icons.Rounded.SwapHoriz, Color(0xFFE5E5E5)),
    FmToolEntry(FmTool.QQ, "文件互传", Icons.Rounded.QrCodeScanner, Color(0xFF1DCD3A)),
    FmToolEntry(FmTool.WECHAT, "微信文件", Icons.Rounded.Forum, Color(0xFF1DCD3A)),
    FmToolEntry(FmTool.OCR, "文字提取", Icons.Rounded.TextFields, Color(0xFF7767F9)),
    FmToolEntry(FmTool.SCAN, "扫描文档", Icons.Rounded.QrCodeScanner, Color(0xFF1DCD3A)),
    FmToolEntry(FmTool.TRASH, "回收站", Icons.Rounded.Delete, Color(0xFFE8452C)),
    FmToolEntry(FmTool.CONVERT, "拍照扫描", Icons.Rounded.QrCodeScanner, Color(0xFF16C060)),
    FmToolEntry(FmTool.OCR, "更多功能", Icons.Rounded.SwapHoriz, Color(0xFF9AA0A6)),
)

internal fun fmStorageHint(fraction: Float): String = when {
    fraction >= 0.95f -> "存储空间已接近上限"
    fraction >= 0.8f -> "存储空间比较紧张"
    fraction >= 0.5f -> "存储空间还算宽裕"
    else -> "当前存储空间十分充裕"
}

internal data class FmDetectedApp(val name: String, val dirPath: String, val color: Color)

internal fun fmDetectedApps(context: android.content.Context): List<FmDetectedApp> {
    val pm = context.packageManager
    val root = android.os.Environment.getExternalStorageDirectory()

    data class Def(
        val name: String,
        val pkg: String,
        val color: Color,
        val paths: List<String>,
    )

    val defs = listOf(
        Def(
            "QQ", "com.tencent.mobileqq", Color(0xFF12B7F5),
            listOf(
                "Android/data/com.tencent.mobileqq/tencent/QQfile_recv",
                "tencent/QQfile_recv",
            ),
        ),
        Def(
            "微信", "com.tencent.mm", Color(0xFF07C160),
            listOf(
                "Android/data/com.tencent.mm/MicroMsg/Download",
                "tencent/MicroMsg/Download",
            ),
        ),
        Def(
            "X", "com.twitter.android", Color(0xFF1A1A1A),
            listOf(
                "Android/data/com.twitter.android/files",
                "Pictures/Twitter",
            ),
        ),
        Def(
            "哔哩哔哩", "tv.danmaku.bili", Color(0xFFFB7299),
            listOf(
                "Android/data/tv.danmaku.bili/download",
                "Android/data/tv.danmaku.bili/Download",
            ),
        ),
    )

    val out = mutableListOf<FmDetectedApp>()
    defs.forEach { def ->
        val installed = runCatching { pm.getPackageInfo(def.pkg, 0) }.isSuccess
        val resolved = def.paths
            .map { java.io.File(root, it) }
            .firstOrNull { it.exists() }
            ?: java.io.File(root, def.paths.first())
        if (installed || resolved.exists()) {
            out.add(FmDetectedApp(def.name, resolved.absolutePath, def.color))
        }
    }
    return out
}
