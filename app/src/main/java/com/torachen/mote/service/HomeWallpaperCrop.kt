@file:Suppress("SpellCheckingInspection")

package com.torachen.mote.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.RotateLeft
import top.yukonga.miuix.kmp.theme.MiuixTheme
// ★ 跨包引用 ui 包里的壁纸相关符号
import com.torachen.mote.ui.customWallpaperFile
import com.torachen.mote.ui.computeAverageColor
import com.torachen.mote.ui.KEY_HOME_CUSTOM_WALLPAPER_COLOR
import com.torachen.mote.ui.KEY_HOME_CUSTOM_WALLPAPER_PATH
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

// ============================================================
// 从 Uri 加载 Bitmap（含 EXIF 旋转校正 + 内存采样）
//   ★ 修复：maxDim 4096 → 2048，旋转耗时降到 1/4
// ============================================================

internal fun loadBitmapForCrop(context: Context, uri: Uri): Bitmap? {
    return try {
        var orientation = ExifInterface.ORIENTATION_NORMAL
        try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                val exif = ExifInterface(input)
                orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            }
        } catch (_: Exception) {}

        val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        }
        if (opts.outWidth <= 0 || opts.outHeight <= 0) return null

        // ★ 最长边不超过 2048（原 4096）：旋转、缩放、裁剪都更快
        val maxDim = 2048
        var sample = 1
        while (opts.outWidth / (sample * 2) >= maxDim ||
            opts.outHeight / (sample * 2) >= maxDim
        ) {
            sample *= 2
        }

        val opts2 = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val raw = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts2)
        } ?: return null

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        }
        if (matrix.isIdentity) {
            raw
        } else {
            val rotated = Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
            if (rotated !== raw) {
                try { raw.recycle() } catch (_: Exception) {}
            }
            rotated
        }
    } catch (_: Exception) {
        null
    }
}

// ============================================================
// 保存裁剪后的 Bitmap 为壁纸
// ============================================================

internal fun saveCroppedWallpaper(context: Context, bitmap: Bitmap): Boolean {
    return try {
        val destFile = customWallpaperFile(context)
        val tempFile = File(destFile.parentFile, "${destFile.name}.tmp")
        FileOutputStream(tempFile).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)
        }
        if (destFile.exists()) destFile.delete()
        if (!tempFile.renameTo(destFile)) {
            tempFile.delete()
            return false
        }
        val avgColor = computeAverageColor(bitmap)
        context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_HOME_CUSTOM_WALLPAPER_COLOR, avgColor.toArgb().toLong())
            .putString(KEY_HOME_CUSTOM_WALLPAPER_PATH, destFile.absolutePath)
            .apply()
        true
    } catch (_: Exception) {
        false
    }
}

// ============================================================
// 裁剪页（作为页面栈里的普通页面）
// ============================================================

private val RotationEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
private const val ROTATION_ANIM_MS = 420
private const val UI_FADE_MS = 260
private const val CROP_BLUR_RADIUS = 15f
private const val CROP_SCRIM_ALPHA = 0.55f
private val CROP_BUTTON_CORNER = 16.dp
private val TOP_BUTTON_SIZE = 40.dp
private val TOP_BUTTON_PADDING_START = 12.dp
private val TOP_BUTTON_PADDING_TOP = 8.dp
private val TOP_ICON_SIZE = 20.dp

