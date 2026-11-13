@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.ai

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.Manifest
import android.graphics.BitmapFactory
import android.os.SystemClock
import android.provider.Settings as AndroidSettings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.BackHandler
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import com.torachen.mote.R
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.torachen.mote.ui.filemanager.LocalFileViewerRequest
import com.torachen.mote.ui.filemanager.LocalFileViewerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
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
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.ExpandLess
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.icon.extended.File
import top.yukonga.miuix.kmp.icon.extended.Folder
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.icon.extended.MindMap
import top.yukonga.miuix.kmp.icon.extended.Pause
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Send
import top.yukonga.miuix.kmp.icon.extended.SelectAll
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Share
import top.yukonga.miuix.kmp.icon.extended.Sidebar
import top.yukonga.miuix.kmp.menu.WindowIconCascadingDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import com.torachen.mote.service.MoteAccessibilityService
import com.torachen.mote.ui.KEY_PROGRESSIVE_TITLE_BLUR
import com.torachen.mote.ui.SettingsBackButton
import com.torachen.mote.ui.buildBackCardMaterialModifier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CHAT_TOP_BAR_FALLBACK_HEIGHT = 72.dp
private val ChatEase = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

/**
 * AI 对话页。
 *
 * 视觉与设置页保持一致：MIUIX 大标题栏、渐进模糊、返回按钮卡片；
 * 对话使用气泡：AI 在左（surfaceContainer 白）、用户在右（primary 蓝），
 * 正文使用 MIUIX body1（16sp），内容按 Markdown 渲染。
 */
internal object AiGeneratedFilesRequest {
    var pending by mutableStateOf(false)
}

@Composable
fun AiChatPage(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    AiChatStore.ensureLoaded(context)
    LaunchedEffect(Unit) {
        if (AiSettings.newConversationOnOpen) {
            AiChatStore.newConversation()
        }
    }
    AiPageTheme {
        AiChatContent(
            onBack = onBack,
            onOpenSettings = onOpenSettings,
        )
    }
}

