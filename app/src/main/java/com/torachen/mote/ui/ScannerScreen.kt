@file:Suppress("SpellCheckingInspection")
@file:androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)

package com.torachen.mote.ui

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Size
import android.view.Surface
import android.widget.Toast
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size as CvSize
import org.opencv.imgproc.Imgproc
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect as GeometryRect
import androidx.compose.ui.geometry.Size as GeometrySize
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronBackward
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.Image
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Replace
import top.yukonga.miuix.kmp.layout.DialogDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowCascadingListPopup
import top.yukonga.miuix.kmp.window.WindowDialog

private const val CONTROL_BLUR_RADIUS = 15f
private const val BUTTON_SCRIM_ALPHA = 0.7f
private const val NO_DETECT_FRAMES_TO_CLEAR = 5
private const val CODE_OVERLAY_ENTER_MS = 220
private const val CODE_OVERLAY_EXIT_MS = 180
private val CodeOverlayEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
private const val IMAGE_FLIP_IN_MS = 520
private const val IMAGE_FLIP_OUT_MS = 420
private const val PAGE_BLUR_DP = 5f

private const val SOURCE_LANG_AUTO = "自动检测"
private const val TARGET_LANG_ZH = "中文"
private const val TARGET_LANG_EN = "英语"

private val SOURCE_LANG_OPTIONS = listOf(
    SOURCE_LANG_AUTO, "中文", "英语", "日语", "韩语", "法语", "德语", "西班牙语", "俄语"
)
private val TARGET_LANG_OPTIONS = listOf(
    "中文", "英语", "日语", "韩语", "法语", "德语", "西班牙语", "俄语"
)

private enum class ScannerMode { SCAN, TRANSLATE, SCAN_DOC }

private enum class ScanFilter { ORIGINAL, ENHANCED, GRAYSCALE }

@Composable
private fun buildScannerButtonModifier(
    materialStyle: Int,
    blurSupported: Boolean,
    backdrop: LayerBackdrop?,
    shape: Shape = CircleShape,
): Modifier {
    val isBlurActive = blurSupported && backdrop != null && materialStyle != 0

    return if (isBlurActive && backdrop != null) {
        if (materialStyle == 2) {
            val highlight = Highlight.GlassStrokeSmallDark
            val baseSurface = Color(0xFF121212)
            val blendColor = androidx.compose.ui.graphics.lerp(baseSurface, Color.Black, 0.25f).copy(alpha = 0.7f)

            val blurColors = BlurDefaults.blurColors(
                blendColors = listOf(
                    BlendColorEntry(
                        color = blendColor,
                        mode = BlurBlendMode.SrcOver,
                    ),
                ),
                contrast = 1.25f,
                saturation = 1.25f,
            )
            Modifier
                .graphicsLayer {
                    shadowElevation = 24.dp.toPx()
                    ambientShadowColor = Color.Black.copy(alpha = 0.35f)
                    spotShadowColor = Color.Transparent
                }
                .textureBlur(
                    backdrop = backdrop,
                    shape = shape,
                    blurRadius = 25f,
                    noiseCoefficient = BlurDefaults.NoiseCoefficient,
                    colors = blurColors,
                    highlight = highlight,
                )
        } else {
            Modifier.textureBlur(
                backdrop = backdrop,
                shape = shape,
                blurRadius = CONTROL_BLUR_RADIUS,
                noiseCoefficient = BlurDefaults.NoiseCoefficient,
                colors = BlurDefaults.blurColors(
                    blendColors = listOf(
                        BlendColorEntry(color = Color.Black.copy(alpha = BUTTON_SCRIM_ALPHA)),
                    ),
                )
            )
        }
    } else {
        Modifier.background(Color.Black.copy(alpha = BUTTON_SCRIM_ALPHA), shape)
    }
}

private data class WifiQrInfo(
    val ssid: String,
    val password: String,
    val securityType: String,
    val isHidden: Boolean,
)

private data class DetectedCode(
    val rawValue: String,
    val xFraction: Float,
    val yFraction: Float,
    val format: Int,
)

private fun parseWifiQrCode(raw: String): WifiQrInfo? {
    if (!raw.startsWith("WIFI:", ignoreCase = true)) return null
    val body = raw.substringAfter(":", "")

    val parts = mutableListOf<String>()
    val sb = StringBuilder()
    var i = 0
    while (i < body.length) {
        val c = body[i]
        if (c == '\\' && i + 1 < body.length) {
            sb.append(body[i + 1])
            i += 2
        } else if (c == ';') {
            parts.add(sb.toString())
            sb.clear()
            i++
        } else {
            sb.append(c)
            i++
        }
    }
    if (sb.isNotEmpty()) parts.add(sb.toString())

    var ssid = ""
    var password = ""
    var security = "nopass"
    var hidden = false

    parts.forEach { part ->
        if (part.length >= 2 && part[1] == ':') {
            val k = part[0].uppercaseChar()
            val v = part.substring(2)
            when (k) {
                'S' -> ssid = v
                'P' -> password = v
                'T' -> security = v
                'H' -> hidden = v.equals("true", ignoreCase = true)
            }
        }
    }

    if (ssid.isBlank()) return null
    return WifiQrInfo(
        ssid = ssid,
        password = password,
        securityType = security,
        isHidden = hidden,
    )
}

private fun connectToWifi(context: Context, info: WifiQrInfo) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        Toast.makeText(context, "Android 10 以下暂不支持直接连接 WIFI", Toast.LENGTH_LONG).show()
        return
    }
    try {
        val specifierBuilder = WifiNetworkSpecifier.Builder()
            .setSsid(info.ssid)

        if (info.password.isNotBlank() && info.securityType.lowercase() != "nopass") {
            when (info.securityType.uppercase()) {
                "WEP" -> {
                    Toast.makeText(context, "系统不支持自动连接 WEP 网络，正在打开系统设置…", Toast.LENGTH_LONG).show()
                    try {
                        val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    } catch (_: Exception) {}
                    return
                }
                "WPA3" -> specifierBuilder.setWpa3Passphrase(info.password)
                else -> specifierBuilder.setWpa2Passphrase(info.password)
            }
        }

        val specifier = specifierBuilder.build()
        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        cm.requestNetwork(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                val act = context as? Activity
                act?.runOnUiThread {
                    Toast.makeText(context, "已连接到 ${info.ssid}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onUnavailable() {
                val act = context as? Activity
                act?.runOnUiThread {
                    Toast.makeText(context, "连接已取消或失败", Toast.LENGTH_SHORT).show()
                }
            }
        })
    } catch (e: Exception) {
        Toast.makeText(context, "无法连接：${e.message}", Toast.LENGTH_LONG).show()
    }
}

private fun buildBarcodeScanner(): BarcodeScanner {
    return BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_QR_CODE,
                Barcode.FORMAT_AZTEC,
                Barcode.FORMAT_DATA_MATRIX,
                Barcode.FORMAT_PDF417,
                Barcode.FORMAT_CODE_128,
                Barcode.FORMAT_CODE_39,
                Barcode.FORMAT_CODE_93,
                Barcode.FORMAT_CODABAR,
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_ITF,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
            )
            .build()
    )
}

private fun langNameToCode(name: String): String = when (name) {
    "中文" -> "zh-Hans"
    "英语" -> "en"
    "日语" -> "ja"
    "韩语" -> "ko"
    "法语" -> "fr"
    "德语" -> "de"
    "西班牙语" -> "es"
    "俄语" -> "ru"
    else -> ""
}

private fun langCodeToName(code: String): String {
    val lower = code.lowercase().substringBefore("-")
    return when (lower) {
        "zh" -> "中文"
        "en" -> "英语"
        "ja" -> "日语"
        "ko" -> "韩语"
        "fr" -> "法语"
        "de" -> "德语"
        "es" -> "西班牙语"
        "ru" -> "俄语"
        else -> ""
    }
}

private fun computeEffectiveTargetLang(
    userSource: String,
    userTarget: String,
    detectedLangName: String,
): String {
    if (userSource != SOURCE_LANG_AUTO) return userTarget
    if (detectedLangName.isBlank()) return userTarget
    return if (userTarget == TARGET_LANG_ZH) {
        if (detectedLangName == TARGET_LANG_ZH) TARGET_LANG_EN else TARGET_LANG_ZH
    } else {
        if (detectedLangName == userTarget) TARGET_LANG_ZH else userTarget
    }
}

private data class OcrBlock(
    val text: String,
    val boundingBox: Rect,
)

private data class TranslatedBlock(
    val translatedText: String,
    val boundingBox: Rect,
)

private data class OcrResult(
    val fullText: String,
    val detectedLanguageName: String,
    val blocks: List<OcrBlock>,
)

private suspend fun recognizeTextAndDetectLanguage(
    context: Context,
    bitmap: Bitmap,
): OcrResult = withContext(Dispatchers.IO) {
    val recognizer = TextRecognition.getClient(
        ChineseTextRecognizerOptions.Builder().build()
    )
    try {
        val image = InputImage.fromBitmap(bitmap, 0)
        suspendCancellableCoroutine<OcrResult> { cont ->
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    if (!cont.isActive) return@addOnSuccessListener
                    val detectedLang = run {
                        for (block in result.textBlocks) {
                            val lang = block.recognizedLanguage
                            if (lang.isNotBlank()) return@run lang
                        }
                        ""
                    }
                    val blocks = result.textBlocks.mapNotNull { block ->
                        val text = block.text
                        val box = block.boundingBox
                        if (text.isNotBlank() && box != null) OcrBlock(text, box) else null
                    }
                    cont.resume(
                        OcrResult(
                            fullText = result.text,
                            detectedLanguageName = langCodeToName(detectedLang),
                            blocks = blocks,
                        )
                    )
                }
                .addOnFailureListener { e ->
                    if (cont.isActive) cont.resumeWithException(e)
                }
        }
    } finally {
        try { recognizer.close() } catch (_: Exception) {}
    }
}

private fun tryEdgeTranslate(
    texts: List<String>,
    sourceCode: String,
    targetCode: String,
): List<String>? {
    if (texts.isEmpty()) return emptyList()
    return try {
        val fromParam = if (sourceCode.isBlank()) "" else sourceCode
        val conn = URL(
            "https://edge.microsoft.com/translate/translatetext" +
                    "?from=$fromParam&to=$targetCode&isEnterpriseClient=false"
        ).openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.doOutput = true
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36")
        conn.connectTimeout = 8000
        conn.readTimeout = 12000

        val body = JSONArray().apply { texts.forEach { put(it) } }
        conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

        if (conn.responseCode == 200) {
            val respText = conn.inputStream.bufferedReader().readText()
            val resArr = JSONArray(respText)
            val result = mutableListOf<String>()
            for (i in 0 until resArr.length()) {
                val obj = resArr.getJSONObject(i)
                val transArr = obj.optJSONArray("translations")
                if (transArr != null && transArr.length() > 0) {
                    result.add(transArr.getJSONObject(0).optString("text", texts[i]))
                } else {
                    result.add(texts[i])
                }
            }
            conn.disconnect()
            result
        } else {
            conn.disconnect()
            null
        }
    } catch (_: Exception) {
        null
    }
}

private fun translateTextsWithLang(
    texts: List<String>,
    sourceLangName: String,
    targetLangName: String,
): List<String> {
    val sourceCode = if (sourceLangName == SOURCE_LANG_AUTO) "" else langNameToCode(sourceLangName)
    val targetCode = langNameToCode(targetLangName).ifBlank { "zh-Hans" }

    val edgeResult = tryEdgeTranslate(texts, sourceCode, targetCode)
    if (edgeResult != null && edgeResult.size == texts.size) return edgeResult

    val fallbackTarget = if (targetCode == "zh-Hans") "zh-CN" else targetCode
    return translateTextBatch(texts, fallbackTarget)
}

private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
    return try {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        val rotation = imageProxy.imageInfo.rotationDegrees
        if (rotation != 0 && bmp != null) {
            val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
            val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
            if (rotated !== bmp) bmp.recycle()
            rotated
        } else bmp
    } catch (_: Exception) {
        null
    }
}

