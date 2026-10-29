package com.example.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.JarvisAiEngine
import com.example.core.PhoneActionExecutor
import com.example.core.QiblaCompassManager
import com.example.core.SpeechAndVoiceEngine
import com.example.data.IslamicRepository
import com.example.data.JarvisPreferences
import com.example.model.*
import com.example.service.JarvisAccessibilityService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PermissionStatusMatrix(
    val hasMic: Boolean = false,
    val hasContacts: Boolean = false,
    val hasPhoneCall: Boolean = false,
    val hasSms: Boolean = false,
    val hasLocation: Boolean = false,
    val hasAccessibility: Boolean = false,
    val hasOverlay: Boolean = false,
    val hasNotifications: Boolean = false
)

class JarvisViewModel(application: Application) : AndroidViewModel(application) {

    val preferences = JarvisPreferences(application)
    val executor = PhoneActionExecutor(application)
    val aiEngine = JarvisAiEngine()
    val qiblaManager = QiblaCompassManager(application)

    private val _status = MutableStateFlow(JarvisStatus.STANDBY)
    val status: StateFlow<JarvisStatus> = _status.asStateFlow()

    private val _currentTask = MutableStateFlow<JarvisTask?>(null)
    val currentTask: StateFlow<JarvisTask?> = _currentTask.asStateFlow()

    private val _taskHistory = MutableStateFlow<List<JarvisTask>>(emptyList())
    val taskHistory: StateFlow<List<JarvisTask>> = _taskHistory.asStateFlow()

    private val _activeConfirmation = MutableStateFlow<ConfirmationPayload?>(null)
    val activeConfirmation: StateFlow<ConfirmationPayload?> = _activeConfirmation.asStateFlow()

    private val _transcript = MutableStateFlow("Press microphone or say 'Hey Jarvis' to speak.")
    val transcript: StateFlow<String> = _transcript.asStateFlow()

    private val _permissions = MutableStateFlow(PermissionStatusMatrix())
    val permissions: StateFlow<PermissionStatusMatrix> = _permissions.asStateFlow()

    private val _floatingHudExpanded = MutableStateFlow(false)
    val floatingHudExpanded: StateFlow<Boolean> = _floatingHudExpanded.asStateFlow()

    private val _prayerTimes = MutableStateFlow(IslamicRepository.calculatePrayerTimes())
    val prayerTimes: StateFlow<List<PrayerTime>> = _prayerTimes.asStateFlow()

    private val _userCoordinates = MutableStateFlow(Pair(21.4225, 39.8262)) // Default Holy Mecca
    val userCoordinates: StateFlow<Pair<Double, Double>> = _userCoordinates.asStateFlow()

    private val _qiblaBearing = MutableStateFlow(265f)
    val qiblaBearing: StateFlow<Float> = _qiblaBearing.asStateFlow()

    // Voice Engine
    val voiceEngine: SpeechAndVoiceEngine = SpeechAndVoiceEngine(
        context = application,
        onSpeechResult = { text ->
            processSpokenCommand(text)
        },
        onSpeechPartial = { partial ->
            _transcript.value = partial
        }
    )

    init {
        refreshPermissions()
        recalculateQibla()
        greetUserOnLaunch()
    }

    private fun greetUserOnLaunch() {
        viewModelScope.launch {
            delay(800)
            val name = preferences.userName.value
            val greeting = "Assalamu Alaikum wa Rahmatullah, $name! Muslim Jarvis VIP is operational and at your service."
            _transcript.value = greeting
            speak(greeting)
        }
    }

