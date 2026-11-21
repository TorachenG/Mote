package com.torachen.mote.ui.ai.mcp

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

/**
 * MCP 工具调用的前台确认。
 *
 * 与文件修改确认一样，用 CompletableDeferred 把 Agent 协程挂起，等 UI 返回决策。
 * 后台任务不会调用这里；后台应直接 fail-closed。
 */
internal object AiToolApproval {

    var request by mutableStateOf<AiToolApprovalRequest?>(null)
        private set

    private var deferred: CompletableDeferred<AiToolApprovalDecision>? = null
    private var context: Context? = null

    suspend fun ask(context: Context, request: AiToolApprovalRequest): AiToolApprovalDecision {
        this.context = context.applicationContext
        this.request = request
        val waiter = CompletableDeferred<AiToolApprovalDecision>()
        deferred = waiter
        val decision = withTimeoutOrNull(60_000L) { waiter.await() }
            ?: AiToolApprovalDecision.Deny
        if (deferred === waiter) deferred = null
        this.request = null
        applyPolicyIfNeeded(request, decision)
        return decision
    }

    fun resolve(decision: AiToolApprovalDecision) {
        val waiter = deferred
        deferred = null
        request = null
        waiter?.complete(decision)
    }

    fun cancelAll() {
        deferred?.complete(AiToolApprovalDecision.Deny)
        deferred = null
        request = null
    }

    private fun applyPolicyIfNeeded(
        request: AiToolApprovalRequest,
        decision: AiToolApprovalDecision,
    ) {
        val serverId = request.serverId ?: return
        val app = context ?: return
        when (decision) {
            AiToolApprovalDecision.AllowAlways ->
                McpManager.setToolPolicy(app, serverId, request.toolName, McpToolPolicy.Allow)

            AiToolApprovalDecision.DenyAlways ->
                McpManager.setToolPolicy(app, serverId, request.toolName, McpToolPolicy.Deny)

            else -> Unit
        }
    }
}
