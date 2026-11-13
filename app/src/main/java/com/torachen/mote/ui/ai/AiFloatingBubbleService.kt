@file:Suppress("DEPRECATION")

package com.torachen.mote.ui.ai

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.animation.ValueAnimator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.SweepGradient
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.torachen.mote.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * 悬浮球服务：在其它应用上层显示一个白色圆形 AI 入口。
 * 球内显示当前模型/服务商的原始彩色图标；没有在线图标时显示品牌色圆点。
 */
class AiFloatingBubbleService : Service() {

    companion object {
        private const val CHANNEL_ID = "ai_floating_assistant"
        private const val NOTIFICATION_ID = 4201
        private const val ACTION_STOP = "com.torachen.mote.ai.STOP_FLOATING"

        @Volatile
        private var instance: AiFloatingBubbleService? = null

        @Volatile
        private var running = false

        @Volatile
        private var agentBusy = false

        fun isRunning(): Boolean = running && instance != null

        /** 悬浮面板打开时隐藏悬浮球，关闭后恢复。 */
        fun setBubbleVisible(visible: Boolean) {
            instance?.bubbleView?.visibility = if (visible) View.VISIBLE else View.GONE
        }

        /** Agent 控制时：球内显示三点加载，并显示全屏边缘流光。 */
        fun setAgentBusy(busy: Boolean) {
            agentBusy = busy
            instance?.applyAgentBusy(busy)
        }

        /** 切换模型/服务商后刷新球内图标。 */
        fun refreshIcon() {
            instance?.updateBubbleIcon()
        }
    }