@Composable
private fun AiChatContent(
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val context = LocalContext.current
    val navOwner = rememberNavigationEventDispatcherOwner()

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
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_PROGRESSIVE_TITLE_BLUR ->
                    progressiveTitleBlur = prefs.getBoolean(key, true)

                "ui_material_style" -> materialStyle = prefs.getInt(key, 2)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val listScrollState = rememberScrollState()
    val isAtTop by remember {
        derivedStateOf { listScrollState.value == 0 && !listScrollState.isScrollInProgress }
    }
    val themeIsDark = surfaceColor.luminance() < 0.5f
    val backCardAlpha by animateFloatAsState(
        targetValue = if (isAtTop || !progressiveTitleBlur) 0f else 1f,
        animationSpec = tween(400, easing = ChatEase),
        label = "aiChatBackCardAlpha",
    )
    val backCardMaterialModifier = if (progressiveTitleBlur) {
        buildBackCardMaterialModifier(materialStyle, blurSupported, backdrop, themeIsDark)
    } else {
        Modifier
    }
    val capsuleMaterialModifier = if (progressiveTitleBlur) {
        buildBackCardMaterialModifier(
            materialStyle = materialStyle,
            blurSupported = blurSupported,
            backdrop = backdrop,
            themeIsDark = themeIsDark,
            shape = RoundedCornerShape(50),
        )
    } else {
        Modifier
    }
    val titleBarBlendColorEntries = remember(surfaceColor) {
        listOf(BlendColorEntry(color = surfaceColor.copy(alpha = 0.8f)))
    }
    val inputBarBackgroundModifier = if (blurSupported && backdrop != null && materialStyle != 0) {
        if (progressiveTitleBlur) {
            Modifier.progressiveTextureBlur(
                backdrop = backdrop,
                shape = RectangleShape,
                blurRadius = 10f,
                gradient = ProgressiveBlur.Bottom.copy(
                    startFraction = 0.625f,
                    curve = 1f,
                ),
                colors = BlurDefaults.blurColors(
                    blendColors = listOf(
                        BlendColorEntry(color = surfaceColor.copy(alpha = 0.3f)),
                    ),
                ),
            ).background(
                Brush.verticalGradient(
                    listOf(Color.Transparent, surfaceColor),
                ),
            )
        } else {
            Modifier.textureBlur(
                backdrop = backdrop,
                shape = RectangleShape,
                blurRadius = 25f,
                noiseCoefficient = BlurDefaults.NoiseCoefficient,
                colors = BlurDefaults.blurColors(
                    blendColors = titleBarBlendColorEntries,
                ),
            )
        }
    } else {
        Modifier
    }

    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var topBarBottomPx by remember { mutableIntStateOf(0) }
    val measuredTop = with(density) { topBarBottomPx.toDp() }
    val contentTopPadding =
        if (topBarBottomPx > 0) measuredTop else statusBarHeight + CHAT_TOP_BAR_FALLBACK_HEIGHT

    val messages = AiChatStore.messages
    val generatedFiles by remember {
        derivedStateOf { collectAiGeneratedFiles(messages) }
    }
    val conversationId = AiChatStore.activeConversationId
    val provider = AiConversationSettings.providerFor(conversationId)
    val configured = provider?.isConfigured == true
    val activeConversation = AiChatStore.conversations.firstOrNull {
        it.id == AiChatStore.activeConversationId
    }
    val pageTitle = activeConversation?.title?.ifBlank { "新对话" } ?: "新对话"

    var input by remember {
        mutableStateOf(AiDraftStore.get(context, AiChatStore.activeConversationId))
    }
    val attachments = remember { mutableStateListOf<AiAttachment>() }
    var readingFile by remember { mutableStateOf(false) }
    var pendingSuggestionPrompt by remember { mutableStateOf<String?>(null) }
    var inputBarHeightPx by remember { mutableIntStateOf(0) }
    var followBottom by remember { mutableStateOf(true) }
    var programmaticScroll by remember { mutableStateOf(false) }
    var showSidebar by remember { mutableStateOf(false) }
    var editingMessage by remember { mutableStateOf<AiUiMessage?>(null) }
    var editText by remember { mutableStateOf("") }
    BackHandler(enabled = showSidebar) { showSidebar = false }

    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val notice = AiChatStore.notice

    // 键盘弹出时输入栏内容与滚动区域都上移，隐藏时只上移导航栏高度；
    // 这里补上导航栏未被 IME 覆盖的部分，让两种状态的气泡底间距一致。
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val imeBottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val inputBarBottomGap = (navigationBarBottom - imeBottom).coerceAtLeast(0.dp)

    // 每个对话的输入草稿：切换对话时恢复；输入内容变化时写回当前对话。
    LaunchedEffect(AiChatStore.activeConversationId) {
        input = AiDraftStore.get(context, AiChatStore.activeConversationId)
        attachments.clear()
    }

    fun setInput(value: String) {
        input = value
        AiDraftStore.set(context, AiChatStore.activeConversationId, value)
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->        if (uri == null) {
            pendingSuggestionPrompt = null
            return@rememberLauncherForActivityResult
        }
        if (attachments.size >= 3) {
            Toast.makeText(context, "最多同时附加 3 个文件", Toast.LENGTH_SHORT).show()
            pendingSuggestionPrompt = null
            return@rememberLauncherForActivityResult
        }
        readingFile = true
        scope.launch {
            try {
                val attachment = AiFileReader.read(context, uri)
                attachments.add(attachment)
                pendingSuggestionPrompt?.let {
                    setInput(it)
                    pendingSuggestionPrompt = null
                }
                attachment.note?.let { note ->
                    Toast.makeText(context, "${attachment.name}：$note", Toast.LENGTH_SHORT).show()
                }
            } catch (error: Exception) {
                Toast.makeText(
                    context,
                    error.message ?: "读取文件失败",
                    Toast.LENGTH_SHORT,
                ).show()
            } finally {
                readingFile = false
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            filePicker.launch(arrayOf("audio/*"))
        } else {
            pendingSuggestionPrompt = null
            Toast.makeText(
                context,
                "需要录音权限才能转写音频内容",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    LaunchedEffect(listScrollState) {
        snapshotFlow { listScrollState.isScrollInProgress }
            .drop(1)
            .collect { scrolling ->
                // maxValue 在首帧布局前是 Int.MAX_VALUE，此时不能据此判断位置
                if (listScrollState.maxValue == Int.MAX_VALUE) return@collect
                if (scrolling) {
                    if (!programmaticScroll &&
                        listScrollState.value < listScrollState.maxValue - 32
                    ) {
                        followBottom = false
                    }
                } else if (!programmaticScroll) {
                    followBottom = listScrollState.value >= listScrollState.maxValue - 32
                }
            }
    }
    LaunchedEffect(listScrollState) {
        // 内容高度变化（新消息、流式输出、Markdown 重新排版）时跟随到底部
        snapshotFlow { listScrollState.maxValue }.collect { max ->
            if (max == Int.MAX_VALUE) return@collect
            if (!followBottom || programmaticScroll) return@collect
            programmaticScroll = true
            try {
                withFrameNanos { }
                listScrollState.scrollTo(max)
            } finally {
                programmaticScroll = false
            }
        }
    }

    fun createNewConversation() {
        // 输入框有内容也可以新建：当前内容先存到当前对话的草稿里。
        val previousConversation = AiChatStore.activeConversationId
        AiDraftStore.set(
            context,
            previousConversation ?: AiDraftStore.NEW_CONVERSATION_KEY,
            input,
        )
        AiChatStore.newConversation()
        // 原本还没有对话时，把这段草稿转到刚创建的对话上，避免输入内容“消失”。
        if (previousConversation == null) {
            AiChatStore.activeConversationId?.let { newConversationId ->
                AiDraftStore.set(context, newConversationId, input)
            }
        }
        attachments.clear()
        followBottom = true
    }

    fun submit() {
        val text = input.trim()
        if (!configured) {
            Toast.makeText(context, "请先配置 AI 服务", Toast.LENGTH_SHORT).show()
            return
        }
        if (AiChatStore.streaming) return
        if (text.isEmpty() && attachments.isEmpty()) return
        val draftKey = AiChatStore.activeConversationId
        AiChatStore.send(context, text, attachments.toList())
        AiDraftStore.remove(context, draftKey)
        if (draftKey == null) {
            AiDraftStore.remove(
                context,
                AiChatStore.activeConversationId ?: AiDraftStore.NEW_CONVERSATION_KEY,
            )
        }
        input = ""
        attachments.clear()
        focusManager.clearFocus()
        followBottom = true
    }

    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides navOwner,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(surfaceColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (blurSupported && backdrop != null) {
                            Modifier.layerBackdrop(backdrop)
                        } else {
                            Modifier
                        },
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .overScrollVertical()
                        .verticalScroll(listScrollState)
                        .padding(top = contentTopPadding + 6.dp),
                ) {
                    if (notice != null) {
                        AiNoticeCard(
                            text = notice,
                            onDismiss = { AiChatStore.clearNotice() },
                        )
                    }

                    if (!configured) {
                        AiConfigCard(onOpenSettings = onOpenSettings)
                    }

                    if (messages.isEmpty()) {
                        if (configured) {
                            AiEmptyState(
                                onPickSuggestion = { suggestion ->
                                    if (suggestion.mimeTypes != null) {
                                        setInput(suggestion.prompt)
                                        pendingSuggestionPrompt = suggestion.prompt
                                        val isAudio = suggestion.mimeTypes.any { it == "audio/*" }
                                        if (isAudio && ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO,
                                            ) != PackageManager.PERMISSION_GRANTED
                                        ) {
                                            audioPermissionLauncher.launch(
                                                Manifest.permission.RECORD_AUDIO,
                                            )
                                        } else {
                                            filePicker.launch(suggestion.mimeTypes)
                                        }
                                    } else if (suggestion.isWebSummary &&
                                        AiWebContext.isDefaultPage()
                                    ) {
                                        Toast.makeText(
                                            context,
                                            "当前页面暂不支持总结，请先打开一个网页",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    } else {
                                        setInput(suggestion.prompt)
                                    }
                                },
                            )
                        }
                    } else {
                        messages.forEach { message ->
                            key(AiChatStore.activeConversationId, message.id) {
                                AiMessageBubble(
                                    message = message,
                                    onRefresh = { AiChatStore.regenerate(message.id) },
                                    onEdit = {
                                        editingMessage = message
                                        editText = message.content
                                    },
                                )
                            }
                        }
                    }

                    if (generatedFiles.isNotEmpty()) {
                        AiGeneratedFilesSection(files = generatedFiles)
                    }

                    Spacer(
                        modifier = Modifier.height(
                            with(density) { inputBarHeightPx.toDp() } + inputBarBottomGap + 18.dp,
                        ),
                    )
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
                                        startFraction = 0.625f,
                                        curve = 1f,
                                    ),
                                    colors = BlurDefaults.blurColors(
                                        blendColors = listOf(
                                            BlendColorEntry(
                                                color = surfaceColor.copy(alpha = 0.3f),
                                            ),
                                        ),
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
                                    blurRadius = 25f,
                                    noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                    colors = BlurDefaults.blurColors(
                                        blendColors = titleBarBlendColorEntries,
                                    ),
                                )
                            }
                        } else {
                            Modifier.background(surfaceColor)
                        },
                    )
                    .statusBarsPadding()
                    .onGloballyPositioned { coordinates ->
                        val bottomPx = coordinates.boundsInWindow().bottom.toInt()
                        if (bottomPx != topBarBottomPx) {
                            topBarBottomPx = bottomPx
                        }
                    },
            ) {
                SmallTopAppBar(
                    title = pageTitle,
                    subtitle = AiConversationSettings.permissionFor(
                        AiChatStore.activeConversationId,
                    ).label,
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        AiTopBarGroup(
                            materialModifier = capsuleMaterialModifier,
                            alpha = backCardAlpha,
                        ) {
                            SettingsBackButton(
                                onBack = onBack,
                                progressiveTitleBlur = false,
                                backCardAlpha = 0f,
                                backCardMaterialModifier = Modifier,
                                iconTint = MiuixTheme.colorScheme.onSurface,
                            )
                            AiTopBarIcon(
                                icon = MiuixIcons.Sidebar,
                                contentDescription = "对话列表",
                                onClick = { showSidebar = true },
                                progressiveTitleBlur = false,
                                backCardAlpha = 0f,
                                backCardMaterialModifier = Modifier,
                            )
                        }
                    },
                    actions = {
                        AiTopBarGroup(
                            materialModifier = capsuleMaterialModifier,
                            alpha = backCardAlpha,
                        ) {
                            AiTopBarIcon(
                                icon = MiuixIcons.Add,
                                contentDescription = "新建对话",
                                onClick = { createNewConversation() },
                                progressiveTitleBlur = false,
                                backCardAlpha = 0f,
                                backCardMaterialModifier = Modifier,
                            )
                            AiTopBarIcon(
                                icon = MiuixIcons.Settings,
                                contentDescription = "AI 设置",
                                onClick = onOpenSettings,
                                progressiveTitleBlur = false,
                                backCardAlpha = 0f,
                                backCardMaterialModifier = Modifier,
                            )
                        }
                    },
                )
            }

            AiChatInputBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                backgroundModifier = inputBarBackgroundModifier,
                input = input,
                onInputChange = { value ->
                    setInput(value)
                },
                quickActions = {
                    AiQuickActionBar(onOpenSettings = onOpenSettings)
                },
                attachments = attachments,
                readingFile = readingFile,
                streaming = AiChatStore.streaming,
                configured = configured,
                canSend = configured &&
                        (input.isNotBlank() || attachments.isNotEmpty()),
                onAttach = { filePicker.launch(arrayOf("*/*")) },
                onRemoveAttachment = { attachment -> attachments.remove(attachment) },
                onSend = { submit() },
                onStop = { AiChatStore.stop() },
                onMeasured = { height ->
                    if (height != inputBarHeightPx) inputBarHeightPx = height
                },
            )

            AnimatedVisibility(
                visible = showSidebar,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(160)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.32f))
                        .clickable { showSidebar = false },
                )
            }
            AnimatedVisibility(
                visible = showSidebar,
                modifier = Modifier.align(Alignment.CenterStart),
                enter = slideInHorizontally(tween(260)) { -it } + fadeIn(tween(180)),
                exit = slideOutHorizontally(tween(220)) { -it } + fadeOut(tween(140)),
            ) {
                AiConversationSidebar(
                    conversations = AiChatStore.conversations,
                    activeId = AiChatStore.activeConversationId,
                    backCardAlpha = backCardAlpha,
                    onSelect = { id ->
                        AiChatStore.switchConversation(id)
                        showSidebar = false
                    },
                    onNew = {
                        createNewConversation()
                        showSidebar = false
                        followBottom = true
                    },
                    onClose = { showSidebar = false },
                )
            }
        }
        AiFileEditConfirmDialog()
        val editing = editingMessage
        if (editing != null) {
            WindowDialog(
                show = true,
                title = "编辑消息",
                summary = "修改后将重新发送，并删除这条消息之后的对话。",
                onDismissRequest = { editingMessage = null },
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = "消息内容",
                        useLabelAsPlaceholder = true,
                        maxLines = 8,
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Button(
                            onClick = { editingMessage = null },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.secondaryContainer,
                                contentColor = MiuixTheme.colorScheme.onSurface,
                            ),
                        ) { Text("取消") }
                        Button(
                            onClick = {
                                val text = editText.trim()
                                if (text.isNotEmpty()) {
                                    AiChatStore.editUserMessage(context, editing.id, text)
                                }
                                editingMessage = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.primary,
                                contentColor = MiuixTheme.colorScheme.onPrimary,
                            ),
                        ) { Text("重新发送") }
                    }
                }
            }
        }
    }
}

