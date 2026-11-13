@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.ai

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Pause
import top.yukonga.miuix.kmp.icon.extended.Send
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.darkColorScheme
import top.yukonga.miuix.kmp.theme.lightColorScheme
import top.yukonga.miuix.kmp.window.WindowDialog
import com.torachen.mote.ui.AiChatLaunchRequest
import com.torachen.mote.ui.MainActivity

/**
 * 悬浮在其它应用上层的 AI 面板。
 *
 * Activity 使用 Theme.Mote.Floating，窗口本身透明且无背景，只有内部的 MIUIX 卡片可见。
 */
class AiFloatingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setDimAmount(0f)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        AiChatStore.ensureLoaded(this)
        val autoPrompt = intent?.getStringExtra(AiFloatingController.EXTRA_PROMPT)
        val startNewConversation = intent?.getBooleanExtra(
            AiFloatingController.EXTRA_NEW_CONVERSATION,
            false,
        ) == true
        setContent {
            FloatingMiuixTheme {
                AiFloatingPanel(
                    onClose = { finish() },
                    autoPrompt = autoPrompt,
                    startNewConversation = startNewConversation,
                )
            }
        }
        val metrics = resources.displayMetrics
        val width = minOf(dp(348), (metrics.widthPixels - dp(24)).coerceAtLeast(dp(220)))
        val height = minOf(
            dp(548),
            (metrics.heightPixels * 0.92f).toInt().coerceAtLeast(dp(260)),
        )
        window.setLayout(width, height)
        window.setGravity(Gravity.CENTER)
        setFinishOnTouchOutside(true)
        AiFloatingBubbleService.setBubbleVisible(false)
    }

    override fun onDestroy() {
        AiFloatingBubbleService.setBubbleVisible(true)
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}

@Composable
private fun FloatingMiuixTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
    }
    val mode = prefs.getInt("ui_color_mode", 0)
    val dark = when (mode) {
        1 -> false
        2, 3 -> true
        else -> isSystemInDarkTheme()
    }
    val colors = remember(dark) {
        if (dark) darkColorScheme() else lightColorScheme()
    }
    MiuixTheme(colors = colors) { content() }
}

@Composable
private fun AiFloatingPanel(
    onClose: () -> Unit,
    autoPrompt: String? = null,
    startNewConversation: Boolean = false,
) {
    val context = LocalContext.current
    val messages = AiChatStore.messages
    val visibleMessages = messages.takeLast(30)
    val provider = AiConversationSettings.providerFor(AiChatStore.activeConversationId)
    val listState = rememberLazyListState()
    var input by remember {
        mutableStateOf(AiDraftStore.get(context, AiChatStore.activeConversationId))
    }
    var editingMessage by remember { mutableStateOf<AiUiMessage?>(null) }
    var editText by remember { mutableStateOf("") }
    var dragUp by remember { mutableFloatStateOf(0f) }
    val density = LocalDensity.current
    val openThreshold = with(density) { 72.dp.toPx() }
    val secondary = MiuixTheme.colorScheme.onSurfaceVariantSummary

    LaunchedEffect(AiChatStore.activeConversationId) {
        input = AiDraftStore.get(context, AiChatStore.activeConversationId)
    }

    fun setInput(value: String) {
        input = value
        AiDraftStore.set(context, AiChatStore.activeConversationId, value)
    }

    LaunchedEffect(Unit) {
        if (startNewConversation) {
            AiChatStore.newConversation()
        }
        val prompt = autoPrompt?.trim()
        if (!prompt.isNullOrEmpty() && !AiChatStore.streaming) {
            AiChatStore.send(context, prompt, emptyList())
            AiDraftStore.remove(context, AiChatStore.activeConversationId)
            input = ""
        }
    }

    val lastMessage = messages.lastOrNull()
    val signal = messages.size * 1000 +
            (lastMessage?.content?.length ?: 0) +
            (lastMessage?.reasoning?.length ?: 0) +
            (lastMessage?.steps?.size ?: 0) * 37
    LaunchedEffect(signal) {
        if (visibleMessages.isNotEmpty()) {
            listState.scrollToItem(visibleMessages.lastIndex)
        }
    }

    fun submit() {
        if (AiChatStore.streaming) return
        val text = input.trim()
        if (text.isEmpty()) return
        if (provider == null || !provider.isConfigured) {
            Toast.makeText(context, "请先在主应用中配置 AI 服务", Toast.LENGTH_SHORT).show()
            return
        }
        AiChatStore.send(context, text, emptyList())
        AiDraftStore.remove(context, AiChatStore.activeConversationId)
        input = ""
    }

    Box(modifier = Modifier.fillMaxSize().imePadding()) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .graphicsLayer {
                    val progress = (dragUp / openThreshold).coerceIn(0f, 1.4f)
                    translationY = -dragUp * 0.08f
                    scaleX = 1f + progress * 0.015f
                    scaleY = 1f + progress * 0.015f
                },
            cornerRadius = 28.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 6.dp, top = 10.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Mote AI",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MiuixTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = provider?.let {
                                "${it.name} · ${it.displayModelName(it.model)}" +
                                        if (AiSettings.agentEnabled) " · 自动操作" else ""
                            } ?: "尚未配置服务",
                            fontSize = 11.sp,
                            color = secondary,
                            maxLines = 1,
                        )
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = MiuixIcons.Close,
                            contentDescription = "关闭悬浮助手",
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MiuixTheme.colorScheme.dividerLine),
                )

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(
                        visibleMessages,
                        key = { "${AiChatStore.activeConversationId}_${it.id}" },
                    ) { message ->
                        AiMessageBubble(
                            message = message,
                            widthFraction = 0.78f,
                            aiBubbleColor = MiuixTheme.colorScheme.secondaryContainer,
                            onRefresh = { AiChatStore.regenerate(message.id) },
                            onEdit = {
                                editingMessage = message
                                editText = message.content
                            },
                        )
                    }
                    if (visibleMessages.isEmpty()) {
                        item {
                            Text(
                                text = "可以直接向 AI 提问，也可以开启自动操作让它操控手机。",
                                fontSize = 13.sp,
                                color = secondary,
                                modifier = Modifier.padding(16.dp),
                            )
                        }
                    }
                }

                Box(modifier = Modifier.padding(vertical = 2.dp)) {
                    AiQuickActionBar(onOpenSettings = { onClose() })
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        value = input,
                        onValueChange = { setInput(it) },
                        modifier = Modifier.weight(1f),
                        label = "输入消息…",
                        useLabelAsPlaceholder = true,
                        maxLines = 4,
                        minLines = 1,
                        colors = top.yukonga.miuix.kmp.basic.TextFieldDefaults.textFieldColors(
                            borderColor = androidx.compose.ui.graphics.Color.Transparent,
                        ),
                        cursorBrush = SolidColor(MiuixTheme.colorScheme.primary),
                    )
                    Spacer(Modifier.width(8.dp))
                    val streaming = AiChatStore.streaming
                    val active = streaming || input.isNotBlank()
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                if (active) MiuixTheme.colorScheme.primary
                                else MiuixTheme.colorScheme.secondaryContainer,
                            )
                            .clickable(enabled = active) {
                                if (streaming) AiChatStore.stop() else submit()
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (streaming) MiuixIcons.Pause else MiuixIcons.Send,
                            contentDescription = if (streaming) "停止" else "发送",
                            tint = if (active) MiuixTheme.colorScheme.onPrimary
                            else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .pointerInput(openThreshold) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    if (dragUp >= openThreshold) {
                                        AiChatLaunchRequest.pending = true
                                        context.startActivity(
                                            Intent(context, MainActivity::class.java)
                                                .addFlags(
                                                    Intent.FLAG_ACTIVITY_NEW_TASK or
                                                            Intent.FLAG_ACTIVITY_CLEAR_TOP,
                                                ),
                                        )
                                        (context as? Activity)?.finish()
                                    }
                                    dragUp = 0f
                                },
                                onDragCancel = { dragUp = 0f },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    dragUp = (dragUp - dragAmount)
                                        .coerceIn(0f, openThreshold * 1.4f)
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(38.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.45f),
                            ),
                    )
                }
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
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