    private lateinit var windowManager: WindowManager
    private var bubbleView: View? = null
    private var bubbleIconView: ImageView? = null
    private var bubbleDotsView: View? = null
    private var closeView: View? = null
    private var closeParams: WindowManager.LayoutParams? = null
    private var closeShown = false
    private var glowView: View? = null
    private var snapAnimator: ValueAnimator? = null
    private var isTemporary = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        instance = this
        running = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        if (!AiFloatingController.hasOverlayPermission(this)) {
            Toast.makeText(this, "请先授予悬浮窗权限", Toast.LENGTH_SHORT).show()
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, buildNotification())
        isTemporary = intent?.getBooleanExtra(AiFloatingController.EXTRA_TEMPORARY, false) == true ||
                isTemporary
        if (bubbleView == null) addBubble()
        applyAgentBusy(agentBusy)
        return if (isTemporary) START_NOT_STICKY else START_STICKY
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mote AI 悬浮球",
                NotificationManager.IMPORTANCE_LOW,
            ).apply {
                description = "在其它应用上层显示 Mote AI 悬浮球"
                setShowBadge(false)
            }
            manager.createNotificationChannel(channel)
        }
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, AiFloatingBubbleService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Mote AI 悬浮球")
            .setContentText("点击悬浮球随时唤起 AI")
            .setContentIntent(openIntent)
            .addAction(0, "关闭悬浮助手", stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun addBubble() {
        val size = dp(50)
        val container = FrameLayout(this).apply {
            background = BubbleShadowDrawable(resources.displayMetrics.density)
            contentDescription = "Mote AI 悬浮球"
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            container.systemGestureExclusionRects = listOf(Rect(0, 0, size, size))
        }
        val icon = ImageView(this).apply {
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        val dots = TypingDotsView(this).apply {
            visibility = View.GONE
        }
        container.addView(
            icon,
            FrameLayout.LayoutParams(dp(22), dp(22), Gravity.CENTER),
        )
        container.addView(
            dots,
            FrameLayout.LayoutParams(dp(34), dp(22), Gravity.CENTER),
        )
        val layoutParams = WindowManager.LayoutParams(
            size,
            size,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = dp(12)
            y = dp(220)
        }

        var downRawX = 0f
        var downRawY = 0f
        var startX = 0
        var startY = 0
        var moved = false
        var overClose = false
        container.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    snapAnimator?.cancel()
                    downRawX = event.rawX
                    downRawY = event.rawY
                    startX = layoutParams.x
                    startY = layoutParams.y
                    moved = false
                    overClose = false
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - downRawX
                    val dy = event.rawY - downRawY
                    if (abs(dx) > dp(4) || abs(dy) > dp(4)) moved = true
                    val metrics = resources.displayMetrics
                    val maxX = (metrics.widthPixels - size).coerceAtLeast(0)
                    val maxY = (metrics.heightPixels - size).coerceAtLeast(0)
                    layoutParams.x = (startX + dx.toInt()).coerceIn(0, maxX)
                    layoutParams.y = (startY + dy.toInt()).coerceIn(0, maxY)
                    windowManager.updateViewLayout(view, layoutParams)
                    if (moved) {
                        showCloseTarget(true)
                        overClose = isInsideClose(layoutParams, size)
                        updateCloseTarget(overClose)
                        view.alpha = if (overClose) 0.72f else 1f
                    }
                    true
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    showCloseTarget(false)
                    view.alpha = 1f
                    if (event.actionMasked == MotionEvent.ACTION_UP) {
                        if (overClose) {
                            stopSelf()
                        } else if (moved) {
                            snapToEdge(view, layoutParams, size)
                        } else {
                            openFloatingPanel()
                        }
                    }
                    true
                }

                else -> false
            }
        }

        val close = FrameLayout(this).apply {
            background = CloseTargetDrawable(resources.displayMetrics.density)
            visibility = View.GONE
            scaleX = 0.8f
            scaleY = 0.8f
            alpha = 0f
        }
        val closeParams = WindowManager.LayoutParams(
            dp(64),
            dp(64),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = dp(96)
        }

        runCatching {
            windowManager.addView(container, layoutParams)
            windowManager.addView(close, closeParams)
        }.onFailure {
            stopSelf()
            return
        }
        bubbleView = container
        bubbleIconView = icon
        bubbleDotsView = dots
        closeView = close
        this.closeParams = closeParams
        updateBubbleIcon()
    }

    private fun applyAgentBusy(busy: Boolean) {
        bubbleIconView?.visibility = if (busy) View.GONE else View.VISIBLE
        bubbleDotsView?.visibility = if (busy) View.VISIBLE else View.GONE
        if (busy) showAgentGlow() else hideAgentGlow()
        if (!busy) updateBubbleIcon()
    }

    private fun showAgentGlow() {
        if (glowView != null) return
        val glow = AgentGlowView(this)
        val metrics = resources.displayMetrics
        val params = WindowManager.LayoutParams(
            metrics.widthPixels,
            metrics.heightPixels,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                fitInsetsTypes = 0
                fitInsetsSides = 0
            }
        }
        runCatching {
            windowManager.addView(glow, params)
            glowView = glow
        }
    }

    private fun hideAgentGlow() {
        glowView?.let { view ->
            runCatching { windowManager.removeView(view) }
        }
        glowView = null
    }

    private fun showCloseTarget(show: Boolean) {
        val view = closeView ?: return
        if (show) {
            view.animate().cancel()
            val params = closeParams
            if (!closeShown && params != null) {
                closeShown = true
                // 重新挂载可确保关闭区域位于流光和悬浮球窗口之上。
                runCatching {
                    windowManager.removeView(view)
                    windowManager.addView(view, params)
                }
            }
            view.visibility = View.VISIBLE
            view.alpha = 1f
            view.scaleX = 0.86f
            view.scaleY = 0.86f
            view.bringToFront()
            closeParams?.let { params ->
                runCatching { windowManager.updateViewLayout(view, params) }
            }
            view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
        } else {
            closeShown = false
            view.animate().cancel()
            view.animate().alpha(0f).scaleX(0.8f).scaleY(0.8f).setDuration(120)
                .withEndAction { view.visibility = View.GONE }
                .start()
        }
    }

    private fun updateCloseTarget(over: Boolean) {
        closeView?.animate()
            ?.scaleX(if (over) 1.22f else 1f)
            ?.scaleY(if (over) 1.22f else 1f)
            ?.setDuration(140)
            ?.start()
    }

    private fun isInsideClose(params: WindowManager.LayoutParams, size: Int): Boolean {
        val metrics = resources.displayMetrics
        val targetX = metrics.widthPixels / 2f
        val targetY = metrics.heightPixels - dp(96) - dp(64) / 2f
        val centerX = params.x + size / 2f
        val centerY = params.y + size / 2f
        return abs(centerX - targetX) < dp(72) && abs(centerY - targetY) < dp(80)
    }

    private fun snapToEdge(
        view: View,
        params: WindowManager.LayoutParams,
        size: Int,
    ) {
        val metrics = resources.displayMetrics
        val targetX = if (params.x + size / 2 < metrics.widthPixels / 2) {
            dp(8)
        } else {
            metrics.widthPixels - size - dp(8)
        }
        params.y = params.y.coerceIn(0, (metrics.heightPixels - size).coerceAtLeast(0))
        snapAnimator?.cancel()
        snapAnimator = ValueAnimator.ofInt(params.x, targetX).apply {
            duration = 220L
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                params.x = animation.animatedValue as Int
                runCatching { windowManager.updateViewLayout(view, params) }
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    // 重新挂载一次，确保窗口移动后触摸区域同步到新位置。
                    runCatching {
                        windowManager.removeView(view)
                        windowManager.addView(view, params)
                    }
                }
            })
            start()
        }
    }

    private fun updateBubbleIcon() {
        val iconView = bubbleIconView ?: return
        scope.launch {
            AiProviderStore.ensureLoaded(this@AiFloatingBubbleService)
            val provider = AiProviderStore.active
            val preset = AiPresets.byId(provider?.presetId)
            val bitmap = preset?.iconUrl
                ?.takeIf { it.isNotBlank() }
                ?.let { url -> AiIconLoader.load(url) }
            val brandColor = preset?.brandColor
                ?.let { it.toInt() }
                ?: 0xFF3482FF.toInt()
            withContext(Dispatchers.Main) {
                if (bitmap != null) {
                    iconView.setImageBitmap(bitmap)
                } else {
                    iconView.setImageDrawable(SparkDrawable(brandColor))
                }
            }
        }
    }

    private fun openFloatingPanel() {
        runCatching {
            startActivity(
                Intent(this, AiFloatingActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            )
        }
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        running = false
        snapAnimator?.cancel()
        snapAnimator = null
        hideAgentGlow()
        closeView?.let { view ->
            runCatching { windowManager.removeView(view) }
        }
        closeView = null
        scope.cancel()
        bubbleView?.let { view ->
            runCatching { windowManager.removeView(view) }
        }
        bubbleView = null
        bubbleIconView = null
        bubbleDotsView = null
        super.onDestroy()
    }

    private fun dp(value: Int): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            resources.displayMetrics,
        ).toInt()
}

