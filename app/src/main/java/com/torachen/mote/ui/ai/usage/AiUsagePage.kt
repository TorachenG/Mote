package com.torachen.mote.ui.ai.usage

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.torachen.mote.ui.SearchableItem
import com.torachen.mote.ui.ai.AiSettingsScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.preference.WindowDropdownPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private enum class UsagePeriod(val label: String) {
    Today("今日"),
    Week("本周"),
    Month("本月"),
    All("全部"),
}

@Composable
fun AiUsagePage(onBack: () -> Unit) {
    val context = LocalContext.current
    var period by remember { mutableStateOf(UsagePeriod.Today) }
    var records by remember { mutableStateOf<List<AiUsageRecord>>(emptyList()) }
    var reload by remember { mutableIntStateOf(0) }
    var showClear by remember { mutableStateOf(false) }
    val summary = remember(records) { AiUsageRecorder.summarize(records) }
    val byModel = remember(records) { AiUsageRecorder.groupByModel(records) }
    val byProvider = remember(records) { AiUsageRecorder.groupByProvider(records) }
    val priced = records.any { it.costUsd != null }

    LaunchedEffect(period, reload) {
        val since = periodStart(period)
        records = withContext(Dispatchers.IO) {
            if (since <= 0L) AiUsageDb.queryAll(context) else AiUsageDb.querySince(context, since)
        }
    }

    val csvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        if (uri != null) {
            val text = AiUsageExport.csv(records)
            val ok = AiUsageExport.write(context, uri, text)
            Toast.makeText(context, if (ok) "已导出 CSV" else "导出失败", Toast.LENGTH_SHORT).show()
        }
    }
    val jsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) {
            val text = AiUsageExport.json(records)
            val ok = AiUsageExport.write(context, uri, text)
            Toast.makeText(context, if (ok) "已导出 JSON" else "导出失败", Toast.LENGTH_SHORT).show()
        }
    }

    AiSettingsScaffold(
        title = "用量统计",
        pageRoute = "ai_usage",
        onBack = onBack,
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .padding(top = 12.dp, bottom = 12.dp),
        ) {
            WindowDropdownPreference(
                title = "统计范围",
                summary = period.label,
                items = UsagePeriod.entries.map { it.label },
                selectedIndex = UsagePeriod.entries.indexOf(period),
                onSelectedIndexChange = { index ->
                    period = UsagePeriod.entries.getOrElse(index) { UsagePeriod.Today }
                },
            )
        }
        Text(
            text = "数据只保存在本机，不上传、不参与云同步；费用为本地估算，provider 账单为准。",
            fontSize = 12.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        )

        SmallTitle("概览")
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            UsageMetric(
                title = "请求",
                value = "${summary.requests}",
                modifier = Modifier.weight(1f),
            )
            UsageMetric(
                title = "Token",
                value = formatTokens(summary.totalTokens),
                modifier = Modifier.weight(1f),
            )
            UsageMetric(
                title = "费用",
                value = if (!priced) "—" else formatCost(summary.costUsd),
                modifier = Modifier.weight(1f),
            )
            UsageMetric(
                title = "成功率",
                value = if (summary.requests == 0) "—" else {
                    "${(summary.requests - summary.failedRequests) * 100 / summary.requests}%"
                },
                modifier = Modifier.weight(1f),
            )
        }
        if (summary.estimatedRequests > 0) {
            Text(
                text = "其中 ${summary.estimatedRequests} 个请求没有 provider usage，已按字符估算。",
                fontSize = 11.sp,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            )
        }

        SmallTitle("趋势")
        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
            UsageBars(records)
        }

        if (byProvider.isNotEmpty()) {
            SmallTitle("按服务商")
            Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                byProvider.forEach { group ->
                    BasicComponent(
                        title = group.label,
                        summary = "${group.summary.requests} 次 · " +
                                "${formatTokens(group.summary.totalTokens)} token · " +
                                if (priced) formatCost(group.summary.costUsd) else "费用未知",
                    )
                }
            }
        }

        if (byModel.isNotEmpty()) {
            SmallTitle("按模型")
            Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
                byModel.take(20).forEach { group ->
                    BasicComponent(
                        title = group.label,
                        summary = "${group.summary.requests} 次 · " +
                                "${formatTokens(group.summary.totalTokens)} token · " +
                                if (priced) formatCost(group.summary.costUsd) else "费用未知",
                    )
                }
            }
        }

        SmallTitle("最近请求")
        Card(modifier = Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp)) {
            if (records.isEmpty()) {
                Text(
                    text = "还没有用量记录。发起一次 AI 对话后这里会出现数据。",
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                records.take(20).forEach { record ->
                    SearchableItem(
                        title = record.modelId,
                        pageRoute = "ai_usage",
                        pageTitle = "用量统计",
                        itemKey = "ai_usage_${record.requestId}",
                    ) {
                        BasicComponent(
                            title = record.modelId,
                            summary = buildString {
                                append(formatDateTime(record.startedAt))
                                append(" · ")
                                append(formatTokens(record.totalTokens))
                                append(" token")
                                if (record.estimated) append("（估算）")
                                if (!record.success) append(" · 失败")
                                record.costUsd?.let { append(" · ").append(formatCost(it)) }
                            },
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(
                onClick = {
                    csvLauncher.launch(
                        "Mote_AI_usage_" + SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
                            .format(Date()) + ".csv",
                    )
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface,
                ),
            ) { Text("导出 CSV") }
            Button(
                onClick = {
                    jsonLauncher.launch(
                        "Mote_AI_usage_" + SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
                            .format(Date()) + ".json",
                    )
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface,
                ),
            ) { Text("导出 JSON") }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = { showClear = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            colors = ButtonDefaults.buttonColors(
                color = MiuixTheme.colorScheme.secondaryContainer,
                contentColor = MiuixTheme.colorScheme.error,
            ),
        ) { Text("清空用量数据") }
        Spacer(Modifier.height(24.dp))
    }

    if (showClear) {
        WindowDialog(
            show = true,
            title = "清空用量数据",
            summary = "只删除本机统计，不影响对话记录和 API Key。",
            onDismissRequest = { showClear = false },
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Button(
                    onClick = { showClear = false },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("取消") }
                Button(
                    onClick = {
                        AiUsageDb.clear(context)
                        showClear = false
                        reload++
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.error,
                        contentColor = MiuixTheme.colorScheme.onPrimary,
                    ),
                ) { Text("清空") }
            }
        }
    }
}

@Composable
private fun UsageMetric(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(value, fontSize = 17.sp, color = MiuixTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(title, fontSize = 11.sp, color = MiuixTheme.colorScheme.onSurfaceVariantSummary)
        }
    }
}

@Composable
private fun UsageBars(records: List<AiUsageRecord>) {
    if (records.isEmpty()) {
        Text(
            text = "暂无数据",
            fontSize = 13.sp,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(16.dp),
        )
        return
    }
    val days = remember(records) { dailyTotals(records) }
    val max = days.maxOfOrNull { it.second }?.coerceAtLeast(1) ?: 1
    Row(
        modifier = Modifier.fillMaxWidth().height(140.dp).padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        days.forEach { (label, value) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((100f * value / max).coerceAtLeast(2f).dp)
                        .background(
                            if (value == max) MiuixTheme.colorScheme.primary
                            else MiuixTheme.colorScheme.primary.copy(alpha = 0.45f),
                            RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp),
                        ),
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 9.sp,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    maxLines = 1,
                )
            }
        }
    }
}