// ============================================================
// 顶栏图标 / 对话侧栏
// ============================================================

@Composable
private fun AiTopBarGroup(
    materialModifier: Modifier,
    alpha: Float,
    content: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(50)),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer { this.alpha = alpha }
                .then(materialModifier),
        )
        Row(
            modifier = Modifier.padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

@Composable
private fun AiTopBarIcon(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    progressiveTitleBlur: Boolean,
    backCardAlpha: Float,
    backCardMaterialModifier: Modifier,
) {
    if (progressiveTitleBlur) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = backCardAlpha }
                    .then(backCardMaterialModifier),
            )
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp),
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AiConversationSidebar(
    conversations: List<AiConversationMeta>,
    activeId: String?,
    backCardAlpha: Float,
    onSelect: (String) -> Unit,
    onNew: () -> Unit,
    onClose: () -> Unit,
) {
    var selectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<String>() }
    BackHandler(enabled = selectionMode) {
        selectionMode = false
        selectedIds.clear()
    }
    val secondary = MiuixTheme.colorScheme.onSurfaceVariantSummary
    val isDark = MiuixTheme.colorScheme.surface.luminance() < 0.5f
    val selectedBackground = if (isDark) {
        MiuixTheme.colorScheme.surfaceContainer
    } else {
        Color.White
    }

    Box(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(MiuixTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(
                    bottom = WindowInsets.navigationBars.asPaddingValues()
                        .calculateBottomPadding(),
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(
                        MiuixTheme.colorScheme.surface.copy(
                            alpha = 0.92f * backCardAlpha,
                        ),
                    ),
            ) {
                SmallTopAppBar(
                    title = "对话",
                    color = Color.Transparent,
                    defaultWindowInsetsPadding = false,
                    navigationIcon = {
                        AiSidebarHeaderIcon(
                            icon = if (selectionMode) MiuixIcons.Close else MiuixIcons.Sidebar,
                            contentDescription = if (selectionMode) "退出多选" else "关闭对话列表",
                            showMaterial = backCardAlpha > 0.05f,
                            onClick = {
                                if (selectionMode) {
                                    selectionMode = false
                                    selectedIds.clear()
                                } else {
                                    onClose()
                                }
                            },
                        )
                    },
                    actions = {
                        AiSidebarHeaderIcon(
                            icon = if (selectionMode) MiuixIcons.SelectAll else MiuixIcons.Add,
                            contentDescription = if (selectionMode) "全选" else "新建对话",
                            showMaterial = backCardAlpha > 0.05f,
                            onClick = {
                                if (selectionMode) {
                                    if (selectedIds.size == conversations.size) {
                                        selectedIds.clear()
                                    } else {
                                        selectedIds.clear()
                                        selectedIds.addAll(conversations.map { it.id })
                                    }
                                } else {
                                    onNew()
                                }
                            },
                        )
                    },
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(conversations, key = { it.id }) { meta ->
                    val isActive = meta.id == activeId
                    val isChecked = meta.id in selectedIds
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when {
                                    isChecked -> MiuixTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    isActive -> selectedBackground
                                    else -> Color.Transparent
                                },
                            )
                            .combinedClickable(
                                onClick = {
                                    if (selectionMode) {
                                        if (isChecked) selectedIds.remove(meta.id)
                                        else selectedIds.add(meta.id)
                                    } else {
                                        onSelect(meta.id)
                                    }
                                },
                                onLongClick = {
                                    if (!selectionMode) {
                                        selectionMode = true
                                        selectedIds.clear()
                                        selectedIds.add(meta.id)
                                    }
                                },
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (selectionMode) {
                            Checkbox(
                                state = if (isChecked) {
                                    androidx.compose.ui.state.ToggleableState.On
                                } else {
                                    androidx.compose.ui.state.ToggleableState.Off
                                },
                                onClick = null,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = meta.title,
                                fontSize = 15.sp,
                                color = MiuixTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            val time = remember(meta.updatedAt) {
                                SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
                                    .format(Date(meta.updatedAt))
                            }
                            Text(
                                text = time,
                                fontSize = 11.sp,
                                color = secondary,
                            )
                        }
                    }
                }
                if (conversations.isEmpty()) {
                    item {
                        Text(
                            text = "暂无对话",
                            fontSize = 13.sp,
                            color = secondary,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            if (!selectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MiuixTheme.colorScheme.surfaceContainer)
                        .clickable {
                            AiGeneratedFilesRequest.pending = true
                            onClose()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = MiuixIcons.Folder,
                        contentDescription = null,
                        tint = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "生成的文件",
                        fontSize = 15.sp,
                        color = MiuixTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 10.dp),
                    )
                }
            }
        }

        if (selectionMode) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 28.dp)
                    .clip(CircleShape)
                    .background(
                        if (selectedIds.isEmpty()) {
                            MiuixTheme.colorScheme.secondaryContainer
                        } else {
                            MiuixTheme.colorScheme.primary
                        },
                    )
                    .clickable(enabled = selectedIds.isNotEmpty()) {
                        AiChatStore.deleteConversations(selectedIds.toSet())
                        selectionMode = false
                        selectedIds.clear()
                    }
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "删除",
                    fontSize = 14.sp,
                    color = if (selectedIds.isEmpty()) {
                        MiuixTheme.colorScheme.onSurfaceVariantSummary
                    } else {
                        Color.White
                    },
                )
            }
        }
    }
}

