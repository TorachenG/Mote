@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.ProgressiveBlur
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.progressiveTextureBlur
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur

import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Album
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.CloudFill
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Photos
import top.yukonga.miuix.kmp.icon.extended.Report
import top.yukonga.miuix.kmp.icon.extended.Search

import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

import androidx.compose.material3.DropdownMenu as M3DropdownMenu
import androidx.compose.material3.DropdownMenuItem as M3DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api as M3ExperimentalApi
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.OutlinedTextField as M3OutlinedTextField
import androidx.compose.material3.Scaffold as M3Scaffold
import androidx.compose.material3.Surface as M3Surface
import androidx.compose.material3.Switch as M3Switch
import androidx.compose.material3.Text as M3Text
import androidx.compose.material3.TopAppBar as M3TopAppBar
import androidx.compose.material3.TopAppBarDefaults as M3TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Cloud

import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ============================================================
// 数据模型
// ============================================================

data class SettingsItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val iconInnerSize: Dp = DEFAULT_ICON_INNER_SIZE,
    val iconBgColor: Color? = null,
)

data class SettingsSection(
    val title: String,
    val items: List<SettingsItem>
)

// ============================================================
// 常量
// ============================================================

private val ICON_CONTAINER_SIZE = 27.5.dp
private val ICON_CORNER_RADIUS = 8.25.dp
private val ICON_TO_TITLE_GAP = 6.dp
private val DEFAULT_ICON_INNER_SIZE = 23.dp
private val SMALL_ICON_INNER_SIZE = 20.5.dp
private val MEDIUM_ICON_INNER_SIZE = 21.75.dp

private const val TOP_BAR_BLUR_RADIUS = 25f
private const val TOP_BAR_BLEND_ALPHA = 0.8f

private val TOP_BAR_FALLBACK_HEIGHT = 128.dp

private val SEARCH_SHIFT_DISTANCE = 100.dp
private const val SEARCH_FOCUS_ANIM_MS = 520
private const val SEARCH_RESULTS_DELAY_MS = 150L

private const val FLASH_FADE_IN_MS = 220
private const val FLASH_HOLD_MS = 120
private const val FLASH_FADE_OUT_MS = 320
private const val FLASH_GAP_MS = 140
private const val FLASH_ALPHA = 0.08f
private val FlashEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

private val SCROLL_TARGET_TOP_GAP = 24.dp

private val SearchCardEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
private const val SEARCH_CARD_ENTER_FADE_MS = 220
private const val SEARCH_CARD_ENTER_SLIDE_MS = 380
private const val SEARCH_CARD_EXIT_FADE_MS = 180
private const val SEARCH_CARD_EXIT_SLIDE_MS = 320
private const val SEARCH_CARD_MOVE_MS = 360

private const val EMPTY_HINT_DELAY_MS = 320L

// ★ 统一常量：整个 ui 包共享此配置
internal const val KEY_PROGRESSIVE_TITLE_BLUR = "progressive_title_blur"

// SearchBar 展开后已经占用的高度（估算）：
// 状态栏 + 顶部 margin(8) + 输入框(56) + 输入框与内容的间距(12)
// 用于给搜索结果区算出一个确定的可用高度上限，
// 避免用 fillMaxHeight() 在 LazyColumn item 的无限高约束下引发测量异常。
private val SEARCH_BAR_OCCUPIED_EXTRA = 76.dp
private val SEARCH_RESULT_MIN_HEIGHT = 240.dp

// ============================================================
// 图标
// ============================================================

@Composable
private fun SettingsIcon(
    icon: ImageVector,
    innerSize: Dp,
    bgColor: Color? = null,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(ICON_CONTAINER_SIZE)
            .clip(RoundedCornerShape(ICON_CORNER_RADIUS))
            .background(bgColor ?: MiuixTheme.colorScheme.primary),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(innerSize),
        )
    }
}

// ============================================================
// 设置分区
// ============================================================

private val settingsSections = listOf(
    SettingsSection(
        title = "常规",
        items = listOf(
            SettingsItem("主页设置", MiuixIcons.Home, "settings_home", SMALL_ICON_INNER_SIZE),
            SettingsItem("搜索设置", MiuixIcons.Search, "settings_search", SMALL_ICON_INNER_SIZE),
            SettingsItem("网页浏览设置", MiuixIcons.Months, "settings_web"),
        )
    ),
    SettingsSection(
        title = "定制",
        items = listOf(
            SettingsItem("应用图标", MiuixIcons.Photos, "settings_icon", iconBgColor = Color(0xFF1DCD3A)),
            SettingsItem("主题与界面", MiuixIcons.Album, "settings_layout", iconBgColor = Color(0xFF7767F9)),
        )
    ),
    SettingsSection(
        title = "隐私",
        items = listOf(
            SettingsItem("网页安全中心", MiuixIcons.Link, "settings_web_security"),
            SettingsItem("通知与权限", MiuixIcons.Report, "settings_app_permissions", MEDIUM_ICON_INNER_SIZE, Color(0xFFA4A4A4)),
            SettingsItem("个人信息", MiuixIcons.Lock, "settings_personal_info", MEDIUM_ICON_INNER_SIZE, Color(0xFF1DCD3A)),
        )
    ),
    SettingsSection(
        title = "其它",
        items = listOf(
            SettingsItem(
                "空间管理",
                MiuixIcons.Delete,
                "settings_space_management",
                iconBgColor = Color(0xFFFA382E),
            ),
            SettingsItem("法律信息", MiuixIcons.Contacts, "settings_legal", MEDIUM_ICON_INNER_SIZE),
            SettingsItem("关于", MiuixIcons.Info, "settings_about", iconBgColor = Color(0xFFA4A4A4)),
        )
    ),
)

// ============================================================
// ★ 中文拼音映射表
// ============================================================

