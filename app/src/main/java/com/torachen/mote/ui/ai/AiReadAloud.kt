package com.torachen.mote.ui.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/** AI 回复朗读：使用系统 TTS，按段落切分避免单次文本过长。 */
internal object AiReadAloud {
    private var engine: TextToSpeech? = null
    private var ready = false
    private var pending: String? = null
    private var utteranceCount = 0

    var speaking by mutableStateOf(false)
        private set
    var loading by mutableStateOf(false)
        private set

    fun toggle(context: Context, text: String) {
        if (speaking || loading) {
            stop()
            return
        }
        speak(context, text)
    }

    fun speak(context: Context, text: String) {
        val clean = text.trim()
        if (clean.isBlank()) return
        loading = true
        pending = clean
        val tts = engine
        if (tts != null) {
            if (ready) speakNow(tts, clean)
            return
        }
        engine = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (!ready) {
                loading = false
                Toast.makeText(context, "系统朗读不可用", Toast.LENGTH_SHORT).show()
                return@TextToSpeech
            }
            val textToSpeak = pending ?: return@TextToSpeech
            pending = null
            speakNow(engine ?: return@TextToSpeech, textToSpeak)
        }
        engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                loading = false
                speaking = true
            }

            override fun onDone(utteranceId: String?) {
                utteranceCount = (utteranceCount - 1).coerceAtLeast(0)
                if (utteranceCount == 0) speaking = false
            }

            override fun onError(utteranceId: String?) {
                loading = false
                utteranceCount = 0
                speaking = false
            }

            override fun onStop(utteranceId: String?, interrupted: Boolean) {
                loading = false
                utteranceCount = 0
                speaking = false
            }
        })
    }

    private fun speakNow(tts: TextToSpeech, text: String) {
        loading = true
        tts.language = Locale.getDefault()
        tts.stop()
        speaking = true
        val chunks = text.chunked(2_800)
        utteranceCount = chunks.size
        chunks.forEachIndexed { index, chunk ->
            tts.speak(
                chunk,
                if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD,
                null,
                "mote-ai-$index",
            )
        }
    }

    fun stop() {
        pending = null
        loading = false
        speaking = false
        utteranceCount = 0
        engine?.stop()
    }
}