/** 白色圆形 + 无方向的柔和环境阴影（类似 iOS 的均匀弥散阴影）。 */
private class TypingDotsView(context: android.content.Context) : View(context) {
    private val density = resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3482FF")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val gap = 8f * density
        val radius = 3.2f * density
        val phase = (SystemClock.uptimeMillis() % 960L) / 960f
        for (index in 0..2) {
            val wave = ((phase + index * 0.16f) % 1f)
            val alpha = (0.35f + 0.65f * (1f - abs(wave * 2f - 1f))).coerceIn(0.35f, 1f)
            paint.alpha = (alpha * 255).toInt()
            canvas.drawCircle(centerX + (index - 1) * gap, centerY, radius, paint)
        }
        postInvalidateOnAnimation()
    }
}

/** 底部红色关闭目标：红色圆形 + 白色 X。 */
private class CloseTargetDrawable(private val density: Float) : Drawable() {
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF3B30")
    }
    private val xPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2.6f * density
        strokeCap = Paint.Cap.ROUND
    }

    override fun draw(canvas: Canvas) {
        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val radius = minOf(bounds.width(), bounds.height()) / 2f - 2f * density
        canvas.drawCircle(cx, cy, radius, circlePaint)
        val arm = radius * 0.35f
        canvas.drawLine(cx - arm, cy - arm, cx + arm, cy + arm, xPaint)
        canvas.drawLine(cx + arm, cy - arm, cx - arm, cy + arm, xPaint)
    }

    override fun setAlpha(alpha: Int) {
        circlePaint.alpha = alpha
        xPaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        circlePaint.colorFilter = colorFilter
        xPaint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}