private val PINYIN_TABLE: Map<Char, String> = run {
    val data = "主zhu页ye设she置zhi搜sou索suo网wang浏liu览lan应ying用yong图tu标biao题ti界jie面mian安an全quan中zhong心xin通tong知zhi权quan限xian法fa律lv信xin息xi关guan于yu打da开kai新xin签qian自zi定ding义yi上shang次ci退tui出chu时shi的de询xun问wen恢hui复fu默mo认ren引yin擎qing切qie换huan栏lan快kuai捷jie输shu入ru扫sao一yi模mo块kuai历li史shi文wen字zi大da小xiao强qiang制zhi启qi缩suo放fang比bi例li夜ye间jian式shi电dian脑nao无wu屏ping视shi频pin播bo器qi顶ding部bu下xia拉la刷shua左zuo右you滑hua动dong后hou前qian进jin工gong具ju最zui侧ce建jian议yi痕hen记ji住zhu表biao单dan数shu据ju接jie受shou窗chuang口kou清qing空kong拦lan截jie广guang告gao跟gen踪zong防fang护hu调diao整zheng提ti示shi读du取qu手shou机ji存cun储chu相xiang录lu位wei或huo写xie剪jian贴tie板ban户hu协xie议yi隐yin私si政zheng策ce源yuan代dai码ma许xu可ke第di三san方fang体ti验yan改gai进jin计ji划hua检jian查cha更geng新xin来lai深shen色se系xi统tong壁bi纸zhi彩cai风feng格ge准zhun控kong件jian背bei景jing重chong选xuan择ze片pian横heng幅fu内nei容rong常chang规gui找zhao到dao白bai名ming单dan基ji本ben均jun衡heng推tui荐jian严yan格ge材cai质zhi颜yan暗an角jiao滤lv镜jing密mi管guan理li器qi导dao入ru出chu联lian反fan馈kui用yong户hu开kai放fang网wang址zhi自zi动dong"
    val map = HashMap<Char, String>()
    var i = 0
    while (i < data.length) {
        val ch = data[i]
        i++
        val start = i
        while (i < data.length && data[i] in 'a'..'z') i++
        if (start < i) {
            map[ch] = data.substring(start, i)
        }
    }
    map
}

private val pinyinFormsCache = ConcurrentHashMap<String, Pair<String, String>>()

private fun getPinyinForms(text: String): Pair<String, String> {
    return pinyinFormsCache.getOrPut(text) {
        val full = StringBuilder()
        val initials = StringBuilder()
        text.forEach { ch ->
            val py = PINYIN_TABLE[ch]
            if (py != null) {
                full.append(py)
                initials.append(py[0])
            } else if (ch.isLetterOrDigit()) {
                val lc = ch.lowercaseChar()
                full.append(lc)
                initials.append(lc)
            }
        }
        full.toString() to initials.toString()
    }
}

// ============================================================
// 设置项搜索注册表
// ============================================================

internal data class SearchableEntry(
    val title: String,
    val pageRoute: String,
    val pageTitle: String,
    val itemKey: String,
    val tabIndex: Int? = null,
) {
    val subtitle: String
        get() = if (pageTitle.isBlank()) title else "$pageTitle > $title"
}

internal object SettingsSearchRegistry {

    private val preloadedEntries = ConcurrentHashMap<String, SearchableEntry>()
    private val runtimeEntries = ConcurrentHashMap<String, SearchableEntry>()
    private val itemRootY = ConcurrentHashMap<String, Float>()

    var version by mutableIntStateOf(0)
        private set

    var pendingScrollItemKey: String? by mutableStateOf(null)
    var pendingFlashItemKey: String? by mutableStateOf(null)

    fun preload(entries: List<SearchableEntry>) {
        entries.forEach { preloadedEntries[it.itemKey] = it }
        version++
    }

    fun clearPreload() {
        if (preloadedEntries.isNotEmpty()) {
            preloadedEntries.clear()
            version++
        }
    }

    fun register(entry: SearchableEntry) {
        runtimeEntries[entry.itemKey] = entry
        version++
    }

    fun unregister(itemKey: String) {
        if (runtimeEntries.remove(itemKey) != null) version++
        itemRootY.remove(itemKey)
    }

    private fun mergedAll(): Collection<SearchableEntry> {
        val merged = HashMap<String, SearchableEntry>(preloadedEntries.size + runtimeEntries.size)
        preloadedEntries.forEach { (k, v) -> merged[k] = v }
        runtimeEntries.forEach { (k, v) -> merged[k] = v }
        return merged.values
    }

    fun query(keyword: String): List<SearchableEntry> {
        if (keyword.isBlank()) return emptyList()
        val lower = keyword.lowercase().trim()
        return mergedAll()
            .filter { entry ->
                if (entry.title.lowercase().contains(lower)) return@filter true
                val (full, initials) = getPinyinForms(entry.title)
                full.contains(lower) || initials.contains(lower)
            }
            .sortedBy { it.title }
    }

    fun all(): List<SearchableEntry> = mergedAll().toList()

    fun setItemRootY(itemKey: String, y: Float) {
        itemRootY[itemKey] = y
    }

    fun getItemRootY(itemKey: String): Float? = itemRootY[itemKey]

    fun beginJump(targetItemKey: String) {
        pendingScrollItemKey = targetItemKey
        pendingFlashItemKey = null
    }
}

// ============================================================
// SearchableItem 包装器
// ============================================================

@Composable
internal fun SearchableItem(
    title: String,
    pageRoute: String,
    pageTitle: String,
    itemKey: String,
    visible: Boolean = true,
    tabIndex: Int? = null,
    content: @Composable () -> Unit,
) {
    if (!visible) return

    DisposableEffect(itemKey) {
        SettingsSearchRegistry.register(
            SearchableEntry(
                title = title,
                pageRoute = pageRoute,
                pageTitle = pageTitle,
                itemKey = itemKey,
                tabIndex = tabIndex,
            )
        )
        onDispose {
            SettingsSearchRegistry.unregister(itemKey)
        }
    }

    val flashProgress = remember { Animatable(0f) }
    val isFlashTarget = SettingsSearchRegistry.pendingFlashItemKey == itemKey

    LaunchedEffect(isFlashTarget) {
        if (!isFlashTarget) return@LaunchedEffect
        repeat(2) { idx ->
            if (idx > 0) {
                delay(FLASH_GAP_MS.toLong())
            }
            flashProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(FLASH_FADE_IN_MS, easing = FlashEase),
            )
            delay(FLASH_HOLD_MS.toLong())
            flashProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(FLASH_FADE_OUT_MS, easing = FlashEase),
            )
        }
        if (SettingsSearchRegistry.pendingFlashItemKey == itemKey) {
            SettingsSearchRegistry.pendingFlashItemKey = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                SettingsSearchRegistry.setItemRootY(
                    itemKey,
                    coords.positionInRoot().y,
                )
            }
    ) {
        content()
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    MiuixTheme.colorScheme.onSurface.copy(
                        alpha = FLASH_ALPHA * flashProgress.value
                    )
                )
        )
    }
}

// ============================================================
// SettingsPageAutoScroll
// ============================================================

