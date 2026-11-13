@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.ui.ai

import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torachen.mote.ui.SearchableItem
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TabRowWithContour
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.MindMap
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowBottomSheet
import top.yukonga.miuix.kmp.window.WindowDialog

/** 进入编辑页时携带的参数（与项目里其它 NavArgs 一致，进程内共享）。 */
object AiProviderNav {
    var providerId: String? = null
    var presetId: String? = null

    fun openExisting(id: String) {
        providerId = id
        presetId = null
    }

    fun openPreset(id: String) {
        providerId = null
        presetId = id
    }
}

/**
 * 「AI 服务」列表页。
 *
 * 结构与「脚本」「默认搜索引擎」页一致：
 * - 没有服务时，内容区只显示空状态占位；
 * - 底部悬浮胶囊工具栏提供添加入口；
 * - 点击添加后从底部弹出「添加 AI 服务」，用 Tab + 大卡片展示预设 / 自定义两种方式。
 */
@Composable
fun AiProvidersPage(
    onBack: () -> Unit,
    onEditProvider: (String) -> Unit,
    onAddPreset: (String) -> Unit,
) {
    val context = LocalContext.current
    AiProviderStore.ensureLoaded(context)

    val providers = AiProviderStore.providers
    val activeId = AiProviderStore.activeId
    var showAddSheet by remember { mutableStateOf(false) }

    AiSettingsScaffold(
        title = "AI 服务",
        pageRoute = "ai_providers",
        onBack = onBack,
        floatingBottomBar = {
            AiAddServiceCapsule(onClick = { showAddSheet = true })
        },
    ) {
        if (providers.isEmpty()) {
            AiServicesEmptyPlaceholder()
        } else {
            SmallTitle("AI 服务")
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
            ) {
                providers.forEach { provider ->
                    SearchableItem(
                        title = provider.name,
                        pageRoute = "ai_providers",
                        pageTitle = "AI 服务",
                        itemKey = "ai_provider_${provider.id}",
                    ) {
                        val preset = AiPresets.byId(provider.presetId)
                        BasicComponent(
                            title = provider.name,
                            summary = provider.summary.ifBlank { "尚未填写模型" },
                            startAction = {
                                Box(modifier = Modifier.padding(end = 10.dp)) {
                                    AiBrandIcon(
                                        text = provider.name,
                                        color = preset?.let { Color(it.brandColor) }
                                            ?: MiuixTheme.colorScheme.primary,
                                        iconUrl = preset?.iconUrl,
                                    )
                                }
                            },
                            endActions = {
                                if (provider.id == activeId) {
                                    Icon(
                                        imageVector = MiuixIcons.Ok,
                                        contentDescription = "当前使用",
                                        tint = MiuixTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            },
                            onClick = { onEditProvider(provider.id) },
                        )
                    }
                }
            }
        }

        // 给底部胶囊工具栏留出滚动空间
        Spacer(Modifier.height(88.dp))
    }

    AiAddServiceSheet(
        show = showAddSheet,
        onDismiss = { showAddSheet = false },
        onAddPreset = { id ->
            showAddSheet = false
            onAddPreset(id)
        },
        onCustomSaved = { showAddSheet = false },
    )
}

@Composable
private fun AiServicesEmptyPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 56.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = MiuixIcons.MindMap,
                    contentDescription = null,
                    tint = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.size(48.dp),
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "暂无 AI 服务",
                    fontSize = 14.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
        }
    }
}