@Composable
internal fun HomeWallpaperCropOverlay(
    sourceBitmap: Bitmap,
    onConfirm: (Bitmap) -> Unit,
    onCancel: () -> Unit,
) {
    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(Color.Black)
            drawContent()
        }
    } else null

    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var rotationDegrees by remember { mutableIntStateOf(0) }
    var rotationTick by remember { mutableIntStateOf(0) }

    val entryRotation = remember { Animatable(0f) }

    var uiVisible by remember { mutableStateOf(true) }
    val uiAlpha by animateFloatAsState(
        targetValue = if (uiVisible) 1f else 0f,
        animationSpec = tween(durationMillis = UI_FADE_MS, easing = RotationEasing),
        label = "cropUiAlpha",
    )

    // ★ 关键修复：把 4 个方向预先旋转好，点击时直接取用，零延迟
    //   由于 maxDim 已降到 2048，这里预生成 4 张旋转图的总耗时依然可接受
    //   （4 张 2048 边长 = 4 × 4M 像素，旋转约 100~200ms，且只在进入页面时做一次）
    val rotationCache = remember(sourceBitmap) {
        buildList<Bitmap> {
            add(sourceBitmap)                                   // 0°
            for (deg in listOf(90, 180, 270)) {
                val m = Matrix().apply { postRotate(deg.toFloat()) }
                add(
                    Bitmap.createBitmap(
                        sourceBitmap, 0, 0,
                        sourceBitmap.width, sourceBitmap.height,
                        m, true,
                    )
                )
            }
        }
    }

    // ★ 直接按 rotationDegrees / 90 索引取缓存 → 点击时无阻塞
    val currentBitmap = rotationCache[(rotationDegrees / 90) % 4]
    val imageBitmap = remember(currentBitmap) { currentBitmap.asImageBitmap() }

    val minScale = remember(viewportSize, currentBitmap) {
        if (viewportSize.width <= 0 || viewportSize.height <= 0) 1f
        else maxOf(
            viewportSize.width.toFloat() / currentBitmap.width,
            viewportSize.height.toFloat() / currentBitmap.height,
        )
    }
    val maxScale = minScale * 6f

    LaunchedEffect(viewportSize, currentBitmap) {
        if (viewportSize.width > 0 && viewportSize.height > 0) {
            scale = minScale
            offsetX = (viewportSize.width - currentBitmap.width * scale) / 2f
            offsetY = (viewportSize.height - currentBitmap.height * scale) / 2f
        }
    }

    LaunchedEffect(rotationTick) {
        if (rotationTick > 0) {
            entryRotation.snapTo(-90f)
            entryRotation.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = ROTATION_ANIM_MS,
                    easing = RotationEasing,
                ),
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (blurSupported && backdrop != null) {
                        Modifier.layerBackdrop(backdrop)
                    } else Modifier
                )
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { viewportSize = it }
                    .pointerInput(currentBitmap, minScale, maxScale) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val vpW = size.width.toFloat()
                            val vpH = size.height.toFloat()
                            if (vpW <= 0f || vpH <= 0f) return@detectTransformGestures

                            val newScale = (scale * zoom).coerceIn(minScale, maxScale)
                            val zoomFactor = if (scale > 0f) newScale / scale else 1f

                            offsetX = centroid.x - (centroid.x - offsetX) * zoomFactor
                            offsetY = centroid.y - (centroid.y - offsetY) * zoomFactor
                            scale = newScale

                            offsetX += pan.x
                            offsetY += pan.y

                            val dispW = currentBitmap.width * scale
                            val dispH = currentBitmap.height * scale
                            if (dispW <= vpW) {
                                offsetX = (vpW - dispW) / 2f
                            } else {
                                offsetX = offsetX.coerceIn(vpW - dispW, 0f)
                            }
                            if (dispH <= vpH) {
                                offsetY = (vpH - dispH) / 2f
                            } else {
                                offsetY = offsetY.coerceIn(vpH - dispH, 0f)
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { uiVisible = !uiVisible },
                        )
                    }
            ) {
                withTransform({
                    translate(left = offsetX, top = offsetY)
                    scale(scaleX = scale, scaleY = scale, pivot = Offset.Zero)
                }) {
                    rotate(
                        degrees = entryRotation.value,
                        pivot = Offset(
                            currentBitmap.width / 2f,
                            currentBitmap.height / 2f,
                        ),
                    ) {
                        drawImage(
                            image = imageBitmap,
                            dstSize = IntSize(currentBitmap.width, currentBitmap.height),
                        )
                    }
                }
            }
        }

        // ---------- 四角高亮 ----------
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = uiAlpha }
        ) {
            val w = size.width
            val h = size.height
            val cornerLen = 24.dp.toPx()
            val strokeW = 3.dp.toPx()
            val color = Color.White

            drawLine(color, Offset(0f, 0f), Offset(cornerLen, 0f), strokeW)
            drawLine(color, Offset(0f, 0f), Offset(0f, cornerLen), strokeW)

            drawLine(color, Offset(w, 0f), Offset(w - cornerLen, 0f), strokeW)
            drawLine(color, Offset(w, 0f), Offset(w, cornerLen), strokeW)

            drawLine(color, Offset(0f, h), Offset(cornerLen, h), strokeW)
            drawLine(color, Offset(0f, h), Offset(0f, h - cornerLen), strokeW)

            drawLine(color, Offset(w, h), Offset(w - cornerLen, h), strokeW)
            drawLine(color, Offset(w, h), Offset(w, h - cornerLen), strokeW)
        }

        // ---------- 顶部左：返回 ----------
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = TOP_BUTTON_PADDING_START, top = TOP_BUTTON_PADDING_TOP)
                .size(TOP_BUTTON_SIZE)
                .graphicsLayer { alpha = uiAlpha }
                .clip(CircleShape)
                .then(
                    if (blurSupported && backdrop != null) {
                        Modifier.textureBlur(
                            backdrop = backdrop,
                            shape = CircleShape,
                            blurRadius = CROP_BLUR_RADIUS,
                            noiseCoefficient = BlurDefaults.NoiseCoefficient,
                            colors = BlurDefaults.blurColors(
                                blendColors = listOf(
                                    BlendColorEntry(color = Color.Black.copy(alpha = CROP_SCRIM_ALPHA)),
                                ),
                            ),
                        )
                    } else {
                        Modifier.background(Color.Black.copy(alpha = CROP_SCRIM_ALPHA))
                    }
                )
                .clickable(enabled = uiVisible, onClick = onCancel),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MiuixIcons.Back,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.size(TOP_ICON_SIZE),
            )
        }

        // ---------- 顶部中：提示 ----------
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 20.dp)
                .graphicsLayer { alpha = uiAlpha },
        ) {
            Text(
                text = "拖动 / 双指缩放",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
            )
        }

        // ---------- 顶部右：旋转 ----------
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = TOP_BUTTON_PADDING_START, top = TOP_BUTTON_PADDING_TOP)
                .size(TOP_BUTTON_SIZE)
                .graphicsLayer { alpha = uiAlpha }
                .clip(CircleShape)
                .then(
                    if (blurSupported && backdrop != null) {
                        Modifier.textureBlur(
                            backdrop = backdrop,
                            shape = CircleShape,
                            blurRadius = CROP_BLUR_RADIUS,
                            noiseCoefficient = BlurDefaults.NoiseCoefficient,
                            colors = BlurDefaults.blurColors(
                                blendColors = listOf(
                                    BlendColorEntry(color = Color.Black.copy(alpha = CROP_SCRIM_ALPHA)),
                                ),
                            ),
                        )
                    } else {
                        Modifier.background(Color.Black.copy(alpha = CROP_SCRIM_ALPHA))
                    }
                )
                .clickable(enabled = uiVisible) {
                    rotationDegrees = (rotationDegrees + 90) % 360
                    rotationTick++
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MiuixIcons.RotateLeft,
                contentDescription = "旋转",
                tint = Color.White,
                modifier = Modifier.size(TOP_ICON_SIZE),
            )
        }

        // ---------- 底部：确定 ----------
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 20.dp)
                .fillMaxWidth()
                .height(52.dp)
                .graphicsLayer { alpha = uiAlpha }
                .clip(RoundedCornerShape(CROP_BUTTON_CORNER))
                .background(MiuixTheme.colorScheme.primary)
                .clickable(enabled = uiVisible) {
                    val bmpW = currentBitmap.width
                    val bmpH = currentBitmap.height
                    val vpW = viewportSize.width
                    val vpH = viewportSize.height

                    if (vpW <= 0 || vpH <= 0 || scale <= 0f) {
                        onCancel()
                        return@clickable
                    }

                    val srcLeft = ((0f - offsetX) / scale)
                        .roundToInt().coerceIn(0, bmpW - 1)
                    val srcTop = ((0f - offsetY) / scale)
                        .roundToInt().coerceIn(0, bmpH - 1)
                    val srcRight = ((vpW - offsetX) / scale)
                        .roundToInt().coerceIn(srcLeft + 1, bmpW)
                    val srcBottom = ((vpH - offsetY) / scale)
                        .roundToInt().coerceIn(srcTop + 1, bmpH)

                    val srcW = srcRight - srcLeft
                    val srcH = srcBottom - srcTop

                    if (srcW <= 0 || srcH <= 0) {
                        onCancel()
                        return@clickable
                    }

                    try {
                        val cropped = Bitmap.createBitmap(
                            currentBitmap, srcLeft, srcTop, srcW, srcH,
                        )
                        onConfirm(cropped)
                    } catch (_: Exception) {
                        onCancel()
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "确定",
                color = Color.White,
                fontSize = MiuixTheme.textStyles.body1.fontSize,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}