@Composable
internal fun SettingsPageAutoScroll(
    lazyListState: LazyListState,
    pageRoute: String,
    contentTopPadding: Dp,
    tabIndex: Int? = null,
) {
    val density = LocalDensity.current
    val targetKey = SettingsSearchRegistry.pendingScrollItemKey

    LaunchedEffect(targetKey, pageRoute, tabIndex) {
        if (targetKey == null) return@LaunchedEffect

        if (tabIndex != null) {
            val targetEntry = SettingsSearchRegistry
                .all()
                .firstOrNull { it.itemKey == targetKey }
            val targetTab = targetEntry?.tabIndex
            if (targetTab != null && targetTab != tabIndex) {
                return@LaunchedEffect
            }
        }

        delay(180L)

        var itemY: Float? = null
        repeat(12) { attempt ->
            itemY = SettingsSearchRegistry.getItemRootY(targetKey)
            if (itemY != null) return@repeat
            delay(60L)
        }

        val resolvedY = itemY
        if (resolvedY == null) {
            SettingsSearchRegistry.pendingScrollItemKey = null
            return@LaunchedEffect
        }

        val targetScreenY = with(density) { contentTopPadding.toPx() } +
                with(density) { SCROLL_TARGET_TOP_GAP.toPx() }

        val scrollAmount = resolvedY - targetScreenY

        if (scrollAmount > 1f) {
            try {
                lazyListState.animateScrollBy(scrollAmount)
            } catch (_: Exception) {}
        }

        SettingsSearchRegistry.pendingScrollItemKey = null
        SettingsSearchRegistry.pendingFlashItemKey = targetKey

        delay(3000L)
        if (SettingsSearchRegistry.pendingFlashItemKey == targetKey) {
            SettingsSearchRegistry.pendingFlashItemKey = null
        }
    }
}

// ============================================================
// ★ 顺序变动时的平滑移动动画
// ============================================================

@Composable
private fun Modifier.animatedPlacement(): Modifier = composed {
    val scope = rememberCoroutineScope()
    var lastPlacedY by remember { mutableStateOf(Float.NaN) }
    val offsetY = remember { Animatable(0f) }

    this
        .onPlaced { coordinates ->
            val newY = coordinates.positionInParent().y
            val oldY = lastPlacedY
            if (oldY.isNaN()) {
                lastPlacedY = newY
            } else if (oldY != newY) {
                lastPlacedY = newY
                val delta = oldY - newY
                scope.launch {
                    offsetY.snapTo(offsetY.value + delta)
                    offsetY.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = SEARCH_CARD_MOVE_MS,
                            easing = SearchCardEase,
                        ),
                    )
                }
            }
        }
        .graphicsLayer {
            translationY = offsetY.value
        }
}

// ============================================================
// 搜索结果图标反查
// ============================================================

internal fun findSettingsItemByRoute(route: String): SettingsItem? {
    settingsSections.forEach { section ->
        section.items.firstOrNull { it.route == route }?.let { return it }
    }
    return null
}

internal fun lookupPageIcon(route: String): ImageVector? =
    findSettingsItemByRoute(route)?.icon

internal fun lookupPageIconColor(route: String): Color? =
    findSettingsItemByRoute(route)?.iconBgColor

// ============================================================
// ★ Material 图标反查（圆润风格）
// ============================================================

private fun materialIconFor(title: String): ImageVector = when (title) {
    "同步" -> Icons.Rounded.Cloud
    "主页设置" -> Icons.Rounded.Home
    "搜索设置" -> Icons.Rounded.Search
    "网页浏览设置" -> Icons.Rounded.Language
    "应用图标" -> Icons.Rounded.Apps
    "主题与界面" -> Icons.Rounded.Palette
    "网页安全中心" -> Icons.Rounded.Security
    "通知与权限" -> Icons.Rounded.Notifications
    "法律信息" -> Icons.Rounded.Gavel
    "个人信息" -> Icons.Rounded.Person
    "关于" -> Icons.Rounded.Info
    else -> Icons.Rounded.Info
}

// ============================================================
// ★ Material 设置图标（容器形状对齐 MIUIX）
// ============================================================

@Composable
private fun M3SettingsIcon(
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
) {
    Box(
        modifier = Modifier
            .size(ICON_CONTAINER_SIZE)
            .clip(RoundedCornerShape(ICON_CORNER_RADIUS))
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        M3Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(DEFAULT_ICON_INNER_SIZE * 0.85f),
        )
    }
}

// ============================================================
// 设置主页
// ============================================================

@Composable
fun SettingsMainPage(
    onBack: () -> Unit,
    onItemClick: (String) -> Unit,
    isTransitioning: Boolean = false,
) {
    when (LocalUiMode.current) {
        UiMode.Miuix -> SettingsMainPageMiuix(onBack, onItemClick, isTransitioning)
        UiMode.Material -> SettingsMainPageMaterial(onBack, onItemClick, isTransitioning)
    }
}

// ============================================================
// 设置主页 —— MIUIX 版
// ============================================================

