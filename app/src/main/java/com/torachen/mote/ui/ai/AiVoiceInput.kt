package com.torachen.mote.ui.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** 系统语音识别输入：实时返回音量与临时识别文字，结束把最终文本交给输入框。 */
internal object AiVoiceInput {

    var listening by mutableStateOf(false)
        private set
    var partialText by mutableStateOf("")
        private set
    var level by mutableFloatStateOf(0f)
        private set

    private var recognizer: SpeechRecognizer? = null

    fun start(
        context: Context,
        languageTag: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("当前设备不支持系统语音识别")
            return
        }
        stop()
        listening = true
        partialText = ""
        level = 0f
        val speech = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
        recognizer = speech
        speech.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) {
                level = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
            }

            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() = Unit
            override fun onError(error: Int) {
                val message = when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH -> "没有识别到内容"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "没有检测到说话"
                    SpeechRecognizer.ERROR_AUDIO -> "录音失败"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "没有录音权限"
                    SpeechRecognizer.ERROR_NETWORK,
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "语音识别网络错误"
                    else -> "语音识别失败（$error）"
                }
                release()
                onError(message)
            }

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                release()
                if (text.isBlank()) onError("没有识别到内容") else onResult(text)
            }

            override fun onPartialResults(partialResults: Bundle?) {
                partialText = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        runCatching { speech.startListening(intent) }.onFailure {
            release()
            onError("语音识别启动失败")
        }
    }

    fun stop() {
        runCatching { recognizer?.stopListening() }
        runCatching { recognizer?.cancel() }
        release()
    }

    private fun release() {
        listening = false
        level = 0f
        partialText = ""
        val speech = recognizer
        recognizer = null
        if (speech != null) runCatching { speech.destroy() }
    }
}
