package com.torachen.mote.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.lerp
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu as M3DropdownMenu
import androidx.compose.material3.DropdownMenuItem as M3DropdownMenuItem
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
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
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.window.WindowCascadingListPopup

private const val TOOL_MENU_PROGRESSIVE_BLUR_KEY = "progressive_title_blur"
private const val TOOL_MENU_TOP_BAR_BLEND_ALPHA = 0.8f
private val TOOL_MENU_TOP_BAR_FALLBACK_HEIGHT = 128.dp
private val ToolMenuSmoothEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
private val TOOL_MENU_CELL_HEIGHT = 68.dp
private val TOOL_MENU_ROW_GAP = 10.dp
private const val TOOL_MENU_MIN_ENABLED = 10

/** 工具菜单全部可用 id（默认顺序，含未记录的新工具） */
internal fun allToolMenuIds(prefs: SharedPreferences): List<String> {
    val all = defaultTools().filter { it.id != "play_in_player" }.map { it.id }
    val enabledRaw = prefs.getString("tool_enabled_ids", null)
    val legacyRaw = prefs.getString("tool_order", null)
    val base = when {
        !enabledRaw.isNullOrEmpty() -> enabledRaw.split(",")
        !legacyRaw.isNullOrEmpty() -> legacyRaw.split(",")
        else -> all
    }.filter { it.isNotBlank() && it in all }.distinct()
    val missing = all.filter { it !in base }
    return base + missing
}

/** 工具菜单中「未启用的功能」的 id（按保存顺序） */
internal fun toolMenuDisabledIds(prefs: SharedPreferences): List<String> {
    val raw = prefs.getString("tool_disabled_ids", null) ?: return emptyList()
    val all = defaultTools().filter { it.id != "play_in_player" }.map { it.id }.toSet()
    return raw.split(",").filter { it.isNotBlank() && it in all }.distinct()
}

