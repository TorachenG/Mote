@file:Suppress("SpellCheckingInspection")
@file:androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)

package com.torachen.mote.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RenderEffect
import android.graphics.Shader
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.provider.Settings
import android.util.Size
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
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
import java.util.concurrent.Executors
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
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
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Image
import top.yukonga.miuix.kmp.layout.DialogDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

// ★ 按钮背景模糊半径（与视频播放器一致）
private const val CONTROL_BLUR_RADIUS = 15f

// ★ 按钮背景黑色透明度（与视频播放器 UI_SCRIM_ALPHA 一致）
private const val BUTTON_SCRIM_ALPHA = 0.7f

// ★ 连续多少帧无识别才清空二维码覆盖按钮（避免偶尔识别失败导致闪烁）
private const val NO_DETECT_FRAMES_TO_CLEAR = 5

// ★ 提示球出现/消失的插值动画时长
private const val CODE_OVERLAY_ENTER_MS = 220
private const val CODE_OVERLAY_EXIT_MS = 180

// ★ 提示球出现/消失的缓动（MIUI 风格快出慢入）
private val CodeOverlayEasing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

// ★ 图片 3D 翻转动画参数
private const val IMAGE_FLIP_IN_MS = 520
private const val IMAGE_FLIP_OUT_MS = 420

// ★ 背景模糊半径（与 AppRoot 里打开新页面的 5.dp 保持一致）
private const val PAGE_BLUR_DP = 5f

// ============================================================
// WIFI 二维码数据模型
// ============================================================

private data class WifiQrInfo(
    val ssid: String,
    val password: String,
    val securityType: String,
    val isHidden: Boolean,
)

// ============================================================
// 识别到的二维码
//
// 坐标用归一化 [0, 1] 的 xFraction / yFraction，
// 在 processImageProxyMulti 中根据 rotationDegrees 正确归一化。
// ============================================================

private data class DetectedCode(
    val rawValue: String,
    val xFraction: Float,
    val yFraction: Float,
    val format: Int,
)

// ============================================================
// WIFI 二维码解析
// ============================================================

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

// ============================================================
// WIFI 连接
// ============================================================

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
                    Toast.makeText(
                        context,
                        "系统不支持自动连接 WEP 网络，正在打开系统设置…",
                        Toast.LENGTH_LONG,
                    ).show()
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

// ============================================================
// 通用 BarcodeScanner 构建
// ============================================================

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

// ============================================================
// 扫码页
// ============================================================