@Composable
private fun AiSidebarHeaderIcon(
    icon: ImageVector,
    contentDescription: String,
    showMaterial: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (showMaterial) MiuixTheme.colorScheme.surfaceContainer
                else Color.Transparent,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
internal fun AiFileEditConfirmDialog() {
    val request = AiFileEditConfirmation.request ?: return
    WindowDialog(
        show = true,
        title = "确认修改文件",
        summary = request.summary,
        onDismissRequest = { AiFileEditConfirmation.resolve(false) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { AiFileEditConfirmation.resolve(false) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface,
                ),
            ) { Text("拒绝") }
            Button(
                onClick = { AiFileEditConfirmation.resolve(true) },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.primary,
                    contentColor = MiuixTheme.colorScheme.onPrimary,
                ),
            ) { Text("确认修改") }
        }
    }
}

// ============================================================
// 气泡
// ============================================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun AiMessageBubble(
    message: AiUiMessage,
    widthFraction: Float = 0.86f,
    aiBubbleColor: Color? = null,
    onRefresh: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val maxBubbleWidth = (configuration.screenWidthDp * widthFraction).dp
    val isUser = message.isUser

    val bubbleColor = if (isUser) {
        MiuixTheme.colorScheme.primary
    } else {
        aiBubbleColor ?: MiuixTheme.colorScheme.surfaceContainer
    }
    val contentColor = if (isUser) MiuixTheme.colorScheme.onPrimary else MiuixTheme.colorScheme.onSurface
    val secondaryColor = if (isUser) {
        MiuixTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
    } else {
        MiuixTheme.colorScheme.onSurfaceVariantSummary
    }
    val palette = AiMarkdownPalette(
        text = contentColor,
        secondary = secondaryColor,
        accent = if (isUser) Color.White else MiuixTheme.colorScheme.primary,
        codeBackground = if (isUser) {
            Color.White.copy(alpha = 0.18f)
        } else {
            MiuixTheme.colorScheme.secondaryContainer
        },
        codeText = contentColor,
        divider = if (isUser) Color.White.copy(alpha = 0.3f) else MiuixTheme.colorScheme.outline,
    )
    val bubbleShape = if (isUser) {
        RoundedCornerShape(18.dp, 18.dp, 6.dp, 18.dp)
    } else {
        RoundedCornerShape(18.dp, 18.dp, 18.dp, 6.dp)
    }

    fun copyMessage() {
        val text = buildString {
            if (message.reasoning.isNotBlank()) {
                append("【思考过程】\n").append(message.reasoning).append("\n\n")
            }
            append(message.content)
        }.trim()
        if (text.isBlank()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        clipboard?.setPrimaryClip(ClipData.newPlainText("AI 对话", text))
        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
    }

    fun shareMessage() {
        val text = buildString {
            if (message.reasoning.isNotBlank()) {
                append("【思考过程】\n").append(message.reasoning).append("\n\n")
            }
            append(message.content)
            message.error?.takeIf { it.isNotBlank() }?.let {
                append("\n\n【错误】\n").append(it)
            }
        }.trim()
        if (text.isBlank()) return
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "分享 AI 回复"))
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = maxBubbleWidth)
                    .clip(bubbleShape)
                    .background(bubbleColor)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                SelectionContainer {
                    Column {
                        if (message.attachments.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        message.attachments.forEach { attachment ->
                            AiAttachmentBubbleChip(
                                attachment = attachment,
                                isUser = isUser,
                            )
                        }
                    }
                    if (message.reasoning.isNotBlank() || message.content.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (!isUser && (message.error.isNullOrBlank() || message.reasoning.isNotBlank())) {
                    // 没有任何工具动作、也没有思维链时不必显示思考区；
                    // 真正调用过工具（step 数 > 1）或正在生成时才展示过程。
                    val shouldShowThinking = message.streaming ||
                            message.reasoning.isNotBlank() ||
                            message.steps.size > 1
                    if (shouldShowThinking) {
                        AiThinkingSection(message = message)
                    }
                    if (message.content.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                    }
                }

                if (message.content.isNotBlank()) {
                    AiMarkdown(
                        source = message.content,
                        palette = palette,
                        baseStyle = MiuixTheme.textStyles.body1.copy(color = contentColor),
                        baseFontSize = MiuixTheme.textStyles.body1.fontSize,
                    )
                } else if (!isUser && message.streaming && message.reasoning.isBlank()) {
                    // 与思考框拉开和气泡底部一致的间距
                    Spacer(Modifier.height(10.dp))
                    AiTypingDots(color = secondaryColor)
                }

                if (message.error != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = message.error ?: "请求失败",
                        fontSize = 13.sp,
                        color = if (isUser) Color.White else MiuixTheme.colorScheme.error,
                        lineHeight = 19.sp,
                    )
                }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            AiMessageActions(
                message = message,
                onCopy = { copyMessage() },
                onShare = { shareMessage() },
                onRefresh = onRefresh,
                onEdit = onEdit,
            )
        }
    }
}