internal fun saveToolMenuIds(
    prefs: SharedPreferences,
    enabled: List<String>,
    disabled: List<String>,
) {
    prefs.edit()
        .putString("tool_enabled_ids", enabled.joinToString(","))
        .putString("tool_disabled_ids", disabled.joinToString(","))
        .apply()
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun ToolMenuCustomizePage(onBack: () -> Unit) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }
    val isMaterial = LocalUiMode.current == UiMode.Material
    val surfaceColor = if (isMaterial) {
        androidx.compose.ui.graphics.lerp(
            M3MaterialTheme.colorScheme.surfaceContainerLow,
            M3MaterialTheme.colorScheme.secondaryContainer,
            0.10f,
        )
    } else {
        MiuixTheme.colorScheme.surface
    }
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else null

    var progressiveTitleBlur by remember {
        mutableStateOf(prefs.getBoolean(TOOL_MENU_PROGRESSIVE_BLUR_KEY, true))
    }
    var materialStyle by remember { mutableIntStateOf(prefs.getInt("ui_material_style", 2)) }
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                TOOL_MENU_PROGRESSIVE_BLUR_KEY -> progressiveTitleBlur = prefs.getBoolean(key, true)
                "ui_material_style" -> materialStyle = prefs.getInt(key, 2)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val listState = rememberLazyListState()
    val isAtTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0 &&
                    !listState.isScrollInProgress
        }
    }
    val themeIsDark = surfaceColor.luminance() < 0.5f
    val backCardAlpha by animateFloatAsState(
        targetValue = if (isAtTop || !progressiveTitleBlur) 0f else 1f,
        animationSpec = tween(400, easing = ToolMenuSmoothEase),
        label = "toolMenuBackCardAlpha",
    )
    val backCardMaterialModifier = if (progressiveTitleBlur) {
        buildBackCardMaterialModifier(materialStyle, blurSupported, backdrop, themeIsDark)
    } else Modifier
    val titleBarBlendColorEntries = remember(surfaceColor) {
        listOf(BlendColorEntry(color = surfaceColor.copy(alpha = TOOL_MENU_TOP_BAR_BLEND_ALPHA)))
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val measuredTop = with(density) { topBarBottomPx.toDp() }
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop
    else statusBarHeight + TOOL_MENU_TOP_BAR_FALLBACK_HEIGHT
    val rowPitchPx = with(density) { (TOOL_MENU_CELL_HEIGHT + TOOL_MENU_ROW_GAP).toPx() }

    val allTools = remember { defaultTools().filter { it.id != "play_in_player" } }
    val toolById = remember { allTools.associateBy { it.id } }
    val disabledInit = remember { toolMenuDisabledIds(prefs) }
    val enabledInit = remember { allToolMenuIds(prefs).filter { it !in disabledInit } }

    val enabled = remember { mutableStateListOf<String>().apply { addAll(enabledInit) } }
    val disabled = remember { mutableStateListOf<String>().apply { addAll(disabledInit) } }

    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragFromEnabled by remember { mutableStateOf(true) }
    var dragCenter by remember { mutableStateOf(Offset.Zero) }
    var crossIndex by remember { mutableStateOf<Int?>(null) }
    var crossIntoEnabled by remember { mutableStateOf<Boolean?>(null) }
    var editMode by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var enabledBounds by remember { mutableStateOf<Rect?>(null) }
    var disabledBounds by remember { mutableStateOf<Rect?>(null) }
    val scope = rememberCoroutineScope()
    val settleAnim = remember { Animatable(0f) }
    var settling by remember { mutableStateOf(false) }
    var settleFrom by remember { mutableStateOf(Offset.Zero) }
    var settleTo by remember { mutableStateOf(Offset.Zero) }

    fun persist() {
        saveToolMenuIds(prefs, enabled.toList(), disabled.toList())
    }

    fun moveAcross(id: String, intoEnabled: Boolean, index: Int) {
        if (intoEnabled) {
            if (id in enabled) return
            if (!disabled.remove(id)) return
            enabled.add(index.coerceIn(0, enabled.size), id)
        } else {
            if (id in disabled) return
            if (enabled.size <= TOOL_MENU_MIN_ENABLED) return
            if (!enabled.remove(id)) return
            disabled.add(index.coerceIn(0, disabled.size), id)
        }
        persist()
    }

    fun moveToEnabledEnd(id: String) {
        if (id in enabled) return
        if (!disabled.remove(id)) return
        enabled.add(id)
        persist()
    }

    fun moveToDisabledEnd(id: String) {
        if (id in disabled) return
        if (enabled.size <= TOOL_MENU_MIN_ENABLED) return
        if (!enabled.remove(id)) return
        disabled.add(id)
        persist()
    }

    fun resetAll() {
        enabled.clear()
        enabled.addAll(allTools.map { it.id })
        disabled.clear()
        persist()
    }

    fun indexAt(bounds: Rect, center: Offset, size: Int, forInsert: Boolean): Int {
        val cellW = bounds.width / 5f
        val col = ((center.x - bounds.left) / cellW).toInt().coerceIn(0, 4)
        val row = ((center.y - bounds.top) / rowPitchPx).toInt().coerceAtLeast(0)
        val raw = row * 5 + col
        return if (forInsert) raw.coerceIn(0, size)
        else raw.coerceIn(0, (size - 1).coerceAtLeast(0))
    }

    fun startDrag(id: String, fromEnabled: Boolean, center: Offset) {
        editMode = true
        draggingId = id
        dragFromEnabled = fromEnabled
        dragCenter = center
        crossIndex = null
        crossIntoEnabled = null
    }

    fun dragDelta(delta: Offset) {
        val id = draggingId ?: return
        dragCenter += delta
        val eb = enabledBounds
        val db = disabledBounds
        val inEnabled = eb != null && eb.contains(dragCenter)
        val inDisabled = db != null && db.contains(dragCenter)
        if (dragFromEnabled) {
            if (inDisabled && db != null) {
                crossIntoEnabled = false
                crossIndex = indexAt(db, dragCenter, disabled.size, true)
            } else if (inEnabled && eb != null) {
                crossIntoEnabled = null
                crossIndex = null
                val target = indexAt(eb, dragCenter, enabled.size, false)
                val cur = enabled.indexOf(id)
                if (cur >= 0 && target != cur) {
                    enabled.add(target, enabled.removeAt(cur))
                    persist()
                }
            } else {
                crossIntoEnabled = null
                crossIndex = null
            }
        } else {
            if (inEnabled && eb != null) {
                crossIntoEnabled = true
                crossIndex = indexAt(eb, dragCenter, enabled.size, true)
            } else if (inDisabled && db != null) {
                crossIntoEnabled = null
                crossIndex = null
                val target = indexAt(db, dragCenter, disabled.size, false)
                val cur = disabled.indexOf(id)
                if (cur >= 0 && target != cur) {
                    disabled.add(target, disabled.removeAt(cur))
                    persist()
                }
            } else {
                crossIntoEnabled = null
                crossIndex = null
            }
        }
    }

    fun endDrag() {
        val id = draggingId ?: return
        val into = crossIntoEnabled
        val idx = crossIndex
        if (into != null && idx != null) {
            moveAcross(id, into, idx)
        }
        crossIntoEnabled = null
        crossIndex = null

        val targetEnabled = into ?: dragFromEnabled
        settleFrom = dragCenter
        settleTo = dragCenter
        settling = true
        scope.launch {
            // 先等数据落位并完成一次布局，卡片高度与位置稳定后再取目标格位
            withFrameNanos { }
            withFrameNanos { }
            val list = if (targetEnabled) enabled else disabled
            val bounds = if (targetEnabled) enabledBounds else disabledBounds
            val pos = list.indexOf(id)
            if (bounds != null && pos >= 0) {
                val cellW = bounds.width / 5f
                settleTo = Offset(
                    bounds.left + cellW * ((pos % 5) + 0.5f),
                    bounds.top + rowPitchPx * ((pos / 5) + 0.5f),
                )
            }
            settleAnim.snapTo(0f)
            settleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(220, easing = ToolMenuSmoothEase),
            )
            draggingId = null
            settling = false
            settleAnim.snapTo(0f)
        }
    }

    fun exitEditMode() {
        editMode = false
        draggingId = null
        crossIndex = null
        crossIntoEnabled = null
    }

    BackHandler(enabled = editMode) { exitEditMode() }

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides navOwner) {
        Box(modifier = Modifier.fillMaxSize().background(surfaceColor)) {
            Box(
                modifier = Modifier.fillMaxSize().then(
                    if (blurSupported && backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier
                )
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .scrollEndHaptic()
                        .then(
                            if (editMode && draggingId == null) Modifier
                            else Modifier.overScrollVertical()
                        ),
                    contentPadding = PaddingValues(
                        top = contentTopPadding,
                        bottom = WindowInsets.navigationBars.asPaddingValues()
                            .calculateBottomPadding() + 32.dp,
                    ),
                    overscrollEffect = null,
                ) {
                    item(key = "tool_hint") {
                        ToolMenuCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp)
                                .padding(top = 12.dp, bottom = 12.dp),
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(vertical = 20.dp, horizontal = 16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                ToolMenuHintText()
                            }
                        }
                    }

                    item(key = "tool_enabled_header") { ToolMenuSectionTitle("工具菜单") }

                    item(key = "tool_enabled_grid") {
                        ToolMenuCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(tween(220, easing = ToolMenuSmoothEase))
                                .padding(horizontal = 12.dp)
                                .padding(bottom = 12.dp),
                        ) {
                            ToolMenuGrid(
                                ids = enabled,
                                toolById = toolById,
                                showBadges = editMode && enabled.size > TOOL_MENU_MIN_ENABLED,
                                badgeColor = Color(0xFFFF3B30),
                                badgeIsAdd = false,
                                draggingId = draggingId,
                                onBadgeClick = { moveToDisabledEnd(it) },
                                onStartDrag = { id, center -> startDrag(id, true, center) },
                                onDragDelta = { dragDelta(it) },
                                onDragEnd = { endDrag() },
                                onBoundsChanged = { enabledBounds = it },
                            )
                        }
                    }

                    item(key = "tool_gap") { Spacer(Modifier.height(4.dp)) }

                    item(key = "tool_disabled_header") { ToolMenuSectionTitle("未启用的功能") }

                    if (disabled.isEmpty()) {
                        item(key = "tool_disabled_empty") {
                            ToolMenuCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(tween(220, easing = ToolMenuSmoothEase))
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp)
                                    .onGloballyPositioned { disabledBounds = it.boundsInWindow() },
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 28.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    ToolMenuEmptyText()
                                }
                            }
                        }
                    } else {
                        item(key = "tool_disabled_grid") {
                            ToolMenuCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(tween(220, easing = ToolMenuSmoothEase))
                                    .padding(horizontal = 12.dp)
                                    .padding(bottom = 12.dp),
                            ) {
                                ToolMenuGrid(
                                    ids = disabled,
                                    toolById = toolById,
                                    showBadges = editMode,
                                    badgeColor = MiuixTheme.colorScheme.primary,
                                    badgeIsAdd = true,
                                    draggingId = draggingId,
                                    onBadgeClick = { moveToEnabledEnd(it) },
                                    onStartDrag = { id, center -> startDrag(id, false, center) },
                                    onDragDelta = { dragDelta(it) },
                                    onDragEnd = { endDrag() },
                                    onBoundsChanged = { disabledBounds = it },
                                )
                            }
                        }
                    }

                    item(key = "tool_bottom_spacer") { Spacer(Modifier.height(24.dp)) }
                }
            }

            // 拖动浮层：绘制在最上层，可跨卡片显示
            val overlayTool = draggingId?.let { toolById[it] }
            val sourceBounds = if (dragFromEnabled) enabledBounds else disabledBounds
            if (overlayTool != null && sourceBounds != null) {
                val cellWpx = sourceBounds.width / 5f
                val cellWdp = with(density) { cellWpx.toDp() }
                val overlayPos = if (settling) {
                    lerp(settleFrom, settleTo, settleAnim.value)
                } else {
                    dragCenter
                }
                val overlayScale = if (settling) 1.12f - 0.12f * settleAnim.value else 1.12f
                Box(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                (overlayPos.x - cellWpx / 2f).roundToInt(),
                                (overlayPos.y - rowPitchPx / 2f).roundToInt(),
                            )
                        }
                        .size(cellWdp, TOOL_MENU_CELL_HEIGHT)
                        .zIndex(100f)
                        .graphicsLayer {
                            scaleX = overlayScale
                            scaleY = overlayScale
                        }
                        .background(
                            MiuixTheme.colorScheme.surfaceContainer,
                            RoundedCornerShape(14.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    ToolGridItem(tool = overlayTool, isActive = false, isEnabled = true)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .then(
                        if (blurSupported && backdrop != null && materialStyle != 0) {
                            if (progressiveTitleBlur) {
                                Modifier.progressiveTextureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = 10f,
                                    gradient = ProgressiveBlur.Top.copy(
                                        startFraction = 0.35f,
                                        endFraction = 1.35f,
                                        curve = 2.2f,
                                    ),
                                    colors = BlurDefaults.blurColors(
                                        blendColors = listOf(BlendColorEntry(color = surfaceColor.copy(alpha = 0.3f))),
                                    ),
                                )
                            } else {
                                Modifier.textureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = 24f,
                                    noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                    colors = BlurDefaults.blurColors(blendColors = titleBarBlendColorEntries),
                                )
                            }
                        } else Modifier.background(surfaceColor)
                    )
                    .statusBarsPadding()
                    .onGloballyPositioned { coords ->
                        val bottomPx = coords.boundsInWindow().bottom.toInt()
                        if (bottomPx != topBarBottomPx) topBarBottomPx = bottomPx
                    }
            ) {
                val barTitle = if (editMode) "编辑模式" else "工具弹窗自定义"
                if (isMaterial) {
                    M3TopAppBar(
                        title = { M3Text(barTitle) },
                        colors = M3TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        navigationIcon = {
                            if (editMode) {
                                M3IconButton(onClick = { exitEditMode() }) {
                                    M3Icon(Icons.Filled.Close, contentDescription = "退出编辑模式")
                                }
                            } else {
                                M3IconButton(onClick = onBack) {
                                    M3Icon(
                                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "返回",
                                    )
                                }
                            }
                        },
                        actions = {
                            Box {
                                M3IconButton(onClick = { showMoreMenu = true }) {
                                    M3Icon(MiuixIcons.More, contentDescription = "更多")
                                }
                                M3DropdownMenu(
                                    expanded = showMoreMenu,
                                    onDismissRequest = { showMoreMenu = false },
                                    shape = RoundedCornerShape(16.dp),
                                    containerColor = M3MaterialTheme.colorScheme.surfaceContainerHigh,
                                ) {
                                    M3DropdownMenuItem(
                                        text = { M3Text("重置") },
                                        onClick = {
                                            resetAll()
                                            showMoreMenu = false
                                        },
                                    )
                                }
                            }
                        },
                    )
                } else {
                    TopAppBar(
                        title = barTitle,
                        largeTitle = barTitle,
                        scrollBehavior = scrollBehavior,
                        color = Color.Transparent,
                        defaultWindowInsetsPadding = false,
                        navigationIcon = {
                            if (editMode) {
                                IconButton(onClick = { exitEditMode() }) {
                                    Icon(
                                        imageVector = MiuixIcons.Close,
                                        contentDescription = "退出编辑模式",
                                        tint = MiuixTheme.colorScheme.onSurface,
                                    )
                                }
                            } else {
                                SettingsBackButton(
                                    onBack = onBack,
                                    progressiveTitleBlur = progressiveTitleBlur,
                                    backCardAlpha = backCardAlpha,
                                    backCardMaterialModifier = backCardMaterialModifier,
                                    iconTint = MiuixTheme.colorScheme.onSurface,
                                )
                            }
                        },
                        actions = {
                            Box {
                                IconButton(onClick = { showMoreMenu = true }) {
                                    Icon(
                                        imageVector = MiuixIcons.More,
                                        contentDescription = "更多",
                                        tint = MiuixTheme.colorScheme.onSurface,
                                    )
                                }
                                WindowCascadingListPopup(
                                    show = showMoreMenu,
                                    entries = listOf(
                                        DropdownEntry(
                                            items = listOf(
                                                DropdownItem(
                                                    text = "重置",
                                                    selected = false,
                                                    onClick = {
                                                        resetAll()
                                                        showMoreMenu = false
                                                    },
                                                ),
                                            ),
                                        ),
                                    ),
                                    onDismissRequest = { showMoreMenu = false },
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

/** 5×N 功能网格（与工具弹窗一致）；拖动由页面统一处理，本组件负责上报位置 */
@Composable
private fun ToolMenuGrid(
    ids: List<String>,
    toolById: Map<String, ToolDef>,
    showBadges: Boolean,
    badgeColor: Color,
    badgeIsAdd: Boolean,
    draggingId: String?,
    onBadgeClick: (String) -> Unit,
    onStartDrag: (id: String, center: Offset) -> Unit,
    onDragDelta: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onBoundsChanged: (Rect) -> Unit,
) {
    val density = LocalDensity.current
    var cardBounds by remember { mutableStateOf(Rect.Zero) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .onGloballyPositioned {
                cardBounds = it.boundsInWindow()
                onBoundsChanged(cardBounds)
            },
    ) {
        val cellWidth = maxWidth / 5
        val cellHeight = TOOL_MENU_CELL_HEIGHT
        val rowPitch = cellHeight + TOOL_MENU_ROW_GAP
        val cellWidthPx = with(density) { cellWidth.toPx() }
        val rowPitchPx = with(density) { rowPitch.toPx() }
        val rows = (ids.size + 4) / 5
        val gridHeight = if (rows <= 0) 0.dp else rowPitch * rows - TOOL_MENU_ROW_GAP

        Box(modifier = Modifier.fillMaxWidth().height(gridHeight)) {
            ids.forEachIndexed { index, id ->
                val tool = toolById[id] ?: return@forEachIndexed
                key(id) {
                    val isDragging = draggingId == id
                    val col = index % 5
                    val row = index / 5
                    val targetX = cellWidthPx * col
                    val targetY = rowPitchPx * row

                    val displayX by animateFloatAsState(
                        targetValue = targetX,
                        animationSpec = tween(200, easing = ToolMenuSmoothEase),
                        label = "toolCellX_$id",
                    )
                    val displayY by animateFloatAsState(
                        targetValue = targetY,
                        animationSpec = tween(200, easing = ToolMenuSmoothEase),
                        label = "toolCellY_$id",
                    )

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(displayX.roundToInt(), displayY.roundToInt()) }
                            .size(cellWidth, cellHeight)
                            .graphicsLayer { alpha = if (isDragging) 0f else 1f }
                            .pointerInput(id, ids.size) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        val idx = ids.indexOf(id).coerceAtLeast(0)
                                        val c = idx % 5
                                        val r = idx / 5
                                        onStartDrag(
                                            id,
                                            Offset(
                                                cardBounds.left + cellWidthPx * (c + 0.5f),
                                                cardBounds.top + rowPitchPx * (r + 0.5f),
                                            ),
                                        )
                                    },
                                    onDrag = { change, delta ->
                                        change.consume()
                                        onDragDelta(delta)
                                    },
                                    onDragEnd = { onDragEnd() },
                                    onDragCancel = { onDragEnd() },
                                )
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        ToolGridItem(tool = tool, isActive = false, isEnabled = true)
                        AnimatedVisibility(
                            visible = showBadges,
                            enter = scaleIn(tween(160, easing = ToolMenuSmoothEase)) +
                                    fadeIn(tween(140)),
                            exit = scaleOut(tween(140, easing = ToolMenuSmoothEase)) +
                                    fadeOut(tween(110)),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 2.dp, end = 6.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                                    .clickable { onBadgeClick(id) },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = if (badgeIsAdd) MiuixIcons.Add else MiuixIcons.Close,
                                    contentDescription = if (badgeIsAdd) "启用" else "移除",
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun ToolMenuCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    if (LocalUiMode.current == UiMode.Material) {
        M3Surface(
            modifier = modifier,
            shape = RoundedCornerShape(25.dp),
            color = M3MaterialTheme.colorScheme.surface,
        ) {
            content()
        }
    } else {
        Card(modifier = modifier) {
            content()
        }
    }
}

@Composable
private fun ToolMenuSectionTitle(text: String) {
    if (LocalUiMode.current == UiMode.Material) {
        M3Text(
            text = text,
            color = M3MaterialTheme.colorScheme.primary,
            style = M3MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
        )
    } else {
        SmallTitle(text)
    }
}

@Composable
private fun ToolMenuHintText() {
    if (LocalUiMode.current == UiMode.Material) {
        M3Text(
            text = "按住并拖动进行排序",
            style = M3MaterialTheme.typography.bodyLarge,
            color = M3MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        Text(
            text = "按住并拖动进行排序",
            style = MiuixTheme.textStyles.body1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ToolMenuEmptyText() {
    if (LocalUiMode.current == UiMode.Material) {
        M3Text(
            text = "暂无未启用或隐藏的功能",
            style = M3MaterialTheme.typography.bodyMedium,
            color = M3MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    } else {
        Text(
            text = "暂无未启用或隐藏的功能",
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            textAlign = TextAlign.Center,
        )
    }
}