@Suppress("NewApi")
@Composable
fun ScannerScreen(
    onCodeScanned: (String) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()

    val scannerNavOwner = rememberNavigationEventDispatcherOwner(parent = null)

    val blurSupported = isRuntimeShaderSupported()
    // ★ 相机层的 backdrop（用于按钮 textureBlur 捕获相机画面）
    val backdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(Color.Black)
            drawContent()
        }
    } else null
    // ★ 图片层的 backdrop（保留，供未来扩展）
    val imageBackdrop = if (blurSupported) {
        rememberLayerBackdrop {
            drawRect(Color.Black)
            drawContent()
        }
    } else null

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

    // ★ 识别到的二维码列表（数据源）
    val detectedCodes = remember { mutableStateListOf<DetectedCode>() }
    // ★ 显示用的二维码列表（延迟移除，让 exit 动画有机会播放）
    val displayCodes = remember { mutableStateListOf<DetectedCode>() }
    // ★ 连续无识别帧计数
    var noDetectFrameCount by remember { mutableIntStateOf(0) }
    // ★ 已自动弹出过 dialog 的二维码内容（页面生命周期内只弹一次）
    val autoPoppedCodes = remember { mutableSetOf<String>() }
    // ★ dialog 状态
    var textDialogContent by remember { mutableStateOf<String?>(null) }
    var wifiDialogInfo by remember { mutableStateOf<WifiQrInfo?>(null) }
    var textDialogDisplay by remember { mutableStateOf("") }
    var textDialogEverShown by remember { mutableStateOf(false) }
    var wifiDialogEverShown by remember { mutableStateOf(false) }
    // ★ 从相册选择的图片
    var pickedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    // ★ 上传模式下的识别结果
    val pickedImageCodes = remember { mutableStateListOf<DetectedCode>() }

    // ★ 图片 3D 翻转动画
    //   rotationX: 90 → 0 入场；0 → -90 出场
    //   alpha: 0 → 1 入场；1 → 0 出场
    val imageRotationX = remember { Animatable(90f) }
    val imageAlpha = remember { Animatable(0f) }
    // ★ 新增：图片模式提示球的显示状态。
    //   初始 false，等图片 3D 翻转动画播完后再置 true，
    //   让提示球以"1.2 → 1 缩放 + 淡入"的方式延后出现。
    var imageCodesRevealed by remember { mutableStateOf(false) }

    // 状态栏图标变白
    DisposableEffect(Unit) {
        val activity = context as? Activity
        val window = activity?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previous = controller?.isAppearanceLightStatusBars ?: false
        controller?.isAppearanceLightStatusBars = false
        onDispose {
            controller?.isAppearanceLightStatusBars = previous
        }
    }

    // 退出时解绑相机
    DisposableEffect(Unit) {
        onDispose {
            try { cameraProviderRef?.unbindAll() } catch (_: Exception) {}
            try { analyzerExecutorRef?.shutdown() } catch (_: Exception) {}
            cameraProviderRef = null
            analyzerExecutorRef = null
            cameraRef = null
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    // ★ 修复（问题 3）：WiFi 快捷连接需要 ACCESS_FINE_LOCATION（API 29+）
    var pendingWifiInfo by remember { mutableStateOf<WifiQrInfo?>(null) }
    val locationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        val info = pendingWifiInfo
        pendingWifiInfo = null
        if (granted && info != null) {
            connectToWifi(context, info)
        } else if (!granted) {
            Toast.makeText(
                context,
                "需要位置权限才能连接 WiFi",
                Toast.LENGTH_LONG,
            ).show()
        }
    }

    fun tryConnectWifi(info: WifiQrInfo) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Toast.makeText(
                context,
                "Android 10 以下暂不支持直接连接 WIFI",
                Toast.LENGTH_LONG,
            ).show()
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

    // ★ 相册选图（同步版本，能工作的）
    val pickImageLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bmp = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bmp != null) {
                    pickedImageBitmap = bmp
                    pickedImageCodes.clear()
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

    // ★ 图片入场时播放 3D 翻转动画
    // ★ 修复：翻转动画结束后再让提示球从 1.2 缩到 1 出现（原来立即显示很突兀）
    LaunchedEffect(pickedImageBitmap) {
        if (pickedImageBitmap != null) {
            // 图片进场前先隐藏提示球
            imageCodesRevealed = false
            imageRotationX.snapTo(90f)
            imageAlpha.snapTo(0f)
            coroutineScope {
                launch {
                    imageRotationX.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(
                            durationMillis = IMAGE_FLIP_IN_MS,
                            easing = CodeOverlayEasing,
                        ),
                    )
                }
                launch {
                    imageAlpha.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(durationMillis = 280),
                    )
                }
            }
            // ★ 翻转动画结束后再加 100ms 缓冲，让收尾自然一点，然后显示提示球
            delay(100)
            // ★ 防止用户在动画期间已经点了关闭（此时 pickedImageBitmap 即将被置 null）
            if (pickedImageBitmap != null) {
                imageCodesRevealed = true
            }
        } else {
            // 图片关闭后重置，避免下次立即显示
            imageCodesRevealed = false
        }
    }

    // ★ 同步 displayCodes（延迟移除，让 exit 动画播完）
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

    // ★ 扫码结果处理
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

    // ★ 用户主动点击圆形按钮时显示 dialog
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

    // ★ 关闭图片模式（先播反向 3D 翻转动画，再清状态）
    fun closePickedImage() {
        scope.launch {
            coroutineScope {
                launch {
                    imageRotationX.animateTo(
                        targetValue = -90f,
                        animationSpec = tween(
                            durationMillis = IMAGE_FLIP_OUT_MS,
                            easing = CodeOverlayEasing,
                        ),
                    )
                }
                launch {
                    imageAlpha.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 240),
                    )
                }
            }
            pickedImageBitmap = null
            pickedImageCodes.clear()
        }
    }

    // ★ 整个页面用共享的 navOwner 包裹
    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides scannerNavOwner
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            // ============================================================
            // ★ 相机层（含无权限兜底）
            //   ★ 图片模式时给这一层加 RenderEffect 模糊（与 AppRoot 打开新页面一致）
            // ============================================================
            if (hasPermission) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            if (pickedImageBitmap != null && blurSupported) {
                                // GraphicsLayerScope 继承 Density，density 是屏幕密度倍数（Float）
                                // PAGE_BLUR_DP 是 Float（dp 数值），相乘得到像素值
                                val blurPx = PAGE_BLUR_DP * density
                                renderEffect = RenderEffect
                                    .createBlurEffect(blurPx, blurPx, Shader.TileMode.CLAMP)
                                    .asComposeRenderEffect()
                            }
                        }
                        .then(
                            if (blurSupported && backdrop != null) {
                                Modifier.layerBackdrop(backdrop)
                            } else Modifier
                        )
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
                                    val executor = Executors.newSingleThreadExecutor()
                                    analyzerExecutorRef = executor

                                    val scanner = buildBarcodeScanner()
                                    analysis.setAnalyzer(executor) { imageProxy ->
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
                                    )
                                } catch (_: Exception) {}
                            }, ContextCompat.getMainExecutor(context))
                        },
                    )
                }

                // ★ 修复（问题 4）：相机模式 UI 加缩放 + 淡入淡出动画。
                //   - 进入图片模式（visible: true → false）：放大到 1.2x + 淡出
                //   - 退出图片模式（visible: false → true）：从 1.2x 缩小到 1x + 淡入
                //   动画时长与图片 3D 翻转严格对齐：
                //     exit 消失（进入图片模式）→ IMAGE_FLIP_IN_MS  (520ms) 与图片入场同步
                //     enter 出现（退出图片模式）→ IMAGE_FLIP_OUT_MS (420ms) 与图片出场同步
                AnimatedVisibility(
                    visible = pickedImageBitmap == null,
                    enter = scaleIn(
                        animationSpec = tween(
                            durationMillis = IMAGE_FLIP_OUT_MS,
                            easing = CodeOverlayEasing,
                        ),
                        initialScale = 1.2f,
                    ) + fadeIn(
                        animationSpec = tween(durationMillis = IMAGE_FLIP_OUT_MS),
                    ),
                    exit = scaleOut(
                        animationSpec = tween(
                            durationMillis = IMAGE_FLIP_IN_MS,
                            easing = CodeOverlayEasing,
                        ),
                        targetScale = 1.2f,
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = IMAGE_FLIP_IN_MS),
                    ),
                ) {
                    // ★ 必须是 Box(fillMaxSize)，因为 AnimatedVisibility 不是 BoxScope，
                    //   内部的 Modifier.align(Alignment.TopStart/TopEnd/BottomEnd/Center) 需要一个 Box 父容器
                    Box(modifier = Modifier.fillMaxSize()) {

                        ScanFrameOverlay(modifier = Modifier.fillMaxSize())

                        // ★ 提示球（带缩放动画）
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

                        // 返回按钮
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .statusBarsPadding()
                                .padding(start = 12.dp, top = 8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .then(
                                    if (blurSupported && backdrop != null) {
                                        Modifier.textureBlur(
                                            backdrop = backdrop,
                                            shape = CircleShape,
                                            blurRadius = CONTROL_BLUR_RADIUS,
                                            noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                            colors = BlurDefaults.blurColors(
                                                blendColors = listOf(
                                                    BlendColorEntry(color = Color.Black.copy(alpha = BUTTON_SCRIM_ALPHA)),
                                                ),
                                            ),
                                        )
                                    } else Modifier.background(Color.Black.copy(alpha = BUTTON_SCRIM_ALPHA))
                                )
                                .clickable { onClose() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = "返回",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        // 手电筒按钮
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .statusBarsPadding()
                                .padding(end = 12.dp, top = 8.dp)
                                .size(40.dp)
                                .clip(CircleShape)
                                .then(
                                    if (blurSupported && backdrop != null) {
                                        Modifier.textureBlur(
                                            backdrop = backdrop,
                                            shape = CircleShape,
                                            blurRadius = CONTROL_BLUR_RADIUS,
                                            noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                            colors = BlurDefaults.blurColors(
                                                blendColors = listOf(
                                                    BlendColorEntry(color = Color.Black.copy(alpha = BUTTON_SCRIM_ALPHA)),
                                                ),
                                            ),
                                        )
                                    } else Modifier.background(Color.Black.copy(alpha = BUTTON_SCRIM_ALPHA))
                                )
                                .clickable { torchOn = !torchOn },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = if (torchOn) Icons.Filled.FlashlightOn
                                else Icons.Filled.FlashlightOff,
                                contentDescription = if (torchOn) "关闭手电筒" else "打开手电筒",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        // 相册上传按钮
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                // ★ 修复（问题 2）：避让虚拟导航键
                                .navigationBarsPadding()
                                .padding(end = 16.dp, bottom = 32.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .then(
                                    if (blurSupported && backdrop != null) {
                                        Modifier.textureBlur(
                                            backdrop = backdrop,
                                            shape = CircleShape,
                                            blurRadius = CONTROL_BLUR_RADIUS,
                                            noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                            colors = BlurDefaults.blurColors(
                                                blendColors = listOf(
                                                    BlendColorEntry(color = Color.Black.copy(alpha = BUTTON_SCRIM_ALPHA)),
                                                ),
                                            ),
                                        )
                                    } else Modifier.background(Color.Black.copy(alpha = BUTTON_SCRIM_ALPHA))
                                )
                                .clickable { pickImageLauncher.launch("image/*") },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Image,
                                contentDescription = "从相册选择",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        // 文案位置：扫码框下方 20dp
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .offset(y = 150.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "将二维码 / 条形码放入取景框内",
                                color = Color.White,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            } else {
                // 无权限
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "需要相机权限才能使用扫一扫",
                            color = Color.White,
                        )
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            },
                            colors = ButtonDefaults.buttonColors(
                                color = MiuixTheme.colorScheme.primary,
                                contentColor = Color.White,
                            ),
                        ) { Text("授予权限") }
                    }
                }
            }

            // ============================================================
            // ★ 图片层（3D 翻转）
            // ============================================================
            if (pickedImageBitmap != null) {
                val bmp = pickedImageBitmap!!
                // ★ 圆角改为设备圆角的三分之二
                val screenCornerRadius = rememberScreenCornerRadius(fallback = 28.dp) * (2f / 3f)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationX = imageRotationX.value
                            alpha = imageAlpha.value
                            cameraDistance = 20f * density
                            transformOrigin = TransformOrigin.Center
                        }
                ) {
                    // 图片 + imageBackdrop
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (blurSupported && imageBackdrop != null) {
                                    Modifier.layerBackdrop(imageBackdrop)
                                } else Modifier
                            )
                    ) {
                        // ★ 修复（问题 1）：圆角跟随图片本身，而不是 padding 后的外框。
                        //   之前 ContentScale.Fit 会在 padding 区域内留白，
                        //   clip 圆角贴着外框，图片顶部/底部不贴合圆角，
                        //   看起来"顶部圆角没衔接上"。
                        //   现在用 BoxWithConstraints 计算 Fit 后的真实绘制尺寸，
                        //   把 Image 的 size 直接设成这个尺寸再 clip。
                        BoxWithConstraints(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 40.dp, vertical = 80.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            val availW = maxWidth
                            val availH = maxHeight
                            val availWVal = availW.value
                            val availHVal = availH.value
                            val bmpRatio = bmp.width.toFloat() /
                                    bmp.height.toFloat().coerceAtLeast(1f)
                            val availRatio = if (availHVal > 0f) {
                                availWVal / availHVal
                            } else bmpRatio

                            // 选更受限的方向作为基准，保证图片完整显示且不留空
                            val drawW: Dp
                            val drawH: Dp
                            if (bmpRatio >= availRatio) {
                                // 图片更宽 → 宽度受限
                                drawW = availW
                                drawH = availW / bmpRatio
                            } else {
                                // 图片更高 → 高度受限
                                drawH = availH
                                drawW = availH * bmpRatio
                            }

                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "选择的图片",
                                modifier = Modifier
                                    .size(drawW, drawH)
                                    .clip(RoundedCornerShape(screenCornerRadius)),
                                contentScale = ContentScale.FillBounds,
                            )
                        }
                    }

                    // 图片模式的提示球（位置相对外层 Box，即全屏）
                    // ★ 修复：等图片翻转结束后，以"1.2 → 1 缩放 + 淡入"的方式出现。
                    //   这里不用 CodeOverlayItem（它的 enter 是从 0 放大），
                    //   而是用 AnimatedVisibility + scaleIn(initialScale = 1.2f) 单独控制。
                    pickedImageCodes.forEach { code ->
                        key(code.rawValue) {
                            AnimatedVisibility(
                                visible = imageCodesRevealed,
                                enter = scaleIn(
                                    animationSpec = tween(
                                        durationMillis = IMAGE_FLIP_OUT_MS,
                                        easing = CodeOverlayEasing,
                                    ),
                                    initialScale = 1.2f,
                                ) + fadeIn(
                                    animationSpec = tween(durationMillis = 260),
                                ),
                                exit = fadeOut(
                                    animationSpec = tween(durationMillis = 140),
                                ) + scaleOut(
                                    animationSpec = tween(
                                        durationMillis = CODE_OVERLAY_EXIT_MS,
                                        easing = CodeOverlayEasing,
                                    ),
                                    targetScale = 0f,
                                ),
                            ) {
                                CodeOverlayButton(
                                    xFraction = code.xFraction,
                                    yFraction = code.yFraction,
                                    onClick = { showDialogFor(code.rawValue) },
                                )
                            }
                        }
                    }

                    // ★ 关闭按钮（左上角，Close 图标，与相机模式返回按钮位置一致）
                    //   跟随图片 3D 翻转
                    // ★ 修复（问题 3）：用 backdrop（相机层 backdrop）代替 imageBackdrop，
                    //   与相机模式的返回按钮完全同款 —— 同样的半透明黑 + MIUIX 模糊。
                    //   之前用 imageBackdrop 时，因为图片层最外层有 graphicsLayer 形成独立渲染层，
                    //   backdrop 捕获链被阻断，导致按钮看起来"没有模糊"。
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .statusBarsPadding()
                            .padding(start = 12.dp, top = 8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .then(
                                if (blurSupported && backdrop != null) {
                                    Modifier.textureBlur(
                                        backdrop = backdrop,
                                        shape = CircleShape,
                                        blurRadius = CONTROL_BLUR_RADIUS,
                                        noiseCoefficient = BlurDefaults.NoiseCoefficient,
                                        colors = BlurDefaults.blurColors(
                                            blendColors = listOf(
                                                BlendColorEntry(color = Color.Black.copy(alpha = BUTTON_SCRIM_ALPHA)),
                                            ),
                                        ),
                                    )
                                } else Modifier.background(Color.Black.copy(alpha = BUTTON_SCRIM_ALPHA))
                            )
                            .clickable { closePickedImage() },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Close,
                            contentDescription = "关闭",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    // ★ 修复（问题 2）：删除右下角重新上传按钮 ——
                    //   进入图片模式后用户只需要看识别结果或关闭，不需要"重新上传"入口。
                }
            }
        }

        // ============================================================
        // ★ 文本结果 dialog
        // ============================================================
        if (textDialogEverShown) {
            TextResultDialog(
                show = textDialogContent != null,
                content = textDialogDisplay,
                onClose = { textDialogContent = null },
            )
        }

        // ============================================================
        // ★ WIFI 结果 dialog
        // ============================================================
        if (wifiDialogEverShown) {
            WifiResultDialog(
                show = wifiDialogInfo != null,
                info = wifiDialogInfo,
                onConnect = {
                    // ★ 修复（问题 3）：先检查/请求位置权限，再连接
                    wifiDialogInfo?.let { tryConnectWifi(it) }
                    wifiDialogInfo = null
                },
                onClose = { wifiDialogInfo = null },
            )
        }
    }
}

