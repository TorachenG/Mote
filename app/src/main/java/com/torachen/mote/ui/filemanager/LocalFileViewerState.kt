package com.torachen.mote.ui.filemanager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 文件管理里直接打开 PDF / 文本文件的查看请求 */
internal sealed interface LocalFileViewerRequest {
    val path: String
    val title: String

    data class Pdf(override val path: String, override val title: String) : LocalFileViewerRequest

    data class Text(override val path: String, override val title: String) : LocalFileViewerRequest

    /**
     * 本地 HTML / 图片：path 用于简易 WebView 直接读取；
     * browserUrl 是交给主页浏览器打开的 URI（通常是 FileProvider content://）。
     */
    data class Web(
        override val path: String,
        override val title: String,
        val browserUrl: String,
        val isImage: Boolean,
    ) : LocalFileViewerRequest
}

internal object LocalFileViewerState {
    var current: LocalFileViewerRequest? by mutableStateOf(null)
}