// 给扫描图片应用专业文档滤镜（模拟扫描全能王效果）
private fun applyScanFilter(src: Bitmap, filter: ScanFilter): Bitmap {
    if (filter == ScanFilter.ORIGINAL) return src

    val MAX_EDGE = 2000
    val needsScale = src.width > MAX_EDGE || src.height > MAX_EDGE
    val workSrc: Bitmap = if (needsScale) {
        try {
            val ratio = MAX_EDGE.toFloat() / maxOf(src.width, src.height)
            Bitmap.createScaledBitmap(
                src,
                (src.width * ratio).toInt().coerceAtLeast(1),
                (src.height * ratio).toInt().coerceAtLeast(1),
                true,
            )
        } catch (_: OutOfMemoryError) {
            return src
        } catch (_: Exception) {
            return src
        }
    } else src

    return try {
        val width = workSrc.width
        val height = workSrc.height
        val pixels = IntArray(width * height)
        workSrc.getPixels(pixels, 0, width, 0, 0, width, height)

        // 优先用 OpenCV（裁剪版，仅 core/imgproc/imgcodecs）处理，
        // 不可用时回退到纯 Kotlin 实现
        if (ensureOpenCvLoaded()) {
            val cv = runCatching {
                when (filter) {
                    ScanFilter.ENHANCED -> enhancedDocumentOpenCv(workSrc)
                    ScanFilter.GRAYSCALE -> blackWhiteDocumentOpenCv(workSrc)
                    ScanFilter.ORIGINAL -> workSrc
                }
            }.getOrNull()
            if (cv != null && cv !== workSrc) {
                if (workSrc !== src) {
                    try { workSrc.recycle() } catch (_: Exception) {}
                }
                return cv
            }
        }

        val out = when (filter) {
            ScanFilter.ENHANCED -> enhancedDocumentFilter(pixels, width, height)
            ScanFilter.GRAYSCALE -> blackWhiteDocumentFilter(pixels, width, height)
            ScanFilter.ORIGINAL -> return workSrc
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(out, 0, width, 0, 0, width, height)
        // 中间缩放图处理完即可释放
        if (workSrc !== src) {
            try { workSrc.recycle() } catch (_: Exception) {}
        }
        bitmap
    } catch (_: OutOfMemoryError) {
        if (workSrc !== src) workSrc else src
    } catch (_: Exception) {
        if (workSrc !== src) workSrc else src
    }
}
private var openCvLoaded: Boolean? = null

/** 加载裁剪版 OpenCV 原生库（core/imgproc/imgcodecs），失败则回退纯 Kotlin 实现 */
private fun ensureOpenCvLoaded(): Boolean {
    openCvLoaded?.let { return it }
    val ok = try {
        System.loadLibrary("opencv_java4")
        true
    } catch (_: Throwable) {
        false
    }
    openCvLoaded = ok
    return ok
}

/** 文档增强：LAB 亮度通道 CLAHE + 轻度反锐化，纸白字实且不破坏色彩 */
private fun enhancedDocumentOpenCv(src: Bitmap): Bitmap {
    val rgba = Mat()
    Utils.bitmapToMat(src, rgba)
    val rgb = Mat()
    Imgproc.cvtColor(rgba, rgb, Imgproc.COLOR_RGBA2RGB)

    val lab = Mat()
    Imgproc.cvtColor(rgb, lab, Imgproc.COLOR_RGB2Lab)
    val channels = ArrayList<Mat>()
    Core.split(lab, channels)
    val clahe = Imgproc.createCLAHE(2.2, CvSize(8.0, 8.0))
    clahe.apply(channels[0], channels[0])
    Core.merge(channels, lab)
    Imgproc.cvtColor(lab, rgb, Imgproc.COLOR_Lab2RGB)

    // 反锐化：原图 + 少量高频
    val blurred = Mat()
    Imgproc.GaussianBlur(rgb, blurred, CvSize(0.0, 0.0), 2.4)
    Core.addWeighted(rgb, 1.45, blurred, -0.45, 0.0, rgb)

    val outRgba = Mat()
    Imgproc.cvtColor(rgb, outRgba, Imgproc.COLOR_RGB2RGBA)
    val out = Bitmap.createBitmap(outRgba.cols(), outRgba.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(outRgba, out)
    rgba.release(); rgb.release(); lab.release(); blurred.release(); outRgba.release()
    return out
}

/** 黑白：CLAHE 提局部对比度 + 高斯自适应阈值，专治光照不均 */
private fun blackWhiteDocumentOpenCv(src: Bitmap): Bitmap {
    val rgba = Mat()
    Utils.bitmapToMat(src, rgba)
    val gray = Mat()
    Imgproc.cvtColor(rgba, gray, Imgproc.COLOR_RGBA2GRAY)

    val clahe = Imgproc.createCLAHE(2.0, CvSize(8.0, 8.0))
    clahe.apply(gray, gray)
    Imgproc.GaussianBlur(gray, gray, CvSize(0.0, 0.0), 1.2)

    // 窗口取短边的 1/16，必须为奇数且 >= 3
    var block = ((minOf(gray.cols(), gray.rows()) / 16) * 2 + 1).coerceIn(3, 251)
    if (block % 2 == 0) block -= 1

    val bw = Mat()
    Imgproc.adaptiveThreshold(
        gray, bw, 255.0,
        Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
        Imgproc.THRESH_BINARY,
        block, 9.0,
    )

    val outRgba = Mat()
    Imgproc.cvtColor(bw, outRgba, Imgproc.COLOR_GRAY2RGBA)
    val out = Bitmap.createBitmap(outRgba.cols(), outRgba.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(outRgba, out)
    rgba.release(); gray.release(); bw.release(); outRgba.release()
    return out
}

// 增强滤镜：模拟小米相机「文档增强」——纸白、字实、颜色不被破坏
private fun enhancedDocumentFilter(pixels: IntArray, width: Int, height: Int): IntArray {
    val n = width * height
    val r = FloatArray(n)
    val g = FloatArray(n)
    val b = FloatArray(n)
    for (i in 0 until n) {
        val c = pixels[i]
        r[i] = ((c shr 16) and 0xFF).toFloat()
        g[i] = ((c shr 8) and 0xFF).toFloat()
        b[i] = (c and 0xFF).toFloat()
    }

    // 1. 3x3 降噪，每个通道一次（比多次全分辨率卷积快得多）
    val dr = gaussianBlur3x3(r, width, height)
    val dg = gaussianBlur3x3(g, width, height)
    val db = gaussianBlur3x3(b, width, height)

    // 2. 背景光照估计：先 1/4 降采样再做积分图模糊，再双线性上采样
    //    （原来对三个通道各做一次全分辨率 boxBlur，是耗时大头）
    val scale = 4
    val smallW = (width + scale - 1) / scale
    val smallH = (height + scale - 1) / scale
    val bgRadius = ((minOf(width, height) / 12) / scale).coerceIn(4, 40)
    val bgR = SmallPlane(boxBlur(downsamplePlane(dr, width, height, scale), smallW, smallH, bgRadius), smallW, smallH)
    val bgG = SmallPlane(boxBlur(downsamplePlane(dg, width, height, scale), smallW, smallH, bgRadius), smallW, smallH)
    val bgB = SmallPlane(boxBlur(downsamplePlane(db, width, height, scale), smallW, smallH, bgRadius), smallW, smallH)

    // 3. 色调曲线 LUT：反射率 0.34 以下压成黑（文字），0.98 以上提成纯白（纸张），中间平滑过渡
    val curve = FloatArray(512) { idx ->
        smoothstep(0.34f, 0.98f, idx / 511f * 1.5f) * 255f
    }

    val out = IntArray(n)
    val invScale = 1f / scale
    for (y in 0 until height) {
        val fy = y * invScale
        val row = y * width
        for (x in 0 until width) {
            val i = row + x
            val fx = x * invScale
            val bgRv = sampleBilinear(bgR, fx, fy).coerceAtLeast(16f)
            val bgGv = sampleBilinear(bgG, fx, fy).coerceAtLeast(16f)
            val bgBv = sampleBilinear(bgB, fx, fy).coerceAtLeast(16f)

            // 4. 反射率归一化：逐通道除以背景 → 去掉阴影/偏色，同时保留通道比例
            var nr = dr[i] / bgRv * 255f
            var ng = dg[i] / bgGv * 255f
            var nb = db[i] / bgBv * 255f

            // 5. 曲线只作用在亮度上，色相/饱和度基本不变（不破坏色彩）
            val lum = nr * 0.299f + ng * 0.587f + nb * 0.114f
            val target = curve[(lum * (511f / 382.5f)).toInt().coerceIn(0, 511)]
            if (lum > 1f) {
                // 限制增益，避免彩色区域被拉出荧光感
                val ratio = (target / lum).coerceIn(0.55f, 1.75f)
                nr *= ratio
                ng *= ratio
                nb *= ratio
            }

            // 6. 轻微提饱和，避免整张纸发灰
            val y2 = nr * 0.299f + ng * 0.587f + nb * 0.114f
            nr = y2 + (nr - y2) * 1.05f
            ng = y2 + (ng - y2) * 1.05f
            nb = y2 + (nb - y2) * 1.05f

            // 7. 只在"背景确实很亮（纸面）"的区域做强补偿；
            //    背景偏暗（普通照片、阴影、深色桌面）保留原始明暗，避免过曝
            val bgLum = (bgRv + bgGv + bgBv) / 3f
            val w = smoothstep(150f, 235f, bgLum)
            if (w < 0.999f) {
                nr = dr[i] + (nr - dr[i]) * w
                ng = dg[i] + (ng - dg[i]) * w
                nb = db[i] + (nb - db[i]) * w
            }

            out[i] = packArgb(nr, ng, nb, 255)
        }
    }

    // 7. 轻度锐化，强化笔画边缘（强度比原来更低，避免小字出现白边）
    return sharpenArgb(out, width, height, amount = 0.26f)
}

// 黑白滤镜：光照补偿 + Otsu 二值化（专为文档小字优化）
private fun blackWhiteDocumentFilter(pixels: IntArray, width: Int, height: Int): IntArray {
    val n = width * height

    // 1. 只保留灰度，省掉 r/g/b 三张全尺寸数组（大图可省 2/3 内存）
    val gray = FloatArray(n)
    for (i in 0 until n) {
        val c = pixels[i]
        gray[i] = 0.299f * ((c shr 16) and 0xFF) +
                0.587f * ((c shr 8) and 0xFF) +
                0.114f * (c and 0xFF)
    }

    // 2. 灰度降噪
    val denoised = gaussianBlur3x3(gray, width, height)

    // 3. 背景估计同样走 1/4 降采样
    val scale = 4
    val smallW = (width + scale - 1) / scale
    val smallH = (height + scale - 1) / scale
    val bgRadius = ((minOf(width, height) / 12) / scale).coerceIn(4, 40)
    val bg = SmallPlane(
        boxBlur(downsamplePlane(denoised, width, height, scale), smallW, smallH, bgRadius),
        smallW,
        smallH,
    )

    // 4. 局部光照补偿
    val norm = FloatArray(n)
    val invScale = 1f / scale
    for (y in 0 until height) {
        val fy = y * invScale
        val row = y * width
        for (x in 0 until width) {
            val i = row + x
            val bgv = sampleBilinear(bg, x * invScale, fy).coerceAtLeast(12f)
            norm[i] = (denoised[i] / bgv * 255f).coerceIn(0f, 255f)
        }
    }

    // 5. 全局 Otsu 阈值作为基准
    val otsu = computeOtsuThreshold(norm, n).coerceIn(70f, 205f)

    // 6. 自适应阈值：积分图求局部均值（窗口约为短边的 1/12），
    //    解决拍照光照不均时「一半黑一半白」的问题，比纯 Otsu 更接近文档扫描效果
    val integral = LongArray((width + 1) * (height + 1))
    for (y in 0 until height) {
        var rowSum = 0L
        val rowBase = (y + 1) * (width + 1)
        val prevBase = y * (width + 1)
        for (x in 0 until width) {
            rowSum += norm[y * width + x].toLong()
            integral[rowBase + x + 1] = integral[prevBase + x + 1] + rowSum
        }
    }

    val radius = ((minOf(width, height) / 12).coerceAtLeast(6)).coerceAtMost(40)
    val c = 9f
    val out = IntArray(n)
    for (y in 0 until height) {
        val y0 = (y - radius).coerceAtLeast(0)
        val y1 = (y + radius).coerceAtMost(height - 1)
        for (x in 0 until width) {
            val x0 = (x - radius).coerceAtLeast(0)
            val x1 = (x + radius).coerceAtMost(width - 1)
            val area = ((x1 - x0 + 1) * (y1 - y0 + 1)).toFloat()
            val sum = integral[(y1 + 1) * (width + 1) + (x1 + 1)] -
                    integral[y0 * (width + 1) + (x1 + 1)] -
                    integral[(y1 + 1) * (width + 1) + x0] +
                    integral[y0 * (width + 1) + x0]
            val localMean = sum / area
            val value = norm[y * width + x]
            // 局部对比度低（纯色纸面/背景）时退回 Otsu，避免大面积噪点
            val threshold = if (kotlin.math.abs(localMean - value) < 6f) otsu
            else (localMean - c).coerceIn(otsu - 45f, otsu + 45f)
            out[y * width + x] = if (value < threshold) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }
    }
    return out
}

private fun computeOtsuThreshold(values: FloatArray, n: Int): Float {
    val hist = IntArray(256)
    for (v in values) {
        hist[v.toInt().coerceIn(0, 255)]++
    }

    var sumAll = 0.0
    for (i in 0 until 256) sumAll += i.toDouble() * hist[i]

    var sumBg = 0.0
    var wBg = 0
    var maxVariance = 0.0
    var bestThreshold = 128

    for (t in 0 until 256) {
        wBg += hist[t]
        if (wBg == 0) continue
        val wFg = n - wBg
        if (wFg == 0) break

        sumBg += t.toDouble() * hist[t]
        val meanBg = sumBg / wBg
        val meanFg = (sumAll - sumBg) / wFg
        val variance = wBg.toDouble() * wFg * (meanBg - meanFg) * (meanBg - meanFg)

        if (variance > maxVariance) {
            maxVariance = variance
            bestThreshold = t
        }
    }
    return bestThreshold.toFloat()
}

/** 小尺寸单通道平面（用于 1/4 分辨率的背景光照估计）。 */
private class SmallPlane(val data: FloatArray, val w: Int, val h: Int)

/** 按整数倍降采样（区域平均），用于加速大半径模糊。 */
private fun downsamplePlane(src: FloatArray, width: Int, height: Int, scale: Int): FloatArray {
    val sw = (width + scale - 1) / scale
    val sh = (height + scale - 1) / scale
    val out = FloatArray(sw * sh)
    for (sy in 0 until sh) {
        val y0 = sy * scale
        val y1 = minOf(y0 + scale, height)
        for (sx in 0 until sw) {
            val x0 = sx * scale
            val x1 = minOf(x0 + scale, width)
            var sum = 0f
            var count = 0
            for (y in y0 until y1) {
                val row = y * width
                for (x in x0 until x1) {
                    sum += src[row + x]
                    count++
                }
            }
            out[sy * sw + sx] = if (count > 0) sum / count else 0f
        }
    }
    return out
}

/** 双线性采样（小图坐标）。 */
private fun sampleBilinear(plane: SmallPlane, fx: Float, fy: Float): Float {
    if (plane.w <= 0 || plane.h <= 0) return 0f
    val x = fx.coerceIn(0f, (plane.w - 1).toFloat())
    val y = fy.coerceIn(0f, (plane.h - 1).toFloat())
    val x0 = x.toInt()
    val y0 = y.toInt()
    val x1 = minOf(x0 + 1, plane.w - 1)
    val y1 = minOf(y0 + 1, plane.h - 1)
    val tx = x - x0
    val ty = y - y0
    val a = plane.data[y0 * plane.w + x0]
    val b = plane.data[y0 * plane.w + x1]
    val c = plane.data[y1 * plane.w + x0]
    val d = plane.data[y1 * plane.w + x1]
    return a + (b - a) * tx + (c - a) * ty + (a - b - c + d) * tx * ty
}

private fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
    if (edge1 <= edge0) return if (x < edge0) 0f else 1f
    val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

private fun gaussianBlur3x3(src: FloatArray, width: Int, height: Int): FloatArray {
    val n = width * height
    val out = FloatArray(n)
    val kernel = floatArrayOf(1f, 2f, 1f, 2f, 4f, 2f, 1f, 2f, 1f)
    val kernelSum = 16f
    for (y in 0 until height) {
        for (x in 0 until width) {
            var sum = 0f
            var k = 0
            for (dy in -1..1) {
                for (dx in -1..1) {
                    val px = (x + dx).coerceIn(0, width - 1)
                    val py = (y + dy).coerceIn(0, height - 1)
                    sum += src[py * width + px] * kernel[k]
                    k++
                }
            }
            out[y * width + x] = sum / kernelSum
        }
    }
    return out
}

private fun boxBlur(src: FloatArray, width: Int, height: Int, radius: Int): FloatArray {
    val integral = LongArray((width + 1) * (height + 1))
    for (y in 0 until height) {
        for (x in 0 until width) {
            integral[(y + 1) * (width + 1) + (x + 1)] =
                src[y * width + x].toLong() +
                        integral[y * (width + 1) + (x + 1)] +
                        integral[(y + 1) * (width + 1) + x] -
                        integral[y * (width + 1) + x]
        }
    }
    val out = FloatArray(width * height)
    for (y in 0 until height) {
        val y0 = (y - radius).coerceAtLeast(0)
        val y1 = (y + radius + 1).coerceAtMost(height)
        for (x in 0 until width) {
            val x0 = (x - radius).coerceAtLeast(0)
            val x1 = (x + radius + 1).coerceAtMost(width)
            val area = ((x1 - x0) * (y1 - y0)).toLong().coerceAtLeast(1L)
            val sum = integral[y1 * (width + 1) + x1] -
                    integral[y0 * (width + 1) + x1] -
                    integral[y1 * (width + 1) + x0] +
                    integral[y0 * (width + 1) + x0]
            out[y * width + x] = sum.toFloat() / area
        }
    }
    return out
}

private fun morphologicalOpen(bin: IntArray, width: Int, height: Int, size: Int): IntArray {
    val eroded = morphologyErode(bin, width, height, size)
    return morphologyDilate(eroded, width, height, size)
}

private fun morphologicalClose(bin: IntArray, width: Int, height: Int, size: Int): IntArray {
    val dilated = morphologyDilate(bin, width, height, size)
    return morphologyErode(dilated, width, height, size)
}

private fun morphologyErode(bin: IntArray, width: Int, height: Int, size: Int): IntArray {
    val n = width * height
    val out = IntArray(n)
    val half = size / 2
    for (y in 0 until height) {
        for (x in 0 until width) {
            var minV = 255
            for (dy in -half..half) {
                for (dx in -half..half) {
                    val px = (x + dx).coerceIn(0, width - 1)
                    val py = (y + dy).coerceIn(0, height - 1)
                    val v = bin[py * width + px]
                    if (v < minV) minV = v
                }
            }
            out[y * width + x] = minV
        }
    }
    return out
}

private fun morphologyDilate(bin: IntArray, width: Int, height: Int, size: Int): IntArray {
    val n = width * height
    val out = IntArray(n)
    val half = size / 2
    for (y in 0 until height) {
        for (x in 0 until width) {
            var maxV = 0
            for (dy in -half..half) {
                for (dx in -half..half) {
                    val px = (x + dx).coerceIn(0, width - 1)
                    val py = (y + dy).coerceIn(0, height - 1)
                    val v = bin[py * width + px]
                    if (v > maxV) maxV = v
                }
            }
            out[y * width + x] = maxV
        }
    }
    return out
}

private fun sharpenArgb(
    argb: IntArray, width: Int, height: Int, amount: Float,
): IntArray {
    val n = width * height
    val blurred = IntArray(n)
    val kernel = floatArrayOf(
        1f / 9, 1f / 9, 1f / 9,
        1f / 9, 1f / 9, 1f / 9,
        1f / 9, 1f / 9, 1f / 9,
    )
    for (y in 0 until height) {
        for (x in 0 until width) {
            var rr = 0f; var gg = 0f; var bb = 0f
            var k = 0
            for (dy in -1..1) {
                for (dx in -1..1) {
                    val px = (x + dx).coerceIn(0, width - 1)
                    val py = (y + dy).coerceIn(0, height - 1)
                    val c = argb[py * width + px]
                    rr += ((c shr 16) and 0xFF) * kernel[k]
                    gg += ((c shr 8) and 0xFF) * kernel[k]
                    bb += (c and 0xFF) * kernel[k]
                    k++
                }
            }
            blurred[y * width + x] = packArgb(rr, gg, bb, 255)
        }
    }

    val out = IntArray(n)
    for (i in 0 until n) {
        val orig = argb[i]
        val blur = blurred[i]
        val r0 = (orig shr 16) and 0xFF
        val g0 = (orig shr 8) and 0xFF
        val b0 = orig and 0xFF
        val rb = (blur shr 16) and 0xFF
        val gb = (blur shr 8) and 0xFF
        val bb2 = blur and 0xFF

        val nr = r0 + (r0 - rb) * amount
        val ng = g0 + (g0 - gb) * amount
        val nb = b0 + (b0 - bb2) * amount
        out[i] = packArgb(nr, ng, nb, 255)
    }
    return out
}

private fun packArgb(r: Float, g: Float, b: Float, a: Int): Int {
    val ri = r.toInt().coerceIn(0, 255)
    val gi = g.toInt().coerceIn(0, 255)
    val bi = b.toInt().coerceIn(0, 255)
    return (a shl 24) or (ri shl 16) or (gi shl 8) or bi
}

private fun applyPerspectiveTransform(src: Bitmap, quadNormalized: List<Offset>): Bitmap {
    if (quadNormalized.size != 4) return src

    val MAX_SRC_EDGE = 2600f
    val srcScale = if (src.width > MAX_SRC_EDGE || src.height > MAX_SRC_EDGE) {
        MAX_SRC_EDGE / maxOf(src.width.toFloat(), src.height.toFloat())
    } else 1f
    val workSrc: Bitmap = if (srcScale < 1f) {
        try {
            Bitmap.createScaledBitmap(
                src,
                (src.width * srcScale).toInt().coerceAtLeast(1),
                (src.height * srcScale).toInt().coerceAtLeast(1),
                true,
            )
        } catch (_: OutOfMemoryError) {
            return src
        } catch (_: Exception) {
            return src
        }
    } else src

    val workW = workSrc.width.toFloat()
    val workH = workSrc.height.toFloat()

    val lx0 = quadNormalized[0].x * workW
    val ly0 = quadNormalized[0].y * workH
    val rx0 = quadNormalized[1].x * workW
    val ry0 = quadNormalized[1].y * workH
    val rx1 = quadNormalized[2].x * workW
    val ry1 = quadNormalized[2].y * workH
    val lx1 = quadNormalized[3].x * workW
    val ly1 = quadNormalized[3].y * workH

    fun dist(ax: Float, ay: Float, bx: Float, by: Float): Float {
        val dx = ax - bx
        val dy = ay - by
        return kotlin.math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
    }

    val topW = dist(lx0, ly0, rx0, ry0)
    val bottomW = dist(lx1, ly1, rx1, ry1)
    val leftH = dist(lx0, ly0, lx1, ly1)
    val rightH = dist(rx0, ry0, rx1, ry1)

    var outW = ((topW + bottomW) / 2f).toInt().coerceAtLeast(1)
    var outH = ((leftH + rightH) / 2f).toInt().coerceAtLeast(1)

    // ★ 输出尺寸同样限高，避免 Bitmap.createBitmap 直接 OOM
    val MAX_OUT_EDGE = 3000
    if (outW > MAX_OUT_EDGE || outH > MAX_OUT_EDGE) {
        val s = MAX_OUT_EDGE.toFloat() / maxOf(outW, outH)
        outW = (outW * s).toInt().coerceAtLeast(1)
        outH = (outH * s).toInt().coerceAtLeast(1)
    }

    val isIdentity =
        kotlin.math.abs(quadNormalized[0].x) < 0.001f &&
                kotlin.math.abs(quadNormalized[0].y) < 0.001f &&
                kotlin.math.abs(quadNormalized[1].x - 1f) < 0.001f &&
                kotlin.math.abs(quadNormalized[1].y) < 0.001f &&
                kotlin.math.abs(quadNormalized[2].x - 1f) < 0.001f &&
                kotlin.math.abs(quadNormalized[2].y - 1f) < 0.001f &&
                kotlin.math.abs(quadNormalized[3].x) < 0.001f &&
                kotlin.math.abs(quadNormalized[3].y - 1f) < 0.001f
    if (isIdentity) {
        return if (workSrc !== src) workSrc else src
    }

    val srcPoints = floatArrayOf(lx0, ly0, rx0, ry0, rx1, ry1, lx1, ly1)
    val dstPoints = floatArrayOf(
        0f, 0f,
        outW.toFloat(), 0f,
        outW.toFloat(), outH.toFloat(),
        0f, outH.toFloat(),
    )

    return try {
        val matrix = android.graphics.Matrix()
        val ok = matrix.setPolyToPoly(srcPoints, 0, dstPoints, 0, 4)
        if (!ok) {
            return if (workSrc !== src) workSrc else src
        }

        val out = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(out)
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            isFilterBitmap = true
        }
        canvas.drawBitmap(workSrc, matrix, paint)
        // 中间缩放图处理完即可释放，减轻后续滤镜的内存压力
        if (workSrc !== src) {
            try { workSrc.recycle() } catch (_: Exception) {}
        }
        out
    } catch (_: OutOfMemoryError) {
        if (workSrc !== src) workSrc else src
    } catch (_: Exception) {
        if (workSrc !== src) workSrc else src
    }
}

private fun cropBitmapToFrame(
    src: Bitmap,
    frameWidthPx: Float,
    frameHeightPx: Float,
    screenWidthPx: Float,
    screenHeightPx: Float
): Bitmap {
    val imgW = src.width.toFloat()
    val imgH = src.height.toFloat()
    val scale = maxOf(screenWidthPx / imgW, screenHeightPx / imgH)
    val dx = (screenWidthPx - imgW * scale) / 2f
    val dy = (screenHeightPx - imgH * scale) / 2f

    val screenLeft = screenWidthPx / 2f - frameWidthPx / 2f
    val screenTop = screenHeightPx / 2f - frameHeightPx / 2f

    val imgX = (screenLeft - dx) / scale
    val imgY = (screenTop - dy) / scale
    val imgW_crop = frameWidthPx / scale
    val imgH_crop = frameHeightPx / scale

    val x = imgX.roundToInt().coerceIn(0, src.width - 1)
    val y = imgY.roundToInt().coerceIn(0, src.height - 1)
    val w = imgW_crop.roundToInt().coerceIn(1, src.width - x)
    val h = imgH_crop.roundToInt().coerceIn(1, src.height - y)

    return Bitmap.createBitmap(src, x, y, w, h)
}

private fun isPureNumber(text: String): Boolean {
    val cleanText = text.replace(Regex("[\\s\\p{Punct}]"), "")
    return cleanText.isNotEmpty() && cleanText.all { it.isDigit() }
}

private fun isMostlyChinese(text: String): Boolean {
    var chineseCount = 0
    var letterCount = 0
    for (char in text) {
        if (char.isLetter()) {
            letterCount++
            if (char.code in 0x4E00..0x9FFF) {
                chineseCount++
            }
        }
    }
    return letterCount > 0 && (chineseCount.toFloat() / letterCount) > 0.5f
}

private fun createTranslatedBitmap(
    original: Bitmap,
    blocks: List<TranslatedBlock>
): Bitmap {
    val newBmp = original.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = android.graphics.Canvas(newBmp)

    val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
    }
    val bgPaint = android.graphics.Paint().apply {
        style = android.graphics.Paint.Style.FILL
        isAntiAlias = true
    }

    blocks.forEach { block ->
        val rect = block.boundingBox
        val left = (rect.left - 6).coerceIn(0, original.width - 1)
        val top = (rect.top - 6).coerceIn(0, original.height - 1)
        val right = (rect.right + 6).coerceIn(0, original.width)
        val bottom = (rect.bottom + 6).coerceIn(0, original.height)
        val w = right - left
        val h = bottom - top

        if (w <= 0 || h <= 0) return@forEach

        var rSum = 0; var gSum = 0; var bSum = 0; var count = 0
        val samplePoints = listOf(
            android.graphics.Point(left, top), android.graphics.Point(right, top),
            android.graphics.Point(left, bottom), android.graphics.Point(right, bottom),
            android.graphics.Point(left + w / 2, top), android.graphics.Point(left + w / 2, bottom),
            android.graphics.Point(left, top + h / 2), android.graphics.Point(right, top + h / 2)
        )
        for (p in samplePoints) {
            if (p.x in 0 until newBmp.width && p.y in 0 until newBmp.height) {
                val pixel = newBmp.getPixel(p.x, p.y)
                val lum = 0.299 * android.graphics.Color.red(pixel) + 0.587 * android.graphics.Color.green(pixel) + 0.114 * android.graphics.Color.blue(pixel)
                if (lum > 30 && lum < 225) {
                    rSum += android.graphics.Color.red(pixel)
                    gSum += android.graphics.Color.green(pixel)
                    bSum += android.graphics.Color.blue(pixel)
                    count++
                }
            }
        }

        val avgR = if (count > 0) rSum / count else 128
        val avgG = if (count > 0) gSum / count else 128
        val avgB = if (count > 0) bSum / count else 128

        bgPaint.color = android.graphics.Color.rgb(avgR, avgG, avgB)
        canvas.drawRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), bgPaint)

        val lum = 0.299 * avgR + 0.587 * avgG + 0.114 * avgB
        if (lum > 128) {
            textPaint.color = android.graphics.Color.parseColor("#111111")
            textPaint.setShadowLayer(2f, 0f, 1f, android.graphics.Color.parseColor("#33FFFFFF"))
        } else {
            textPaint.color = android.graphics.Color.WHITE
            textPaint.setShadowLayer(3f, 0f, 1.5f, android.graphics.Color.parseColor("#66000000"))
        }

        var textSize = h * 0.7f
        if (textSize < 10f) textSize = 10f
        textPaint.textSize = textSize

        val textWidth = textPaint.measureText(block.translatedText)
        if (textWidth > w) {
            textSize = textSize * (w / textWidth)
            textPaint.textSize = textSize
        }

        val x = left + w / 2f
        val y = top + h / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(block.translatedText, x, y, textPaint)
    }

    return newBmp
}