@Composable
private fun SettingsMainPageMiuix(
    onBack: () -> Unit,
    onItemClick: (String) -> Unit,
    isTransitioning: Boolean = false,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()
    // ★ 键盘焦点管理器 —— 用于输入法搜索按钮收起键盘
    val focusManager = LocalFocusManager.current

    // ★ 渐进模糊标题栏 + 材质风格（与「主题与界面」共享 prefs，实时同步）
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    var progressiveTitleBlur by remember {
        mutableStateOf(prefs.getBoolean(KEY_PROGRESSIVE_TITLE_BLUR, true))
    }
    var materialStyle by remember {
        mutableIntStateOf(prefs.getInt("ui_material_style", 2))
    }
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_PROGRESSIVE_TITLE_BLUR ->
                    progressiveTitleBlur = prefs.getBoolean(key, true)
                "ui_material_style" ->
                    materialStyle = prefs.getInt(key, 2)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else {
        null
    }

    val listState = rememberLazyListState()
    val isAtTop by remember {
        derivedStateOf {
            // 只有在顶层且没有滑动时才隐藏卡片。
            // 加入 !isScrollInProgress 后，只要手指开始拖动（哪怕还在消耗顶部 padding），卡片就会立即出现。
            listState.firstVisibleItemIndex == 0 &&
                    listState.firstVisibleItemScrollOffset == 0 &&
                    !listState.isScrollInProgress
        }
    }

    // ★ 将动画和材质层提升到 TopAppBar 外部，确保 isAtTop 变化时能稳定触发重组
    val themeIsDark = surfaceColor.luminance() < 0.5f
    val backCardAlpha by animateFloatAsState(
        targetValue = if (isAtTop || !progressiveTitleBlur) 0f else 1f,
        animationSpec = tween(
            // ★ 动画时长从 220ms 延长到 400ms，过渡更自然
            durationMillis = 400,
            easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
        ),
        label = "settingsBackCardAlpha",
    )

    val backCardMaterialModifier = if (progressiveTitleBlur) {
        buildBackCardMaterialModifier(materialStyle, blurSupported, backdrop, themeIsDark)
    } else {
        Modifier
    }

    var searchValue by remember { mutableStateOf("") }
    var searchExpanded by remember { mutableStateOf(false) }

    val searchFocusAnim = remember { Animatable(0f) }
    LaunchedEffect(searchExpanded) {
        searchFocusAnim.animateTo(
            targetValue = if (searchExpanded) 1f else 0f,
            animationSpec = tween(
                durationMillis = SEARCH_FOCUS_ANIM_MS,
                easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f),
            ),
        )
    }

    val shouldShowSettings by remember {
        derivedStateOf { searchFocusAnim.value < 0.99f }
    }

    var searchResultsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(searchExpanded) {
        if (searchExpanded) {
            delay(SEARCH_RESULTS_DELAY_MS)
            searchResultsVisible = true
        } else {
            delay(SEARCH_RESULTS_DELAY_MS)
            searchResultsVisible = false
        }
    }

    DisposableEffect(Unit) {
        SettingsSearchRegistry.preload(SETTINGS_SEARCH_CATALOG)
        onDispose {
            SettingsSearchRegistry.clearPreload()
        }
    }

    val registryVersion = SettingsSearchRegistry.version
    val searchResults = remember(searchValue, registryVersion) {
        SettingsSearchRegistry.query(searchValue)
    }

    val groupedResults = remember(searchResults) {
        searchResults.groupBy { it.pageRoute }
    }

    val allGroups = remember {
        SETTINGS_SEARCH_CATALOG.groupBy { it.pageRoute }
    }

    var showEmptyHint by remember { mutableStateOf(false) }
    LaunchedEffect(searchResults, searchValue) {
        if (searchValue.isNotBlank() && searchResults.isEmpty()) {
            delay(EMPTY_HINT_DELAY_MS)
            showEmptyHint = true
        } else {
            showEmptyHint = false
        }
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val measuredTop = with(density) { topBarBottomPx.toDp() }
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop else statusBarHeight + TOP_BAR_FALLBACK_HEIGHT

    // ★ 搜索结果列表高度的确定上限：
    //   屏幕总高度 - 状态栏 - SearchBar 已占用高度 - 底部冗余，
    //   避免使用 fillMaxHeight()（LazyColumn item 的高度约束是无限的，
    //   fillMaxHeight 会和内部 verticalScroll 冲突引发测量异常）。
    val configuration = LocalConfiguration.current
    val maxSearchResultHeight = remember(configuration.screenHeightDp, statusBarHeight) {
        (configuration.screenHeightDp.dp - statusBarHeight - SEARCH_BAR_OCCUPIED_EXTRA)
            .coerceAtLeast(SEARCH_RESULT_MIN_HEIGHT)
    }

    val shiftPx = with(density) { SEARCH_SHIFT_DISTANCE.toPx() }

    BackHandler(enabled = searchExpanded) {
        searchValue = ""
        searchExpanded = false
    }

    fun jumpToResult(entry: SearchableEntry) {
        SettingsSearchRegistry.beginJump(entry.itemKey)
        if (entry.tabIndex != null) {
            SettingsLayoutNavArgs.initialTab = entry.tabIndex
        }
        searchValue = ""
        searchExpanded = false
        onItemClick(entry.pageRoute)
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (blurSupported && backdrop != null) {
                            Modifier.layerBackdrop(backdrop)
                        } else {
                            Modifier
                        }
                    )
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .scrollEndHaptic()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(top = contentTopPadding + 4.dp),
                    overscrollEffect = null,
                ) {
                    item(key = "searchbar") {
                        SearchBar(
                            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
                            inputField = {
                                InputField(
                                    query = searchValue,
                                    onQueryChange = { searchValue = it },
                                    // ★ 点击输入法搜索按钮收起键盘，不退出搜索状态
                                    onSearch = { focusManager.clearFocus() },
                                    expanded = searchExpanded,
                                    onExpandedChange = { searchExpanded = it },
                                    label = "搜索设置",
                                )
                            },
                            expanded = searchExpanded,
                            onExpandedChange = { searchExpanded = it },
                            outsideEndAction = {
                                Text(
                                    text = "取消",
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clickable(
                                            interactionSource = null,
                                            indication = null,
                                        ) {
                                            searchValue = ""
                                            searchExpanded = false
                                        },
                                    style = TextStyle(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                    ),
                                    color = MiuixTheme.colorScheme.primary,
                                )
                            },
                        ) {
                            androidx.compose.animation.AnimatedVisibility(
                                visible = searchResultsVisible,
                                enter = fadeIn(
                                    tween(
                                        durationMillis = 220,
                                        easing = SearchCardEase,
                                    )
                                ) + slideInVertically(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = SearchCardEase,
                                    ),
                                    initialOffsetY = { -it / 3 },
                                ),
                                exit = fadeOut(
                                    tween(
                                        durationMillis = 220,
                                        easing = SearchCardEase,
                                    )
                                ) + slideOutVertically(
                                    animationSpec = tween(
                                        durationMillis = 300,
                                        easing = SearchCardEase,
                                    ),
                                    targetOffsetY = { -it / 3 },
                                ),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        // ★ 用确定的 heightIn(max = ...) 替代 fillMaxHeight()
                                        .heightIn(max = maxSearchResultHeight)
                                        .background(MiuixTheme.colorScheme.surface)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            onClick = { },
                                        )
                                        .verticalScroll(rememberScrollState())
                                        .padding(top = 14.dp),
                                ) {
                                    val displayGroupOrder = remember(groupedResults, allGroups) {
                                        groupedResults.keys.toList() +
                                                allGroups.keys.filter { it !in groupedResults }
                                    }

                                    displayGroupOrder.forEach { pageRoute ->
                                        key(pageRoute) {
                                            val matchedEntries = groupedResults[pageRoute]
                                            val initialEntries = allGroups[pageRoute] ?: emptyList()
                                            val pageIcon = lookupPageIcon(pageRoute)
                                            val pageIconBg = lookupPageIconColor(pageRoute)
                                                ?: MiuixTheme.colorScheme.primary

                                            val cachedEntries = remember {
                                                mutableStateOf(initialEntries)
                                            }
                                            if (matchedEntries != null) {
                                                cachedEntries.value = matchedEntries
                                            }

                                            val cardVisibleState = remember {
                                                MutableTransitionState(false)
                                            }
                                            LaunchedEffect(matchedEntries) {
                                                cardVisibleState.targetState = matchedEntries != null
                                            }

                                            AnimatedVisibility(
                                                visibleState = cardVisibleState,
                                                enter = fadeIn(
                                                    tween(
                                                        durationMillis = SEARCH_CARD_ENTER_FADE_MS,
                                                        easing = SearchCardEase,
                                                    )
                                                ) + slideInVertically(
                                                    animationSpec = tween(
                                                        durationMillis = SEARCH_CARD_ENTER_SLIDE_MS,
                                                        easing = SearchCardEase,
                                                    ),
                                                    initialOffsetY = { it * 2 / 3 },
                                                ),
                                                exit = fadeOut(
                                                    tween(
                                                        durationMillis = SEARCH_CARD_EXIT_FADE_MS,
                                                        easing = SearchCardEase,
                                                    )
                                                ) + slideOutVertically(
                                                    animationSpec = tween(
                                                        durationMillis = SEARCH_CARD_EXIT_SLIDE_MS,
                                                        easing = SearchCardEase,
                                                    ),
                                                    targetOffsetY = { it * 2 / 3 },
                                                ),
                                                modifier = Modifier.animatedPlacement(),
                                            ) {
                                                Card(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 12.dp)
                                                        .padding(bottom = 12.dp),
                                                ) {
                                                    cachedEntries.value.forEachIndexed { idx, entry ->
                                                        ArrowPreference(
                                                            title = entry.title,
                                                            summary = entry.subtitle,
                                                            startAction = if (idx == 0) {
                                                                {
                                                                    Box(
                                                                        modifier = Modifier.padding(
                                                                            end = ICON_TO_TITLE_GAP
                                                                        )
                                                                    ) {
                                                                        if (pageIcon != null) {
                                                                            SettingsIcon(
                                                                                icon = pageIcon,
                                                                                innerSize = DEFAULT_ICON_INNER_SIZE,
                                                                                bgColor = pageIconBg,
                                                                            )
                                                                        }
                                                                    }
                                                                }
                                                            } else {
                                                                {
                                                                    Spacer(
                                                                        modifier = Modifier.width(
                                                                            ICON_CONTAINER_SIZE + ICON_TO_TITLE_GAP
                                                                        )
                                                                    )
                                                                }
                                                            },
                                                            onClick = { jumpToResult(entry) },
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    AnimatedVisibility(
                                        visible = showEmptyHint,
                                        enter = fadeIn(
                                            tween(
                                                durationMillis = 220,
                                                easing = SearchCardEase,
                                            )
                                        ) + slideInVertically(
                                            animationSpec = tween(
                                                durationMillis = 280,
                                                easing = SearchCardEase,
                                            ),
                                            initialOffsetY = { it / 2 },
                                        ),
                                        exit = fadeOut(
                                            tween(
                                                durationMillis = 180,
                                                easing = SearchCardEase,
                                            )
                                        ) + slideOutVertically(
                                            animationSpec = tween(
                                                durationMillis = 240,
                                                easing = SearchCardEase,
                                            ),
                                            targetOffsetY = { it / 2 },
                                        ),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 100.dp, bottom = 40.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = "没有找到设置项",
                                                fontSize = 14.sp,
                                                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                            )
                                        }
                                    }

                                    // ★ 底部安全区：撑起导航条 + captionBar，
                                    //   让最后一项能滚到导航条上方，
                                    //   同时页面背景延伸到导航条后面
                                    Spacer(
                                        Modifier.height(
                                            4.dp +
                                                    WindowInsets.navigationBars
                                                        .asPaddingValues()
                                                        .calculateBottomPadding() +
                                                    WindowInsets.captionBar
                                                        .asPaddingValues()
                                                        .calculateBottomPadding()
                                        )
                                    )
                                }
                            }
                        }
                    }

                    if (shouldShowSettings) {
                        // 新增：顶部无标题“同步”卡片
                        item(key = "sync_card") {
                            SearchAnimatedItem(
                                searchFocus = searchFocusAnim,
                                shiftPx = shiftPx,
                            ) {
                                Card(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .padding(top = 4.dp, bottom = 12.dp)
                                ) {
                                    ArrowPreference(
                                        title = "同步",
                                        startAction = {
                                            Box(modifier = Modifier.padding(end = ICON_TO_TITLE_GAP)) {
                                                SettingsIcon(
                                                    icon = MiuixIcons.CloudFill,
                                                    innerSize = DEFAULT_ICON_INNER_SIZE,
                                                    bgColor = null
                                                )
                                            }
                                        },
                                        onClick = { onItemClick("settings_sync") }
                                    )
                                }
                            }
                        }

                        settingsSections.forEach { section ->
                            item(key = "title_${section.title}") {
                                SearchAnimatedItem(
                                    searchFocus = searchFocusAnim,
                                    shiftPx = shiftPx,
                                ) {
                                    SmallTitle(section.title)
                                }
                            }
                            item(key = "card_${section.title}") {
                                SearchAnimatedItem(
                                    searchFocus = searchFocusAnim,
                                    shiftPx = shiftPx,
                                ) {
                                    Card(
                                        modifier = Modifier
                                            .padding(horizontal = 12.dp)
                                            .padding(bottom = 12.dp),
                                    ) {
                                        section.items.forEach { item ->
                                            ArrowPreference(
                                                title = item.title,
                                                startAction = {
                                                    Box(modifier = Modifier.padding(end = ICON_TO_TITLE_GAP)) {
                                                        SettingsIcon(
                                                            icon = item.icon,
                                                            innerSize = item.iconInnerSize,
                                                            bgColor = item.iconBgColor,
                                                        )
                                                    }
                                                },
                                                onClick = { onItemClick(item.route) },
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item(key = "bottom_spacer") {
                            SearchAnimatedItem(
                                searchFocus = searchFocusAnim,
                                shiftPx = shiftPx,
                            ) {
                                Spacer(
                                    Modifier.height(
                                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                                WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                                    )
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
                        if (!isTransitioning && blurSupported && backdrop != null && materialStyle != 0) {
                            if (progressiveTitleBlur) {
                                // ★ 渐进模糊：顶部最强、向下渐变到底部锐利，
                                //   与内容无缝衔接。noiseCoefficient 按渐进模糊推荐保持默认 0f。
                                Modifier.progressiveTextureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = TOP_BAR_BLUR_RADIUS,
                                    gradient = ProgressiveBlur.Top,
                                    colors = BlurDefaults.blurColors(
                                        blendColors = listOf(
                                            BlendColorEntry(
                                                color = surfaceColor.copy(alpha = TOP_BAR_BLEND_ALPHA),
                                            ),
                                        ),
                                    ),
                                )
                            } else {
                                // ★ 关闭渐进模糊后改回原来的普通均匀模糊
                                Modifier.textureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = TOP_BAR_BLUR_RADIUS,
                                    noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                    colors = BlurDefaults.blurColors(
                                        blendColors = listOf(
                                            BlendColorEntry(
                                                color = surfaceColor.copy(alpha = TOP_BAR_BLEND_ALPHA),
                                            ),
                                        ),
                                    ),
                                )
                            }
                        } else {
                            Modifier.background(surfaceColor)
                        }
                    )
                    .statusBarsPadding()
                    .onGloballyPositioned { coords ->
                        val bottomPx = coords.boundsInWindow().bottom.toInt()
                        if (bottomPx != topBarBottomPx) {
                            topBarBottomPx = bottomPx
                        }
                    }
            ) {
                TopAppBar(
                    title = "设置",
                    largeTitle = "设置",
                    scrollBehavior = scrollBehavior,
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        SettingsBackButton(
                            onBack = onBack,
                            progressiveTitleBlur = progressiveTitleBlur,
                            backCardAlpha = backCardAlpha,
                            backCardMaterialModifier = backCardMaterialModifier,
                            iconTint = MiuixTheme.colorScheme.onSurface,
                        )
                    }
                )
            }
        }
    }
}

@Composable
internal fun SettingsTopBarAction(
    backCardAlpha: Float,
    backCardMaterialModifier: Modifier,
    onClick: () -> Unit,
    contentDescription: String?,
    icon: ImageVector,
    tint: Color = MiuixTheme.colorScheme.onSurface,
) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { alpha = backCardAlpha }
                .then(backCardMaterialModifier)
        )
        IconButton(onClick = onClick, modifier = Modifier.matchParentSize()) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
internal fun SettingsBackButton(
    onBack: () -> Unit,
    progressiveTitleBlur: Boolean,
    backCardAlpha: Float,
    backCardMaterialModifier: Modifier,
    iconTint: Color,
) {
    if (progressiveTitleBlur) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            // ★ 卡片背景层：仅它做渐变显示/隐藏，图标常显
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = backCardAlpha }
                    .then(backCardMaterialModifier)
            )
            Icon(
                imageVector = MiuixIcons.ChevronBackward,
                contentDescription = "返回",
                tint = iconTint,
                modifier = Modifier.size(22.dp),
            )
        }
    } else {
        // ★ 关闭渐进模糊时，卡片永久隐藏，返回图标恢复为之前的默认样式
        IconButton(onClick = onBack) {
            Icon(
                imageVector = MiuixIcons.Back,
                contentDescription = "返回",
                tint = iconTint,
            )
        }
    }
}

