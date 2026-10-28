package com.torachen.mote.ui.filemanager

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon as M3Icon
import androidx.compose.material3.IconButton as M3IconButton
import androidx.compose.material3.CircularProgressIndicator as M3CircularProgressIndicator
import androidx.compose.material3.MaterialTheme as M3MaterialTheme
import androidx.compose.material3.Text as M3Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torachen.mote.ui.LocalUiMode
import com.torachen.mote.ui.UiMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Redo
import top.yukonga.miuix.kmp.icon.extended.Search
import top.yukonga.miuix.kmp.icon.extended.Undo
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowCascadingListPopup
import java.io.File
import java.nio.charset.Charset

private data class LoadedText(val text: String, val charset: Charset)

@Composable
internal fun LocalTextEditorPage(
    request: LocalFileViewerRequest.Text,
    onBack: () -> Unit,
) {
    val isMaterial = LocalUiMode.current == UiMode.Material
    val surface = if (isMaterial) M3MaterialTheme.colorScheme.surface else MiuixTheme.colorScheme.surface
    var loaded by remember(request.path) { mutableStateOf<LoadedText?>(null) }
    var failed by remember(request.path) { mutableStateOf(false) }

    LaunchedEffect(request.path) {
        val result = withContext(Dispatchers.IO) { readTextFile(File(request.path)) }
        if (result == null) failed = true else loaded = result
    }

    if (loaded == null) {
        Column(modifier = Modifier.fillMaxSize().background(surface)) {
            if (isMaterial) {
                FileViewerMaterialTopBar(title = request.title, onBack = onBack)
            } else {
                SmallTopAppBar(
                    title = request.title,
                    color = surface,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = MiuixIcons.ChevronBackward,
                                contentDescription = "返回",
                                tint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                    },
                )
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (!failed) {
                        if (isMaterial) {
                            M3CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 2.5.dp,
                            )
                        } else {
                            CircularProgressIndicator(progress = 0f, size = 28.dp)
                        }
                        Text(
                            text = "正在打开…",
                            color = if (isMaterial) M3MaterialTheme.colorScheme.onSurfaceVariant
                            else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    } else {
                        Text(
                            text = "无法打开该文本文件",
                            color = if (isMaterial) M3MaterialTheme.colorScheme.onSurfaceVariant
                            else MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                    }
                }
            }
        }
        return
    }

    TextEditorContent(request, loaded!!, onBack)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TextEditorContent(
    request: LocalFileViewerRequest.Text,
    loaded: LoadedText,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val isMaterial = LocalUiMode.current == UiMode.Material
    val surface = if (isMaterial) M3MaterialTheme.colorScheme.surface else MiuixTheme.colorScheme.surface
    val onSurface = if (isMaterial) M3MaterialTheme.colorScheme.onSurface else MiuixTheme.colorScheme.onSurface
    val summaryColor = if (isMaterial) M3MaterialTheme.colorScheme.onSurfaceVariant
    else MiuixTheme.colorScheme.onSurfaceVariantSummary
    val dividerColor = onSurface.copy(alpha = 0.1f)
    val containerColor = if (isMaterial) M3MaterialTheme.colorScheme.surfaceContainerHigh
    else MiuixTheme.colorScheme.surfaceContainer
    val primary = if (isMaterial) M3MaterialTheme.colorScheme.primary else MiuixTheme.colorScheme.primary
    val density = LocalDensity.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val editorState = rememberTextFieldState(loaded.text, TextRange(0))
    val scrollState = rememberScrollState()
    val lineHeight = 22.sp
    val lineHeightPx = with(density) { lineHeight.toPx() }

    var editing by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var replacement by remember { mutableStateOf("") }

    val content = editorState.text.toString()
    val lineCount = content.count { it == '\n' } + 1
    val numbers = remember(content) { (1..lineCount).joinToString("\n") }
    val gutterWidth = remember(lineCount) { (18 + lineCount.toString().length * 9).dp }
    val undoState = editorState.undoState

    val editorStyle = remember(onSurface, lineHeight) {
        TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            lineHeight = lineHeight,
            color = onSurface,
        )
    }
    val textMeasurer = rememberTextMeasurer()
    val longestLine = remember(content) { content.lineSequence().maxByOrNull { it.length } ?: "" }
    val contentWidth = remember(longestLine, editorStyle, density) {
        with(density) { textMeasurer.measure(longestLine, editorStyle).size.width.toDp() } + 32.dp
    }
    val horizontalScroll = rememberScrollState()

    BackHandler(enabled = editing) {
        editing = false
        searchOpen = false
        focusManager.clearFocus()
    }

    LaunchedEffect(editing) {
        if (editing) focusRequester.requestFocus() else focusManager.clearFocus()
    }

    fun save() {
        val text = editorState.text.toString()
        scope.launch {
            val ok = withContext(Dispatchers.IO) {
                runCatching { File(request.path).writeBytes(text.toByteArray(loaded.charset)) }.isSuccess
            }
            Toast.makeText(context, if (ok) "已保存" else "保存失败", Toast.LENGTH_SHORT).show()
        }
    }

    fun scrollToOffset(offset: Int) {
        val line = content.take(offset.coerceIn(0, content.length)).count { it == '\n' }
        scope.launch { scrollState.animateScrollTo((line * lineHeightPx).toInt()) }
    }

    fun select(start: Int, end: Int) {
        editorState.edit { selection = TextRange(start, end) }
        focusRequester.requestFocus()
        scrollToOffset(start)
    }

    fun findNext() {
        val q = query
        if (q.isEmpty()) return
        val text = editorState.text.toString()
        val from = editorState.selection.max.coerceAtMost(text.length)
        var at = text.indexOf(q, from, ignoreCase = true)
        if (at < 0) at = text.indexOf(q, ignoreCase = true)
        if (at >= 0) select(at, at + q.length)
    }

    fun findPrev() {
        val q = query
        if (q.isEmpty()) return
        val text = editorState.text.toString()
        val from = (editorState.selection.min - 1).coerceAtLeast(0)
        var at = text.lastIndexOf(q, from, ignoreCase = true)
        if (at < 0) at = text.lastIndexOf(q, text.length - 1, ignoreCase = true)
        if (at >= 0) select(at, at + q.length)
    }

    fun replaceCurrent() {
        val q = query
        if (q.isEmpty()) return
        val text = editorState.text.toString()
        val selection = editorState.selection
        if (selection.min in 0..text.length &&
            selection.max in selection.min..text.length &&
            text.substring(selection.min, selection.max).equals(q, ignoreCase = true)
        ) {
            editorState.edit { replace(selection.min, selection.max, replacement) }
        }
        findNext()
    }

    fun replaceAll() {
        val q = query
        if (q.isEmpty()) return
        val text = editorState.text.toString()
        val starts = mutableListOf<Int>()
        var index = 0
        while (index <= text.length - q.length) {
            val at = text.indexOf(q, index, ignoreCase = true)
            if (at < 0) break
            starts.add(at)
            index = at + q.length
        }
        if (starts.isEmpty()) return
        editorState.edit {
            starts.asReversed().forEach { at ->
                replace(at, at + q.length, replacement)
            }
        }
        Toast.makeText(context, "已替换 ${starts.size} 处", Toast.LENGTH_SHORT).show()
    }

    Column(modifier = Modifier.fillMaxSize().background(surface)) {
        if (editing) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surface)
                    .statusBarsPadding()
                    .height(56.dp)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isMaterial) {
                    M3IconButton(
                        onClick = {
                            editing = false
                            searchOpen = false
                            focusManager.clearFocus()
                        },
                    ) {
                        M3Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "退出编辑",
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            editing = false
                            searchOpen = false
                            focusManager.clearFocus()
                        },
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Close,
                            contentDescription = "退出编辑",
                            tint = onSurface,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                EditorActions(
                    canUndo = undoState.canUndo,
                    canRedo = undoState.canRedo,
                    onUndo = { undoState.undo() },
                    onRedo = { undoState.redo() },
                    onSave = { save() },
                    onSearch = { searchOpen = true },
                    tint = onSurface,
                    disabledTint = onSurface.copy(alpha = 0.3f),
                    isMaterial = isMaterial,
                )
            }
        } else {
            if (isMaterial) {
                FileViewerMaterialTopBar(
                    title = request.title,
                    onBack = onBack,
                    actions = {
                        M3IconButton(onClick = { editing = true }) {
                            M3Icon(Icons.Filled.Edit, contentDescription = "编辑")
                        }
                    },
                )
            } else {
                SmallTopAppBar(
                    title = request.title,
                    color = surface,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = MiuixIcons.ChevronBackward,
                                contentDescription = "返回",
                                tint = onSurface,
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { editing = true }) {
                            Icon(
                                imageVector = MiuixIcons.Edit,
                                contentDescription = "编辑",
                                tint = onSurface,
                            )
                        }
                    },
                )
            }
        }

        if (editing && searchOpen) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(containerColor)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CompactInput(
                        value = query,
                        onValueChange = { query = it },
                        hint = "查找",
                        modifier = Modifier.weight(1f),
                        onSurface = onSurface,
                        summaryColor = summaryColor,
                        container = surface,
                        cursor = primary,
                    )
                    IconButton(onClick = { searchOpen = false }) {
                        Icon(
                            imageVector = MiuixIcons.Close,
                            contentDescription = "关闭查找",
                            tint = onSurface,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CompactInput(
                        value = replacement,
                        onValueChange = { replacement = it },
                        hint = "替换为",
                        modifier = Modifier.weight(1f),
                        onSurface = onSurface,
                        summaryColor = summaryColor,
                        container = surface,
                        cursor = primary,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SearchAction("上一个", primary) { findPrev() }
                    SearchAction("下一个", primary) { findNext() }
                    SearchAction("替换", primary) { replaceCurrent() }
                    SearchAction("全部替换", primary) { replaceAll() }
                }
            }
        }

        Row(modifier = Modifier.fillMaxSize().background(surface)) {
            Box(
                modifier = Modifier
                    .width(gutterWidth)
                    .fillMaxHeight()
                    .clipToBounds(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .verticalScroll(scrollState, enabled = false)
                        .padding(top = 8.dp),
                ) {
                    Text(
                        text = numbers,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 6.dp),
                        textAlign = TextAlign.End,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        lineHeight = lineHeight,
                        color = summaryColor,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(dividerColor),
            )
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                val fieldWidth = maxOf(maxWidth, contentWidth)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScroll),
                ) {
                    BasicTextField(
                        state = editorState,
                        modifier = Modifier
                            .width(fieldWidth)
                            .fillMaxHeight()
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .focusRequester(focusRequester),
                        readOnly = !editing,
                        textStyle = editorStyle,
                        lineLimits = TextFieldLineLimits.MultiLine(),
                        scrollState = scrollState,
                        cursorBrush = SolidColor(primary),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EditorActions(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onSave: () -> Unit,
    onSearch: () -> Unit,
    tint: Color,
    disabledTint: Color,
    isMaterial: Boolean,
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val capacity = ((screenWidth - 56 - 8) / 44).coerceAtLeast(0)
    val showMore = capacity < 4
    val visibleCount = if (showMore) (capacity - 1).coerceAtLeast(0) else 4

    val actions = listOf(
        EditorAction("撤回", MiuixIcons.Undo, canUndo, onUndo),
        EditorAction("重做", MiuixIcons.Redo, canRedo, onRedo),
        EditorAction("保存", MiuixIcons.Ok, true, onSave),
        EditorAction("查找", MiuixIcons.Search, true, onSearch),
    )

    Row(verticalAlignment = Alignment.CenterVertically) {
        actions.take(visibleCount).forEach { action ->
            EditorActionButton(action, tint, disabledTint, isMaterial)
        }
        if (showMore) {
            var showMenu by remember { mutableStateOf(false) }
            Box {
                EditorActionButton(
                    action = EditorAction("更多", MiuixIcons.More, true, { showMenu = true }),
                    tint = tint,
                    disabledTint = disabledTint,
                    isMaterial = isMaterial,
                )
                if (isMaterial) {
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                    ) {
                        actions.drop(visibleCount).forEach { action ->
                            DropdownMenuItem(
                                text = { M3Text(action.label) },
                                onClick = {
                                    showMenu = false
                                    action.onClick()
                                },
                            )
                        }
                    }
                } else {
                    WindowCascadingListPopup(
                        show = showMenu,
                        entries = listOf(
                            DropdownEntry(
                                items = actions.drop(visibleCount).map { action ->
                                    DropdownItem(
                                        text = action.label,
                                        onClick = {
                                            showMenu = false
                                            action.onClick()
                                        },
                                    )
                                },
                            ),
                        ),
                        onDismissRequest = { showMenu = false },
                    )
                }
            }
        }
    }
}

private data class EditorAction(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val enabled: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun EditorActionButton(
    action: EditorAction,
    tint: Color,
    disabledTint: Color,
    isMaterial: Boolean,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(enabled = action.enabled, onClick = action.onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isMaterial) {
            M3Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                tint = if (action.enabled) tint else disabledTint,
                modifier = Modifier.size(22.dp),
            )
        } else {
            Icon(
                imageVector = action.icon,
                contentDescription = action.label,
                tint = if (action.enabled) tint else disabledTint,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun CompactInput(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier,
    onSurface: Color,
    summaryColor: Color,
    container: Color,
    cursor: Color,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(fontSize = 14.sp, color = onSurface),
        cursorBrush = SolidColor(cursor),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(container)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(text = hint, fontSize = 14.sp, color = summaryColor)
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun SearchAction(label: String, color: Color, onClick: () -> Unit) {
    Text(
        text = label,
        fontSize = 13.sp,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
    )
}

private fun readTextFile(file: File): LoadedText? = runCatching {
    val bytes = file.readBytes()
    val (text, charset) = decodeText(bytes)
    LoadedText(text, charset)
}.getOrNull()

private fun decodeText(bytes: ByteArray): Pair<String, Charset> {
    if (bytes.size >= 3 &&
        bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()
    ) {
        return String(bytes, 3, bytes.size - 3, Charsets.UTF_8) to Charsets.UTF_8
    }
    if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) {
        return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE) to Charsets.UTF_16LE
    }
    if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) {
        return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE) to Charsets.UTF_16BE
    }
    val utf8 = String(bytes, Charsets.UTF_8)
    if ('\uFFFD' !in utf8) return utf8 to Charsets.UTF_8
    val gbk = runCatching { Charset.forName("GBK") }.getOrNull() ?: Charsets.UTF_8
    return String(bytes, gbk) to gbk
}
