@file:Suppress("SpellCheckingInspection", "UNUSED_PARAMETER")

package com.torachen.mote.ui.ai

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import top.yukonga.miuix.kmp.basic.Text
import com.torachen.mote.ui.MoteAppContextHolder
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * 在线品牌图标加载器。
 *
 * 内存 + `cacheDir/ai_icons` 常驻缓存；系统清理缓存或用户清理空间后会重新下载。
 */
internal object AiIconLoader {

    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val cache = ConcurrentHashMap<String, Bitmap?>()
    private val failed = ConcurrentHashMap.newKeySet<String>()

    private fun cacheFile(url: String): File? {
        val context = MoteAppContextHolder.appContext ?: return null
        val dir = File(context.cacheDir, "ai_icons").apply { mkdirs() }
        val digest = MessageDigest.getInstance("SHA-1")
            .digest(url.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(dir, "$digest.img")
    }

    suspend fun load(url: String): Bitmap? = withContext(Dispatchers.IO) {
        cache[url]?.let { return@withContext it }
        if (url in failed) return@withContext null

        cacheFile(url)?.takeIf { it.isFile }?.let { file ->
            val cached = runCatching {
                BitmapFactory.decodeFile(file.absolutePath)
            }.getOrNull()
            if (cached != null) {
                cache[url] = cached
                return@withContext cached
            }
        }

        val bitmap = runCatching {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mote/Android")
                .build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@use null
                val body = response.body ?: return@use null
                if (body.contentLength() > 2L * 1024L * 1024L) return@use null
                val bytes = body.bytes()
                val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (decoded != null) {
                    runCatching { cacheFile(url)?.writeBytes(bytes) }
                }
                decoded
            }
        }.getOrNull()

        if (bitmap == null) {
            failed.add(url)
        } else {
            cache[url] = bitmap
        }
        bitmap
    }
}

/** 设置页品牌图标：品牌色底板 + 白色图案/首字母（保持设置页原有风格）。 */
@Composable
internal fun AiBrandIcon(
    text: String,
    color: Color,
    iconUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 27.5.dp,
    cornerRadius: Dp = 8.25.dp,
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, iconUrl) {
        value = iconUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { url -> AiIconLoader.load(url) }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(size * 0.18f),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.White),
            )
        } else {
            Text(
                text = text.trim().take(1).uppercase(),
                color = Color.White,
                fontSize = (size.value * 0.47f).sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}

/** 对话页快捷栏/下拉菜单图标：透明背景、原色、1:1、图案尽量填满。 */
@Composable
internal fun AiPlainBrandIcon(
    text: String,
    color: Color,
    iconUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = 20.dp,
) {
    val bitmap by produceState<Bitmap?>(initialValue = null, iconUrl) {
        value = iconUrl
            ?.takeIf { it.isNotBlank() }
            ?.let { url -> AiIconLoader.load(url) }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        val image = bitmap
        if (image != null) {
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        } else {
            Text(
                text = text.trim().take(1).uppercase(),
                color = color,
                fontSize = (size.value * 0.72f).sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
        }
    }
}