@Composable
internal fun buildBackCardMaterialModifier(
    materialStyle: Int,
    blurSupported: Boolean,
    backdrop: LayerBackdrop?,
    themeIsDark: Boolean,
    shape: Shape = CircleShape, // ★ 新增参数，默认圆形，保证返回按钮不受影响
): Modifier {
    return when (materialStyle) {
        1 -> {
            if (blurSupported && backdrop != null) {
                val tint = if (themeIsDark) {
                    Color.Black.copy(alpha = 0.2f)
                } else {
                    Color.White.copy(alpha = 0.2f)
                }
                Modifier.textureBlur(
                    backdrop = backdrop,
                    shape = shape, // ★ 替换
                    blurRadius = 25f,
                    noiseCoefficient = BlurDefaults.NoiseCoefficient,
                    colors = BlurDefaults.blurColors(
                        blendColors = listOf(
                            BlendColorEntry(color = tint),
                        ),
                    ),
                )
            } else {
                Modifier.background(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                    shape = shape,
                )
            }
        }
        2 -> {
            if (blurSupported && backdrop != null) {
                val highlight = if (themeIsDark) {
                    Highlight.GlassStrokeSmallDark
                } else {
                    Highlight.GlassStrokeSmallLight
                }
                val baseSurface = if (themeIsDark) {
                    Color(0xFF121212)
                } else {
                    Color(0xFFF2F2F2)
                }
                val blendColor = if (themeIsDark) {
                    lerp(baseSurface, Color.Black, 0.25f)
                        .copy(alpha = 0.25f)
                } else {
                    lerp(baseSurface, Color.White, 0.25f)
                        .copy(alpha = 0.25f)
                }
                val blurColors = BlurDefaults.blurColors(
                    blendColors = listOf(
                        BlendColorEntry(
                            color = blendColor,
                            mode = BlurBlendMode.SrcOver,
                        ),
                    ),
                    contrast = 1.25f,
                    saturation = 1.25f,
                )
                Modifier
                    .graphicsLayer {
                        shadowElevation = 24.dp.toPx()
                        ambientShadowColor = Color.Black.copy(alpha = 0.35f)
                        spotShadowColor = Color.Transparent
                    }
                    .textureBlur(
                        backdrop = backdrop,
                        shape = shape, // ★ 替换
                        blurRadius = 25f,
                        noiseCoefficient = BlurDefaults.NoiseCoefficient,
                        colors = blurColors,
                        highlight = highlight,
                    )
            } else {
                Modifier.background(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                    shape = shape,
                )
            }
        }
        else -> Modifier.background(
            color = MiuixTheme.colorScheme.surfaceContainer,
            shape = shape,
        )
    }
}