@Composable
private fun AiMessageActions(
    message: AiUiMessage,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onRefresh: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
) {
    val color = MiuixTheme.colorScheme.onSurfaceVariantSummary
    val time = remember(message.createdAt) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.createdAt))
    }
    val tokens = if (message.usageTokens > 0) {
        message.usageTokens
    } else {
        estimateTokens(message.content + message.reasoning)
    }
    val usage = "用量 ${formatTokenCount(tokens)} tok"

    Row(
        modifier = Modifier.padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!message.isUser && onRefresh != null) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onRefresh),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.Refresh,
                    contentDescription = "重新生成",
                    tint = color,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        if (message.isUser && onEdit != null) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onEdit),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = MiuixIcons.Edit,
                    contentDescription = "编辑消息",
                    tint = color,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onCopy),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MiuixIcons.Copy,
                contentDescription = "复制",
                tint = color,
                modifier = Modifier.size(14.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onShare),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MiuixIcons.Share,
                contentDescription = "分享",
                tint = color,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(text = usage, fontSize = 11.sp, color = color)
        Text(text = time, fontSize = 11.sp, color = color)
    }
}

@Composable
private fun AiImageThumb(
    path: String,
    size: Dp,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, path) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(path, bounds)
                var sample = 1
                val maxDim = maxOf(bounds.outWidth, bounds.outHeight)
                while (maxDim / sample > 800) sample *= 2
                BitmapFactory.decodeFile(
                    path,
                    BitmapFactory.Options().apply { inSampleSize = sample },
                )?.asImageBitmap()
            }.getOrNull()
        }
    }
    val image = bitmap
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(10.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            Image(
                bitmap = image,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Icon(
                imageVector = MiuixIcons.File,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.size(size * 0.4f),
            )
        }
    }
}

@Composable
private fun AiAttachmentBubbleChip(
    attachment: AiAttachmentMeta,
    isUser: Boolean,
) {
    val imagePath = attachment.imagePath
    if (imagePath != null) {
        AiImageThumb(
            path = imagePath,
            size = 84.dp,
            modifier = Modifier.padding(2.dp),
            onClick = {
                LocalFileViewerState.current = LocalFileViewerRequest.Web(
                    path = imagePath,
                    title = attachment.name,
                    browserUrl = "file://$imagePath",
                    isImage = true,
                )
            },
        )
        return
    }
    val background = if (isUser) {
        Color.White.copy(alpha = 0.16f)
    } else {
        MiuixTheme.colorScheme.secondaryContainer
    }
    val contentColor = if (isUser) Color.White else MiuixTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .widthIn(max = 260.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = MiuixIcons.File,
            contentDescription = null,
            tint = contentColor.copy(alpha = 0.8f),
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = attachment.name,
            fontSize = 12.sp,
            color = contentColor.copy(alpha = 0.9f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 5.dp),
        )
    }
}

// ============================================================
// 思考过程
// ============================================================

@Composable
private fun AiThinkingSection(message: AiUiMessage) {
    val steps = message.steps
    var expanded by remember(message.id, message.reasoningDone) {
        mutableStateOf(AiSettings.showThinking && !message.reasoningDone)
    }
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(message.streaming, message.reasoningDone, message.reasoningStartedAt) {
        while (message.streaming && !message.reasoningDone) {
            now = SystemClock.elapsedRealtime()
            delay(1000L)
        }
    }

    val elapsed = when {
        message.reasoningDone -> message.reasoningElapsedMs
        message.reasoningStartedAt > 0L -> (now - message.reasoningStartedAt).coerceAtLeast(0L)
        else -> 0L
    }
    val runningStep = steps.lastOrNull { it.state == AiStepState.Running }
    val failedCount = steps.count { it.state == AiStepState.Failed }
    val header = buildString {
        append(
            when {
                message.streaming && message.reasoningDone -> "正在回答"
                message.streaming -> "正在思考"
                else -> "已完成"
            },
        )
        if (elapsed > 0L) append(" · ").append(formatAiElapsed(elapsed))
        if (steps.isNotEmpty()) append(" · ").append(steps.size).append(" 步")
        if (failedCount > 0) append(" · ").append(failedCount).append(" 步失败")
    }
    val secondary = MiuixTheme.colorScheme.onSurfaceVariantSummary
    val bodySize = MiuixTheme.textStyles.subtitle.fontSize

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MiuixTheme.colorScheme.secondaryContainer)
            .padding(vertical = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { expanded = !expanded }
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = MiuixIcons.MindMap,
                contentDescription = null,
                tint = secondary,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = header,
                fontSize = bodySize,
                color = secondary,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp),
            )
            if (message.streaming && !message.reasoningDone) {
                CircularProgressIndicator(size = 12.dp)
                Spacer(Modifier.width(6.dp))
            }
            Icon(
                imageVector = if (expanded) MiuixIcons.ExpandLess else MiuixIcons.ExpandMore,
                contentDescription = if (expanded) "收起思考过程" else "展开思考过程",
                tint = secondary,
                modifier = Modifier.size(16.dp),
            )
        }

        // 收起时也能看到当前正在做什么，例如「正在滑动屏幕」。
        if (!expanded && runningStep != null) {
            Row(
                modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AiStepIndicator(step = runningStep)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = runningStep.title,
                    fontSize = bodySize,
                    color = secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(180)) + expandVertically(tween(220)),
            exit = fadeOut(tween(140)) + shrinkVertically(tween(180)),
        ) {
            Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 9.dp)) {
                steps.forEach { step ->
                    key(step.id) {
                        AiThinkingStepRow(step = step)
                    }
                }
                if (steps.isNotEmpty() && message.reasoning.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                }
                when {
                    // 模型自己的思维链是纯文字，原样显示，不做任何“美化”改写。
                    message.reasoning.isNotBlank() -> Text(
                        text = message.reasoning,
                        fontSize = bodySize,
                        color = secondary,
                        lineHeight = bodySize * 1.45f,
                    )

                    steps.isEmpty() && message.reasoningDone -> Text(
                        text = "本次回答没有返回思考内容。",
                        fontSize = bodySize,
                        color = secondary,
                    )

                    steps.isEmpty() -> Text(
                        text = "正在理解你的问题…",
                        fontSize = bodySize,
                        color = secondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun AiThinkingStepRow(step: AiAgentStep) {
    val secondary = MiuixTheme.colorScheme.onSurfaceVariantSummary
    val errorColor = MiuixTheme.colorScheme.error
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(15.dp)
                .padding(top = 1.dp),
            contentAlignment = Alignment.Center,
        ) {
            AiStepIndicator(step = step)
        }
        Spacer(Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.title,
                fontSize = 12.sp,
                color = when (step.state) {
                    AiStepState.Running -> MiuixTheme.colorScheme.onSurface
                    AiStepState.Failed -> errorColor
                    AiStepState.Done -> secondary
                },
                lineHeight = 17.sp,
            )
            if (step.detail.isNotBlank()) {
                Text(
                    text = step.detail,
                    fontSize = 11.sp,
                    color = secondary,
                    lineHeight = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        }
    }
}

@Composable
private fun AiStepIndicator(step: AiAgentStep) {
    when (step.state) {
        AiStepState.Running -> CircularProgressIndicator(size = 11.dp)
        AiStepState.Done -> Text(
            text = "✓",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )

        AiStepState.Failed -> Text(
            text = "!",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MiuixTheme.colorScheme.error,
        )
    }
}

@Composable
private fun AiTypingDots(color: Color) {
    val transition = rememberInfiniteTransition(label = "aiTypingDots")
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(vertical = 4.dp),
    ) {
        repeat(3) { index ->
            val alpha by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 480,
                        delayMillis = index * 160,
                        easing = LinearEasing,
                    ),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "aiTypingDot$index",
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = alpha)),
            )
        }
    }
}

