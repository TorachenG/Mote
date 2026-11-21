package com.torachen.mote.ui.ai.usage

import com.torachen.mote.ui.ai.AiTokenUsage

/** Agent 一次模型请求的原始计量；usage 为空时由 Recorder 按字符估算。 */
internal data class AiRequestUsage(
    val requestId: String,
    val usage: AiTokenUsage?,
    val startedAt: Long,
    val endedAt: Long,
    val success: Boolean,
    val error: String? = null,
    val streamed: Boolean,
    val estimatedInputChars: Int,
    val estimatedOutputChars: Int,
)
