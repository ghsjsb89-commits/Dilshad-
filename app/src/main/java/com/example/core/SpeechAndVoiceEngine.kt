package com.example.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import com.example.model.LanguagePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class SpeechAndVoiceEngine(
    private val context: Context,
    private val onSpeechResult: (String) -> Unit,
    private val onSpeechPartial: (String) -> Unit
) : RecognitionListener, TextToSpeech.OnInitListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Real-time sound level for the pulsing VIP AI Core sound-wave animation (0f to 1f)
    private val _soundWaveLevel = MutableStateFlow(0f)
    val soundWaveLevel: StateFlow<Float> = _soundWaveLevel.asStateFlow()

    private val _lastRecognizedText = MutableStateFlow("")
    val lastRecognizedText: StateFlow<String> = _lastRecognizedText.asStateFlow()

    private val _statusMessage = MutableStateFlow("Ready")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    init {
        initSpeechRecognizer()
        textToSpeech = TextToSpeech(context, this)
    }

    private fun initSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@SpeechAndVoiceEngine)
            }
        }
    }

    fun startListening(languagePreference: LanguagePreference) {
        if (speechRecognizer == null) {
            initSpeechRecognizer()
        }

        stopSpeaking()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)

            val langTag = when (languagePreference) {
                LanguagePreference.URDU -> "ur-PK"
                LanguagePreference.ROMAN_URDU -> "ur"
                LanguagePreference.ENGLISH -> "en-US"
                LanguagePreference.AUTO -> "ur-PK"
            }
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
        }

        try {
            speechRecognizer?.startListening(intent)
            _isListening.value = true
            _statusMessage.value = "Listening to your command..."
        } catch (e: Exception) {
            Log.e(TAG, "Error starting speech listening", e)
            _isListening.value = false
            _statusMessage.value = "Mic unavailable"
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {}
        _isListening.value = false
        _soundWaveLevel.value = 0f
    }

    fun cancelListening() {
        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {}
        _isListening.value = false
        _soundWaveLevel.value = 0f
    }

    fun speak(text: String, speed: Float = 1.0f, pitch: Float = 1.0f, onDone: (() -> Unit)? = null) {
        if (!isTtsReady || textToSpeech == null) {
            Log.w(TAG, "TTS not ready yet")
            return
        }

        textToSpeech?.setSpeechRate(speed)
        textToSpeech?.setPitch(pitch)

        // Try setting Urdu locale if text contains Urdu characters, otherwise default English
        val hasUrdu = text.any { it in '\u0600'..'\u06FF' }
        if (hasUrdu) {
            val urduLocale = Locale("ur", "PK")
            val result = textToSpeech?.setLanguage(urduLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                textToSpeech?.language = Locale.ENGLISH
            }
        } else {
            textToSpeech?.language = Locale.ENGLISH
        }

        _isSpeaking.value = true
        val utteranceId = "jarvis_speech_${System.currentTimeMillis()}"

        textToSpeech?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
                onDone?.invoke()
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })

        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (_: Exception) {}
        _isSpeaking.value = false
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            textToSpeech?.language = Locale.ENGLISH
            Log.i(TAG, "TextToSpeech initialized successfully.")
        } else {
            Log.e(TAG, "TextToSpeech init failed with status: $status")
        }
    }

    override fun onReadyForSpeech(params: Bundle?) {
        _isListening.value = true
        _statusMessage.value = "Listening..."
    }

    override fun onBeginningOfSpeech() {
        _statusMessage.value = "Voice detected..."
    }

    override fun onRmsChanged(rmsdB: Float) {
        // Normalize rmsdB from typical -2dB..10dB to 0f..1f for core pulsing animation
        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
        _soundWaveLevel.value = normalized
    }

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        _isListening.value = false
        _soundWaveLevel.value = 0f
        _statusMessage.value = "Processing command..."
    }

    override fun onError(error: Int) {
        _isListening.value = false
        _soundWaveLevel.value = 0f
        val msg = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client speech error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Mic permission needed"
            SpeechRecognizer.ERROR_NETWORK -> "Network required for speech"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer busy"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timed out"
            else -> "Speech recognition error ($error)"
        }
        _statusMessage.value = msg
    }

    override fun onResults(results: Bundle?) {
        _isListening.value = false
        _soundWaveLevel.value = 0f
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val text = matches[0]
            _lastRecognizedText.value = text
            onSpeechResult(text)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val partial = matches[0]
            _lastRecognizedText.value = partial
            onSpeechPartial(partial)
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    fun destroy() {
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Exception) {}
    }

    companion object {
        private const val TAG = "JarvisSpeechEngine"
    }
}
