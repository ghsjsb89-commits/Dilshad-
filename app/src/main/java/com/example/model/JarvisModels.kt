package com.example.model

enum class JarvisStatus(val label: String, val labelUrdu: String) {
    STANDBY("VIP Standby", "تیار - حاضر ہوں"),
    LISTENING("Listening...", "سن رہا ہوں..."),
    REASONING("Reasoning & Planning...", "غور و فکر جاری ہے..."),
    EXECUTING("Executing Action...", "عمل درآمد جاری ہے..."),
    SPEAKING("Jarvis Speaking...", "جواب دیا جا رہا ہے..."),
    ERROR("Attention Required", "توجہ درکار ہے")
}

enum class StepState {
    WAITING, RUNNING, DONE, FAILED
}

data class TaskStep(
    val id: String,
    val title: String,
    val titleUrdu: String,
    val state: StepState = StepState.WAITING,
    val detail: String = ""
)

enum class TaskExecutionStatus {
    PENDING, CONFIRMATION_REQUIRED, EXECUTING, COMPLETED, FAILED
}

data class ConfirmationPayload(
    val actionType: String,
    val targetName: String,
    val targetContactOrPhone: String,
    val content: String,
    val onConfirmAction: () -> Unit
)

data class JarvisTask(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val titleUrdu: String,
    val timestamp: Long = System.currentTimeMillis(),
    val intentType: IntentType,
    val status: TaskExecutionStatus = TaskExecutionStatus.PENDING,
    val steps: List<TaskStep> = emptyList(),
    val resultMessage: String = "",
    val requiresConfirmation: Boolean = false,
    val confirmationPayload: ConfirmationPayload? = null
)

enum class IntentType {
    OPEN_APP,
    SYSTEM_SETTING,
    CALL_PHONE,
    SEND_SMS,
    SET_ALARM,
    SET_TIMER,
    ACCESSIBILITY_NAV,
    ACCESSIBILITY_TYPE,
    ISLAMIC_QURAN,
    ISLAMIC_DUA,
    ISLAMIC_PRAYER,
    ISLAMIC_QIBLA,
    GENERAL_CHAT,
    EMERGENCY_STOP,
    UNKNOWN
}

enum class LanguagePreference(val displayName: String) {
    AUTO("Smart Auto (Urdu / Roman / Eng)"),
    URDU("Urdu (اردو)"),
    ROMAN_URDU("Roman Urdu"),
    ENGLISH("English")
}

data class CommandIntent(
    val type: IntentType,
    val targetApp: String? = null,
    val contactName: String? = null,
    val phoneNumber: String? = null,
    val messageText: String? = null,
    val timerSeconds: Int? = null,
    val alarmHour: Int? = null,
    val alarmMinute: Int? = null,
    val navAction: String? = null,
    val textToType: String? = null,
    val searchQuery: String? = null,
    val duaCategory: String? = null,
    val surahNumberOrName: String? = null,
    val rawText: String = "",
    val detectedLanguage: String = "English"
)

data class QuranAyah(
    val surahNumber: Int,
    val surahNameArabic: String,
    val surahNameEnglish: String,
    val ayahNumber: Int,
    val arabicText: String,
    val urduTranslation: String,
    val englishTranslation: String,
    val theme: String
)

enum class DuaCategory(val titleUrdu: String, val titleEnglish: String) {
    MORNING_EVENING("صبح و شام کے اذکار", "Morning & Evening Azkar"),
    TRAVEL("سفر کی دعا", "Travel & Journey"),
    PROTECTION("حفاظت اور پناہ", "Protection & Evil Eye"),
    DISTRESS("پریشانی اور غم دور کرنے کی دعا", "Relief from Distress & Anxiety"),
    HEALTH_SHIFA("شفائے امراض کی دعا", "Health & Healing"),
    KNOWLEDGE("علم و حکمت کی دعا", "Wisdom & Knowledge"),
    FORGIVENESS("استغفار اور توبہ", "Forgiveness & Istighfar")
}

data class IslamicDua(
    val id: String,
    val titleUrdu: String,
    val titleEnglish: String,
    val category: DuaCategory,
    val arabicText: String,
    val transliteration: String,
    val urduTranslation: String,
    val englishTranslation: String,
    val authenticSource: String
)

data class PrayerTime(
    val nameEnglish: String,
    val nameUrdu: String,
    val timeFormatted: String,
    val hour24: Int,
    val minute: Int,
    val isNext: Boolean = false,
    val minutesRemaining: Long = 0
)

data class InstalledAppItem(
    val name: String,
    val packageName: String,
    val isSuggested: Boolean = false
)
