package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.model.LanguagePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class JarvisPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("muslim_jarvis_vip_prefs", Context.MODE_PRIVATE)

    private val _userName = MutableStateFlow(prefs.getString(KEY_USER_NAME, "VIP Boss") ?: "VIP Boss")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _language = MutableStateFlow(
        try {
            LanguagePreference.valueOf(prefs.getString(KEY_LANGUAGE, LanguagePreference.AUTO.name) ?: LanguagePreference.AUTO.name)
        } catch (_: Exception) {
            LanguagePreference.AUTO
        }
    )
    val language: StateFlow<LanguagePreference> = _language.asStateFlow()

    private val _wakeWordEnabled = MutableStateFlow(prefs.getBoolean(KEY_WAKE_WORD, true))
    val wakeWordEnabled: StateFlow<Boolean> = _wakeWordEnabled.asStateFlow()

    private val _voiceSpeed = MutableStateFlow(prefs.getFloat(KEY_VOICE_SPEED, 1.0f))
    val voiceSpeed: StateFlow<Float> = _voiceSpeed.asStateFlow()

    private val _voicePitch = MutableStateFlow(prefs.getFloat(KEY_VOICE_PITCH, 0.95f))
    val voicePitch: StateFlow<Float> = _voicePitch.asStateFlow()

    private val _floatingHudEnabled = MutableStateFlow(prefs.getBoolean(KEY_FLOATING_HUD, true))
    val floatingHudEnabled: StateFlow<Boolean> = _floatingHudEnabled.asStateFlow()

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
        _userName.value = name
    }

    fun setLanguage(preference: LanguagePreference) {
        prefs.edit().putString(KEY_LANGUAGE, preference.name).apply()
        _language.value = preference
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_WAKE_WORD, enabled).apply()
        _wakeWordEnabled.value = enabled
    }

    fun setVoiceSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_VOICE_SPEED, speed).apply()
        _voiceSpeed.value = speed
    }

    fun setVoicePitch(pitch: Float) {
        prefs.edit().putFloat(KEY_VOICE_PITCH, pitch).apply()
        _voicePitch.value = pitch
    }

    fun setFloatingHudEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_FLOATING_HUD, enabled).apply()
        _floatingHudEnabled.value = enabled
    }

    companion object {
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_LANGUAGE = "key_language"
        private const val KEY_WAKE_WORD = "key_wake_word"
        private const val KEY_VOICE_SPEED = "key_voice_speed"
        private const val KEY_VOICE_PITCH = "key_voice_pitch"
        private const val KEY_FLOATING_HUD = "key_floating_hud"
    }
}