// ============================================================
// 设置主页 —— Material 3 版
// ============================================================

@OptIn(M3ExperimentalApi::class)
@Composable
private fun SettingsMainPageMaterial(
    onBack: () -> Unit,
    onItemClick: (String) -> Unit,
    isTransitioning: Boolean = false,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()

    var searchValue by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        SettingsSearchRegistry.preload(SETTINGS_SEARCH_CATALOG)
        onDispose {
            SettingsSearchRegistry.clearPreload()
        }
    }

    val registryVersion = SettingsSearchRegistry.version
    val searchResults = remember(searchValue, registryVersion) {
        SettingsSearchRegistry.query(searchValue)
    }
    val groupedResults = remember(searchResults) {
        searchResults.groupBy { it.pageRoute }
    }

    var showEmptyHint by remember { mutableStateOf(false) }
    LaunchedEffect(searchResults, searchValue) {
        if (searchValue.isNotBlank() && searchResults.isEmpty()) {
            delay(EMPTY_HINT_DELAY_MS)
            showEmptyHint = true
        } else {
            showEmptyHint = false
        }
    }

    val pageBg = M3MaterialTheme.colorScheme.surface
    val iconContainerColor = M3MaterialTheme.colorScheme.primary
    val iconContentColor = M3MaterialTheme.colorScheme.onPrimary

    val isSearching = searchValue.isNotBlank()

    BackHandler(enabled = isSearching) {
        searchValue = ""
    }

    fun jumpToResult(entry: SearchableEntry) {
        SettingsSearchRegistry.beginJump(entry.itemKey)
        if (entry.tabIndex != null) {
            SettingsLayoutNavArgs.initialTab = entry.tabIndex
        }
        searchValue = ""
        onItemClick(entry.pageRoute)
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        M3Scaffold(
            containerColor = pageBg,
            contentWindowInsets = WindowInsets.navigationBars.union(WindowInsets.captionBar),
            topBar = {
                M3TopAppBar(
                    title = { M3Text("设置") },
                    navigationIcon = {
                        M3IconButton(onClick = onBack) {
                            M3Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = "返回",
                            )
                        }
                    },
                    colors = M3TopAppBarDefaults.topAppBarColors(
                        containerColor = pageBg,
                    ),
                )
            },
        ) { innerPadding ->
            val displayGroupOrder = remember(groupedResults) {
                groupedResults.keys.toList()
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item(key = "search") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp, bottom = 12.dp),
                    ) {
                        M3OutlinedTextField(
                            value = searchValue,
                            onValueChange = { searchValue = it },
                            placeholder = { M3Text("搜索设置") },
                            singleLine = true,
                            leadingIcon = {
                                M3Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            },
                            trailingIcon = if (searchValue.isNotEmpty()) {
                                {
                                    M3IconButton(onClick = { searchValue = "" }) {
                                        M3Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "清空",
                                            tint = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            } else null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(28.dp),
                        )
                    }
                }

                if (isSearching) {
                    if (displayGroupOrder.isEmpty() && showEmptyHint) {
                        item(key = "empty_hint") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 100.dp, bottom = 40.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                M3Text(
                                    text = "没有找到设置项",
                                    style = M3MaterialTheme.typography.bodyMedium,
                                    color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    displayGroupOrder.forEach { pageRoute ->
                        val matchedEntries = groupedResults[pageRoute] ?: emptyList()
                        if (matchedEntries.isEmpty()) return@forEach

                        val pageTitle = matchedEntries.first().pageTitle.ifBlank { "设置" }

                        item(key = "result_header_$pageRoute") {
                            M3Text(
                                text = pageTitle,
                                color = M3MaterialTheme.colorScheme.primary,
                                style = M3MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 32.dp,
                                        end = 16.dp,
                                        top = 8.dp,
                                        bottom = 8.dp,
                                    ),
                            )
                        }

                        item(key = "result_card_$pageRoute") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                                    .padding(bottom = M3_SEGMENT_GROUP_GAP),
                            ) {
                                val items: List<@Composable () -> Unit> = buildList {
                                    matchedEntries.forEach { entry ->
                                        add {
                                            M3SegmentArrowItem(
                                                title = entry.title,
                                                onClick = { jumpToResult(entry) },
                                            )
                                        }
                                    }
                                }
                                MoteSegmentedColumn(content = items)
                            }
                        }
                    }
                } else {
                    item(key = "sync_card") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                                .padding(top = 4.dp, bottom = M3_SEGMENT_GROUP_GAP)
                        ) {
                            MoteSegmentedColumn(
                                content = listOf(
                                    {
                                        M3SegmentArrowItem(
                                            title = "同步",
                                            leading = {
                                                M3SettingsIcon(
                                                    icon = materialIconFor("同步"),
                                                    containerColor = iconContainerColor,
                                                    contentColor = iconContentColor
                                                )
                                            },
                                            onClick = { onItemClick("settings_sync") }
                                        )
                                    }
                                )
                            )
                        }
                    }

                    settingsSections.forEach { section ->
                        item(key = "header_${section.title}") {
                            M3Text(
                                text = section.title,
                                color = M3MaterialTheme.colorScheme.primary,
                                style = M3MaterialTheme.typography.titleSmall,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 32.dp,
                                        end = 16.dp,
                                        top = 8.dp,
                                        bottom = 8.dp,
                                    ),
                            )
                        }

                        item(key = "card_${section.title}") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = M3_SEGMENT_LIST_HORIZONTAL_PADDING)
                                    .padding(bottom = M3_SEGMENT_GROUP_GAP),
                            ) {
                                val items: List<@Composable () -> Unit> = buildList {
                                    section.items.forEach { item ->
                                        add {
                                            M3SegmentArrowItem(
                                                title = item.title,
                                                leading = {
                                                    M3SettingsIcon(
                                                        icon = materialIconFor(item.title),
                                                        containerColor = iconContainerColor,
                                                        contentColor = iconContentColor,
                                                    )
                                                },
                                                onClick = { onItemClick(item.route) },
                                            )
                                        }
                                    }
                                }
                                MoteSegmentedColumn(content = items)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================
// 搜索激活时下移 + 渐隐的包装器
// ============================================================

@Composable
private fun SearchAnimatedItem(
    searchFocus: Animatable<Float, *>,
    shiftPx: Float,
    content: @Composable () -> Unit,
) {
    val p = searchFocus.value
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, (p * shiftPx).toInt()) }
            .graphicsLayer { alpha = 1f - p },
    ) {
        content()
    }
}