private fun saveDocumentToGallery(context: Context, bitmap: Bitmap): Boolean {
    val name = "Mote_Scan_${System.currentTimeMillis()}.jpg"
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, name)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Mote")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }
    val resolver = context.contentResolver
    val uri = try {
        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    } catch (e: Exception) { null } ?: return false

    return try {
        resolver.openOutputStream(uri)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        } ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            resolver.update(uri, ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) }, null, null)
        }
        true
    } catch (e: Exception) {
        try { resolver.delete(uri, null, null) } catch (_: Exception) {}
        false
    }
}

@Suppress("NewApi")
@Composable
fun ScannerScreen(
    onCodeScanned: (String) -> Unit,
    onClose: () -> Unit,
    autoRequestPermission: Boolean = true,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val scannerNavOwner = rememberNavigationEventDispatcherOwner()

    val prefs = remember { context.getSharedPreferences("mote_prefs", Context.MODE_PRIVATE) }
    var materialStyle by remember { mutableIntStateOf(prefs.getInt("ui_material_style", 2)) }
    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == "ui_material_style") {
                materialStyle = prefs.getInt(key, 2)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    val blurSupported = isRuntimeShaderSupported()
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(Color.Black)
            drawContent()
        }
    } else null

    var mode by remember { mutableStateOf(ScannerMode.SCAN) }
    var sourceLang by remember { mutableStateOf(SOURCE_LANG_AUTO) }
    var targetLang by remember { mutableStateOf(TARGET_LANG_ZH) }
    var scanFilter by remember { mutableStateOf(ScanFilter.ORIGINAL) }

    var translatedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isTranslating by remember { mutableStateOf(false) }
    var translateError by remember { mutableStateOf<String?>(null) }
    var pickedImageMode by remember { mutableStateOf(ScannerMode.SCAN) }

    var perspectiveEditing by remember { mutableStateOf(false) }

    // 「提取」：把照片和识别出的文字一起精排后展示
    var extractBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var extractText by remember { mutableStateOf("") }
    var isExtracting by remember { mutableStateOf(false) }
    var isGeneratingScan by remember { mutableStateOf(false) }
    var perspectiveQuad by remember {
        mutableStateOf(
            listOf(
                Offset(0f, 0f),
                Offset(1f, 0f),
                Offset(1f, 1f),
                Offset(0f, 1f),
            )
        )
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED
        )
    }
    var cameraRef by remember { mutableStateOf<Camera?>(null) }
    var torchOn by remember { mutableStateOf(false) }
    var cameraProviderRef by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var analyzerExecutorRef by remember {
        mutableStateOf<java.util.concurrent.ExecutorService?>(null)
    }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }

    val detectedCodes = remember { mutableStateListOf<DetectedCode>() }
    val displayCodes = remember { mutableStateListOf<DetectedCode>() }
    var noDetectFrameCount by remember { mutableIntStateOf(0) }
    val autoPoppedCodes = remember { mutableSetOf<String>() }
    var textDialogContent by remember { mutableStateOf<String?>(null) }
    var wifiDialogInfo by remember { mutableStateOf<WifiQrInfo?>(null) }
    var textDialogDisplay by remember { mutableStateOf("") }
    var textDialogEverShown by remember { mutableStateOf(false) }
    var wifiDialogEverShown by remember { mutableStateOf(false) }
    var pickedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val pickedImageCodes = remember { mutableStateListOf<DetectedCode>() }

    val imageRotationX = remember { Animatable(90f) }
    val imageAlpha = remember { Animatable(0f) }
    var imageCodesRevealed by remember { mutableStateOf(false) }

    var swipeOffset by remember { mutableFloatStateOf(0f) }

    SideEffect {
        val activity = context as? Activity
        val window = activity?.window
        if (window != null) {
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
            window.statusBarColor = android.graphics.Color.TRANSPARENT
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try { cameraProviderRef?.unbindAll() } catch (_: Exception) {}
            try { analyzerExecutorRef?.shutdown() } catch (_: Exception) {}
            cameraProviderRef = null
            analyzerExecutorRef = null
            cameraRef = null
            imageCaptureRef = null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }
    LaunchedEffect(Unit) {
        if (autoRequestPermission && !hasPermission) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }

    var pendingWifiInfo by remember { mutableStateOf<WifiQrInfo?>(null) }
    val locationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val info = pendingWifiInfo
        pendingWifiInfo = null
        if (granted && info != null) {
            connectToWifi(context, info)
        } else if (!granted) {
            Toast.makeText(context, "需要位置权限才能连接 WiFi", Toast.LENGTH_LONG).show()
        }
    }

    fun tryConnectWifi(info: WifiQrInfo) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(context, "Android 10 以下暂不支持直接连接 WIFI", Toast.LENGTH_LONG).show()
            return
        }
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            connectToWifi(context, info)
        } else {
            pendingWifiInfo = info
            locationPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    fun processTranslateCapture(bmp: Bitmap) {
        scope.launch {
            isTranslating = true
            translatedBitmap = null
            translateError = null
            try {
                val ocrResult = withContext(Dispatchers.IO) {
                    recognizeTextAndDetectLanguage(context, bmp)
                }
                if (ocrResult.blocks.isEmpty()) {
                    translateError = "未识别到文字"
                    isTranslating = false
                    return@launch
                }
                val effectiveTarget = computeEffectiveTargetLang(
                    userSource = sourceLang,
                    userTarget = targetLang,
                    detectedLangName = ocrResult.detectedLanguageName,
                )

                val blocksToTranslate = ocrResult.blocks.filter { block ->
                    val isTargetZh = effectiveTarget == TARGET_LANG_ZH
                    val isTextZh = isMostlyChinese(block.text)
                    val isPureNum = isPureNumber(block.text)
                    !(isTargetZh && isTextZh) && !isPureNum
                }

                if (blocksToTranslate.isEmpty()) {
                    translateError = "无需翻译"
                    isTranslating = false
                    return@launch
                }

                val translatedTexts = withContext(Dispatchers.IO) {
                    translateTextsWithLang(
                        texts = blocksToTranslate.map { it.text },
                        sourceLangName = sourceLang,
                        targetLangName = effectiveTarget,
                    )
                }
                val newBlocks = blocksToTranslate.mapIndexed { idx, block ->
                    TranslatedBlock(
                        translatedText = translatedTexts.getOrElse(idx) { block.text },
                        boundingBox = block.boundingBox
                    )
                }

                val pBmp = withContext(Dispatchers.Default) {
                    createTranslatedBitmap(bmp, newBlocks)
                }

                withContext(Dispatchers.Main) {
                    translatedBitmap = pBmp
                }
            } catch (e: Exception) {
                translateError = "翻译失败：${e.message ?: "未知错误"}"
            } finally {
                isTranslating = false
            }
        }
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenW = configuration.screenWidthDp.dp
    val screenH = configuration.screenHeightDp.dp

    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val topReserve = statusBarHeight + 40.dp + 16.dp
    val bottomReserve = navBarHeight + 40.dp + 24.dp + 72.dp + 24.dp
    val availableH = (screenH - topReserve - bottomReserve).coerceAtLeast(100.dp)
    val availableW = (screenW - 24.dp).coerceAtLeast(100.dp)
    val isPortrait = screenH > screenW
    val targetRatio = if (isPortrait) 3f / 4f else 4f / 3f
    val transW = if (availableW / availableH > targetRatio) availableH * targetRatio else availableW
    val transH = transW / targetRatio
    val scanW = 260.dp
    val scanH = 260.dp
    val currentFrameW = if (mode == ScannerMode.SCAN) scanW else transW
    val currentFrameH = if (mode == ScannerMode.SCAN) scanH else transH

    fun takePhoto() {
        val capture = imageCaptureRef
        if (capture == null) {
            Toast.makeText(context, "相机未就绪", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            capture.takePicture(
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        try {
                            val bmp = imageProxyToBitmap(image)
                            if (bmp != null) {
                                val d = density.density
                                val targetW = if (mode == ScannerMode.SCAN) scanW else transW
                                val targetH = if (mode == ScannerMode.SCAN) scanH else transH
                                val croppedBmp = cropBitmapToFrame(
                                    src = bmp,
                                    frameWidthPx = targetW.value * d,
                                    frameHeightPx = targetH.value * d,
                                    screenWidthPx = screenW.value * d,
                                    screenHeightPx = screenH.value * d
                                )

                                pickedImageBitmap = croppedBmp
                                pickedImageMode = mode
                                pickedImageCodes.clear()
                                translatedBitmap = null
                                translateError = null

                                if (mode == ScannerMode.SCAN_DOC) {
                                    perspectiveQuad = listOf(
                                        Offset(0f, 0f), Offset(1f, 0f),
                                        Offset(1f, 1f), Offset(0f, 1f)
                                    )
                                    perspectiveEditing = true
                                } else {
                                    perspectiveEditing = false
                                    processTranslateCapture(croppedBmp)
                                }
                            } else {
                                Toast.makeText(context, "拍照失败", Toast.LENGTH_SHORT).show()
                            }
                        } catch (_: Exception) {
                            Toast.makeText(context, "拍照失败", Toast.LENGTH_SHORT).show()
                        } finally {
                            try { image.close() } catch (_: Exception) {}
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(context, "拍照失败：${exception.message ?: "未知"}", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        } catch (_: Exception) {
            Toast.makeText(context, "拍照失败", Toast.LENGTH_SHORT).show()
        }
    }

    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.contentResolver.openInputStream(uri)?.use { ins ->
                    BitmapFactory.decodeStream(ins, null, boundsOpts)
                }
                val srcW0 = boundsOpts.outWidth
                val srcH0 = boundsOpts.outHeight
                val sampleSize = if (srcW0 > 0 && srcH0 > 0) {
                    var s = 1
                    while (srcW0 / (s * 2) >= 3000 || srcH0 / (s * 2) >= 3000) s *= 2
                    s
                } else 1
                val decodeOpts = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val inputStream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(inputStream, null, decodeOpts)
                inputStream?.close()
                if (bmp != null) {
                    pickedImageBitmap = bmp
                    pickedImageMode = mode
                    pickedImageCodes.clear()
                    translatedBitmap = null
                    translateError = null

                    if (mode == ScannerMode.SCAN_DOC) {
                        perspectiveQuad = listOf(
                            Offset(0f, 0f), Offset(1f, 0f),
                            Offset(1f, 1f), Offset(0f, 1f)
                        )
                        perspectiveEditing = true
                    } else {
                        perspectiveEditing = false
                        if (mode == ScannerMode.SCAN) {
                            val scanner = buildBarcodeScanner()
                            val image = InputImage.fromBitmap(bmp, 0)
                            val bmpW = bmp.width.toFloat().coerceAtLeast(1f)
                            val bmpH = bmp.height.toFloat().coerceAtLeast(1f)
                            scanner.process(image)
                                .addOnSuccessListener { barcodes ->
                                    barcodes.forEach { bc ->
                                        val raw = bc.rawValue ?: return@forEach
                                        val box = bc.boundingBox ?: return@forEach
                                        val cx = ((box.left + box.right) / 2f / bmpW).coerceIn(0f, 1f)
                                        val cy = ((box.top + box.bottom) / 2f / bmpH).coerceIn(0f, 1f)
                                        pickedImageCodes.add(DetectedCode(raw, cx, cy, bc.format))
                                    }
                                    if (pickedImageCodes.isEmpty()) {
                                        Toast.makeText(context, "未识别到二维码", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(context, "识别失败", Toast.LENGTH_SHORT).show()
                                }
                        } else if (mode == ScannerMode.TRANSLATE) {
                            processTranslateCapture(bmp)
                        }
                    }
                } else {
                    Toast.makeText(context, "无法读取图片", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                Toast.makeText(context, "无法读取图片", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LaunchedEffect(torchOn) {
        try { cameraRef?.cameraControl?.enableTorch(torchOn) } catch (_: Exception) {}
    }

    LaunchedEffect(pickedImageBitmap) {
        if (pickedImageBitmap != null) {
            imageCodesRevealed = false
            imageRotationX.snapTo(90f)
            imageAlpha.snapTo(0f)
            coroutineScope {
                launch {
                    imageRotationX.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = IMAGE_FLIP_IN_MS, easing = CodeOverlayEasing),
                    )
                }
                launch {
                    imageAlpha.animateTo(targetValue = 1f, animationSpec = tween(durationMillis = 280))
                }
            }
            delay(100)
            if (pickedImageBitmap != null) {
                imageCodesRevealed = true
            }
        } else {
            imageCodesRevealed = false
        }
    }

    LaunchedEffect(detectedCodes.toList()) {
        val currentKeys = detectedCodes.map { it.rawValue }.toSet()
        detectedCodes.forEach { c ->
            val idx = displayCodes.indexOfFirst { it.rawValue == c.rawValue }
            if (idx >= 0) {
                displayCodes[idx] = c
            } else {
                displayCodes.add(c)
            }
        }
        val stale = displayCodes.filter { it.rawValue !in currentKeys }
        if (stale.isNotEmpty()) {
            delay((CODE_OVERLAY_EXIT_MS + 80).toLong())
            val nowKeys = detectedCodes.map { it.rawValue }.toSet()
            displayCodes.removeAll { it.rawValue !in nowKeys }
        }
    }

    fun handleResult(raw: String) {
        val trimmed = raw.trim()
        if (trimmed.isBlank()) return
        if (trimmed in autoPoppedCodes) return

        val wifiInfo = parseWifiQrCode(trimmed)
        if (wifiInfo != null) {
            autoPoppedCodes.add(trimmed)
            wifiDialogInfo = wifiInfo
            wifiDialogEverShown = true
            return
        }

        if (tryOpenExternal(context, trimmed)) {
            autoPoppedCodes.add(trimmed)
            onClose()
            return
        }

        val lower = trimmed.lowercase()
        if (lower.startsWith("http://") || lower.startsWith("https://")) {
            autoPoppedCodes.add(trimmed)
            onCodeScanned(trimmed)
            return
        }

        autoPoppedCodes.add(trimmed)
        textDialogContent = trimmed
        textDialogDisplay = trimmed
        textDialogEverShown = true
    }

    fun showDialogFor(raw: String) {
        val wifiInfo = parseWifiQrCode(raw)
        if (wifiInfo != null) {
            wifiDialogInfo = wifiInfo
            wifiDialogEverShown = true
        } else {
            textDialogContent = raw
            textDialogDisplay = raw
            textDialogEverShown = true
        }
    }

    fun closePickedImage() {
        scope.launch {
            coroutineScope {
                launch {
                    imageRotationX.animateTo(
                        targetValue = -90f,
                        animationSpec = tween(durationMillis = IMAGE_FLIP_OUT_MS, easing = CodeOverlayEasing),
                    )
                }
                launch {
                    imageAlpha.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = 240))
                }
            }
            pickedImageBitmap = null
            pickedImageCodes.clear()
            translatedBitmap = null
            translateError = null
            isTranslating = false
            perspectiveEditing = false
            isGeneratingScan = false
        }
    }

    BackHandler(enabled = pickedImageBitmap != null) {
        closePickedImage()
    }

    CompositionLocalProvider(LocalNavigationEventDispatcherOwner provides scannerNavOwner) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            if (hasPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (pickedImageBitmap != null && blurSupported) {
                                val blurPx = PAGE_BLUR_DP * density.density
                                renderEffect = RenderEffect.createBlurEffect(blurPx, blurPx, Shader.TileMode.CLAMP).asComposeRenderEffect()
                            }
                        }
                        .then(if (blurSupported && backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).apply {
                                scaleType = PreviewView.ScaleType.FILL_CENTER
                                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = { previewView ->
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                            cameraProviderFuture.addListener({
                                try {
                                    val cameraProvider = cameraProviderFuture.get()
                                    cameraProviderRef = cameraProvider

                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(previewView.surfaceProvider)
                                    }
                                    val analysis = ImageAnalysis.Builder()
                                        .setTargetResolution(Size(1280, 720))
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                    val activity = context as? Activity
                                    val displayRotation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        activity?.display?.rotation ?: Surface.ROTATION_0
                                    } else {
                                        @Suppress("DEPRECATION")
                                        activity?.windowManager?.defaultDisplay?.rotation ?: Surface.ROTATION_0
                                    }

                                    val imageCapture = ImageCapture.Builder()
                                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                                        .setTargetRotation(displayRotation)
                                        .build()
                                    imageCaptureRef = imageCapture

                                    val executor = Executors.newSingleThreadExecutor()
                                    analyzerExecutorRef = executor

                                    val scanner = buildBarcodeScanner()
                                    analysis.setAnalyzer(executor) { imageProxy ->
                                        if (mode == ScannerMode.TRANSLATE || mode == ScannerMode.SCAN_DOC) {
                                            imageProxy.close()
                                            return@setAnalyzer
                                        }
                                        processImageProxyMulti(scanner, imageProxy) { codes ->
                                            if (codes.isEmpty()) {
                                                noDetectFrameCount++
                                                if (noDetectFrameCount >= NO_DETECT_FRAMES_TO_CLEAR) {
                                                    detectedCodes.clear()
                                                    noDetectFrameCount = 0
                                                }
                                            } else {
                                                noDetectFrameCount = 0
                                                detectedCodes.clear()
                                                detectedCodes.addAll(codes)
                                                codes.firstOrNull()?.let { handleResult(it.rawValue) }
                                            }
                                        }
                                    }
                                    cameraProvider.unbindAll()
                                    cameraRef = cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        analysis,
                                        imageCapture,
                                    )
                                } catch (_: Exception) {}
                            }, ContextCompat.getMainExecutor(context))
                        },
                    )
                }

                AnimatedVisibility(
                    visible = pickedImageBitmap == null,
                    enter = scaleIn(animationSpec = tween(durationMillis = IMAGE_FLIP_OUT_MS, easing = CodeOverlayEasing), initialScale = 1.2f) + fadeIn(animationSpec = tween(durationMillis = IMAGE_FLIP_OUT_MS)),
                    exit = scaleOut(animationSpec = tween(durationMillis = IMAGE_FLIP_IN_MS, easing = CodeOverlayEasing), targetScale = 1.2f) + fadeOut(animationSpec = tween(durationMillis = IMAGE_FLIP_IN_MS)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(mode) {
                                detectHorizontalDragGestures(
                                    onDragStart = { swipeOffset = 0f },
                                    onDragEnd = {
                                        if (swipeOffset < -80.dp.toPx()) {
                                            if (mode == ScannerMode.SCAN) {
                                                mode = ScannerMode.TRANSLATE
                                            } else if (mode == ScannerMode.TRANSLATE) {
                                                mode = ScannerMode.SCAN_DOC
                                            }
                                        } else if (swipeOffset > 80.dp.toPx()) {
                                            if (mode == ScannerMode.SCAN_DOC) {
                                                mode = ScannerMode.TRANSLATE
                                            } else if (mode == ScannerMode.TRANSLATE) {
                                                mode = ScannerMode.SCAN
                                            }
                                        }
                                    },
                                    onHorizontalDrag = { change, dragAmount ->
                                        swipeOffset += dragAmount
                                    }
                                )
                            }
                    ) {

                        ScannerFrameOverlay(
                            mode = mode,
                            isTranslating = isTranslating,
                            translateError = translateError,
                            modifier = Modifier.fillMaxSize()
                        )

                        val visibleKeys = detectedCodes.map { it.rawValue }.toSet()
                        displayCodes.forEach { code ->
                            key(code.rawValue) {
                                CodeOverlayItem(
                                    code = code,
                                    visible = code.rawValue in visibleKeys,
                                    onClick = { showDialogFor(code.rawValue) },
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .statusBarsPadding()
                                .padding(start = 12.dp, top = 8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .then(buildScannerButtonModifier(materialStyle, blurSupported, backdrop, CircleShape))
                                .clickable { onClose() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(MiuixIcons.ChevronBackward, "返回", tint = Color.White, modifier = Modifier.size(22.dp))
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(end = 12.dp, top = 8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .then(buildScannerButtonModifier(materialStyle, blurSupported, backdrop, CircleShape))
                                .clickable { torchOn = !torchOn },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (torchOn) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
                                contentDescription = if (torchOn) "关闭手电筒" else "打开手电筒",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp),
                            )
                        }

                        TopScannerCapsule(
                            mode = mode,
                            sourceLang = sourceLang,
                            targetLang = targetLang,
                            onSourceLangChange = { sourceLang = it },
                            onTargetLangChange = { targetLang = it },
                            onSwap = {
                                if (sourceLang == SOURCE_LANG_AUTO) {
                                    Toast.makeText(context, "不支持与自动检测互换", Toast.LENGTH_SHORT).show()
                                } else {
                                    val tmp = sourceLang
                                    sourceLang = targetLang
                                    targetLang = tmp
                                }
                            },
                            selectedFilter = scanFilter,
                            onFilterChange = { scanFilter = it },
                            materialStyle = materialStyle,
                            blurSupported = blurSupported,
                            backdrop = backdrop,
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .navigationBarsPadding()
                                .padding(end = 12.dp, bottom = 32.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .then(buildScannerButtonModifier(materialStyle, blurSupported, backdrop, CircleShape))
                                .clickable { pickImageLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(MiuixIcons.Image, "从相册选择", tint = Color.White, modifier = Modifier.size(22.dp))
                        }

                        AnimatedVisibility(
                            visible = mode == ScannerMode.TRANSLATE || mode == ScannerMode.SCAN_DOC,
                            enter = fadeIn(tween(240)) + scaleIn(tween(280, easing = CodeOverlayEasing), initialScale = 0.6f, transformOrigin = TransformOrigin(0.5f, 1f)),
                            exit = fadeOut(tween(180)) + scaleOut(tween(220, easing = CodeOverlayEasing), targetScale = 0.6f, transformOrigin = TransformOrigin(0.5f, 1f)),
                            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 32.dp + 48.dp + 16.dp),
                        ) {
                            TranslateShutterButton(onClick = { takePhoto() })
                        }

                        Box(
                            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 32.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            BottomModeNav(
                                currentMode = mode,
                                onModeChange = { mode = it },
                                materialStyle = materialStyle,
                                blurSupported = blurSupported,
                                backdrop = backdrop,
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(text = "需要相机权限才能使用扫一扫", color = Color.White)
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.primary, contentColor = Color.White),
                        ) { Text("授予权限") }
                    }
                }
            }

            if (pickedImageBitmap != null) {
                val bmp = pickedImageBitmap!!
                val displayBmp = if (pickedImageMode == ScannerMode.TRANSLATE && translatedBitmap != null) {
                    translatedBitmap!!
                } else {
                    bmp
                }

                val screenCornerRadius = 8.dp
                val paddingH = 12.dp
                val paddingV = 72.dp

                Box(modifier = Modifier.fillMaxSize().graphicsLayer {
                    rotationX = imageRotationX.value
                    alpha = imageAlpha.value
                    cameraDistance = 20f * density.density
                    transformOrigin = TransformOrigin.Center
                }) {
                    if (perspectiveEditing) {
                        PerspectiveEditView(
                            bitmap = bmp,
                            quad = perspectiveQuad,
                            onQuadChange = { perspectiveQuad = it },
                            paddingH = paddingH,
                            paddingV = paddingV,
                            materialStyle = materialStyle,
                            blurSupported = blurSupported,
                            backdrop = backdrop,
                            onClose = { closePickedImage() },
                            onConfirm = {
                                val src = pickedImageBitmap ?: return@PerspectiveEditView
                                val currentFilter = scanFilter
                                val currentQuad = perspectiveQuad

                                scope.launch {
                                    isGeneratingScan = true
                                    Toast.makeText(context, "正在生成中", Toast.LENGTH_SHORT).show()
                                    try {
                                        val transformed = withContext(Dispatchers.Default) {
                                            applyPerspectiveTransform(src, currentQuad)
                                        }
                                        val finalBmp = withContext(Dispatchers.Default) {
                                            applyScanFilter(transformed, currentFilter)
                                        }
                                        pickedImageBitmap = finalBmp
                                        perspectiveEditing = false
                                    } catch (_: OutOfMemoryError) {
                                        // 极端情况下（设备内存极度紧张）即使已缩放仍可能 OOM，
                                        // 退出透视编辑并提示，不闪退。
                                        Toast.makeText(
                                            context,
                                            "内存不足，无法完成处理",
                                            Toast.LENGTH_LONG,
                                        ).show()
                                        perspectiveEditing = false
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            "处理失败：${e.message ?: "未知错误"}",
                                            Toast.LENGTH_LONG,
                                        ).show()
                                        perspectiveEditing = false
                                    } finally {
                                        isGeneratingScan = false
                                    }
                                }
                            },
                        )
                    } else {
                        BoxWithConstraints(
                            modifier = Modifier.fillMaxSize().padding(horizontal = paddingH, vertical = paddingV),
                            contentAlignment = Alignment.Center,
                        ) {
                            val availW = maxWidth
                            val availH = maxHeight
                            val availWVal = availW.value
                            val availHVal = availH.value
                            val bmpRatio = displayBmp.width.toFloat() / displayBmp.height.toFloat().coerceAtLeast(1f)
                            val availRatio = if (availHVal > 0f) availWVal / availHVal else bmpRatio

                            val drawW: Dp
                            val drawH: Dp
                            if (bmpRatio >= availRatio) {
                                drawW = availW
                                drawH = availW / bmpRatio
                            } else {
                                drawH = availH
                                drawW = availH * bmpRatio
                            }

                            Box(modifier = Modifier.size(drawW, drawH).clip(RoundedCornerShape(screenCornerRadius))) {
                                Image(bitmap = displayBmp.asImageBitmap(), contentDescription = "选择的图片", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.FillBounds)

                                if (isGeneratingScan) {
                                    val infiniteTransition = rememberInfiniteTransition(label = "scan")
                                    val scanLineY by infiniteTransition.animateFloat(
                                        initialValue = 0f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(1200, easing = LinearEasing),
                                            repeatMode = RepeatMode.Restart
                                        ),
                                        label = "scanLineY"
                                    )

                                    // 提前提取主题色，避免在 Canvas 的 DrawScope 中调用 @Composable
                                    val primaryColor = MiuixTheme.colorScheme.primary

                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val y = size.height * scanLineY
                                        val gradient = Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                primaryColor.copy(alpha = 0.15f),
                                                primaryColor.copy(alpha = 0.6f),
                                                primaryColor,
                                                primaryColor.copy(alpha = 0.6f),
                                                primaryColor.copy(alpha = 0.15f),
                                                Color.Transparent
                                            ),
                                            startY = y - 60.dp.toPx(),
                                            endY = y + 60.dp.toPx()
                                        )
                                        drawRect(
                                            brush = gradient,
                                            topLeft = Offset(0f, y - 60.dp.toPx()),
                                            size = GeometrySize(size.width, 120.dp.toPx())
                                        )
                                    }
                                }
                            }
                        }

                        if (pickedImageMode == ScannerMode.SCAN) {
                            pickedImageCodes.forEach { code ->
                                key(code.rawValue) {
                                    AnimatedVisibility(
                                        visible = imageCodesRevealed,
                                        enter = scaleIn(tween(IMAGE_FLIP_OUT_MS, easing = CodeOverlayEasing), initialScale = 1.2f) + fadeIn(tween(260)),
                                        exit = fadeOut(tween(140)) + scaleOut(tween(CODE_OVERLAY_EXIT_MS, easing = CodeOverlayEasing), targetScale = 0f),
                                    ) {
                                        CodeOverlayButton(xFraction = code.xFraction, yFraction = code.yFraction, onClick = { showDialogFor(code.rawValue) })
                                    }
                                }
                            }
                        }

                        if (pickedImageMode == ScannerMode.TRANSLATE && isTranslating) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                InfiniteProgressIndicator(color = Color.White, size = 40.dp, strokeWidth = 3.dp)
                            }
                        }

                        if (pickedImageMode == ScannerMode.TRANSLATE && translateError != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 80.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                Text(text = translateError!!, color = Color(0xFFFF6B6B), fontSize = 13.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .statusBarsPadding()
                                .padding(start = 12.dp, top = 8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .then(buildScannerButtonModifier(materialStyle, blurSupported, backdrop, CircleShape))
                                .clickable { closePickedImage() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(MiuixIcons.ChevronBackward, "关闭", tint = Color.White, modifier = Modifier.size(22.dp))
                        }

                        if (pickedImageMode != ScannerMode.TRANSLATE) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .navigationBarsPadding()
                                    .padding(start = 12.dp, bottom = 32.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .then(buildScannerButtonModifier(materialStyle, blurSupported, backdrop, CircleShape))
                                    .clickable {
                                        if (!isExtracting) {
                                            isExtracting = true
                                            scope.launch {
                                                val text = withContext(Dispatchers.IO) {
                                                    runCatching {
                                                        recognizeTextAndDetectLanguage(context, displayBmp).fullText
                                                    }.getOrDefault("")
                                                }
                                                isExtracting = false
                                                if (text.isBlank()) {
                                                    Toast.makeText(context, "未识别到文字", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    extractBitmap = displayBmp
                                                    extractText = text
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(MiuixIcons.Notes, "提取", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .navigationBarsPadding()
                                .padding(end = 12.dp, bottom = 32.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .then(buildScannerButtonModifier(materialStyle, blurSupported, backdrop, CircleShape))
                                .clickable {
                                    val ok = saveDocumentToGallery(context, displayBmp)
                                    if (ok) Toast.makeText(context, "图片已保存到本地", Toast.LENGTH_SHORT).show()
                                    else Toast.makeText(context, "保存失败", Toast.LENGTH_SHORT).show()
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(MiuixIcons.Download, "保存", tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }
        }

        if (textDialogEverShown) {
            TextResultDialog(show = textDialogContent != null, content = textDialogDisplay, onClose = { textDialogContent = null })
        }
        val extractBmp = extractBitmap
        if (extractBmp != null) {
            ExtractResultDialog(
                show = true,
                bitmap = extractBmp,
                text = extractText,
                onClose = { extractBitmap = null },
            )
        }
        if (wifiDialogEverShown) {
            WifiResultDialog(show = wifiDialogInfo != null, info = wifiDialogInfo, onConnect = { wifiDialogInfo?.let { tryConnectWifi(it) }; wifiDialogInfo = null }, onClose = { wifiDialogInfo = null })
        }
    }
}

@Composable
private fun PerspectiveEditView(
    bitmap: Bitmap,
    quad: List<Offset>,
    onQuadChange: (List<Offset>) -> Unit,
    paddingH: Dp,
    paddingV: Dp,
    materialStyle: Int,
    blurSupported: Boolean,
    backdrop: LayerBackdrop?,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) {
    val density = LocalDensity.current
    val primaryColor = MiuixTheme.colorScheme.primary

    // 内部状态维护拖拽，避免高频重组打断手势
    var localQuad by remember { mutableStateOf(quad) }
    LaunchedEffect(quad) {
        if (quad != localQuad) {
            localQuad = quad
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 加大外层边距，确保图片缩小，四角不会被按钮遮挡
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = maxOf(paddingH, 40.dp),
                    vertical = maxOf(paddingV, 80.dp)
                ),
            contentAlignment = Alignment.Center,
        ) {
            val availW = maxWidth
            val availH = maxHeight
            val availWVal = availW.value
            val availHVal = availH.value
            val bmpRatio = bitmap.width.toFloat() / bitmap.height.toFloat().coerceAtLeast(1f)
            val availRatio = if (availHVal > 0f) availWVal / availHVal else bmpRatio

            val drawW: Dp
            val drawH: Dp
            if (bmpRatio >= availRatio) {
                drawW = availW
                drawH = availW / bmpRatio
            } else {
                drawH = availH
                drawW = availH * bmpRatio
            }

            val drawWpx = with(density) { drawW.toPx() }
            val drawHpx = with(density) { drawH.toPx() }
            val availWpx = with(density) { availW.toPx() }
            val availHpx = with(density) { availH.toPx() }

            // 计算图片在父容器中的居中偏移
            val offsetX = (availWpx - drawWpx) / 2f
            val offsetY = (availHpx - drawHpx) / 2f

            // 底层图片
            Box(modifier = Modifier.size(drawW, drawH)) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "调整透视",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            // 覆盖全屏的触摸与绘制层
            // ★ 修复：pointerInput 的 key 从 Unit 改为 (offsetX, offsetY, drawWpx, drawHpx)，
            //   确保首次测量完成、这些值确定后 lambda 才启动；否则首次组合时
            //   BoxWithConstraints 的 maxWidth/maxHeight 尚未稳定，offsetX/offsetY 会被算错，
            //   导致四个角的命中判定全部失败（表现为「拖四角没反应，拖内部有反应」）。
            //   同时内部判定改为直接使用 Canvas 坐标系（不减去 offsetX/offsetY），
            //   彻底消除整体偏移对命中判定的影响。
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(offsetX, offsetY, drawWpx, drawHpx) {
                        var dragType = -1
                        // 超大判定范围，让边缘更容易拖到
                        val thresholdCorner = 100.dp.toPx()
                        val thresholdEdge = 40.dp.toPx()

                        var startQuad = emptyList<Offset>()
                        var startTouch = Offset.Zero
                        var dragDelta = Offset.Zero

                        fun distToSegment(p: Offset, a: Offset, b: Offset): Float {
                            val l2 = (a - b).getDistanceSquared()
                            if (l2 == 0f) return (p - a).getDistance()
                            var t = ((p - a).x * (b - a).x + (p - a).y * (b - a).y) / l2
                            t = t.coerceIn(0f, 1f)
                            val proj = a + (b - a) * t
                            return (p - proj).getDistance()
                        }

                        fun isPointInQuad(pt: Offset, q: List<Offset>): Boolean {
                            var inside = false
                            var j = 3
                            for (i in 0..3) {
                                val xi = q[i].x
                                val yi = q[i].y
                                val xj = q[j].x
                                val yj = q[j].y
                                if (((yi > pt.y) != (yj > pt.y)) &&
                                    (pt.x < (xj - xi) * (pt.y - yi) / (yj - yi) + xi)
                                ) {
                                    inside = !inside
                                }
                                j = i
                            }
                            return inside
                        }

                        detectDragGestures(
                            onDragStart = { offset ->
                                // 触摸点本身就在 Canvas 坐标系里（Canvas 填满内容区），
                                // 而 offsetX/offsetY 是图片相对 Canvas 的偏移，
                                // 所以相减后得到的就是「相对图片左上角」的坐标，
                                // 与 pts（同样相对图片左上角）同一坐标系。
                                val localOffset = Offset(offset.x - offsetX, offset.y - offsetY)

                                val pts = localQuad.map { Offset(it.x * drawWpx, it.y * drawHpx) }
                                dragType = -1
                                startQuad = localQuad
                                startTouch = localOffset
                                dragDelta = Offset.Zero

                                // 1. 优先判定角点
                                var minDist = thresholdCorner
                                pts.forEachIndexed { idx, pt ->
                                    val d = (localOffset - pt).getDistance()
                                    if (d < minDist) {
                                        minDist = d
                                        dragType = idx
                                    }
                                }

                                // 2. 其次判定边
                                if (dragType == -1) {
                                    val edges = listOf(0 to 1, 1 to 2, 2 to 3, 3 to 0)
                                    var minEdgeDist = thresholdEdge
                                    edges.forEachIndexed { idx, (a, b) ->
                                        val d = distToSegment(localOffset, pts[a], pts[b])
                                        if (d < minEdgeDist) {
                                            minEdgeDist = d
                                            dragType = 4 + idx
                                        }
                                    }
                                }

                                // 3. 最后判定内部平移
                                if (dragType == -1 && isPointInQuad(localOffset, pts)) {
                                    dragType = -2
                                }
                            },
                            onDrag = { change, amount ->
                                if (dragType != -1) {
                                    change.consume()
                                    dragDelta += amount

                                    val minGap = 0.05f
                                    val ndx = dragDelta.x / drawWpx
                                    val ndy = dragDelta.y / drawHpx

                                    val q = startQuad.toMutableList()

                                    if (dragType == -2) {
                                        // === 整体平移 ===
                                        val minX = startQuad.minOf { it.x }
                                        val maxX = startQuad.maxOf { it.x }
                                        val minY = startQuad.minOf { it.y }
                                        val maxY = startQuad.maxOf { it.y }

                                        val actualDx = ndx.coerceIn(-minX, 1f - maxX)
                                        val actualDy = ndy.coerceIn(-minY, 1f - maxY)

                                        localQuad = startQuad.map { Offset(it.x + actualDx, it.y + actualDy) }
                                        return@detectDragGestures
                                    }

                                    val basePt = startQuad[dragType % 4]
                                    // 严格的 0~1 绝对限制，防飞出
                                    val targetX = (basePt.x + ndx).coerceIn(0f, 1f)
                                    val targetY = (basePt.y + ndy).coerceIn(0f, 1f)

                                    if (dragType in 0..3) {
                                        // === 角点拖动 ===
                                        when (dragType) {
                                            0 -> q[0] = Offset(targetX.coerceIn(0f, q[1].x - minGap), targetY.coerceIn(0f, q[3].y - minGap))
                                            1 -> q[1] = Offset(targetX.coerceIn(q[0].x + minGap, 1f), targetY.coerceIn(0f, q[2].y - minGap))
                                            2 -> q[2] = Offset(targetX.coerceIn(q[3].x + minGap, 1f), targetY.coerceIn(q[1].y + minGap, 1f))
                                            3 -> q[3] = Offset(targetX.coerceIn(0f, q[2].x - minGap), targetY.coerceIn(q[0].y + minGap, 1f))
                                        }
                                    } else if (dragType in 4..7) {
                                        // === 边拖动 ===
                                        // ★ 修复：整体平移边（两端同步加相同的 dx/dy），
                                        //   不再把两端强制对齐到同一坐标，
                                        //   从而保留拖四角调整出来的透视倾斜，实现「透视 + 宽高」混合裁剪。
                                        //   两端在平移过程中互不改变相对位置，斜边仍然是斜边。
                                        val idx = dragType - 4
                                        when (idx) {
                                            0 -> {
                                                // 顶边：q[0] 和 q[1] 同步沿 y 平移
                                                val limitY = minOf(q[2].y, q[3].y) - minGap
                                                val maxDy = limitY - maxOf(startQuad[0].y, startQuad[1].y)
                                                val minDy = -minOf(startQuad[0].y, startQuad[1].y)
                                                val dy = ndy.coerceIn(minDy, maxDy)
                                                q[0] = q[0].copy(y = startQuad[0].y + dy)
                                                q[1] = q[1].copy(y = startQuad[1].y + dy)
                                            }
                                            1 -> {
                                                // 右边：q[1] 和 q[2] 同步沿 x 平移
                                                val minX = maxOf(q[0].x, q[3].x) + minGap
                                                val maxDx = 1f - maxOf(startQuad[1].x, startQuad[2].x)
                                                val minDx = minX - minOf(startQuad[1].x, startQuad[2].x)
                                                val dx = ndx.coerceIn(minDx, maxDx)
                                                q[1] = q[1].copy(x = startQuad[1].x + dx)
                                                q[2] = q[2].copy(x = startQuad[2].x + dx)
                                            }
                                            2 -> {
                                                // 底边：q[2] 和 q[3] 同步沿 y 平移
                                                val minY = maxOf(q[0].y, q[1].y) + minGap
                                                val maxDy = 1f - maxOf(startQuad[2].y, startQuad[3].y)
                                                val minDy = minY - minOf(startQuad[2].y, startQuad[3].y)
                                                val dy = ndy.coerceIn(minDy, maxDy)
                                                q[2] = q[2].copy(y = startQuad[2].y + dy)
                                                q[3] = q[3].copy(y = startQuad[3].y + dy)
                                            }
                                            3 -> {
                                                // 左边：q[3] 和 q[0] 同步沿 x 平移
                                                val maxX = minOf(q[1].x, q[2].x) - minGap
                                                val maxDx = maxX - maxOf(startQuad[0].x, startQuad[3].x)
                                                val minDx = -minOf(startQuad[0].x, startQuad[3].x)
                                                val dx = ndx.coerceIn(minDx, maxDx)
                                                q[3] = q[3].copy(x = startQuad[3].x + dx)
                                                q[0] = q[0].copy(x = startQuad[0].x + dx)
                                            }
                                        }
                                    }

                                    localQuad = q
                                }
                            },
                            onDragEnd = {
                                if (dragType != -1) {
                                    onQuadChange(localQuad)
                                }
                                dragType = -1
                            },
                            onDragCancel = {
                                if (dragType != -1) {
                                    onQuadChange(localQuad)
                                }
                                dragType = -1
                            },
                        )
                    }
            ) {
                val w = drawWpx
                val h = drawHpx

                // 修复报错：直接计算绝对偏移，不再使用 translate 包裹
                val p0 = Offset(localQuad[0].x * w + offsetX, localQuad[0].y * h + offsetY)
                val p1 = Offset(localQuad[1].x * w + offsetX, localQuad[1].y * h + offsetY)
                val p2 = Offset(localQuad[2].x * w + offsetX, localQuad[2].y * h + offsetY)
                val p3 = Offset(localQuad[3].x * w + offsetX, localQuad[3].y * h + offsetY)

                // 九宫格辅助线
                val leftTop = p0 + (p3 - p0) / 3f
                val leftBottom = p0 + (p3 - p0) * (2f / 3f)
                val rightTop = p1 + (p2 - p1) / 3f
                val rightBottom = p1 + (p2 - p1) * (2f / 3f)
                val topLeft = p0 + (p1 - p0) / 3f
                val topRight = p0 + (p1 - p0) * (2f / 3f)
                val bottomLeft = p3 + (p2 - p3) / 3f
                val bottomRight = p3 + (p2 - p3) * (2f / 3f)

                val gridColor = Color.White.copy(alpha = 0.3f)
                val gridStroke = 1.dp.toPx()
                drawLine(gridColor, leftTop, rightTop, gridStroke)
                drawLine(gridColor, leftBottom, rightBottom, gridStroke)
                drawLine(gridColor, topLeft, bottomLeft, gridStroke)
                drawLine(gridColor, topRight, bottomRight, gridStroke)

                val maskPath = Path().apply {
                    fillType = PathFillType.EvenOdd
                    addRect(GeometryRect(offsetX, offsetY, offsetX + drawWpx, offsetY + drawHpx))
                    moveTo(p0.x, p0.y)
                    lineTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    close()
                }
                drawPath(maskPath, Color.Black.copy(alpha = 0.5f))

                // 边框
                val borderPath = Path().apply {
                    moveTo(p0.x, p0.y)
                    lineTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    close()
                }
                drawPath(borderPath, Color.White, style = Stroke(width = 2.dp.toPx()))

                // 角点
                listOf(p0, p1, p2, p3).forEach { p ->
                    drawCircle(Color.White, radius = 10.dp.toPx(), center = p)
                    drawCircle(primaryColor, radius = 7.dp.toPx(), center = p)
                }
            }
        }

        // 左上角关闭
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 12.dp, top = 8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .then(buildScannerButtonModifier(materialStyle, blurSupported, backdrop, CircleShape))
                .clickable { onClose() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(MiuixIcons.ChevronBackward, "关闭", tint = Color.White, modifier = Modifier.size(22.dp))
        }

        // 右下角确认
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = 12.dp, bottom = 32.dp)
                .size(40.dp)
                .clip(CircleShape)
                .then(buildScannerButtonModifier(materialStyle, blurSupported, backdrop, CircleShape))
                .clickable {
                    onQuadChange(localQuad)
                    onConfirm()
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(MiuixIcons.Ok, "确认", tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun BottomModeNav(
    currentMode: ScannerMode,
    onModeChange: (ScannerMode) -> Unit,
    materialStyle: Int,
    blurSupported: Boolean,
    backdrop: LayerBackdrop?,
) {
    val density = LocalDensity.current
    val tabWidth = 44.dp
    val tabWidthPx = with(density) { tabWidth.toPx() }
    val outerHeight = 40.dp
    val gap = 4.dp
    val gapPx = with(density) { gap.toPx() }
    val indicatorWidth = tabWidth
    val indicatorHeight = outerHeight - 8.dp

    val stepPx = tabWidthPx + gapPx

    val scope = rememberCoroutineScope()
    val initialPos = when (currentMode) {
        ScannerMode.SCAN -> 0f
        ScannerMode.TRANSLATE -> 1f
        ScannerMode.SCAN_DOC -> 2f
    }
    val indicatorPos = remember { Animatable(initialPos) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(currentMode) {
        if (!isDragging) {
            val target = when (currentMode) {
                ScannerMode.SCAN -> 0f
                ScannerMode.TRANSLATE -> 1f
                ScannerMode.SCAN_DOC -> 2f
            }
            if (indicatorPos.value != target) {
                indicatorPos.animateTo(target, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
            }
        }
    }

    val actualOffset = indicatorPos.value * stepPx

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .height(outerHeight)
            .clip(RoundedCornerShape(outerHeight / 2))
            .then(buildScannerButtonModifier(materialStyle, blurSupported, backdrop, RoundedCornerShape(outerHeight / 2)))
            .padding(4.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(actualOffset.roundToInt(), 0) }
                .width(indicatorWidth)
                .height(indicatorHeight)
                .clip(RoundedCornerShape(indicatorHeight / 2))
                .background(Color.White.copy(alpha = 0.1f)),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentWidth()
                .height(indicatorHeight)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                            scope.launch { indicatorPos.stop() }
                        },
                        onDragEnd = {
                            isDragging = false
                            val rounded = indicatorPos.value.roundToInt().coerceIn(0, 2)
                            val targetMode = when (rounded) {
                                0 -> ScannerMode.SCAN
                                1 -> ScannerMode.TRANSLATE
                                else -> ScannerMode.SCAN_DOC
                            }
                            onModeChange(targetMode)
                            val target = rounded.toFloat()
                            scope.launch {
                                indicatorPos.animateTo(target, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow))
                            }
                        },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            val currentX = indicatorPos.value * stepPx + amount
                            val fraction = (currentX / stepPx).coerceIn(0f, 2f)
                            scope.launch { indicatorPos.snapTo(fraction) }
                        }
                    )
                },
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            listOf(
                ScannerMode.SCAN to "扫码",
                ScannerMode.TRANSLATE to "翻译",
                ScannerMode.SCAN_DOC to "扫描"
            ).forEachIndexed { idx, (m, label) ->
                Box(
                    modifier = Modifier.width(tabWidth).height(indicatorHeight).clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { if (!isDragging) onModeChange(m) },
                    contentAlignment = Alignment.Center,
                ) { Text(text = label, color = Color.White, fontSize = 14.sp) }
            }
        }
    }
}

@Composable
private fun ScannerFrameOverlay(
    mode: ScannerMode,
    isTranslating: Boolean,
    translateError: String?,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenW = configuration.screenWidthDp.dp
    val screenH = configuration.screenHeightDp.dp
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val topReserve = statusBarHeight + 40.dp + 16.dp
    val bottomReserve = navBarHeight + 40.dp + 24.dp + 72.dp + 24.dp

    val availableH = (screenH - topReserve - bottomReserve).coerceAtLeast(100.dp)
    val availableW = (screenW - 24.dp).coerceAtLeast(100.dp)

    val isPortrait = screenH > screenW
    val targetRatio = if (isPortrait) 3f / 4f else 4f / 3f

    val targetTransW = if (availableW / availableH > targetRatio) availableH * targetRatio else availableW
    val targetTransH = targetTransW / targetRatio

    val scanW = 260.dp
    val scanH = 260.dp

    val isLargeFrame = mode == ScannerMode.TRANSLATE || mode == ScannerMode.SCAN_DOC

    val currentW by animateDpAsState(
        targetValue = if (isLargeFrame) targetTransW else scanW,
        animationSpec = tween(300, easing = CodeOverlayEasing),
        label = "frameW"
    )
    val currentH by animateDpAsState(
        targetValue = if (isLargeFrame) targetTransH else scanH,
        animationSpec = tween(300, easing = CodeOverlayEasing),
        label = "frameH"
    )

    val targetCenterY = topReserve + availableH / 2f
    val scanCenterY = screenH / 2f
    val currentCenterY by animateDpAsState(
        targetValue = if (isLargeFrame) targetCenterY else scanCenterY,
        animationSpec = tween(300, easing = CodeOverlayEasing),
        label = "frameCenterY"
    )

    val currentCenterX = screenW / 2f

    val cornerColor = MiuixTheme.colorScheme.primary
    val borderColor = Color.White.copy(alpha = 0.5f)
    val overlayColor = Color.Black.copy(alpha = 0.5f)

    Canvas(modifier = modifier) {
        val frameW = with(density) { currentW.toPx() }
        val frameH = with(density) { currentH.toPx() }
        val centerXPx = with(density) { currentCenterX.toPx() }
        val centerYPx = with(density) { currentCenterY.toPx() }

        val left = centerXPx - frameW / 2f
        val top = centerYPx - frameH / 2f
        val right = left + frameW
        val bottom = top + frameH
        val borderStroke = 1.5.dp.toPx()
        val cornerStroke = 4.dp.toPx()
        val cornerLength = 32.dp.toPx()

        drawRect(color = overlayColor, topLeft = Offset(0f, 0f), size = GeometrySize(size.width, top))
        drawRect(color = overlayColor, topLeft = Offset(0f, bottom), size = GeometrySize(size.width, size.height - bottom))
        drawRect(color = overlayColor, topLeft = Offset(0f, top), size = GeometrySize(left, frameH))
        drawRect(color = overlayColor, topLeft = Offset(right, top), size = GeometrySize(size.width - right, frameH))

        drawRect(color = borderColor, topLeft = Offset(left, top), size = GeometrySize(frameW, frameH), style = Stroke(width = borderStroke))

        val cornerStyle = Stroke(width = cornerStroke, cap = StrokeCap.Butt, join = StrokeJoin.Miter)
        drawPath(path = Path().apply { moveTo(left, top + cornerLength); lineTo(left, top); lineTo(left + cornerLength, top) }, color = cornerColor, style = cornerStyle)
        drawPath(path = Path().apply { moveTo(right - cornerLength, top); lineTo(right, top); lineTo(right, top + cornerLength) }, color = cornerColor, style = cornerStyle)
        drawPath(path = Path().apply { moveTo(left, bottom - cornerLength); lineTo(left, bottom); lineTo(left + cornerLength, bottom) }, color = cornerColor, style = cornerStyle)
        drawPath(path = Path().apply { moveTo(right - cornerLength, bottom); lineTo(right, bottom); lineTo(right, bottom - cornerLength) }, color = cornerColor, style = cornerStyle)
    }

    val hintText = when {
        mode == ScannerMode.SCAN -> "将二维码 / 条形码放入取景框内"
        mode == ScannerMode.SCAN_DOC -> "将文档置于框内并拍照"
        isTranslating -> "正在翻译..."
        translateError != null -> translateError
        else -> "请将文字置于框内并拍照"
    }
    val hintColor = if (translateError != null) Color(0xFFFF6B6B) else Color.White.copy(alpha = 1f - if (isLargeFrame) 1f else 0f)

    Text(
        text = hintText,
        color = hintColor,
        fontSize = 14.sp,
        modifier = Modifier
            .fillMaxWidth()
            .offset {
                val centerYPx = with(density) { currentCenterY.toPx() }
                val hPx = with(density) { currentH.toPx() }
                val offsetYPx = with(density) { 20.dp.toPx() }
                IntOffset(0, (centerYPx + hPx / 2f + offsetYPx).toInt())
            },
        textAlign = TextAlign.Center
    )
}

@Composable
private fun TopScannerCapsule(
    mode: ScannerMode,
    sourceLang: String,
    targetLang: String,
    onSourceLangChange: (String) -> Unit,
    onTargetLangChange: (String) -> Unit,
    onSwap: () -> Unit,
    selectedFilter: ScanFilter,
    onFilterChange: (ScanFilter) -> Unit,
    materialStyle: Int,
    blurSupported: Boolean,
    backdrop: LayerBackdrop?,
) {
    val visible = mode == ScannerMode.TRANSLATE || mode == ScannerMode.SCAN_DOC

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(360, easing = CodeOverlayEasing)) + scaleIn(
            tween(360, easing = CodeOverlayEasing),
            initialScale = 0.92f,
            transformOrigin = TransformOrigin(0.5f, 0f),
        ),
        exit = fadeOut(tween(360, easing = CodeOverlayEasing)) + scaleOut(
            tween(360, easing = CodeOverlayEasing),
            targetScale = 0.92f,
            transformOrigin = TransformOrigin(0.5f, 0f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier.animateContentSize(
                    animationSpec = tween(360, easing = CodeOverlayEasing),
                )
            ) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(20.dp))
                        .then(
                            buildScannerButtonModifier(
                                materialStyle,
                                blurSupported,
                                backdrop,
                                RoundedCornerShape(20.dp),
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .wrapContentSize(Alignment.TopCenter)
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                ) {
                    AnimatedContent(
                        targetState = mode,
                        transitionSpec = {
                            (
                                    fadeIn(
                                        animationSpec = tween(
                                            durationMillis = 240,
                                            delayMillis = 120,
                                            easing = CodeOverlayEasing,
                                        ),
                                    ) togetherWith fadeOut(
                                        animationSpec = tween(
                                            durationMillis = 240,
                                            delayMillis = 0,
                                            easing = CodeOverlayEasing,
                                        ),
                                    )
                                    ).using(SizeTransform(clip = false))
                        },
                        label = "topCapsuleContent",
                    ) { m ->
                        when (m) {
                            ScannerMode.TRANSLATE -> {
                                TopLanguageBarContent(
                                    sourceLang = sourceLang,
                                    targetLang = targetLang,
                                    onSourceLangChange = onSourceLangChange,
                                    onTargetLangChange = onTargetLangChange,
                                    onSwap = onSwap,
                                )
                            }
                            ScannerMode.SCAN_DOC -> {
                                TopScanFilterBarContent(
                                    selectedFilter = selectedFilter,
                                    onFilterChange = onFilterChange,
                                )
                            }
                            else -> {
                                Spacer(Modifier.height(32.dp).width(100.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopLanguageBarContent(
    sourceLang: String,
    targetLang: String,
    onSourceLangChange: (String) -> Unit,
    onTargetLangChange: (String) -> Unit,
    onSwap: () -> Unit,
) {
    var sourceExpanded by remember { mutableStateOf(false) }
    var targetExpanded by remember { mutableStateOf(false) }

    val sourceEntries = remember(sourceLang) {
        listOf(
            DropdownEntry(
                items = SOURCE_LANG_OPTIONS.map { lang ->
                    DropdownItem(
                        text = lang,
                        selected = lang == sourceLang,
                        onClick = { onSourceLangChange(lang); sourceExpanded = false }
                    )
                }
            )
        )
    }

    val targetEntries = remember(targetLang) {
        listOf(
            DropdownEntry(
                items = TARGET_LANG_OPTIONS.map { lang ->
                    DropdownItem(
                        text = lang,
                        selected = lang == targetLang,
                        onClick = { onTargetLangChange(lang); targetExpanded = false }
                    )
                }
            )
        )
    }

    Row(
        modifier = Modifier.height(32.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val sourceInteraction = remember { MutableInteractionSource() }
        val sourcePressed by sourceInteraction.collectIsPressedAsState()
        val sourceBg = if (sourcePressed) Color.White.copy(alpha = 0.1f) else Color.Transparent

        Box {
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(sourceBg)
                    .clickable(interactionSource = sourceInteraction, indication = null) { sourceExpanded = true }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = sourceLang,
                    transitionSpec = {
                        (slideInHorizontally(initialOffsetX = { -it }) + fadeIn()) togetherWith (slideOutHorizontally(targetOffsetX = { it }) + fadeOut())
                    },
                    label = "sourceLangAnim"
                ) { text ->
                    Text(text = text, color = Color.White, fontSize = 13.sp)
                }
            }
            WindowCascadingListPopup(
                show = sourceExpanded,
                entries = sourceEntries,
                onDismissRequest = { sourceExpanded = false }
            )
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSwap() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MiuixIcons.Replace,
                contentDescription = "交换",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        val targetInteraction = remember { MutableInteractionSource() }
        val targetPressed by targetInteraction.collectIsPressedAsState()
        val targetBg = if (targetPressed) Color.White.copy(alpha = 0.1f) else Color.Transparent

        Box {
            Box(
                modifier = Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(targetBg)
                    .clickable(interactionSource = targetInteraction, indication = null) { targetExpanded = true }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = targetLang,
                    transitionSpec = {
                        (slideInHorizontally(initialOffsetX = { it }) + fadeIn()) togetherWith (slideOutHorizontally(targetOffsetX = { -it }) + fadeOut())
                    },
                    label = "targetLangAnim"
                ) { text ->
                    Text(text = text, color = Color.White, fontSize = 13.sp)
                }
            }
            WindowCascadingListPopup(
                show = targetExpanded,
                entries = targetEntries,
                onDismissRequest = { targetExpanded = false }
            )
        }
    }
}

@Composable
private fun TopScanFilterBarContent(
    selectedFilter: ScanFilter,
    onFilterChange: (ScanFilter) -> Unit,
) {
    val density = LocalDensity.current
    val tabWidth = 44.dp
    val tabWidthPx = with(density) { tabWidth.toPx() }
    val outerHeight = 32.dp
    val gap = 4.dp
    val gapPx = with(density) { gap.toPx() }
    val indicatorWidth = tabWidth
    val indicatorHeight = outerHeight

    val stepPx = tabWidthPx + gapPx

    val scope = rememberCoroutineScope()
    val initialPos = when (selectedFilter) {
        ScanFilter.ORIGINAL -> 0f
        ScanFilter.ENHANCED -> 1f
        ScanFilter.GRAYSCALE -> 2f
    }
    val indicatorPos = remember { Animatable(initialPos) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFilter) {
        if (!isDragging) {
            val target = when (selectedFilter) {
                ScanFilter.ORIGINAL -> 0f
                ScanFilter.ENHANCED -> 1f
                ScanFilter.GRAYSCALE -> 2f
            }
            if (indicatorPos.value != target) {
                indicatorPos.animateTo(
                    target,
                    spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow,
                    )
                )
            }
        }
    }

    val actualOffset = indicatorPos.value * stepPx

    Box(
        modifier = Modifier
            .wrapContentWidth()
            .height(outerHeight),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(actualOffset.roundToInt(), 0) }
                .width(indicatorWidth)
                .height(indicatorHeight)
                .clip(RoundedCornerShape(indicatorHeight / 2))
                .background(Color.White.copy(alpha = 0.1f)),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .wrapContentWidth()
                .height(indicatorHeight)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = {
                            isDragging = true
                            scope.launch { indicatorPos.stop() }
                        },
                        onDragEnd = {
                            isDragging = false
                            val rounded = indicatorPos.value.roundToInt().coerceIn(0, 2)
                            val targetFilter = when (rounded) {
                                0 -> ScanFilter.ORIGINAL
                                1 -> ScanFilter.ENHANCED
                                else -> ScanFilter.GRAYSCALE
                            }
                            onFilterChange(targetFilter)
                            val target = rounded.toFloat()
                            scope.launch {
                                indicatorPos.animateTo(
                                    target,
                                    spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessLow,
                                    )
                                )
                            }
                        },
                        onHorizontalDrag = { change, amount ->
                            change.consume()
                            val currentX = indicatorPos.value * stepPx + amount
                            val fraction = (currentX / stepPx).coerceIn(0f, 2f)
                            scope.launch { indicatorPos.snapTo(fraction) }
                        }
                    )
                },
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            listOf(
                ScanFilter.ORIGINAL to "原图",
                ScanFilter.ENHANCED to "增强",
                ScanFilter.GRAYSCALE to "黑白"
            ).forEachIndexed { idx, (f, label) ->
                Box(
                    modifier = Modifier
                        .width(tabWidth)
                        .height(indicatorHeight)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) { if (!isDragging) onFilterChange(f) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = label, color = Color.White, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun TranslateShutterButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val innerSize by animateDpAsState(targetValue = if (pressed) 48.dp else 56.dp, animationSpec = tween(150, easing = CodeOverlayEasing), label = "shutterInnerSize")

    Box(modifier = Modifier.size(72.dp).clickable(interactionSource = interactionSource, indication = null, onClick = onClick), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(64.dp).border(width = 3.dp, color = Color.White, shape = CircleShape))
        Box(modifier = Modifier.size(innerSize).clip(CircleShape).background(Color.White))
    }
}

@Composable
private fun CodeOverlayItem(code: DetectedCode, visible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(tween(CODE_OVERLAY_ENTER_MS, easing = CodeOverlayEasing), initialScale = 0f) + fadeIn(tween(160)),
        exit = scaleOut(tween(CODE_OVERLAY_EXIT_MS, easing = CodeOverlayEasing), targetScale = 0f) + fadeOut(tween(140)),
    ) { CodeOverlayButton(xFraction = code.xFraction, yFraction = code.yFraction, onClick = onClick) }
}

@Composable
private fun CodeOverlayButton(xFraction: Float, yFraction: Float, onClick: () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight
        val animX by animateDpAsState(targetValue = w * (xFraction - 0.5f), animationSpec = spring(Spring.DampingRatioNoBouncy, 20000f, visibilityThreshold = 0.5.dp), label = "codeOverlayX")
        val animY by animateDpAsState(targetValue = h * (yFraction - 0.5f), animationSpec = spring(Spring.DampingRatioNoBouncy, 20000f, visibilityThreshold = 0.5.dp), label = "codeOverlayY")
        Box(
            modifier = Modifier.align(Alignment.Center).offset(x = animX, y = animY).size(48.dp).clip(CircleShape).background(MiuixTheme.colorScheme.primary).clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) { Icon(MiuixIcons.ChevronForward, "查看结果", tint = Color.White, modifier = Modifier.size(24.dp)) }
    }
}

/**
 * 「提取」结果弹窗：上方是扫描照片，下方是识别出的文字，
 * 文字可长按选择，底部主按钮为复制。
 */
@Composable
private fun ExtractResultDialog(
    show: Boolean,
    bitmap: Bitmap,
    text: String,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    WindowDialog(show = show, title = "提取", onDismissRequest = onClose) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "扫描照片",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 168.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.04f)),
                contentScale = ContentScale.Fit,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                SelectionContainer {
                    Text(
                        text = text,
                        modifier = Modifier.fillMaxWidth(),
                        style = MiuixTheme.textStyles.body1,
                        color = MiuixTheme.colorScheme.onSurface,
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onClose,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.secondaryContainer,
                        contentColor = MiuixTheme.colorScheme.onSurface,
                    ),
                ) { Text("关闭") }
                Button(
                    onClick = {
                        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        cm?.setPrimaryClip(ClipData.newPlainText("提取文本", text))
                        Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        color = MiuixTheme.colorScheme.primary,
                        contentColor = Color.White,
                    ),
                ) { Text("复制") }
            }
        }
    }
}

@Composable
private fun TextResultDialog(show: Boolean, content: String, onClose: () -> Unit) {
    WindowDialog(show = show, title = "文本", onDismissRequest = onClose) {
        Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.fillMaxWidth().heightIn(max = 320.dp).verticalScroll(rememberScrollState())) {
                Text(text = content, modifier = Modifier.fillMaxWidth(), style = MiuixTheme.textStyles.body1, color = MiuixTheme.colorScheme.onSurface)
            }
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.secondaryContainer, contentColor = MiuixTheme.colorScheme.onSurface)) { Text("关闭") }
        }
    }
}