/** 与「脚本」页一致的底部悬浮胶囊工具栏：只有一个添加按钮。 */
@Composable
private fun AiAddServiceCapsule(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(
                bottom = WindowInsets.navigationBars
                    .asPaddingValues()
                    .calculateBottomPadding() + 24.dp,
            )
            .padding(horizontal = 24.dp)
            .height(56.dp)
            .graphicsLayer {
                shape = RoundedCornerShape(28.dp)
                shadowElevation = 24.dp.toPx()
                ambientShadowColor = Color.Black.copy(alpha = 0.35f)
                spotShadowColor = Color.Transparent
            }
            .clip(RoundedCornerShape(28.dp))
            .background(MiuixTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = MiuixIcons.Add,
                contentDescription = "添加 AI 服务",
                tint = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

/**
 * 「添加 AI 服务」底部弹窗，结构和历史记录弹窗一致：
 * 标题 + TabRowWithContour + 一张大 Card。
 */
@Composable
private fun AiAddServiceSheet(
    show: Boolean,
    onDismiss: () -> Unit,
    onAddPreset: (String) -> Unit,
    onCustomSaved: () -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    LaunchedEffect(show) {
        if (show) tab = 0
    }
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    WindowBottomSheet(
        show = show,
        title = "添加 AI 服务",
        backgroundColor = MiuixTheme.colorScheme.surface,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(durationMillis = 260))
                .padding(bottom = if (navBottom > 12.dp) navBottom else 12.dp),
        ) {
            TabRowWithContour(
                tabs = listOf("预设 AI 服务", "自定义 AI 服务"),
                selectedTabIndex = tab,
                onTabSelected = { tab = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 8.dp),
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
            ) {
                if (tab == 0) {
                    AiPresetSheetList(
                        onAddPreset = onAddPreset,
                    )
                } else {
                    AiCustomProviderForm(
                        show = show,
                        onSaved = onCustomSaved,
                    )
                }
            }
        }
    }
}

@Composable
private fun AiPresetSheetList(
    onAddPreset: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        AiPresets.all.forEach { preset ->
            BasicComponent(
                title = preset.name,
                summary = buildString {
                    append(preset.protocol.label)
                    if (preset.defaultModel.isNotBlank()) {
                        append(" · ").append(autoModelDisplayName(preset.id, preset.defaultModel))
                    }
                },
                startAction = {
                    Box(modifier = Modifier.padding(end = 10.dp)) {
                        AiBrandIcon(
                            text = preset.name,
                            color = Color(preset.brandColor),
                            iconUrl = preset.iconUrl,
                        )
                    }
                },
                onClick = { onAddPreset(preset.id) },
            )
        }
    }
}