// ============================================================
// 空态 / 提示
// ============================================================

private data class AiEmptySuggestion(
    val title: String,
    val prompt: String,
    val mimeTypes: Array<String>? = null,
    val isWebSummary: Boolean = false,
)

@Composable
private fun AiEmptyState(
    onPickSuggestion: (AiEmptySuggestion) -> Unit,
) {
    val defaultPage = AiWebContext.isDefaultPage()
    val suggestions = remember(defaultPage) {
        val pool = mutableListOf(
            AiEmptySuggestion(
                title = "帮我总结文档",
                prompt = "请总结我上传的文档，提炼核心观点、关键数据和结论：",
                mimeTypes = arrayOf("*/*"),
            ),
            AiEmptySuggestion(
                title = "帮我分析图片",
                prompt = "请分析我上传的图片，描述画面内容、文字信息和关键细节：",
                mimeTypes = arrayOf("image/*"),
            ),
            AiEmptySuggestion(
                title = "帮我解析代码",
                prompt = "请解析我上传的代码，说明功能、结构、依赖和潜在问题：",
                mimeTypes = arrayOf("*/*"),
            ),
            AiEmptySuggestion(
                title = "帮我总结音频",
                prompt = "请总结我上传的音频内容，提炼主要信息和结论：",
                mimeTypes = arrayOf("audio/*"),
            ),
            AiEmptySuggestion(
                title = "帮我润色一下文本",
                prompt = "请帮我润色以下文本，保持原意，让表达更自然、有条理，并指出主要修改点：",
            ),
            AiEmptySuggestion(
                title = "帮我搜索并整理信息",
                prompt = "请联网搜索并整理以下主题的最新信息，给出结论、来源和关键要点：",
            ),
            AiEmptySuggestion(
                title = "今天有什么重要新闻",
                prompt = "请搜索今天的国内外重要新闻，按主题整理，并附上每条新闻的来源链接：",
            ),
            AiEmptySuggestion(
                title = "让 AI 帮我操作手机",
                prompt = "请帮我打开手机的设置应用，然后进入显示设置页面看看有哪些选项：",
            ),
            AiEmptySuggestion(
                title = "帮我写一份小红书文案",
                prompt = "请帮我写一篇小红书文案，语气亲切自然，包含吸引人的标题、正文段落和话题标签：",
            ),
            AiEmptySuggestion(
                title = "帮我写一份抖音文案",
                prompt = "请帮我写一份抖音短视频文案，包含开场钩子、口播脚本和结尾互动引导：",
            ),
            AiEmptySuggestion(
                title = "帮我生成一个PPT",
                prompt = "请根据以下主题生成一份 PPT，包含封面、目录、分页要点和结尾：",
            ),
            AiEmptySuggestion(
                title = "帮我生成一个表格",
                prompt = "请根据以下需求生成一个表格，说明列名和每一行内容：",
            ),
            AiEmptySuggestion(
                title = "帮我生成一份Word",
                prompt = "请根据以下主题生成一份 Word 文档，包含标题、章节和正文：",
            ),
        )
        if (!defaultPage) {
            pool.add(
                0,
                AiEmptySuggestion(
                    title = "帮我总结当前网页",
                    prompt = "请总结当前网页的主要内容，提炼关键信息、结论和值得注意的细节：",
                    isWebSummary = true,
                ),
            )
        }
        pool.shuffled().take(3)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(top = 64.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.mipmap.mote_logo),
            contentDescription = "Mote AI",
            modifier = Modifier.size(104.dp),
        )
        Spacer(Modifier.height(18.dp))
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Mote AI",
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.width(6.dp))
            AiPreviewBadge(modifier = Modifier.offset(y = (-1).dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = "能联网搜索、打开网页和应用、生成文档；开启“自动操作”后还能读屏、点击、滑动和输入。",
            fontSize = 12.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(24.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            suggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MiuixTheme.colorScheme.surfaceContainer)
                        .clickable { onPickSuggestion(suggestion) }
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                ) {
                    Text(
                        text = suggestion.title,
                        fontSize = MiuixTheme.textStyles.body1.fontSize,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun AiPreviewBadge(modifier: Modifier = Modifier) {
    val blue = MiuixTheme.colorScheme.primary
    val shape = RoundedCornerShape(6.dp)
    Box(
        modifier = modifier
            .clip(shape)
            .background(blue.copy(alpha = 0.3f))
            .border(BorderStroke(0.5.dp, blue.copy(alpha = 0.5f)), shape)
            .padding(horizontal = 5.dp, vertical = 1.5.dp),
    ) {
        Text(
            text = "预览版",
            fontSize = 10.sp,
            lineHeight = 12.sp,
            color = blue,
        )
    }
}

@Composable
private fun AiConfigCard(onOpenSettings: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "尚未配置 AI 服务",
                fontSize = MiuixTheme.textStyles.headline1.fontSize,
                fontWeight = FontWeight.Medium,
                color = MiuixTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "绑定主流 AI 的 API Key，或添加自定义服务后即可开始对话。",
                fontSize = MiuixTheme.textStyles.body2.fontSize,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                lineHeight = 20.sp,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.primary,
                    contentColor = Color.White,
                ),
                insideMargin = PaddingValues(0.dp),
                cornerRadius = 16.dp,
            ) { Text("去配置") }
        }
    }
}

