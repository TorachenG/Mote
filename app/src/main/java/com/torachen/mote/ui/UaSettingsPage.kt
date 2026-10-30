package com.torachen.mote.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.luminance
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.animation.core.CubicBezierEasing
import top.yukonga.miuix.kmp.icon.extended.Add
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.DropdownMenu as M3DropdownMenu
import androidx.compose.material3.DropdownMenuItem as M3DropdownMenuItem
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.RadioButton as M3RadioButton
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Send
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.window.WindowDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical


private const val TOP_BAR_BLEND_ALPHA = 0.8f
private val TOP_BAR_FALLBACK_HEIGHT = 128.dp
private val TOP_BAR_BLUR_RADIUS = 24.dp
private val SmoothEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
private const val KEY_UA_PROGRESSIVE_TITLE_BLUR = "progressive_title_blur"

@Composable
fun UaSettingsPage(onBack: () -> Unit) {
    if (LocalUiMode.current == UiMode.Miuix) UaSettingsPageMiuix(onBack) else UaSettingsPageMaterial(onBack)
}

@Composable
private fun UaSettingsPageMiuix(onBack: () -> Unit) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }
    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else null

    var progressiveTitleBlur by remember { mutableStateOf(prefs.getBoolean(KEY_UA_PROGRESSIVE_TITLE_BLUR, true)) }
    var materialStyle by remember { mutableIntStateOf(prefs.getInt("ui_material_style", 2)) }
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_UA_PROGRESSIVE_TITLE_BLUR -> progressiveTitleBlur = prefs.getBoolean(key, true)
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
        animationSpec = tween(400, easing = SmoothEase),
        label = "uaBackCardAlpha",
    )
    val backCardMaterialModifier = if (progressiveTitleBlur) {
        buildBackCardMaterialModifier(materialStyle, blurSupported, backdrop, themeIsDark)
    } else Modifier
    val titleBarBlendColorEntries = remember(surfaceColor) {
        listOf(BlendColorEntry(color = surfaceColor.copy(alpha = TOP_BAR_BLEND_ALPHA)))
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val measuredTop = with(density) { topBarBottomPx.toDp() }
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop
    else statusBarHeight + TOP_BAR_FALLBACK_HEIGHT

    var preset by remember { mutableStateOf(UaStore.presetIndex(context)) }
    var customList by remember { mutableStateOf(UaStore.customList(context)) }
    var customSelected by remember { mutableStateOf(UaStore.customSelected(context)) }
    var desktopIndex by remember { mutableStateOf(UaStore.desktopIndex(context)) }
    var selecting by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var addInput by remember { mutableStateOf("") }

    fun exitSelect() {
        selecting = false
        selected = emptySet()
    }

    val barTitle = if (selecting) {
        if (selected.isEmpty()) "请选择项目" else "已选择 ${selected.size} 项"
    } else "浏览器标识"

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
                        .overScrollVertical(),
                    contentPadding = PaddingValues(
                        top = contentTopPadding,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 88.dp,
                    ),
                    overscrollEffect = null,
                ) {
                    item(key = "preset_header") { SmallTitle("预设") }
                    item(key = "preset_card") {
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                            UaStore.presets.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 56.dp)
                                        .clickable {
                                            preset = index
                                            customSelected = null
                                            UaStore.setPresetIndex(context, index)
                                            UaStore.setCustomSelected(context, null)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        item.name,
                                        style = MiuixTheme.textStyles.headline1,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                        color = MiuixTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                    )
                                    RadioButton(selected = customSelected == null && preset == index, onClick = null)
                                }
                            }
                        }
                    }

                    item(key = "custom_header") { SmallTitle("自定义") }
                    item(key = "custom_card") {
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                            if (customList.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = MiuixIcons.Send,
                                            contentDescription = null,
                                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                            modifier = Modifier.size(48.dp),
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            "暂无自定义浏览器标识",
                                            fontSize = 14.sp,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        )
                                    }
                                }
                            } else {
                                customList.forEach { ua ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 56.dp)
                                            .combinedClickable(
                                                onClick = {
                                                    if (selecting) {
                                                        selected = if (ua in selected) selected - ua else selected + ua
                                                    } else {
                                                        customSelected = ua
                                                        UaStore.setCustomSelected(context, ua)
                                                    }
                                                },
                                                onLongClick = {
                                                    selecting = true
                                                    selected = selected + ua
                                                },
                                            )
                                            .padding(horizontal = 16.dp, vertical = 13.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            ua,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 15.sp,
                                            color = MiuixTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f),
                                        )
                                        if (selecting) {
                                            Spacer(Modifier.width(12.dp))
                                            Checkbox(
                                                state = if (ua in selected) ToggleableState.On else ToggleableState.Off,
                                                onClick = null,
                                            )
                                        } else {
                                            RadioButton(selected = customSelected == ua, onClick = null)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item(key = "other_header") { SmallTitle("其它") }
                    item(key = "other_card") {
                        Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                            WindowDropdownPreference(
                                title = "电脑模式下的浏览器标识",
                                entries = listOf(
                                    DropdownEntry(
                                        items = UaStore.desktopOptions.mapIndexed { index, label ->
                                            DropdownItem(
                                                text = label,
                                                selected = desktopIndex == index,
                                                onClick = {
                                                    desktopIndex = index
                                                    UaStore.setDesktopIndex(context, index)
                                                },
                                            )
                                        },
                                    ),
                                ),
                                collapseOnSelection = true,
                            )
                        }
                    }
                }

            }

                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        // 抬到列表之上，阴影才能落在卡片上而不是被卡片盖住
                        .zIndex(10f)
                        .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 24.dp)
                        .height(56.dp)
                        // 只要环境阴影：spot 透明，避免硬边投影；不额外 clip，避免裁掉阴影
                        .graphicsLayer {
                            shape = RoundedCornerShape(28.dp)
                            shadowElevation = 24.dp.toPx()
                            ambientShadowColor = Color.Black.copy(alpha = 0.35f)
                            spotShadowColor = Color.Transparent
                        }
                        .then(
                            buildBackCardMaterialModifier(
                                materialStyle = materialStyle,
                                blurSupported = blurSupported,
                                backdrop = backdrop,
                                themeIsDark = themeIsDark,
                                shape = RoundedCornerShape(28.dp),
                            )
                        )
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (selecting) {
                        IconButton(onClick = {
                            customList = customList.filterNot { it in selected }
                            UaStore.setCustomList(context, customList)
                            if (customSelected in selected) {
                                customSelected = null
                                UaStore.setCustomSelected(context, null)
                            }
                            exitSelect()
                        }) {
                            Icon(MiuixIcons.Delete, contentDescription = "删除", tint = Color(0xFFFF3B30))
                        }
                    } else {
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(MiuixIcons.Add, contentDescription = "添加", tint = MiuixTheme.colorScheme.onSurface)
                        }
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
                                        curve = 2.2f,
                                    ),
                                    colors = BlurDefaults.blurColors(
                                        blendColors = listOf(BlendColorEntry(color = surfaceColor.copy(alpha = 0.3f))),
                                    ),
                                ).background(
                                    Brush.verticalGradient(
                                        listOf(surfaceColor, Color.Transparent),
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
                TopAppBar(
                    title = barTitle,
                    largeTitle = barTitle,
                    scrollBehavior = scrollBehavior,
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        if (selecting) {
                            IconButton(onClick = { exitSelect() }) {
                                Icon(MiuixIcons.Close, contentDescription = "退出多选", tint = MiuixTheme.colorScheme.onSurface)
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
                        if (selecting) {
                            val all = customList.toSet()
                            IconButton(onClick = { selected = if (selected.size == all.size) emptySet() else all }) {
                                Icon(
                                    imageVector = MiuixIcons.SelectAll,
                                    contentDescription = "全选",
                                    tint = if (selected.size == all.size) MiuixTheme.colorScheme.primary
                                    else MiuixTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    },
                )
            }
        }
    }

    WindowDialog(
        show = showAddDialog,
        title = "添加浏览器标识",
        onDismissRequest = { showAddDialog = false },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextField(
                value = addInput,
                onValueChange = { addInput = it },
                label = "User-Agent",
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { showAddDialog = false },
                    modifier = Modifier.weight(1f),
                ) { Text("取消") }
                Button(
                    onClick = {
                        val ua = addInput.trim()
                        if (ua.isNotBlank() && ua !in customList) {
                            customList = customList + ua
                            UaStore.setCustomList(context, customList)
                        }
                        addInput = ""
                        showAddDialog = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("添加") }
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun UaSettingsPageMaterial(onBack: () -> Unit) {
    val context = LocalContext.current
    var preset by remember { mutableStateOf(UaStore.presetIndex(context)) }
    var customList by remember { mutableStateOf(UaStore.customList(context)) }
    var customSelected by remember { mutableStateOf(UaStore.customSelected(context)) }
    var desktopIndex by remember { mutableStateOf(UaStore.desktopIndex(context)) }
    var input by remember { mutableStateOf("") }
    var desktopMenu by remember { mutableStateOf(false) }
    val bg = lerp(
        M3MaterialTheme.colorScheme.surfaceContainerLow,
        M3MaterialTheme.colorScheme.secondaryContainer,
        0.10f,
    )

    M3Scaffold(
        containerColor = bg,
        topBar = {
            M3TopAppBar(
                title = { M3Text("浏览器标识") },
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
            item(key = "preset_header") {
                Text("预设", modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 6.dp), fontSize = 13.sp, color = M3MaterialTheme.colorScheme.primary)
            }
            item(key = "preset_card") {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    androidx.compose.material3.Surface(shape = RoundedCornerShape(16.dp), color = bg) {
                        Column {
                            UaStore.presets.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 56.dp)
                                        .clickable {
                                            preset = index
                                            customSelected = null
                                            UaStore.setPresetIndex(context, index)
                                            UaStore.setCustomSelected(context, null)
                                        }
                                        .padding(horizontal = 16.dp, vertical = 13.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    M3Text(item.name, modifier = Modifier.weight(1f))
                                    M3RadioButton(selected = customSelected == null && preset == index, onClick = null)
                                }
                            }
                        }
                    }
                }
            }
            item(key = "custom_header") {
                Text("自定义", modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 6.dp), fontSize = 13.sp, color = M3MaterialTheme.colorScheme.primary)
            }
            item(key = "custom_card") {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                    androidx.compose.material3.Surface(shape = RoundedCornerShape(16.dp), color = bg) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            if (customList.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    M3Text(
                                        "暂无自定义浏览器标识",
                                        style = M3MaterialTheme.typography.bodyMedium,
                                        color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            } else {
                                customList.forEach { ua ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 56.dp)
                                            .clickable {
                                                customSelected = ua
                                                UaStore.setCustomSelected(context, ua)
                                            }
                                            .padding(horizontal = 16.dp, vertical = 13.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        M3Text(ua, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                        M3RadioButton(selected = customSelected == ua, onClick = null)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item(key = "other_header") {
                Text("其它", modifier = Modifier.padding(start = 26.dp, top = 8.dp, bottom = 6.dp), fontSize = 13.sp, color = M3MaterialTheme.colorScheme.primary)
            }
            item(key = "other_card") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                        .padding(bottom = M3_SEGMENT_GROUP_GAP),
                ) {
                    MoteSegmentedColumn(
                        color = M3MaterialTheme.colorScheme.surface,
                        content = listOf(
                            {
                                M3SegmentDropdownItem(
                                    title = "电脑模式下的浏览器标识",
                                    summary = null,
                                    options = UaStore.desktopOptions,
                                    selectedIndex = desktopIndex,
                                    onSelected = { index ->
                                        desktopIndex = index
                                        UaStore.setDesktopIndex(context, index)
                                    },
                                )
                            },
                        ),
                    )
                }
            }
        }
    }
}