    fun refreshPermissions() {
        val ctx = getApplication<Application>()
        val hasMic = ContextCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        val hasContacts = ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        val hasPhone = ContextCompat.checkSelfPermission(ctx, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
        val hasSms = ContextCompat.checkSelfPermission(ctx, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED
        val hasLoc = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasAccess = JarvisAccessibilityService.isConnected()
        val hasOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Settings.canDrawOverlays(ctx) else true
        val hasNotif = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true

        _permissions.value = PermissionStatusMatrix(
            hasMic = hasMic,
            hasContacts = hasContacts,
            hasPhoneCall = hasPhone,
            hasSms = hasSms,
            hasLocation = hasLoc,
            hasAccessibility = hasAccess,
            hasOverlay = hasOverlay,
            hasNotifications = hasNotif
        )
    }

    fun startListening() {
        refreshPermissions()
        if (!_permissions.value.hasMic) {
            _status.value = JarvisStatus.ERROR
            _transcript.value = "Microphone permission is required to listen. Please grant it in permissions tab."
            speak("Microphone permission is required to listen. Please grant it.")
            return
        }
        _status.value = JarvisStatus.LISTENING
        voiceEngine.startListening(preferences.language.value)
    }

    fun stopListening() {
        voiceEngine.stopListening()
        _status.value = JarvisStatus.STANDBY
    }

    fun processSpokenCommand(commandText: String) {
        if (commandText.isBlank()) return
        _transcript.value = commandText

        viewModelScope.launch {
            _status.value = JarvisStatus.REASONING
            val intent = aiEngine.analyzeCommand(commandText)
            val task = aiEngine.buildTaskForIntent(intent)
            _currentTask.value = task

            executeTask(task, intent)
        }
    }

    private suspend fun executeTask(task: JarvisTask, intent: CommandIntent) {
        _status.value = JarvisStatus.EXECUTING

        when (intent.type) {
            IntentType.EMERGENCY_STOP -> {
                emergencyStop()
                return
            }

            IntentType.CALL_PHONE -> {
                updateStep(0, StepState.RUNNING)
                delay(300)
                val contactName = intent.contactName ?: "Contact"
                val matches = executor.findContacts(contactName)
                updateStep(0, StepState.DONE, if (matches.isNotEmpty()) "Found ${matches.size} match" else "Searching device...")

                updateStep(1, StepState.RUNNING)
                delay(200)
                updateStep(1, StepState.DONE)

                updateStep(2, StepState.RUNNING)
                val targetPhone = if (matches.isNotEmpty()) matches[0].second else contactName
                val (success, message) = executor.initiateCall(targetPhone)
                if (success) {
                    updateStep(2, StepState.DONE, message)
                    finalizeTask(true, message)
                    speak(message)
                } else {
                    updateStep(2, StepState.FAILED, message)
                    finalizeTask(false, message)
                    speak(message)
                }
            }

            IntentType.SEND_SMS -> {
                updateStep(0, StepState.RUNNING)
                val contactName = intent.contactName ?: "Recipient"
                val matches = executor.findContacts(contactName)
                val targetPhone = if (matches.isNotEmpty()) matches[0].second else "03001234567"
                updateStep(0, StepState.DONE, "Recipient: $contactName ($targetPhone)")

                updateStep(1, StepState.RUNNING)
                val textMsg = intent.messageText ?: "Assalamu Alaikum! (Sent via Muslim Jarvis VIP)"
                updateStep(1, StepState.DONE, "Drafted: '$textMsg'")

                // Safety requirement: Request confirmation before sending
                updateStep(2, StepState.RUNNING, "Awaiting your authorization")
                _activeConfirmation.value = ConfirmationPayload(
                    actionType = "Send SMS Message",
                    targetName = contactName,
                    targetContactOrPhone = targetPhone,
                    content = textMsg,
                    onConfirmAction = {
                        viewModelScope.launch {
                            _activeConfirmation.value = null
                            updateStep(2, StepState.DONE, "Authorized by user")
                            updateStep(3, StepState.RUNNING)
                            val (success, message) = executor.sendDirectSms(targetPhone, textMsg)
                            if (success) {
                                updateStep(3, StepState.DONE, message)
                                finalizeTask(true, message)
                                speak("Message dispatched to $contactName.")
                            } else {
                                updateStep(3, StepState.FAILED, message)
                                finalizeTask(false, message)
                                speak(message)
                            }
                        }
                    }
                )
                speak("I have drafted the message for $contactName. Please confirm on screen to send.")
            }

            IntentType.OPEN_APP -> {
                updateStep(0, StepState.RUNNING)
                val app = intent.targetApp ?: "App"
                updateStep(0, StepState.DONE, "Target: $app")

                updateStep(1, StepState.RUNNING)
                val (success, message) = executor.openApp(app)
                if (success) {
                    updateStep(1, StepState.DONE, message)
                    finalizeTask(true, message)
                    speak("Opening $app.")
                } else {
                    updateStep(1, StepState.FAILED, message)
                    finalizeTask(false, message)
                    speak(message)
                }
            }

            IntentType.SYSTEM_SETTING -> {
                updateStep(0, StepState.RUNNING)
                val setting = intent.targetApp ?: "settings"
                updateStep(0, StepState.DONE)

                updateStep(1, StepState.RUNNING)
                val (success, message) = executor.openSystemSetting(setting)
                if (success) {
                    updateStep(1, StepState.DONE, message)
                    finalizeTask(true, message)
                    speak("Opening $setting settings.")
                } else {
                    updateStep(1, StepState.FAILED, message)
                    finalizeTask(false, message)
                    speak(message)
                }
            }

            IntentType.SET_ALARM -> {
                updateStep(0, StepState.RUNNING)
                val h = intent.alarmHour ?: 6
                val m = intent.alarmMinute ?: 0
                updateStep(0, StepState.DONE, "Time: %02d:%02d".format(h, m))

                updateStep(1, StepState.RUNNING)
                val (success, message) = executor.setAlarm(h, m)
                if (success) {
                    updateStep(1, StepState.DONE, message)
                    finalizeTask(true, message)
                    speak(message)
                } else {
                    updateStep(1, StepState.FAILED, message)
                    finalizeTask(false, message)
                    speak(message)
                }
            }

            IntentType.SET_TIMER -> {
                updateStep(0, StepState.RUNNING)
                val secs = intent.timerSeconds ?: 300
                updateStep(0, StepState.DONE, "Length: ${secs}s")

                updateStep(1, StepState.RUNNING)
                val (success, message) = executor.setTimer(secs)
                if (success) {
                    updateStep(1, StepState.DONE, message)
                    finalizeTask(true, message)
                    speak(message)
                } else {
                    updateStep(1, StepState.FAILED, message)
                    finalizeTask(false, message)
                    speak(message)
                }
            }

            IntentType.ISLAMIC_QURAN -> {
                updateStep(0, StepState.RUNNING)
                val query = intent.surahNumberOrName ?: "kursi"
                val ayah = when (query) {
                    "kursi" -> IslamicRepository.quranVerses[0]
                    "inshirah" -> IslamicRepository.quranVerses[1]
                    "fatiha" -> IslamicRepository.quranVerses[5]
                    "ikhlas" -> IslamicRepository.quranVerses[5]
                    else -> IslamicRepository.quranVerses.random()
                }
                updateStep(0, StepState.DONE, "Surah ${ayah.surahNameEnglish} (${ayah.surahNumber}:${ayah.ayahNumber})")

                updateStep(1, StepState.RUNNING)
                val responseMsg = "Surah ${ayah.surahNameEnglish}: ${ayah.arabicText}\nUrdu: ${ayah.urduTranslation}"
                updateStep(1, StepState.DONE)
                finalizeTask(true, responseMsg)
                speak("Bismillahir Rahmanir Raheem. ${ayah.urduTranslation}")
            }

            IntentType.ISLAMIC_DUA -> {
                updateStep(0, StepState.RUNNING)
                val category = intent.duaCategory ?: "travel"
                val dua = when (category) {
                    "travel" -> IslamicRepository.authenticDuas[0]
                    "protection" -> IslamicRepository.authenticDuas[1]
                    "distress" -> IslamicRepository.authenticDuas[2]
                    "morning" -> IslamicRepository.authenticDuas[3]
                    "evening" -> IslamicRepository.authenticDuas[4]
                    "healing" -> IslamicRepository.authenticDuas[5]
                    else -> IslamicRepository.authenticDuas.random()
                }
                updateStep(0, StepState.DONE, dua.titleEnglish)

                updateStep(1, StepState.RUNNING)
                updateStep(1, StepState.DONE, "Source: ${dua.authenticSource}")
                val responseMsg = "${dua.titleUrdu}\n${dua.arabicText}\nترجمہ: ${dua.urduTranslation}\nحوالہ: ${dua.authenticSource}"
                finalizeTask(true, responseMsg)
                speak("${dua.titleEnglish}. ${dua.urduTranslation}")
            }

            IntentType.ISLAMIC_PRAYER -> {
                updateStep(0, StepState.RUNNING)
                val times = IslamicRepository.calculatePrayerTimes(_userCoordinates.value.first, _userCoordinates.value.second)
                _prayerTimes.value = times
                updateStep(0, StepState.DONE)

                updateStep(1, StepState.RUNNING)
                val next = times.find { it.isNext } ?: times[0]
                val msg = "The next prayer is ${next.nameEnglish} (${next.nameUrdu}) at ${next.timeFormatted} (${next.minutesRemaining} minutes remaining)."
                updateStep(1, StepState.DONE, msg)
                finalizeTask(true, msg)
                speak(msg)
            }

            IntentType.ISLAMIC_QIBLA -> {
                updateStep(0, StepState.RUNNING)
                recalculateQibla()
                updateStep(0, StepState.DONE, "Qibla angle: %01f° from North".format(_qiblaBearing.value))

                updateStep(1, StepState.RUNNING)
                qiblaManager.start(_qiblaBearing.value)
                val msg = "Qibla direction is %01f degrees from North towards the Holy Kaaba in Mecca.".format(_qiblaBearing.value)
                updateStep(1, StepState.DONE, msg)
                finalizeTask(true, msg)
                speak(msg)
            }

            IntentType.ACCESSIBILITY_NAV -> {
                updateStep(0, StepState.RUNNING)
                val action = intent.navAction ?: "back"
                if (!JarvisAccessibilityService.isConnected()) {
                    updateStep(0, StepState.FAILED, "Accessibility Service is not enabled")
                    finalizeTask(false, "Please enable Muslim Jarvis VIP in Android Accessibility settings.")
                    speak("Accessibility service is disabled. Please enable it to permit navigation.")
                } else {
                    updateStep(0, StepState.DONE)
                    updateStep(1, StepState.RUNNING)
                    val (success, message) = executor.executeAccessibilityAction(action)
                    if (success) {
                        updateStep(1, StepState.DONE, message)
                        finalizeTask(true, message)
                    } else {
                        updateStep(1, StepState.FAILED, message)
                        finalizeTask(false, message)
                    }
                }
            }

            IntentType.ACCESSIBILITY_TYPE -> {
                updateStep(0, StepState.RUNNING)
                val text = intent.textToType ?: "Hello"
                if (!JarvisAccessibilityService.isConnected()) {
                    updateStep(0, StepState.FAILED, "Accessibility Service disabled")
                    finalizeTask(false, "Enable Accessibility Service to type dictated text.")
                    speak("Accessibility service must be enabled to type text into active fields.")
                } else {
                    updateStep(0, StepState.DONE)
                    updateStep(1, StepState.RUNNING)
                    val (success, message) = executor.typeTextIntoFocused(text)
                    if (success) {
                        updateStep(1, StepState.DONE, message)
                        finalizeTask(true, message)
                        speak("Typed text successfully.")
                    } else {
                        updateStep(1, StepState.FAILED, message)
                        finalizeTask(false, message)
                        speak("No active text box was found on screen.")
                    }
                }
            }

            else -> {
                val greeting = if (intent.searchQuery == "greeting") {
                    "Wa Alaikum Assalam wa Rahmatullah! Main aapki kya khidmat kar sakta hoon, VIP Boss?"
                } else {
                    "Ji Boss! Muslim Jarvis VIP aapki khidmat me haazir hai. Aap mujhe koi bhi hukum de sakte hain jaise app kholna, call milana, ya namaz ke auqat janna."
                }
                finalizeTask(true, greeting)
                speak(greeting)
            }
        }
    }

    private fun updateStep(index: Int, state: StepState, detail: String? = null) {
        val current = _currentTask.value ?: return
        if (index in current.steps.indices) {
            val updatedList = current.steps.toMutableList()
            val oldStep = updatedList[index]
            updatedList[index] = oldStep.copy(state = state, detail = detail ?: oldStep.detail)
            _currentTask.value = current.copy(steps = updatedList)
        }
    }

    private fun finalizeTask(success: Boolean, resultMsg: String) {
        val task = _currentTask.value ?: return
        val finalStatus = if (success) TaskExecutionStatus.COMPLETED else TaskExecutionStatus.FAILED
        val finalized = task.copy(
            status = finalStatus,
            resultMessage = resultMsg
        )
        _currentTask.value = finalized
        _taskHistory.value = listOf(finalized) + _taskHistory.value.take(19)
        _status.value = if (success) JarvisStatus.STANDBY else JarvisStatus.ERROR
    }

    fun dismissConfirmation() {
        _activeConfirmation.value = null
        val task = _currentTask.value
        if (task != null && task.status == TaskExecutionStatus.CONFIRMATION_REQUIRED) {
            _currentTask.value = task.copy(status = TaskExecutionStatus.FAILED, resultMessage = "Cancelled by user.")
        }
        _status.value = JarvisStatus.STANDBY
    }

    fun speak(text: String) {
        _status.value = JarvisStatus.SPEAKING
        voiceEngine.speak(
            text = text,
            speed = preferences.voiceSpeed.value,
            pitch = preferences.voicePitch.value,
            onDone = {
                _status.value = JarvisStatus.STANDBY
            }
        )
    }

    fun emergencyStop() {
        voiceEngine.stopListening()
        voiceEngine.stopSpeaking()
        _activeConfirmation.value = null
        val current = _currentTask.value
        if (current != null) {
            _currentTask.value = current.copy(status = TaskExecutionStatus.FAILED, resultMessage = "Emergency stop initiated.")
        }
        _status.value = JarvisStatus.STANDBY
        _transcript.value = "EMERGENCY HALT: All assistant operations terminated."
    }

    fun toggleFloatingHud() {
        _floatingHudExpanded.value = !_floatingHudExpanded.value
    }

    fun updateCoordinates(lat: Double, lng: Double) {
        _userCoordinates.value = Pair(lat, lng)
        _prayerTimes.value = IslamicRepository.calculatePrayerTimes(lat, lng)
        recalculateQibla()
    }

    private fun recalculateQibla() {
        val bearing = IslamicRepository.calculateQiblaDirection(_userCoordinates.value.first, _userCoordinates.value.second)
        _qiblaBearing.value = bearing
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.destroy()
        qiblaManager.stop()
    }
}