// ============================================================
// 提示球（带进出缩放动画）
// ============================================================

@Composable
private fun CodeOverlayItem(
    code: DetectedCode,
    visible: Boolean,
    onClick: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = tween(
                durationMillis = CODE_OVERLAY_ENTER_MS,
                easing = CodeOverlayEasing,
            ),
            initialScale = 0f,
        ) + fadeIn(animationSpec = tween(durationMillis = 160)),
        exit = scaleOut(
            animationSpec = tween(
                durationMillis = CODE_OVERLAY_EXIT_MS,
                easing = CodeOverlayEasing,
            ),
            targetScale = 0f,
        ) + fadeOut(animationSpec = tween(durationMillis = 140)),
    ) {
        CodeOverlayButton(
            xFraction = code.xFraction,
            yFraction = code.yFraction,
            onClick = onClick,
        )
    }
}

// ============================================================
// 提示球（带位置 spring 平滑跟随）
// ============================================================

@Composable
private fun CodeOverlayButton(
    xFraction: Float,
    yFraction: Float,
    onClick: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight
        val animX by animateDpAsState(
            targetValue = w * (xFraction - 0.5f),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = 20000f,
                visibilityThreshold = 0.5.dp,
            ),
            label = "codeOverlayX",
        )
        val animY by animateDpAsState(
            targetValue = h * (yFraction - 0.5f),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = 20000f,
                visibilityThreshold = 0.5.dp,
            ),
            label = "codeOverlayY",
        )
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = animX, y = animY)
                .size(48.dp)
                .clip(CircleShape)
                .background(MiuixTheme.colorScheme.primary)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MiuixIcons.ChevronForward,
                contentDescription = "查看结果",
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