@Composable
private fun AiCustomProviderForm(
    show: Boolean,
    onSaved: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var providerId by remember { mutableStateOf(AiProviderStore.newCustomId()) }
    var name by remember { mutableStateOf("自定义服务") }
    var protocol by remember { mutableStateOf(AiProtocol.OpenAi) }
    var baseUrl by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var supportsReasoning by remember { mutableStateOf(false) }
    val models = remember { mutableStateListOf<String>() }
    val modelAliases = remember { mutableStateMapOf<String, String>() }
    val modelSpecs = remember { mutableStateMapOf<String, AiModelSpec>() }
    var fetching by remember { mutableStateOf(false) }
    var testMessage by remember { mutableStateOf<String?>(null) }
    var showModelDialog by remember { mutableStateOf(false) }
    var editingModel by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(show) {
        if (show) {
            providerId = AiProviderStore.newCustomId()
            name = "自定义服务"
            protocol = AiProtocol.OpenAi
            baseUrl = ""
            apiKey = ""
            model = ""
            supportsReasoning = false
            models.clear()
            modelAliases.clear()
            modelSpecs.clear()
            fetching = false
            testMessage = null
            showModelDialog = false
            editingModel = null
        }
    }

    fun buildConfig(): AiProviderConfig = AiProviderConfig(
        id = providerId,
        presetId = null,
        name = name.trim(),
        protocol = protocol,
        baseUrl = baseUrl.trim(),
        apiKey = apiKey.trim(),
        model = model.trim(),
        models = models.toList(),
        supportsVision = false,
        supportsReasoning = supportsReasoning,
        modelAliases = modelAliases.toMap(),
        modelSpecs = modelSpecs.toMap(),
    )

    fun probe() {
        if (fetching) return
        val config = buildConfig()
        if (config.baseUrl.isBlank()) {
            testMessage = "请先填写 API 地址"
            return
        }
        fetching = true
        testMessage = null
        scope.launch {
            AiClient.fetchModels(config)
                .onSuccess { fetched ->
                    if (fetched.isEmpty()) {
                        testMessage = "连接成功，但服务没有返回模型列表，可点击「添加模型」手动填写"
                    } else {
                        models.clear()
                        models.addAll(fetched.take(200))
                        if (model.isBlank()) model = fetched.first()
                        testMessage = "连接成功，获取到 ${fetched.size} 个模型"
                    }
                }
                .onFailure { error ->
                    testMessage = aiErrorMessage(error)
                }
            fetching = false
        }
    }

    fun save() {
        val config = buildConfig()
        when {
            config.name.isBlank() -> {
                Toast.makeText(context, "请填写提供商", Toast.LENGTH_SHORT).show()
                return
            }

            config.baseUrl.isBlank() -> {
                Toast.makeText(context, "请填写 API 地址", Toast.LENGTH_SHORT).show()
                return
            }

            !config.baseUrl.startsWith("http://") &&
                    !config.baseUrl.startsWith("https://") -> {
                Toast.makeText(
                    context,
                    "API 地址需要以 http:// 或 https:// 开头",
                    Toast.LENGTH_SHORT,
                ).show()
                return
            }

            config.model.isBlank() -> {
                Toast.makeText(context, "请添加或选择一个模型", Toast.LENGTH_SHORT).show()
                return
            }

            config.apiKey.isBlank() && !config.isLocalEndpoint -> {
                Toast.makeText(context, "请填写 API Key", Toast.LENGTH_SHORT).show()
                return
            }
        }
        AiProviderStore.upsert(context, config, makeActive = true)
        Toast.makeText(context, "已保存并设为当前服务", Toast.LENGTH_SHORT).show()
        onSaved()
    }

    val secondary = MiuixTheme.colorScheme.onSurfaceVariantSummary

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = "提供商",
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        WindowDropdownPreference(
            items = AiProtocol.entries.map { it.label },
            selectedIndex = AiProtocol.entries.indexOf(protocol).coerceAtLeast(0),
            title = "接口协议",
            summary = "选择服务商提供的接口格式",
            insideMargin = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
            onSelectedIndexChange = { index ->
                protocol = AiProtocol.entries.getOrElse(index) { AiProtocol.OpenAi }
                testMessage = null
            },
        )

        TextField(
            value = baseUrl,
            onValueChange = { baseUrl = it },
            label = "API 地址",
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        TextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = "API Key",
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = { probe() },
            enabled = !fetching,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                color = MiuixTheme.colorScheme.secondaryContainer,
                contentColor = MiuixTheme.colorScheme.onSurface,
            ),
            insideMargin = PaddingValues(0.dp),
            cornerRadius = 16.dp,
        ) {
            if (fetching) {
                CircularProgressIndicator(size = 14.dp)
                Spacer(Modifier.width(8.dp))
            }
            Text(if (fetching) "正在连接…" else "获取模型列表")
        }

        if (models.isNotEmpty()) {
            WindowDropdownPreference(
                items = models.map { id -> "${modelAliases[id] ?: autoModelDisplayName(null, id)} · $id" },
                selectedIndex = models.indexOf(model).coerceAtLeast(0),
                title = "选择模型",
                summary = "从已获取的模型中选择",
                insideMargin = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
                onSelectedIndexChange = { index ->
                    models.getOrNull(index)?.let { model = it }
                },
            )
        }
        if (model.isNotBlank()) {
            BasicComponent(
                title = "编辑当前模型",
                summary = modelAliases[model] ?: model,
                onClick = {
                    editingModel = model
                    showModelDialog = true
                },
            )
        }
        BasicComponent(
            title = "添加模型",
            summary = "手动填写模型 ID 与运行参数",
            onClick = {
                editingModel = null
                showModelDialog = true
            },
        )

        if (testMessage != null) {
            Text(
                text = testMessage ?: "",
                fontSize = 12.sp,
                color = if (testMessage?.startsWith("连接成功") == true) {
                    MiuixTheme.colorScheme.primary
                } else {
                    MiuixTheme.colorScheme.error
                },
                lineHeight = 17.sp,
            )
        }

        Text(
            text = "接口地址通常以 /v1 或 /v1beta 结尾，不要填写 /chat/completions 之类的具体路径。",
            fontSize = 12.sp,
            color = secondary,
            lineHeight = 17.sp,
        )

        if (protocol == AiProtocol.Anthropic || protocol == AiProtocol.Gemini) {
            SwitchPreference(
                title = "思考模式（推理）",
                summary = "请求模型返回思考过程；仅对支持推理的模型有效",
                checked = supportsReasoning,
                onCheckedChange = { supportsReasoning = it },
                insideMargin = PaddingValues(horizontal = 0.dp, vertical = 6.dp),
            )
        }

        Spacer(Modifier.height(2.dp))

        Button(
            onClick = { save() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(
                color = MiuixTheme.colorScheme.primary,
                contentColor = Color.White,
            ),
            insideMargin = PaddingValues(0.dp),
            cornerRadius = 16.dp,
        ) { Text("保存并使用") }
    }

    AiModelEditDialog(
        show = showModelDialog,
        initialId = editingModel,
        initialName = editingModel?.let {
            modelAliases[it] ?: autoModelDisplayName(null, it)
        }.orEmpty(),
        initialSpec = editingModel?.let { modelSpecs[it] } ?: AiModelSpec(),
        onDismiss = { showModelDialog = false },
        onSave = { id, displayName, spec ->
            val targetId = id.trim()
            if (targetId.isNotBlank()) {
                val oldId = editingModel
                if (oldId != null && oldId != targetId) {
                    val index = models.indexOf(oldId)
                    if (index >= 0) {
                        models[index] = targetId
                    } else if (targetId !in models) {
                        models.add(targetId)
                    }
                    if (model == oldId) model = targetId
                    modelAliases.remove(oldId)?.let { modelAliases[targetId] = it }
                    modelSpecs.remove(oldId)?.let { modelSpecs[targetId] = it }
                } else if (targetId !in models) {
                    models.add(targetId)
                }
                if (displayName.isBlank()) {
                    modelAliases.remove(targetId)
                } else {
                    modelAliases[targetId] = displayName.trim()
                }
                modelSpecs[targetId] = spec
                if (model.isBlank()) model = targetId
            }
            showModelDialog = false
        },
    )
}