@Composable
private fun WifiResultDialog(show: Boolean, info: WifiQrInfo?, onConnect: () -> Unit, onClose: () -> Unit) {
    WindowDialog(show = show, title = "WIFI", onDismissRequest = onClose) {
        if (info != null) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    WifiInfoRow(label = "网络名称", value = info.ssid)
                    if (info.securityType.lowercase() != "nopass") WifiInfoRow(label = "密码", value = info.password.ifBlank { "（空）" })
                    WifiInfoRow(label = "加密类型", value = when (info.securityType.lowercase()) { "wpa", "wpa2", "wpa/wpa2" -> "WPA/WPA2"; "wpa3" -> "WPA3"; "wep" -> "WEP"; "nopass" -> "开放网络"; else -> info.securityType })
                    if (info.isHidden) WifiInfoRow(label = "隐藏网络", value = "是")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onClose, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.secondaryContainer, contentColor = MiuixTheme.colorScheme.onSurface)) { Text("关闭") }
                    Button(onClick = onConnect, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(color = MiuixTheme.colorScheme.primary, contentColor = Color.White)) { Text("连接网络") }
                }
            }
        }
    }
}

@Composable
private fun WifiInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(text = label, fontSize = 13.sp, color = DialogDefaults.summaryColor(), modifier = Modifier.width(72.dp))
        Spacer(Modifier.width(8.dp))
        Text(text = value, fontSize = 14.sp, color = MiuixTheme.colorScheme.onSurface, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
    }
}