// ============================================================
// 文本结果 dialog
// ============================================================

@Composable
private fun TextResultDialog(
    show: Boolean,
    content: String,
    onClose: () -> Unit,
) {
    WindowDialog(
        show = show,
        title = "文本",
        onDismissRequest = onClose,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = content,
                    modifier = Modifier.fillMaxWidth(),
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurface,
                )
            }

            Button(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    color = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.onSurface,
                ),
            ) {
                Text("关闭")
            }
        }
    }
}

// ============================================================
// WIFI 结果 dialog
// ============================================================

@Composable
private fun WifiResultDialog(
    show: Boolean,
    info: WifiQrInfo?,
    onConnect: () -> Unit,
    onClose: () -> Unit,
) {
    WindowDialog(
        show = show,
        title = "WIFI",
        onDismissRequest = onClose,
    ) {
        if (info != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    WifiInfoRow(label = "网络名称", value = info.ssid)
                    if (info.securityType.lowercase() != "nopass") {
                        WifiInfoRow(
                            label = "密码",
                            value = info.password.ifBlank { "（空）" },
                        )
                    }
                    WifiInfoRow(
                        label = "加密类型",
                        value = when (info.securityType.lowercase()) {
                            "wpa", "wpa2", "wpa/wpa2" -> "WPA/WPA2"
                            "wpa3" -> "WPA3"
                            "wep" -> "WEP"
                            "nopass" -> "开放网络"
                            else -> info.securityType
                        },
                    )
                    if (info.isHidden) {
                        WifiInfoRow(label = "隐藏网络", value = "是")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.secondaryContainer,
                            contentColor = MiuixTheme.colorScheme.onSurface,
                        ),
                    ) {
                        Text("关闭")
                    }

                    Button(
                        onClick = onConnect,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            color = MiuixTheme.colorScheme.primary,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("连接网络")
                    }
                }
            }
        }
    }
}