// ============================================================
// 设置子页面（通用兜底）
// ============================================================

@Composable
fun SettingsSubPage(
    title: String,
    onBack: () -> Unit,
    isTransitioning: Boolean = false,
) {
    val navOwner = rememberNavigationEventDispatcherOwner()
    val scrollBehavior = MiuixScrollBehavior()

    val surfaceColor = MiuixTheme.colorScheme.surface
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(surfaceColor)
            drawContent()
        }
    } else {
        null
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val measuredTop = with(density) { topBarBottomPx.toDp() }
    val contentTopPadding = if (topBarBottomPx > 0) measuredTop else statusBarHeight + TOP_BAR_FALLBACK_HEIGHT

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (blurSupported && backdrop != null) {
                            Modifier.layerBackdrop(backdrop)
                        } else {
                            Modifier
                        }
                    )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(scrollBehavior.nestedScrollConnection)
                        .scrollEndHaptic()
                        .overScrollVertical(),
                    contentPadding = PaddingValues(top = contentTopPadding + 4.dp),
                    overscrollEffect = null,
                ) {
                    item {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(title, color = MiuixTheme.colorScheme.onSurface)
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "该页面正在开发中",
                                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                    )
                                }
                            }

                            Spacer(
                                Modifier.height(
                                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                                            WindowInsets.captionBar.asPaddingValues().calculateBottomPadding()
                                )
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .then(
                        if (!isTransitioning && blurSupported && backdrop != null) {
                            Modifier.textureBlur(
                                backdrop = backdrop,
                                shape = RectangleShape,
                                blurRadius = TOP_BAR_BLUR_RADIUS,
                                noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                colors = BlurDefaults.blurColors(
                                    blendColors = listOf(
                                        BlendColorEntry(
                                            color = surfaceColor.copy(alpha = TOP_BAR_BLEND_ALPHA),
                                        ),
                                    ),
                                ),
                            )
                        } else {
                            Modifier.background(surfaceColor)
                        }
                    )
                    .statusBarsPadding()
                    .onGloballyPositioned { coords ->
                        val bottomPx = coords.boundsInWindow().bottom.toInt()
                        if (bottomPx != topBarBottomPx) {
                            topBarBottomPx = bottomPx
                        }
                    }
            ) {
                TopAppBar(
                    title = title,
                    largeTitle = title,
                    scrollBehavior = scrollBehavior,
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = "返回",
                                tint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                    }
                )
            }
        }
    }
}

