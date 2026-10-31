package com.torachen.mote.ui

import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * 与 miuix 官方导航一致的时间曲线。
 *
 * miuix 的 `NavProgrammaticEasing` 不是普通贝塞尔，而是把一条欠阻尼弹簧的阶跃响应
 * 烘焙成 [Easing]（`response = 0.8`、`damping = 0.95`），再放进固定时长的 tween：
 * 前段起速快、尾段拖得长而柔，总时长恒定（不会像真实弹簧那样随距离变长）。
 *
 * 这里按同一公式实现一份，供页面栈的进出场动画使用。
 */
@Immutable
internal class SpringStepEasing(
    response: Float,
    damping: Float,
) : Easing {

    private val r: Float
    private val w: Float
    private val c2: Float

    init {
        val omega = 2.0 * PI / response
        val k = omega * omega
        val c = damping * 4.0 * PI / response

        w = (sqrt(4.0 * k - c * c) / 2.0).toFloat()
        r = (-c / 2.0).toFloat()
        c2 = r / w
    }

    override fun transform(fraction: Float): Float {
        val t = fraction.toDouble()
        val decay = exp(r * t)
        return (decay * (-cos(w * t) + c2 * sin(w * t)) + 1.0).toFloat()
    }
}

/** miuix 默认的程序化转场曲线（response 0.8 / damping 0.95）。 */
internal val PageTransitionEasing: Easing = SpringStepEasing(response = 0.8f, damping = 0.95f)

/** 手势松手后的续接曲线：比程序化更短，避免从半程继续时拖沓。 */
internal val PageGestureSettleEasing: Easing = SpringStepEasing(response = 0.66f, damping = 0.92f)