@Composable
private fun WifiInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = DialogDefaults.summaryColor(),
            modifier = Modifier.width(72.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            color = MiuixTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start,
        )
    }
}

// ============================================================
// 特殊 scheme 处理
// ============================================================

private fun tryOpenExternal(context: Context, url: String): Boolean {
    val lower = url.lowercase()
    val isSpecial = lower.startsWith("weixin://") ||
            lower.startsWith("mqq://") ||
            lower.startsWith("mqqwpa://") ||
            lower.startsWith("alipay://") ||
            lower.startsWith("alipays://") ||
            lower.startsWith("taobao://") ||
            lower.startsWith("wxwork://") ||
            lower.startsWith("dingtalk://") ||
            lower.contains("weixin.qq.com") ||
            lower.contains("qm.qq.com") ||
            lower.contains("w.url.cn")
    if (!isSpecial) return false
    return try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        true
    } catch (_: Exception) {
        false
    }
}

// ============================================================
// 取景框叠加层
// ============================================================

@Composable
private fun ScanFrameOverlay(modifier: Modifier = Modifier) {
    val frameSize = 260.dp
    val cornerColor = MiuixTheme.colorScheme.primary
    val borderColor = Color.White.copy(alpha = 0.5f)
    val overlayColor = Color.Black.copy(alpha = 0.5f)
    val density = LocalDensity.current

    Canvas(modifier = modifier) {
        val frameSizePx = with(density) { frameSize.toPx() }
        val left = (size.width - frameSizePx) / 2f
        val top = (size.height - frameSizePx) / 2f
        val right = left + frameSizePx
        val bottom = top + frameSizePx
        val borderStroke = 1.5.dp.toPx()
        val cornerStroke = 4.dp.toPx()
        val cornerLength = 40.dp.toPx()

        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, 0f),
            size = androidx.compose.ui.geometry.Size(size.width, top),
        )
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, bottom),
            size = androidx.compose.ui.geometry.Size(size.width, size.height - bottom),
        )
        drawRect(
            color = overlayColor,
            topLeft = Offset(0f, top),
            size = androidx.compose.ui.geometry.Size(left, frameSizePx),
        )
        drawRect(
            color = overlayColor,
            topLeft = Offset(right, top),
            size = androidx.compose.ui.geometry.Size(size.width - right, frameSizePx),
        )

        drawRect(
            color = borderColor,
            topLeft = Offset(left, top),
            size = androidx.compose.ui.geometry.Size(frameSizePx, frameSizePx),
            style = Stroke(width = borderStroke),
        )

        val cornerStyle = Stroke(width = cornerStroke, cap = StrokeCap.Butt, join = StrokeJoin.Miter)
        drawPath(path = Path().apply {
            moveTo(left, top + cornerLength); lineTo(left, top); lineTo(left + cornerLength, top)
        }, color = cornerColor, style = cornerStyle)
        drawPath(path = Path().apply {
            moveTo(right - cornerLength, top); lineTo(right, top); lineTo(right, top + cornerLength)
        }, color = cornerColor, style = cornerStyle)
        drawPath(path = Path().apply {
            moveTo(left, bottom - cornerLength); lineTo(left, bottom); lineTo(left + cornerLength, bottom)
        }, color = cornerColor, style = cornerStyle)
        drawPath(path = Path().apply {
            moveTo(right - cornerLength, bottom); lineTo(right, bottom); lineTo(right, bottom - cornerLength)
        }, color = cornerColor, style = cornerStyle)
    }
}

// ============================================================
// 帧处理
// ============================================================

@androidx.camera.core.ExperimentalGetImage
private fun processImageProxyMulti(
    scanner: BarcodeScanner,
    imageProxy: ImageProxy,
    onResult: (List<DetectedCode>) -> Unit,
) {
    val mediaImage = imageProxy.image ?: run {
        imageProxy.close()
        return
    }
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
                if (existing == null) {
                    dedup[raw] = bc
                } else {
                    val eBox = existing.boundingBox
                    if (eBox == null ||
                        box.width() * box.height() > eBox.width() * eBox.height()
                    ) {
                        dedup[raw] = bc
                    }
                }
            }

            val codes = dedup.mapNotNull { (raw, bc) ->
                val box = bc.boundingBox ?: return@mapNotNull null
                val cx = ((box.left + box.right) / 2f / rotatedWidth).coerceIn(0f, 1f)
                val cy = ((box.top + box.bottom) / 2f / rotatedHeight).coerceIn(0f, 1f)
                DetectedCode(
                    rawValue = raw,
                    xFraction = cx,
                    yFraction = cy,
                    format = bc.format,
                )
            }
            onResult(codes)
        }
        .addOnCompleteListener {
            imageProxy.close()
        }
}