@Composable
private fun AiFloatingBubble(message: AiUiMessage) {
    val isUser = message.isUser
    val bubbleColor = if (isUser) {
        MiuixTheme.colorScheme.primary
    } else {
        MiuixTheme.colorScheme.surfaceContainer
    }
    val contentColor = if (isUser) MiuixTheme.colorScheme.onPrimary
    else MiuixTheme.colorScheme.onSurface
    val secondary = if (isUser) MiuixTheme.colorScheme.onPrimary.copy(alpha = 0.72f)
    else MiuixTheme.colorScheme.onSurfaceVariantSummary
    val palette = AiMarkdownPalette(
        text = contentColor,
        secondary = secondary,
        accent = if (isUser) androidx.compose.ui.graphics.Color.White
        else MiuixTheme.colorScheme.primary,
        codeBackground = if (isUser) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.18f)
        else MiuixTheme.colorScheme.secondaryContainer,
        codeText = contentColor,
        divider = if (isUser) androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f)
        else MiuixTheme.colorScheme.outline,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .widthInMax(isUser)
                .clip(
                    if (isUser) RoundedCornerShape(16.dp, 16.dp, 5.dp, 16.dp)
                    else RoundedCornerShape(16.dp, 16.dp, 16.dp, 5.dp),
                )
                .background(bubbleColor)
                .padding(horizontal = 12.dp, vertical = 9.dp),
        ) {
            if (message.content.isNotBlank()) {
                if (isUser) {
                    Text(
                        text = message.content,
                        fontSize = 14.sp,
                        color = contentColor,
                        lineHeight = 20.sp,
                    )
                } else {
                    AiMarkdown(
                        source = message.content,
                        palette = palette,
                        baseStyle = MiuixTheme.textStyles.body2.copy(color = contentColor),
                        baseFontSize = MiuixTheme.textStyles.body2.fontSize,
                    )
                }
            } else if (!isUser && message.streaming) {
                Text(text = "正在生成…", fontSize = 13.sp, color = secondary)
            }
            message.error?.let { error ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = error,
                    fontSize = 12.sp,
                    color = if (isUser) androidx.compose.ui.graphics.Color.White
                    else MiuixTheme.colorScheme.error,
                )
            }
        }
    }
}

private fun Modifier.widthInMax(user: Boolean): Modifier =
    if (user) this.widthIn(max = 260.dp) else this.widthIn(max = 280.dp)