// ============================================================
// ★★★ Mote 的 Material 3 分段列表 ★★★
// ============================================================

private val MOTE_SEG_BIG_CORNER = 25.dp
private val MOTE_SEG_SMALL_CORNER = 5.dp
private val MOTE_SEG_GAP = 4.dp
private val MOTE_SEG_ITEM_V_PADDING = 16.dp
private val MOTE_SEG_ITEM_H_PADDING = 16.dp

internal val M3_SEGMENT_LIST_HORIZONTAL_PADDING = 16.dp
internal val M3_SEGMENT_GROUP_GAP = 12.dp

private val LocalMoteSegmentShape = compositionLocalOf<Shape?> { null }

private fun moteSegmentShapeFor(index: Int, count: Int): Shape {
    val big = MOTE_SEG_BIG_CORNER
    val small = MOTE_SEG_SMALL_CORNER
    return when {
        count <= 1 -> RoundedCornerShape(big)
        index == 0 -> RoundedCornerShape(
            topStart = big, topEnd = big,
            bottomStart = small, bottomEnd = small,
        )
        index == count - 1 -> RoundedCornerShape(
            topStart = small, topEnd = small,
            bottomStart = big, bottomEnd = big,
        )
        else -> RoundedCornerShape(small)
    }
}

@Composable
internal fun MoteSegmentedColumn(
    modifier: Modifier = Modifier,
    content: List<@Composable () -> Unit>,
) {
    if (content.isEmpty()) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(MOTE_SEG_GAP),
    ) {
        content.forEachIndexed { index, item ->
            CompositionLocalProvider(
                LocalMoteSegmentShape provides moteSegmentShapeFor(index, content.size)
            ) {
                item()
            }
        }
    }
}

@Composable
internal fun M3SegmentItem(
    headline: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    supporting: (@Composable () -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    val shape = LocalMoteSegmentShape.current ?: RoundedCornerShape(12.dp)
    val color = lerp(
        M3MaterialTheme.colorScheme.surfaceContainerLow,
        M3MaterialTheme.colorScheme.secondaryContainer,
        0.10f,
    )
    val contentColor = M3MaterialTheme.colorScheme.onSurface

    val innerContent: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(
                horizontal = MOTE_SEG_ITEM_H_PADDING,
                vertical = MOTE_SEG_ITEM_V_PADDING,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                headline()
                if (supporting != null) {
                    Spacer(Modifier.height(2.dp))
                    supporting()
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
    }

    if (onClick != null) {
        M3Surface(
            onClick = onClick,
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = color,
            contentColor = contentColor,
            content = innerContent,
        )
    } else {
        M3Surface(
            modifier = modifier.fillMaxWidth(),
            shape = shape,
            color = color,
            contentColor = contentColor,
            content = innerContent,
        )
    }
}

@Composable
internal fun M3SegmentDropdownItem(
    title: String,
    summary: String?,
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = options.getOrElse(selectedIndex) { options.firstOrNull() ?: "" }

    val shape = LocalMoteSegmentShape.current ?: RoundedCornerShape(12.dp)
    val color = lerp(
        M3MaterialTheme.colorScheme.surfaceContainerLow,
        M3MaterialTheme.colorScheme.secondaryContainer,
        0.10f,
    )
    val contentColor = M3MaterialTheme.colorScheme.onSurface

    M3Surface(
        onClick = { expanded = true },
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = color,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = MOTE_SEG_ITEM_H_PADDING,
                vertical = MOTE_SEG_ITEM_V_PADDING,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                M3Text(title)
                if (summary != null) {
                    Spacer(Modifier.height(2.dp))
                    M3Text(
                        text = summary,
                        color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                        style = M3MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            Box(
                modifier = Modifier.padding(start = 12.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    M3Text(
                        text = selected,
                        color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                        style = M3MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.width(2.dp))
                    M3Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = M3MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }

                M3DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = M3MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    options.forEachIndexed { index, label ->
                        M3DropdownMenuItem(
                            text = {
                                M3Text(
                                    text = label,
                                    color = if (index == selectedIndex) {
                                        M3MaterialTheme.colorScheme.primary
                                    } else {
                                        M3MaterialTheme.colorScheme.onSurface
                                    },
                                )
                            },
                            onClick = {
                                onSelected(index)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun M3SegmentSwitchItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    summary: String? = null,
    enabled: Boolean = true,
) {
    M3SegmentItem(
        headline = { M3Text(title) },
        supporting = if (summary != null) {
            {
                M3Text(
                    text = summary,
                    color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                    style = M3MaterialTheme.typography.bodyMedium,
                )
            }
        } else null,
        trailing = {
            M3Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
            )
        },
        onClick = if (enabled) ({ onCheckedChange(!checked) }) else null,
    )
}

@Composable
internal fun M3SegmentArrowItem(
    title: String,
    onClick: () -> Unit,
    endText: String? = null,
    leading: (@Composable () -> Unit)? = null,
) {
    M3SegmentItem(
        headline = { M3Text(title) },
        leading = leading,
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (endText != null) {
                    M3Text(
                        text = endText,
                        color = M3MaterialTheme.colorScheme.onSurfaceVariant,
                        style = M3MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.width(6.dp))
                }
                M3Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = M3MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp),
                )
            }
        },
        onClick = onClick,
    )
}

@Composable
internal fun M3SegmentCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    M3Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(MOTE_SEG_BIG_CORNER),
        color = Color.Transparent,
        contentColor = M3MaterialTheme.colorScheme.onSurface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(MOTE_SEG_GAP),
            content = content,
        )
    }
}