@Composable
private fun AiNoticeCard(text: String, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
                color = MiuixTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = MiuixIcons.Close,
                    contentDescription = "关闭",
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

// ============================================================
// 输入栏
// ============================================================

@Composable
internal fun AiQuickActionBar(onOpenSettings: () -> Unit) {
    val context = LocalContext.current
    val providers = AiProviderStore.providers
    val conversationId = AiChatStore.activeConversationId
    val active = AiConversationSettings.providerFor(conversationId)
    val currentPermission = AiConversationSettings.permissionFor(conversationId)
    val currentReasoning = AiConversationSettings.reasoningFor(conversationId)
    var showFullAccessDialog by remember { mutableStateOf(false) }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
        if (active == null) {
            AiQuickChip(text = "选择模型", onClick = onOpenSettings)
        } else {
            val entries = buildModelMenuEntries(
                providers = providers,
                active = active,
                currentPermission = currentPermission,
                currentReasoning = currentReasoning,
                onSelect = { provider, modelId ->
                    AiConversationSettings.setProvider(
                        context = context,
                        conversationId = conversationId,
                        provider = provider,
                        modelId = modelId,
                    )
                },
                onPermission = { permission ->
                    if (permission == AiFilePermission.FullAccess) {
                        showFullAccessDialog = true
                    } else {
                        AiConversationSettings.setPermission(context, conversationId, permission)
                    }
                },
                onReasoning = { level ->
                    AiConversationSettings.setReasoning(context, conversationId, level)
                },
            )
            WindowIconCascadingDropdownMenu(
                entries = entries,
                backgroundColor = MiuixTheme.colorScheme.surfaceContainer,
                cornerRadius = 16.dp,
                minHeight = 36.dp,
                minWidth = 0.dp,
                content = {
                    val preset = AiPresets.byId(active.presetId)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(36.dp)
                            .padding(horizontal = 12.dp),
                    ) {
                        AiPlainBrandIcon(
                            text = active.name,
                            color = preset?.let { Color(it.brandColor) }
                                ?: MiuixTheme.colorScheme.primary,
                            iconUrl = preset?.iconUrl,
                            size = 20.dp,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = active.displayModelName(active.model)
                                .ifBlank { active.name },
                            fontSize = 13.sp,
                            color = MiuixTheme.colorScheme.onSurface,
                            maxLines = 1,
                        )
                    }
                },
            )
        }

        AiWebSearchChip()
        AiAgentChip()
        AiFloatingBallChip()
            }
        }
    }

    if (showFullAccessDialog) {
        WindowDialog(
            show = true,
            title = "开启完全权限",
            summary = "完全权限下，常规文件修改会自动执行；删除、覆盖重要内容等敏感操作仍会先向你确认。",
            onDismissRequest = { showFullAccessDialog = false },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { showFullAccessDialog = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("取消") }
                Button(
                    onClick = {
                        AiConversationSettings.setPermission(
                            context,
                            conversationId,
                            AiFilePermission.FullAccess,
                        )
                        showFullAccessDialog = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = MiuixTheme.colorScheme.onPrimary,
                    ),
                ) { Text("确认开启") }
            }
        }
    }
}

