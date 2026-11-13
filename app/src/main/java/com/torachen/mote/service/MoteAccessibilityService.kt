@file:Suppress("DEPRECATION")

package com.torachen.mote.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.Display
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.ArrayDeque
import kotlin.coroutines.resume

/**
 * 供 AI 自动操作使用的无障碍服务。
 *
 * 只提供最基础的「读屏 / 点击 / 输入 / 滚动 / 全局返回」能力，
 * 所有操作都由用户在自动操作模式下主动触发。
 */
class MoteAccessibilityService : AccessibilityService() {

    companion object {
        @Volatile
        var instance: MoteAccessibilityService? = null
            private set

        fun isEnabled(context: Context): Boolean {
            val expected = ComponentName(context, MoteAccessibilityService::class.java)
            val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
            val viaManager = manager?.let { am ->
                runCatching {
                    am.getEnabledAccessibilityServiceList(
                        AccessibilityServiceInfo.FEEDBACK_ALL_MASK,
                    ).any { info ->
                        val serviceInfo = info.resolveInfo?.serviceInfo ?: return@any false
                        ComponentName(serviceInfo.packageName, serviceInfo.name) == expected
                    }
                }.getOrDefault(false)
            } ?: false
            if (viaManager) return true

            return runCatching {
                val enabled = android.provider.Settings.Secure.getInt(
                    context.contentResolver,
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED,
                    0,
                ) == 1
                if (!enabled) return@runCatching false
                val raw = android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                ).orEmpty()
                val full = expected.flattenToString()
                val short = expected.flattenToShortString()
                raw.split(':').any { item ->
                    item.equals(full, ignoreCase = true) || item.equals(short, ignoreCase = true)
                }
            }.getOrDefault(false)
        }