/** Agent 控制期间的屏幕边缘彩色流光，按屏幕圆角绘制。 */
private class AgentGlowView(context: android.content.Context) : View(context) {
    private val density = resources.displayMetrics.density
    private val cornerRadius: Float = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val display = context.display ?: runCatching {
                context.getSystemService(android.view.WindowManager::class.java)
                    ?.defaultDisplay
            }.getOrNull()
            if (display == null) return@runCatching 42f * density
            val positions = intArrayOf(
                android.view.RoundedCorner.POSITION_TOP_LEFT,
                android.view.RoundedCorner.POSITION_TOP_RIGHT,
                android.view.RoundedCorner.POSITION_BOTTOM_LEFT,
                android.view.RoundedCorner.POSITION_BOTTOM_RIGHT,
            )
            val radius = positions.maxOf {
                display.getRoundedCorner(it)?.radius ?: 0
            }
            if (radius > 0) radius.toFloat() else 42f * density
        } else {
            42f * density
        }
    }.getOrDefault(42f * density)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f * density
    }
    private val rect = RectF()
    private val colors = intArrayOf(
        Color.parseColor("#3482FF"),
        Color.parseColor("#9B5CFF"),
        Color.parseColor("#00D4FF"),
        Color.parseColor("#FF5CA8"),
        Color.parseColor("#3482FF"),
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width <= 0 || height <= 0) return
        val phase = (SystemClock.uptimeMillis() % 3200L) / 3200f
        val matrix = android.graphics.Matrix().apply {
            postRotate(phase * 360f, width / 2f, height / 2f)
        }
        val shader = SweepGradient(
            width / 2f,
            height / 2f,
            colors,
            null,
        ).apply { setLocalMatrix(matrix) }
        paint.shader = shader

        // 由外向内多层描边：外层宽而淡，内层窄而亮，形成向屏幕内部渐隐的流光。
        val layers = listOf(
            22f * density to 24,
            15f * density to 58,
            9f * density to 120,
            4.5f * density to 235,
        )
        layers.forEach { (stroke, alpha) ->
            paint.strokeWidth = stroke
            paint.alpha = alpha
            val inset = stroke / 2f
            rect.set(inset, inset, width - inset, height - inset)
            val radius = (cornerRadius - inset).coerceAtLeast(0f)
            canvas.drawRoundRect(rect, radius, radius, paint)
        }
        postInvalidateOnAnimation()
    }
}

/** 白色圆形 + 无方向的柔和环境阴影（类似 iOS 的均匀弥散阴影）。 */
private class BubbleShadowDrawable(private val density: Float) : Drawable() {

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
    }

    override fun draw(canvas: Canvas) {
        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val shadowRadius = minOf(bounds.width(), bounds.height()) / 2f
        val circleRadius = (shadowRadius - 6f * density).coerceAtLeast(1f)

        shadowPaint.shader = RadialGradient(
            cx,
            cy,
            shadowRadius,
            intArrayOf(0x26000000, 0x14000000, 0x00000000),
            floatArrayOf(0.68f, 0.84f, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawCircle(cx, cy, shadowRadius, shadowPaint)
        canvas.drawCircle(cx, cy, circleRadius, whitePaint)
    }

    override fun setAlpha(alpha: Int) {
        shadowPaint.alpha = alpha
        whitePaint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        shadowPaint.colorFilter = colorFilter
        whitePaint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}

/** 没有在线模型图标时的兜底：品牌色四角星，不显示文字。 */
private class SparkDrawable(private val color: Int) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
    }
    private val path = Path()

    override fun draw(canvas: Canvas) {
        val cx = bounds.exactCenterX()
        val cy = bounds.exactCenterY()
        val radius = minOf(bounds.width(), bounds.height()) * 0.42f
        val inner = radius * 0.16f
        path.reset()
        path.moveTo(cx, cy - radius)
        path.quadTo(cx + inner, cy - inner, cx + radius, cy)
        path.quadTo(cx + inner, cy + inner, cx, cy + radius)
        path.quadTo(cx - inner, cy + inner, cx - radius, cy)
        path.quadTo(cx - inner, cy - inner, cx, cy - radius)
        path.close()
        canvas.drawPath(path, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: android.graphics.ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    override fun getOpacity(): Int = android.graphics.PixelFormat.TRANSLUCENT
}