/**
 * 服务编辑页。预设服务只差一个 API Key，自定义服务可配置全部字段。
 */
@Composable
fun AiProviderEditPage(onBack: () -> Unit) {
    val context = LocalContext.current
    AiProviderStore.ensureLoaded(context)

    val existing = AiProviderStore.providers.firstOrNull { it.id == AiProviderNav.providerId }
    val preset = AiPresets.byId(AiProviderNav.presetId) ?: AiPresets.byId(existing?.presetId)
    if (existing == null && preset == null && AiProviderNav.providerId != null) {
        // 记录已被删除（例如从搜索直接进入），回到列表
        LaunchedEffect(Unit) { onBack() }
        return
    }

    val providerId = remember {
        existing?.id
            ?: preset?.let { "${it.id}_${System.currentTimeMillis().toString(36)}" }
            ?: AiProviderStore.newCustomId()
    }

    var name by remember { mutableStateOf(existing?.name ?: preset?.name ?: "自定义服务") }
    var protocol by remember {
        mutableStateOf(existing?.protocol ?: preset?.protocol ?: AiProtocol.OpenAi)
    }
    var baseUrl by remember { mutableStateOf(existing?.baseUrl ?: preset?.baseUrl ?: "") }
    var apiKey by remember { mutableStateOf(existing?.apiKey ?: "") }
    var model by remember { mutableStateOf(existing?.model ?: preset?.defaultModel ?: "") }
    var supportsVision by remember {
        mutableStateOf(existing?.supportsVision ?: preset?.supportsVision ?: false)
    }
    var supportsReasoning by remember {
        mutableStateOf(existing?.supportsReasoning ?: preset?.supportsReasoning ?: false)
    }
    val models = remember {
        mutableStateListOf<String>().apply {
            addAll(existing?.models ?: preset?.models ?: emptyList())
        }
    }
    val modelAliases = remember {
        mutableStateMapOf<String, String>().apply {
            putAll(existing?.modelAliases ?: emptyMap())
        }
    }
    val modelSpecs = remember {
        mutableStateMapOf<String, AiModelSpec>().apply {
            putAll(existing?.modelSpecs ?: emptyMap())
        }
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var fetching by remember { mutableStateOf(false) }
    var testMessage by remember { mutableStateOf<String?>(null) }
    var showModelDialog by remember { mutableStateOf(false) }
    var editingModel by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    fun buildConfig(): AiProviderConfig = AiProviderConfig(
        id = providerId,
        presetId = preset?.id ?: existing?.presetId,
        name = name.trim(),
        protocol = protocol,
        baseUrl = baseUrl.trim(),
        apiKey = apiKey.trim(),
        model = model.trim(),
        models = models.toList(),
        supportsVision = supportsVision,
        supportsReasoning = supportsReasoning,
        modelAliases = modelAliases.toMap(),
        modelSpecs = modelSpecs.toMap(),
    )

    fun probe(updateModels: Boolean) {
        if (fetching) return
        val config = buildConfig()
        if (config.baseUrl.isBlank()) {
            testMessage = "请先填写 API 地址"
            return
        }
        fetching = true
        testMessage = null
        scope.launch {
            AiClient.fetchModels(config)
                .onSuccess { fetched ->
                    if (fetched.isEmpty()) {
                        testMessage = "连接成功，但服务没有返回模型列表，可点击「添加模型」手动填写"
                    } else {
                        if (updateModels) {
                            models.clear()
                            models.addAll(fetched.take(200))
                            if (model.isBlank() || model !in fetched) {
                                model = preferredModelId(fetched, preset?.id)
                            }
                        }
                        testMessage = "连接成功，获取到 ${fetched.size} 个模型"
                    }
                }
                .onFailure { error ->
                    testMessage = aiErrorMessage(error)
                }
            fetching = false
        }
    }

    fun save() {
        val config = buildConfig()
        when {
            config.name.isBlank() -> {
                Toast.makeText(context, "请填写提供商", Toast.LENGTH_SHORT).show()
                return
            }

            config.baseUrl.isBlank() -> {
                Toast.makeText(context, "请填写 API 地址", Toast.LENGTH_SHORT).show()
                return
            }

            !config.baseUrl.startsWith("http://") &&
                    !config.baseUrl.startsWith("https://") -> {
                Toast.makeText(
                    context,
                    "API 地址需要以 http:// 或 https:// 开头",
                    Toast.LENGTH_SHORT,
                ).show()
                return
            }

            config.model.isBlank() -> {
                Toast.makeText(context, "请添加或选择一个模型", Toast.LENGTH_SHORT).show()
                return
            }

            config.apiKey.isBlank() && !config.isLocalEndpoint -> {
                Toast.makeText(context, "请填写 API Key", Toast.LENGTH_SHORT).show()
                return
            }
        }
        AiProviderStore.upsert(context, config, makeActive = true)
        Toast.makeText(context, "已保存并设为当前服务", Toast.LENGTH_SHORT).show()
        onBack()
    }

    val successColor = MiuixTheme.colorScheme.primary
    val secondary = MiuixTheme.colorScheme.onSurfaceVariantSummary

    AiSettingsScaffold(
        title = if (existing == null) "添加 AI 服务" else "编辑 AI 服务",
        pageRoute = "ai_provider_edit",
        onBack = onBack,
    ) {
        if (preset == null) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(top = 12.dp, bottom = 12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "提供商",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    WindowDropdownPreference(
                        items = AiProtocol.entries.map { it.label },
                        selectedIndex = AiProtocol.entries.indexOf(protocol).coerceAtLeast(0),
                        title = "接口协议",
                        summary = "选择服务商提供的接口格式",
                        insideMargin = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
                        onSelectedIndexChange = { index ->
                            protocol = AiProtocol.entries.getOrElse(index) { AiProtocol.OpenAi }
                        },
                    )
                }
            }
        }

        SmallTitle("连接")

        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (preset == null) {
                    TextField(
                        value = baseUrl,
                        onValueChange = { baseUrl = it },
                        label = "API 地址",
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                TextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = "API Key",
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                val hint = preset?.keyHint
                    ?: "接口地址通常以 /v1 或 /v1beta 结尾，不要填写 /chat/completions 之类的具体路径。"
                Text(
                    text = hint,
                    fontSize = 12.sp,
                    color = secondary,
                    lineHeight = 17.sp,
                )
            }
        }

        SmallTitle("模型")

        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { probe(updateModels = true) },
                    enabled = !fetching,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                    insideMargin = PaddingValues(0.dp),
                    cornerRadius = 16.dp,
                ) {
                    if (fetching) {
                        CircularProgressIndicator(size = 14.dp)
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(if (fetching) "正在连接…" else "获取模型列表")
                }
                if (testMessage != null) {
                    Text(
                        text = testMessage ?: "",
                        fontSize = 12.sp,
                        color = if (testMessage?.startsWith("连接成功") == true) {
                            successColor
                        } else {
                            MiuixTheme.colorScheme.error
                        },
                        lineHeight = 17.sp,
                    )
                }
            }

            val visibleModels = models.take(80)
            val presetIdForAlias = preset?.id ?: existing?.presetId
            visibleModels.forEach { candidate ->
                val displayName = modelAliases[candidate]
                    ?: autoModelDisplayName(presetIdForAlias, candidate)
                BasicComponent(
                    title = displayName,
                    summary = if (candidate == model) {
                        "当前模型 · $candidate"
                    } else {
                        candidate
                    },
                    onClick = {
                        editingModel = candidate
                        showModelDialog = true
                    },
                )
            }
            BasicComponent(
                title = "添加模型",
                summary = "手动填写模型 ID 与运行参数",
                onClick = {
                    editingModel = null
                    showModelDialog = true
                },
            )
            if (models.size > visibleModels.size) {
                Text(
                    text = "仅显示前 ${visibleModels.size} 个模型，可手动添加",
                    fontSize = 12.sp,
                    color = secondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }

        if (protocol == AiProtocol.Anthropic || protocol == AiProtocol.Gemini) {
            SmallTitle("能力")

            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
            ) {
                SwitchPreference(
                    title = "思考模式（推理）",
                    summary = "请求模型返回思考过程；仅对支持推理的模型有效，不支持的模型请关闭",
                    checked = supportsReasoning,
                    onCheckedChange = { supportsReasoning = it },
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
            insideMargin = PaddingValues(0.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Button(
                    onClick = { save() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                    insideMargin = PaddingValues(0.dp),
                    cornerRadius = 16.dp,
                ) { Text("保存并使用") }

                if (existing != null) {
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.secondaryContainer,
                            contentColor = MiuixTheme.colorScheme.error,
                        ),
                        insideMargin = PaddingValues(0.dp),
                        cornerRadius = 16.dp,
                    ) { Text("删除服务") }
                }
            }
        }
    }

    AiModelEditDialog(
        show = showModelDialog,
        initialId = editingModel,
        initialName = editingModel?.let {
            modelAliases[it] ?: autoModelDisplayName(preset?.id, it)
        }.orEmpty(),
        initialSpec = editingModel?.let { modelSpecs[it] }
            ?: AiModelSpec(supportsImage = supportsVision),
        onDismiss = { showModelDialog = false },
        onSave = { id, displayName, spec ->
            val targetId = id.trim()
            if (targetId.isNotBlank()) {
                val oldId = editingModel
                if (oldId != null && oldId != targetId) {
                    val index = models.indexOf(oldId)
                    if (index >= 0) {
                        models[index] = targetId
                    } else if (targetId !in models) {
                        models.add(targetId)
                    }
                    if (model == oldId) model = targetId
                    modelAliases.remove(oldId)?.let { modelAliases[targetId] = it }
                    modelSpecs.remove(oldId)?.let { modelSpecs[targetId] = it }
                } else if (targetId !in models) {
                    models.add(targetId)
                }
                if (displayName.isBlank()) {
                    modelAliases.remove(targetId)
                } else {
                    modelAliases[targetId] = displayName.trim()
                }
                modelSpecs[targetId] = spec
                if (model.isBlank()) model = targetId
            }
            showModelDialog = false
        },
    )

    WindowDialogDeleteService(
        show = showDeleteDialog,
        name = name,
        onDismiss = { showDeleteDialog = false },
        onConfirm = {
            AiProviderStore.remove(context, providerId)
            showDeleteDialog = false
            Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
            onBack()
        },
    )
}