        fun openAccessibilitySettings(context: Context) {
            runCatching {
                context.startActivity(
                    Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        serviceInfo = serviceInfo?.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 80
        }
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    /** 读取当前屏幕上的可读文本、可点击/可输入节点。 */
    fun snapshot(maxChars: Int = 6000): String {
        val root = rootInActiveWindow ?: return "当前无法读取屏幕内容"
        val builder = StringBuilder()
        val count = IntArray(1)
        runCatching {
            collectNode(root, 0, count, builder, maxChars)
        }
        if (builder.isBlank()) return "当前屏幕没有可读取的文字"
        return builder.toString().trim().take(maxChars)
    }

    /**
     * 无障碍节点树之外的补充截图通道。
     * 用于 Unity / Canvas 等不暴露文字节点的画面；仅在节点树信息不足时调用。
     */
    suspend fun captureScreenshot(): Bitmap? = withContext(Dispatchers.Main) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return@withContext null
        suspendCancellableCoroutine { continuation ->
            runCatching {
                takeScreenshot(
                    Display.DEFAULT_DISPLAY,
                    mainExecutor,
                    object : TakeScreenshotCallback {
                        override fun onSuccess(screenshot: ScreenshotResult) {
                            val buffer = screenshot.hardwareBuffer
                            val bitmap = runCatching {
                                Bitmap.wrapHardwareBuffer(
                                    buffer,
                                    ColorSpace.get(ColorSpace.Named.SRGB),
                                )?.copy(Bitmap.Config.ARGB_8888, false)
                            }.getOrNull()
                            buffer.close()
                            if (continuation.isActive) continuation.resume(bitmap)
                        }

                        override fun onFailure(errorCode: Int) {
                            if (continuation.isActive) continuation.resume(null)
                        }
                    },
                )
            }.onFailure {
                if (continuation.isActive) continuation.resume(null)
            }
        }
    }


    private fun collectNode(
        node: AccessibilityNodeInfo,
        depth: Int,
        count: IntArray,
        builder: StringBuilder,
        maxChars: Int,
    ) {
        if (depth > 40 || count[0] > 200 || builder.length > maxChars) return
        val text = node.text?.toString()?.trim().orEmpty()
        val desc = node.contentDescription?.toString()?.trim().orEmpty()
        val label = text.ifBlank { desc }
        if (label.isNotBlank() || node.isClickable || node.isEditable) {
            val rect = Rect()
            node.getBoundsInScreen(rect)
            builder.append("- ")
            if (label.isNotBlank()) {
                builder.append('"').append(label.take(120)).append('"')
            } else {
                builder.append(node.className?.toString()?.substringAfterLast('.') ?: "控件")
            }
            if (node.isClickable) builder.append(" [可点击]")
            if (node.isEditable) builder.append(" [可输入]")
            builder.append(" (").append(rect.left).append(',').append(rect.top)
                .append(',').append(rect.width()).append(',').append(rect.height()).append(")\n")
            count[0]++
        }
        for (index in 0 until node.childCount) {
            val child = node.getChild(index) ?: continue
            collectNode(child, depth + 1, count, builder, maxChars)
        }
    }

    /** 按文本 / 描述点击；支持精确匹配和包含匹配。 */
    fun clickText(target: String): Boolean {
        val query = target.trim()
        if (query.isEmpty()) return false
        val root = rootInActiveWindow ?: return false
        val exact = findNode(root) { node ->
            node.text?.toString()?.trim().equals(query, ignoreCase = true) ||
                    node.contentDescription?.toString()?.trim().equals(query, ignoreCase = true)
        }
        val node = exact ?: findNode(root) { node ->
            node.text?.toString()?.contains(query, ignoreCase = true) == true ||
                    node.contentDescription?.toString()?.contains(query, ignoreCase = true) == true
        }
        if (node == null) return false
        var clickable: AccessibilityNodeInfo? = node
        while (clickable != null && !clickable.isClickable) {
            clickable = clickable.parent
        }
        val targetNode = clickable ?: node
        if (targetNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
        val rect = Rect()
        targetNode.getBoundsInScreen(rect)
        return if (!rect.isEmpty) {
            tap(rect.centerX().toFloat(), rect.centerY().toFloat())
        } else {
            false
        }
    }

    /** 向当前焦点输入框写入文本。 */
    fun inputText(text: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
            ?: root.findFocus(AccessibilityNodeInfo.FOCUS_ACCESSIBILITY)
        val node = if (focused?.isEditable == true) focused else findEditable(root)
        if (node == null) return false
        node.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        return node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    /** 滚动当前可滚动容器。 */
    fun scroll(forward: Boolean): Boolean {
        val root = rootInActiveWindow ?: return false
        val scrollable = findNode(root) { it.isScrollable }
            ?: findNode(root) { it.className?.contains("Scroll", ignoreCase = true) == true }
            ?: return false
        return scrollable.performAction(
            if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD
            else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD,
        )
    }

    fun back(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)

    fun home(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)

    fun recents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)

    fun openApp(packageName: String): Boolean {
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return runCatching {
            startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun tap(x: Float, y: Float): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 80L))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    /** 长按指定坐标。 */
    fun longPress(x: Float, y: Float, durationMs: Long = 650L): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0L, durationMs))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    /** 按方向滑动；也支持直接传入起止坐标。 */
    fun swipe(
        direction: String = "",
        x1: Float = -1f,
        y1: Float = -1f,
        x2: Float = -1f,
        y2: Float = -1f,
        durationMs: Long = 320L,
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return false
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels.toFloat()
        val height = metrics.heightPixels.toFloat()
        val startX: Float
        val startY: Float
        val endX: Float
        val endY: Float
        if (x1 >= 0f && y1 >= 0f && x2 >= 0f && y2 >= 0f) {
            startX = x1
            startY = y1
            endX = x2
            endY = y2
        } else {
            when (direction.lowercase()) {
                "down" -> {
                    startX = width / 2f
                    startY = height * 0.28f
                    endX = width / 2f
                    endY = height * 0.76f
                }

                "left" -> {
                    startX = width * 0.82f
                    startY = height / 2f
                    endX = width * 0.18f
                    endY = height / 2f
                }

                "right" -> {
                    startX = width * 0.18f
                    startY = height / 2f
                    endX = width * 0.82f
                    endY = height / 2f
                }

                else -> {
                    startX = width / 2f
                    startY = height * 0.76f
                    endX = width / 2f
                    endY = height * 0.28f
                }
            }
        }
        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(
                GestureDescription.StrokeDescription(path, 0L, durationMs.coerceIn(80L, 2000L)),
            )
            .build()
        return dispatchGesture(gesture, null, null)
    }

    fun currentPackage(): String? = rootInActiveWindow?.packageName?.toString()

    private fun findEditable(root: AccessibilityNodeInfo): AccessibilityNodeInfo? =
        findNode(root) { it.isEditable }

    private fun findNode(
        root: AccessibilityNodeInfo,
        predicate: (AccessibilityNodeInfo) -> Boolean,
    ): AccessibilityNodeInfo? {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visited = 0
        while (queue.isNotEmpty() && visited < 600) {
            val node = queue.removeFirst()
            visited++
            if (predicate(node)) return node
            for (index in 0 until node.childCount) {
                val child = node.getChild(index) ?: continue
                queue.add(child)
            }
        }
        return null
    }
}