@Composable
private fun AiWebSearchChip() {
    val context = LocalContext.current
    val enabled = AiSettings.webSearchEnabled
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) MiuixTheme.colorScheme.primary
                else MiuixTheme.colorScheme.surfaceContainer,
            )
            .clickable {
                AiSettings.setWebSearchEnabled(context, !enabled)
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "联网搜索",
            fontSize = 13.sp,
            color = if (enabled) MiuixTheme.colorScheme.onPrimary
            else MiuixTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AiPermissionChip(
    onSelect: (AiFilePermission) -> Unit,
) {
    val current = AiPermissionStore.permission
    val entries = listOf(
        DropdownEntry(
            listOf(
                DropdownItem(
                    text = AiFilePermission.ViewOnly.label,
                    summary = AiFilePermission.ViewOnly.summary,
                    selected = current == AiFilePermission.ViewOnly,
                    onClick = { onSelect(AiFilePermission.ViewOnly) },
                ),
                DropdownItem(
                    text = AiFilePermission.EditWithConfirm.label,
                    summary = AiFilePermission.EditWithConfirm.summary,
                    selected = current == AiFilePermission.EditWithConfirm,
                    onClick = { onSelect(AiFilePermission.EditWithConfirm) },
                ),
                DropdownItem(
                    text = AiFilePermission.FullAccess.label,
                    summary = AiFilePermission.FullAccess.summary,
                    selected = current == AiFilePermission.FullAccess,
                    onClick = { onSelect(AiFilePermission.FullAccess) },
                ),
            ),
        ),
    )
    WindowIconCascadingDropdownMenu(
        entries = entries,
        backgroundColor = MiuixTheme.colorScheme.surfaceContainer,
        cornerRadius = 16.dp,
        minHeight = 36.dp,
        minWidth = 0.dp,
        content = {
            val elevated = current != AiFilePermission.ViewOnly
            Row(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (elevated) MiuixTheme.colorScheme.primary
                        else MiuixTheme.colorScheme.surfaceContainer,
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = current.label,
                    fontSize = 13.sp,
                    color = if (elevated) MiuixTheme.colorScheme.onPrimary
                    else MiuixTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
        },
    )
}

@Composable
private fun AiQuickChip(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MiuixTheme.colorScheme.surfaceContainer)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AiAgentChip() {
    val context = LocalContext.current
    val enabled = AiSettings.agentEnabled
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) MiuixTheme.colorScheme.primary
                else MiuixTheme.colorScheme.surfaceContainer,
            )
            .clickable {
                if (!enabled) {
                    if (!MoteAccessibilityService.isEnabled(context)) {
                        Toast.makeText(
                            context,
                            "请在系统无障碍设置开启Mote AI 自动操作",
                            Toast.LENGTH_SHORT,
                        ).show()
                        context.startActivity(
                            Intent(AndroidSettings.ACTION_ACCESSIBILITY_SETTINGS)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    } else {
                        AiSettings.setAgentEnabled(context, true)
                        Toast.makeText(context, "自动操作已开启", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    AiSettings.setAgentEnabled(context, false)
                    Toast.makeText(context, "自动操作已关闭", Toast.LENGTH_SHORT).show()
                }
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "自动操作",
            fontSize = 13.sp,
            color = if (enabled) MiuixTheme.colorScheme.onPrimary
            else MiuixTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun AiFloatingBallChip() {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(AiFloatingBubbleService.isRunning()) }
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) MiuixTheme.colorScheme.primary
                else MiuixTheme.colorScheme.surfaceContainer,
            )
            .clickable {
                if (!enabled) {
                    if (!AiFloatingController.hasOverlayPermission(context)) {
                        Toast.makeText(context, "请先授予悬浮窗权限", Toast.LENGTH_SHORT).show()
                        AiFloatingController.requestOverlayPermission(context)
                    } else if (AiFloatingController.start(context)) {
                        enabled = true
                    } else {
                        Toast.makeText(context, "悬浮球启动失败", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    AiFloatingController.stop(context)
                    enabled = false
                }
            }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "悬浮球",
            fontSize = 13.sp,
            color = if (enabled) MiuixTheme.colorScheme.onPrimary
            else MiuixTheme.colorScheme.onSurface,
        )
    }
}

private fun buildModelMenuEntries(
    providers: List<AiProviderConfig>,
    active: AiProviderConfig,
    currentPermission: AiFilePermission,
    currentReasoning: AiReasoningLevel,
    onSelect: (AiProviderConfig, String) -> Unit,
    onPermission: (AiFilePermission) -> Unit,
    onReasoning: (AiReasoningLevel) -> Unit,
): List<DropdownEntry> {
    val items = if (providers.size == 1) {
        modelDropdownItems(providers.first(), onSelect)
    } else {
        providers.map { provider ->
            val preset = AiPresets.byId(provider.presetId)
            val models = modelDropdownItems(provider, onSelect)
            DropdownItem(
                text = provider.name,
                summary = provider.displayModelName(provider.model),
                selected = provider.id == active.id,
                icon = { modifier ->
                    AiPlainBrandIcon(
                        text = provider.name,
                        color = preset?.let { Color(it.brandColor) }
                            ?: MiuixTheme.colorScheme.primary,
                        iconUrl = preset?.iconUrl,
                        modifier = modifier,
                        size = 20.dp,
                    )
                },
                children = models.ifEmpty { null },
                onClick = if (models.isEmpty()) {
                    { onSelect(provider, provider.model) }
                } else {
                    null
                },
            )
        }
    }
    val permissionItems = AiFilePermission.entries.map { permission ->
        DropdownItem(
            text = permission.label,
            selected = permission == currentPermission,
            onClick = { onPermission(permission) },
        )
    }
    val reasoningItems = AiReasoningLevel.entries.map { level ->
        DropdownItem(
            text = level.label,
            selected = level == currentReasoning,
            onClick = { onReasoning(level) },
        )
    }
    return listOf(
        DropdownEntry(items = items),
        DropdownEntry(
            items = listOf(
                DropdownItem(
                    text = "文件权限",
                    summary = currentPermission.label,
                    children = permissionItems,
                ),
                DropdownItem(
                    text = "推理等级",
                    summary = currentReasoning.label,
                    children = reasoningItems,
                ),
            ),
        ),
    )
}

private fun modelDropdownItems(
    provider: AiProviderConfig,
    onSelect: (AiProviderConfig, String) -> Unit,
): List<DropdownItem> {
    val ids = provider.models
        .ifEmpty { listOfNotNull(provider.model.takeIf { it.isNotBlank() }) }
        .distinct()
    return ids.map { modelId ->
        val displayName = provider.displayModelName(modelId)
        DropdownItem(
            text = displayName,
            summary = modelId,
            selected = modelId == provider.model,
            onClick = { onSelect(provider, modelId) },
        )
    }
}

@Composable
private fun AiChatInputBar(
    modifier: Modifier = Modifier,
    backgroundModifier: Modifier = Modifier,
    input: String,
    onInputChange: (String) -> Unit,
    quickActions: @Composable () -> Unit = {},
    attachments: List<AiAttachment>,
    readingFile: Boolean,
    streaming: Boolean,
    configured: Boolean,
    canSend: Boolean,
    onAttach: () -> Unit,
    onRemoveAttachment: (AiAttachment) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    onMeasured: (Int) -> Unit,
) {
    val secondary = MiuixTheme.colorScheme.onSurfaceVariantSummary
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MiuixTheme.colorScheme.surface)
            .windowInsetsPadding(WindowInsets.ime.union(WindowInsets.navigationBars)),
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(backgroundModifier)
            .padding(vertical = 6.dp)
            .onGloballyPositioned { coordinates -> onMeasured(coordinates.size.height) },
    ) {
        quickActions()
        Spacer(Modifier.height(4.dp))

        if (attachments.isNotEmpty() || readingFile) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 6.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                attachments.forEach { attachment ->
                    AiAttachmentDraftChip(
                        attachment = attachment,
                        onRemove = { onRemoveAttachment(attachment) },
                    )
                }
                if (readingFile) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MiuixTheme.colorScheme.secondaryContainer)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(size = 13.dp)
                        Text(
                            text = "正在解析文件…",
                            fontSize = 12.sp,
                            color = secondary,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onAttach,
                enabled = attachments.size < 3 && !readingFile,
            ) {
                Icon(
                    imageVector = MiuixIcons.Folder,
                    contentDescription = "附加文件",
                    tint = if (attachments.size < 3) {
                        MiuixTheme.colorScheme.onSurface
                    } else {
                        MiuixTheme.colorScheme.disabledOnSurface
                    },
                    modifier = Modifier.size(22.dp),
                )
            }
            TextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                label = if (configured) {
                    "输入消息…"
                } else {
                    "请先在 AI 设置中绑定服务"
                },
                useLabelAsPlaceholder = true,
                maxLines = 6,
                minLines = 1,
                colors = TextFieldDefaults.textFieldColors(
                    borderColor = Color.Transparent,
                ),
                cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
            )
            Spacer(Modifier.width(8.dp))
            val actionColor = if (streaming || canSend) {
                MiuixTheme.colorScheme.primary
            } else {
                MiuixTheme.colorScheme.secondaryContainer
            }
            val actionContent = if (streaming || canSend) {
                MiuixTheme.colorScheme.onPrimary
            } else {
                MiuixTheme.colorScheme.onSurfaceVariantSummary
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(actionColor)
                    .clickable(enabled = streaming || canSend) {
                        if (streaming) onStop() else onSend()
                    },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (streaming) MiuixIcons.Pause else MiuixIcons.Send,
                    contentDescription = if (streaming) "停止生成" else "发送",
                    tint = actionContent,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
    }
}

@Composable
private fun AiAttachmentDraftChip(
    attachment: AiAttachment,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .widthIn(max = 220.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MiuixTheme.colorScheme.secondaryContainer)
            .padding(start = 10.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val imagePath = attachment.imagePath
        if (imagePath != null) {
            AiImageThumb(path = imagePath, size = 40.dp)
        } else {
            Icon(
                imageVector = MiuixIcons.File,
                contentDescription = null,
                tint = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = attachment.name,
            fontSize = 12.sp,
            color = MiuixTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(horizontal = 5.dp),
        )
        Icon(
            imageVector = MiuixIcons.Close,
            contentDescription = "移除附件",
            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier
                .size(14.dp)
                .clickable { onRemove() },
        )
    }
}
