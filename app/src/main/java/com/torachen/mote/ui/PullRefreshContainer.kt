package com.torachen.mote.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.changedToDown
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.abs

/**
 * 与浏览器页一致的下拉刷新容器：
 * 顶部下拉超过 74dp 触发刷新，指示器为 18dp 的 InfiniteProgressIndicator，
 * 内容按拉动距离的一半位移。
 */
@Composable
internal fun PullRefreshContainer(
    canPull: () -> Boolean,
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier,
    indicatorTopPadding: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    val density = LocalDensity.current
    val triggerPx = with(density) { 74.dp.toPx() }
    val scope = rememberCoroutineScope()

    var isRefreshing by remember { mutableStateOf(false) }
    var pullDistance by remember { mutableFloatStateOf(0f) }
    var offsetTarget by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val animatedOffset by animateFloatAsState(
        targetValue = offsetTarget,
        animationSpec = if (isDragging) snap<Float>() else tween(durationMillis = 280),
        label = "pullOffset",
    )

    val indicatorProgress = if (isRefreshing) 1f else (animatedOffset / triggerPx).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent(PointerEventPass.Initial)
                            .changes.firstOrNull { it.changedToDown() } ?: continue
                        val startX = down.position.x
                        val startY = down.position.y
                        var pulled = false
                        var maxPull = 0f

                        while (true) {
                            val ev = awaitPointerEvent(PointerEventPass.Initial)
                            val ch = ev.changes.firstOrNull { it.id == down.id } ?: break
                            if (!ch.pressed) break
                            val dx = ch.position.x - startX
                            val dy = ch.position.y - startY

                            // 只有明确的竖直下拉才接管手势，横向滑动留给 pager
                            if (!pulled && dy > 14f && dy > abs(dx) * 1.8f) {
                                if (canPull()) {
                                    pulled = true
                                    isDragging = true
                                } else {
                                    break
                                }
                            }
                            // 一旦横向位移占优，立刻放弃（交还给横向滑页）
                            if (!pulled && abs(dx) > dy * 1.2f && abs(dx) > 14f) {
                                break
                            }
                            if (pulled) {
                                maxPull = dy.coerceAtLeast(0f)
                                pullDistance = maxPull
                                offsetTarget = maxPull * 0.5f
                                ch.consume()
                            }
                        }

                        if (pulled) {
                            isDragging = false
                            offsetTarget = 0f
                            pullDistance = 0f
                            if (maxPull * 0.5f >= triggerPx && !isRefreshing) {
                                isRefreshing = true
                                scope.launch {
                                    try {
                                        onRefresh()
                                    } finally {
                                        isRefreshing = false
                                    }
                                }
                            }
                        }
                    }
                }
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationY = animatedOffset },
        ) {
            content()
        }

        if (pullDistance > 0f || isRefreshing) {
            val indicatorScale = 1f + indicatorProgress * 0.6f
            val indicatorColor = if (indicatorProgress >= 1f) {
                MiuixTheme.colorScheme.primary
            } else {
                Color(0xFF666666)
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = indicatorTopPadding + 12.dp)
                    .size(22.dp)
                    .graphicsLayer {
                        scaleX = indicatorScale
                        scaleY = indicatorScale
                        translationY = animatedOffset * 0.9f
                    },
                contentAlignment = Alignment.Center,
            ) {
                InfiniteProgressIndicator(
                    color = indicatorColor,
                    size = 22.dp,
                    strokeWidth = 2.5.dp,
                )
            }
        }
    }
}