private fun tryOpenExternal(context: Context, url: String): Boolean {
    val lower = url.lowercase()
    val isSpecial = lower.startsWith("weixin://") || lower.startsWith("mqq://") || lower.startsWith("mqqwpa://") || lower.startsWith("alipay://") || lower.startsWith("alipays://") || lower.startsWith("taobao://") || lower.startsWith("wxwork://") || lower.startsWith("dingtalk://") || lower.contains("weixin.qq.com") || lower.contains("qm.qq.com") || lower.contains("w.url.cn")
    if (!isSpecial) return false
    return try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        context.startActivity(intent)
        true
    } catch (_: Exception) { false }
}

@androidx.camera.core.ExperimentalGetImage
private fun processImageProxyMulti(scanner: BarcodeScanner, imageProxy: ImageProxy, onResult: (List<DetectedCode>) -> Unit) {
    val mediaImage = imageProxy.image ?: run { imageProxy.close(); return }
    val rotationDegrees = imageProxy.imageInfo.rotationDegrees
    val rawWidth = imageProxy.width
    val rawHeight = imageProxy.height
    val rotatedWidth = if (rotationDegrees == 90 || rotationDegrees == 270) rawHeight else rawWidth
    val rotatedHeight = if (rotationDegrees == 90 || rotationDegrees == 270) rawWidth else rawHeight

    val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
    scanner.process(image)
        .addOnSuccessListener { barcodes ->
            val dedup = LinkedHashMap<String, Barcode>()
            barcodes.forEach { bc ->
                val raw = bc.rawValue ?: return@forEach
                val box = bc.boundingBox ?: return@forEach
                val existing = dedup[raw]
                if (existing == null) { dedup[raw] = bc } else {
                    val eBox = existing.boundingBox
                    if (eBox == null || box.width() * box.height() > eBox.width() * eBox.height()) { dedup[raw] = bc }
                }
            }
            val codes = dedup.mapNotNull { (raw, bc) ->
                val box = bc.boundingBox ?: return@mapNotNull null
                val cx = ((box.left + box.right) / 2f / rotatedWidth).coerceIn(0f, 1f)
                val cy = ((box.top + box.bottom) / 2f / rotatedHeight).coerceIn(0f, 1f)
                DetectedCode(rawValue = raw, xFraction = cx, yFraction = cy, format = bc.format)
            }
            onResult(codes)
        }
        .addOnCompleteListener { imageProxy.close() }
}