@Composable
private fun AiModelEditDialog(
    show: Boolean,
    initialId: String?,
    initialName: String,
    initialSpec: AiModelSpec,
    onDismiss: () -> Unit,
    onSave: (String, String, AiModelSpec) -> Unit,
) {
    var id by remember(show, initialId) { mutableStateOf(initialId.orEmpty()) }
    var displayName by remember(show, initialId) { mutableStateOf(initialName) }
    var contextWindow by remember(show, initialId) {
        mutableStateOf(initialSpec.contextWindow.toString())
    }
    var maxOutput by remember(show, initialId) {
        mutableStateOf(initialSpec.maxOutputTokens.toString())
    }
    var supportsText by remember(show, initialId) {
        mutableStateOf(initialSpec.supportsText)
    }
    var supportsImage by remember(show, initialId) {
        mutableStateOf(initialSpec.supportsImage)
    }
    val context = LocalContext.current

    WindowDialog(
        show = show,
        title = "编辑模型",
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(
                value = id,
                onValueChange = { id = it },
                label = "模型 ID",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = "显示名称",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TextField(
                    value = contextWindow,
                    onValueChange = { contextWindow = it.filter(Char::isDigit) },
                    label = "上下文窗口",
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                TextField(
                    value = maxOutput,
                    onValueChange = { maxOutput = it.filter(Char::isDigit) },
                    label = "最大输出 token",
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                insideMargin = PaddingValues(0.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SwitchPreference(
                        title = "文本",
                        summary = "允许向该模型发送文本输入",
                        checked = supportsText,
                        onCheckedChange = { supportsText = it },
                    )
                    SwitchPreference(
                        title = "图片",
                        summary = "允许向该模型发送图片输入",
                        checked = supportsImage,
                        onCheckedChange = { supportsImage = it },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("取消") }
                Button(
                    onClick = {
                        if (id.isBlank()) {
                            Toast.makeText(context, "请填写模型 ID", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        val contextSize = contextWindow.toIntOrNull()
                            ?.coerceIn(1024, 10_000_000)
                            ?: 1_048_576
                        val maxSize = maxOutput.toIntOrNull()
                            ?.coerceIn(256, 1_000_000)
                            ?: 131_072
                        onSave(
                            id.trim(),
                            displayName.trim(),
                            AiModelSpec(
                                contextWindow = contextSize,
                                maxOutputTokens = maxSize,
                                supportsText = supportsText,
                                supportsImage = supportsImage,
                            ),
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("保存") }
            }
        }
    }
}

@Composable
private fun WindowDialogDeleteService(
    show: Boolean,
    name: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    top.yukonga.miuix.kmp.window.WindowDialog(
        show = show,
        title = "删除服务",
        summary = "「$name」的配置将被删除，API Key 也会一并清除。",
        onDismissRequest = onDismiss,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface,
                ),
            ) { Text("取消") }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.error,
                ),
            ) { Text("删除") }
        }
    }
}