private fun dailyTotals(records: List<AiUsageRecord>): List<Pair<String, Int>> {
    val format = SimpleDateFormat("MM-dd", Locale.US)
    val grouped = LinkedHashMap<String, Int>()
    records.sortedBy { it.startedAt }.forEach { record ->
        val key = format.format(Date(record.startedAt))
        grouped[key] = (grouped[key] ?: 0) + record.totalTokens
    }
    return grouped.entries.toList().takeLast(14).map { it.key to it.value }
}

private fun periodStart(period: UsagePeriod): Long {
    if (period == UsagePeriod.All) return 0L
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    when (period) {
        UsagePeriod.Week -> {
            calendar.firstDayOfWeek = Calendar.MONDAY
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }

        UsagePeriod.Month -> calendar.set(Calendar.DAY_OF_MONTH, 1)
        else -> Unit
    }
    return calendar.timeInMillis
}

private fun formatTokens(tokens: Int): String = when {
    tokens >= 1_000_000 -> String.format(Locale.US, "%.2fM", tokens / 1_000_000.0)
    tokens >= 1_000 -> String.format(Locale.US, "%.1fK", tokens / 1_000.0)
    else -> tokens.toString()
}

private fun formatCost(cost: Double): String =
    if (cost < 0.01) String.format(Locale.US, "\$%.4f", cost)
    else String.format(Locale.US, "\$%.2f", cost)

private fun formatDateTime(timestamp: Long): String =
    SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
