package com.example.core

import android.util.Log
import com.example.data.IslamicRepository
import com.example.model.CommandIntent
import com.example.model.IntentType
import com.example.model.JarvisTask
import com.example.model.StepState
import com.example.model.TaskExecutionStatus
import com.example.model.TaskStep
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.withTimeout
import java.util.Locale
import java.util.regex.Pattern

class JarvisAiEngine {

    // Lazy initialization of Firebase AI Logic model per guidelines
    private val geminiModel by lazy {
        try {
            Firebase.ai.generativeModel(
                modelName = "gemini-3.8-flash",
                generationConfig = generationConfig {
                    temperature = 0.4f
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "Firebase AI Logic not active; local NLP reasoning will serve queries.", e)
            null
        }
    }

    suspend fun analyzeCommand(rawText: String): CommandIntent {
        val trimmed = rawText.trim()
        val detectedLanguage = detectLanguage(trimmed)

        // 1. Emergency stop check
        if (isEmergencyStop(trimmed)) {
            return CommandIntent(
                type = IntentType.EMERGENCY_STOP,
                rawText = trimmed,
                detectedLanguage = detectedLanguage
            )
        }

        // 2. High-speed, robust local NLP classifier
        val localIntent = parseLocally(trimmed, detectedLanguage)
        if (localIntent.type != IntentType.UNKNOWN && localIntent.type != IntentType.GENERAL_CHAT) {
            return localIntent
        }

        // 3. Fallback to Gemini AI for nuanced reasoning if available
        val model = geminiModel
        if (model != null) {
            try {
                val prompt = """
                    You are Muslim Jarvis, a VIP Android AI Assistant. Analyze the user command in Urdu, Roman Urdu, or English:
                    Command: "$trimmed"
                    
                    Respond ONLY with one line in this format:
                    INTENT:[OPEN_APP|SYSTEM_SETTING|CALL_PHONE|SEND_SMS|SET_ALARM|SET_TIMER|ISLAMIC_QURAN|ISLAMIC_DUA|ISLAMIC_PRAYER|ISLAMIC_QIBLA|ACCESSIBILITY_NAV|GENERAL_CHAT] | TARGET:[app/contact/time/value] | EXTRA:[message/query]
                """.trimIndent()

                val response = withTimeout(6000L) {
                    model.generateContent(prompt)
                }
                val text = response.text.orEmpty().trim()
                val parsed = parseModelResponse(text, trimmed, detectedLanguage)
                if (parsed != null) return parsed
            } catch (e: Exception) {
                Log.w(TAG, "Gemini analysis skipped/failed: ${e.localizedMessage}")
            }
        }

        return localIntent
    }

    private fun detectLanguage(text: String): String {
        val hasUrduScript = text.any { it in '\u0600'..'\u06FF' }
        if (hasUrduScript) return "Urdu"
        val lower = text.lowercase()
        val romanKeywords = listOf("kholo", "bhejo", "lagao", "suno", "sunao", "waqt", "kya", "hain", "meri", "karo", "batao", "chalao")
        if (romanKeywords.any { lower.contains(it) }) return "Roman Urdu"
        return "English"
    }

    private fun isEmergencyStop(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("stop") || lower.contains("halt") || lower.contains("ruko") || 
               lower.contains("ruk jao") || lower.contains("روک دو") || lower.contains("ٹھہرو")
    }

    private fun parseLocally(text: String, language: String): CommandIntent {
        val lower = text.lowercase().trim()

        // 1. Calling
        // "Call Mama", "Mama ko call karo", "Call lagao Ali", "کال کرو احمد کو"
        if (lower.startsWith("call ") || lower.contains("ko call") || lower.contains("call karo") || 
            lower.contains("call milao") || lower.contains("کال کرو") || lower.contains("کال ملاؤ")) {
            val contact = extractContactName(text)
            return CommandIntent(
                type = IntentType.CALL_PHONE,
                contactName = contact,
                rawText = text,
                detectedLanguage = language
            )
        }

        // 2. Messaging
        // "Send message to Ali Hello", "Ali ko message bhejo kal milte hain"
        if (lower.contains("message") || lower.contains("sms") || lower.contains("میسج") || lower.contains("پیغام")) {
            val (contact, msg) = extractSmsDetails(text)
            return CommandIntent(
                type = IntentType.SEND_SMS,
                contactName = contact,
                messageText = msg,
                rawText = text,
                detectedLanguage = language
            )
        }

        // 3. Islamic - Quran
        if (lower.contains("quran") || lower.contains("ayah") || lower.contains("surah") || 
            lower.contains("قرآن") || lower.contains("آیت") || lower.contains("سورۃ") || lower.contains("kursi") || lower.contains("کرسی")) {
            return CommandIntent(
                type = IntentType.ISLAMIC_QURAN,
                surahNumberOrName = extractSurahQuery(lower),
                rawText = text,
                detectedLanguage = language
            )
        }

        // 4. Islamic - Dua
        if (lower.contains("dua") || lower.contains("دعا") || lower.contains("azkar") || lower.contains("اذکار") ||
            lower.contains("safar") || lower.contains("safari") || lower.contains("travel") || lower.contains("hifazat") || lower.contains("protection")) {
            return CommandIntent(
                type = IntentType.ISLAMIC_DUA,
                duaCategory = extractDuaCategory(lower),
                rawText = text,
                detectedLanguage = language
            )
        }

        // 5. Islamic - Prayer Times & Qibla
        if (lower.contains("namaz") || lower.contains("prayer") || lower.contains("salah") || 
            lower.contains("نماز") || lower.contains("fajr") || lower.contains("maghrib") || lower.contains("dhuhr") || lower.contains("asr") || lower.contains("isha")) {
            return CommandIntent(
                type = IntentType.ISLAMIC_PRAYER,
                rawText = text,
                detectedLanguage = language
            )
        }

        if (lower.contains("qibla") || lower.contains("قبلہ") || lower.contains("kaaba") || lower.contains("کعبہ")) {
            return CommandIntent(
                type = IntentType.ISLAMIC_QIBLA,
                rawText = text,
                detectedLanguage = language
            )
        }

        // 6. Timers & Alarms
        if (lower.contains("timer") || lower.contains("ٹائمر")) {
            val seconds = extractTimeInSeconds(lower)
            return CommandIntent(
                type = IntentType.SET_TIMER,
                timerSeconds = seconds,
                rawText = text,
                detectedLanguage = language
            )
        }

        if (lower.contains("alarm") || lower.contains("الارم") || lower.contains("jga dena") || lower.contains("uthana")) {
            val (hour, minute) = extractAlarmTime(lower)
            return CommandIntent(
                type = IntentType.SET_ALARM,
                alarmHour = hour,
                alarmMinute = minute,
                rawText = text,
                detectedLanguage = language
            )
        }

        // 7. System Navigation & Accessibility
        if (lower.contains("back") || lower.contains("پیچھے") || lower.contains("piche jao")) {
            return CommandIntent(type = IntentType.ACCESSIBILITY_NAV, navAction = "back", rawText = text, detectedLanguage = language)
        }
        if (lower.contains("home") || lower.contains("ہوم")) {
            return CommandIntent(type = IntentType.ACCESSIBILITY_NAV, navAction = "home", rawText = text, detectedLanguage = language)
        }
        if (lower.contains("scroll down") || lower.contains("niche scroll")) {
            return CommandIntent(type = IntentType.ACCESSIBILITY_NAV, navAction = "scroll_down", rawText = text, detectedLanguage = language)
        }
        if (lower.contains("scroll up") || lower.contains("upar scroll")) {
            return CommandIntent(type = IntentType.ACCESSIBILITY_NAV, navAction = "scroll_up", rawText = text, detectedLanguage = language)
        }
        if (lower.startsWith("type ") || lower.contains("likho") || lower.contains("لکھو")) {
            val toType = text.replace(Regex("(?i)^(type|likho|لکھو)\\s+"), "")
            return CommandIntent(type = IntentType.ACCESSIBILITY_TYPE, textToType = toType, rawText = text, detectedLanguage = language)
        }

        // 8. Open Apps / Settings
        if (lower.contains("wifi") || lower.contains("wi-fi") || lower.contains("وای فائی")) {
            return CommandIntent(type = IntentType.SYSTEM_SETTING, targetApp = "wifi", rawText = text, detectedLanguage = language)
        }
        if (lower.contains("bluetooth") || lower.contains("بلوٹوتھ")) {
            return CommandIntent(type = IntentType.SYSTEM_SETTING, targetApp = "bluetooth", rawText = text, detectedLanguage = language)
        }
        if (lower.contains("camera") || lower.contains("کیمرہ")) {
            return CommandIntent(type = IntentType.OPEN_APP, targetApp = "camera", rawText = text, detectedLanguage = language)
        }
        if (lower.contains("settings") || lower.contains("سیٹنگ")) {
            return CommandIntent(type = IntentType.SYSTEM_SETTING, targetApp = "settings", rawText = text, detectedLanguage = language)
        }
        if (lower.startsWith("open ") || lower.contains("kholo") || lower.contains("chalao") || lower.contains("کھولو") || lower.contains("چلاؤ")) {
            val appName = extractAppName(text)
            return CommandIntent(type = IntentType.OPEN_APP, targetApp = appName, rawText = text, detectedLanguage = language)
        }

        // 9. Greetings & Polite Islamic conversation
        if (lower.contains("salam") || lower.contains("assalam") || lower.contains("سلام") || lower.contains("hello") || lower.contains("hi jarvis")) {
            return CommandIntent(
                type = IntentType.GENERAL_CHAT,
                rawText = text,
                detectedLanguage = language,
                searchQuery = "greeting"
            )
        }

        return CommandIntent(
            type = IntentType.GENERAL_CHAT,
            rawText = text,
            detectedLanguage = language
        )
    }

    private fun extractContactName(text: String): String {
        val cleaned = text
            .replace(Regex("(?i)^(call|dial|make a call to|please call)\\s+"), "")
            .replace(Regex("(?i)\\s+(ko call karo|ko call lagao|call lagao|call karo)$"), "")
            .replace(Regex("[^a-zA-Z0-9\\u0600-\\u06FF\\s]"), "")
            .trim()
        return cleaned.ifBlank { "Contact" }
    }

    private fun extractSmsDetails(text: String): Pair<String, String> {
        val pattern = Pattern.compile("(?i)(?:message|sms)\\s+(?:to\\s+)?([a-zA-Z0-9\\u0600-\\u06FF]+)(?:\\s+that\\s+|\\s+saying\\s+|\\s+ko\\s+|\\s+)(.*)")
        val matcher = pattern.matcher(text)
        return if (matcher.find()) {
            val contact = matcher.group(1).orEmpty().trim()
            val msg = matcher.group(2).orEmpty().trim().ifBlank { "Assalamu Alaikum! (Sent via Muslim Jarvis VIP)" }
            Pair(contact, msg)
        } else {
            Pair("Recipient", text)
        }
    }

    private fun extractTimeInSeconds(text: String): Int {
        val minMatcher = Pattern.compile("(\\d+)\\s*(?:min|minute|منٹ)").matcher(text)
        if (minMatcher.find()) {
            val mins = minMatcher.group(1)?.toIntOrNull() ?: 1
            return mins * 60
        }
        val secMatcher = Pattern.compile("(\\d+)\\s*(?:sec|second|سیکنڈ)").matcher(text)
        if (secMatcher.find()) {
            return secMatcher.group(1)?.toIntOrNull() ?: 30
        }
        return 300 // default 5 minutes
    }

    private fun extractAlarmTime(text: String): Pair<Int, Int> {
        val timeMatcher = Pattern.compile("(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE).matcher(text)
        if (timeMatcher.find()) {
            var hour = timeMatcher.group(1)?.toIntOrNull() ?: 6
            val min = timeMatcher.group(2)?.toIntOrNull() ?: 0
            val amPm = timeMatcher.group(3)?.lowercase()
            if (amPm == "pm" && hour < 12) hour += 12
            if (amPm == "am" && hour == 12) hour = 0
            return Pair(hour, min)
        }
        return Pair(6, 0) // default 6:00 AM
    }

    private fun extractAppName(text: String): String {
        return text
            .replace(Regex("(?i)^(open|launch|start|run)\\s+"), "")
            .replace(Regex("(?i)\\s+(kholo|chalao|open karo|کھولو|چلاؤ)$"), "")
            .trim()
    }

    private fun extractSurahQuery(lower: String): String {
        return when {
            lower.contains("kursi") || lower.contains("کرسی") -> "kursi"
            lower.contains("fatiha") || lower.contains("فاتحہ") -> "fatiha"
            lower.contains("ikhlas") || lower.contains("اخلاص") -> "ikhlas"
            lower.contains("rahman") || lower.contains("رحمان") -> "rahman"
            lower.contains("inshirah") || lower.contains("شرح") -> "inshirah"
            else -> "featured"
        }
    }

    private fun extractDuaCategory(lower: String): String {
        return when {
            lower.contains("safar") || lower.contains("travel") || lower.contains("سفر") -> "travel"
            lower.contains("morning") || lower.contains("subah") || lower.contains("صبح") -> "morning"
            lower.contains("evening") || lower.contains("sham") || lower.contains("شام") -> "evening"
            lower.contains("hifazat") || lower.contains("protection") || lower.contains("حفاظت") -> "protection"
            lower.contains("shifa") || lower.contains("health") || lower.contains("شفاء") -> "healing"
            lower.contains("distress") || lower.contains("pareshani") || lower.contains("پریشانی") -> "distress"
            else -> "all"
        }
    }

    private fun parseModelResponse(response: String, rawText: String, language: String): CommandIntent? {
        val parts = response.split("|").map { it.trim() }
        val intentMap = parts.associate {
            val split = it.split(":", limit = 2)
            if (split.size == 2) split[0].trim() to split[1].trim() else "" to ""
        }
        val intentStr = intentMap["INTENT"] ?: return null
        val target = intentMap["TARGET"]
        val extra = intentMap["EXTRA"]

        val type = when (intentStr) {
            "OPEN_APP" -> IntentType.OPEN_APP
            "SYSTEM_SETTING" -> IntentType.SYSTEM_SETTING
            "CALL_PHONE" -> IntentType.CALL_PHONE
            "SEND_SMS" -> IntentType.SEND_SMS
            "SET_ALARM" -> IntentType.SET_ALARM
            "SET_TIMER" -> IntentType.SET_TIMER
            "ISLAMIC_QURAN" -> IntentType.ISLAMIC_QURAN
            "ISLAMIC_DUA" -> IntentType.ISLAMIC_DUA
            "ISLAMIC_PRAYER" -> IntentType.ISLAMIC_PRAYER
            "ISLAMIC_QIBLA" -> IntentType.ISLAMIC_QIBLA
            "ACCESSIBILITY_NAV" -> IntentType.ACCESSIBILITY_NAV
            else -> IntentType.GENERAL_CHAT
        }

        return CommandIntent(
            type = type,
            targetApp = target,
            contactName = target,
            messageText = extra,
            rawText = rawText,
            detectedLanguage = language
        )
    }

    fun buildTaskForIntent(intent: CommandIntent): JarvisTask {
        val steps = mutableListOf<TaskStep>()
        var requiresConfirmation = false

        when (intent.type) {
            IntentType.CALL_PHONE -> {
                steps.add(TaskStep("1", "Find contact details", "رابطہ تلاش کیا جا رہا ہے"))
                steps.add(TaskStep("2", "Verify calling permissions", "اجازت نامہ چیک ہو رہا ہے"))
                steps.add(TaskStep("3", "Initiate phone call", "کال ملائی جا رہی ہے"))
            }
            IntentType.SEND_SMS -> {
                requiresConfirmation = true
                steps.add(TaskStep("1", "Match contact name", "نام کی تصدیق"))
                steps.add(TaskStep("2", "Draft message contents", "پیغام تیار کرنا"))
                steps.add(TaskStep("3", "Await VIP confirmation", "صارف کی اجازت کا انتظار"))
                steps.add(TaskStep("4", "Dispatch SMS message", "پیغام بھیجنا"))
            }
            IntentType.OPEN_APP, IntentType.SYSTEM_SETTING -> {
                steps.add(TaskStep("1", "Resolve target intent", "ایپلیکیشن تلاش کرنا"))
                steps.add(TaskStep("2", "Launch activity safely", "اسکرین کھولنا"))
            }
            IntentType.SET_ALARM, IntentType.SET_TIMER -> {
                steps.add(TaskStep("1", "Calculate duration/time", "وقت کا حساب"))
                steps.add(TaskStep("2", "Register with Clock Provider", "الارم رجسٹر کرنا"))
            }
            IntentType.ISLAMIC_QURAN -> {
                steps.add(TaskStep("1", "Retrieve authentic Quran Ayah", "مستند قرآنی آیت تلاش کرنا"))
                steps.add(TaskStep("2", "Format Arabic & Urdu translation", "ترجمہ و تشریح پیش کرنا"))
            }
            IntentType.ISLAMIC_DUA -> {
                steps.add(TaskStep("1", "Retrieve authentic Sunnah Dua", "مستند مسنون دعا تلاش کرنا"))
                steps.add(TaskStep("2", "Verify Hadith reference", "حوالہ حدیث کی تصدیق"))
            }
            IntentType.ISLAMIC_PRAYER -> {
                steps.add(TaskStep("1", "Query solar astronomical times", "اوقات نماز کا حساب لگانا"))
                steps.add(TaskStep("2", "Calculate next prayer countdown", "اگلی نماز کا وقت دکھانا"))
            }
            IntentType.ISLAMIC_QIBLA -> {
                steps.add(TaskStep("1", "Compute Kaaba coordinates vector", "سمت قبلہ کا زاویہ معلوم کرنا"))
                steps.add(TaskStep("2", "Calibrate magnetometer orientation", "کمپاس فعال کرنا"))
            }
            IntentType.ACCESSIBILITY_NAV, IntentType.ACCESSIBILITY_TYPE -> {
                steps.add(TaskStep("1", "Verify Accessibility Service", "ایکسیسبیلٹی سروس چیک کرنا"))
                steps.add(TaskStep("2", "Perform window action", "حکم پر عمل درآمد کرنا"))
            }
            IntentType.EMERGENCY_STOP -> {
                steps.add(TaskStep("1", "Halt all background operations", "تمام سرگرمیاں فوری بند کرنا", StepState.DONE))
            }
            else -> {
                steps.add(TaskStep("1", "Process natural voice input", "آواز کا تجزیہ", StepState.DONE))
                steps.add(TaskStep("2", "Synthesize VIP assistant reply", "جواب تیار کرنا", StepState.DONE))
            }
        }

        return JarvisTask(
            title = when (intent.type) {
                IntentType.CALL_PHONE -> "Call ${intent.contactName ?: "Contact"}"
                IntentType.SEND_SMS -> "Send SMS to ${intent.contactName ?: "Recipient"}"
                IntentType.OPEN_APP -> "Open ${intent.targetApp ?: "App"}"
                IntentType.SYSTEM_SETTING -> "Open ${intent.targetApp ?: "Settings"}"
                IntentType.SET_ALARM -> "Set Alarm %02d:%02d".format(intent.alarmHour ?: 6, intent.alarmMinute ?: 0)
                IntentType.SET_TIMER -> "Set Timer ${intent.timerSeconds ?: 60}s"
                IntentType.ISLAMIC_QURAN -> "Quran Exploration"
                IntentType.ISLAMIC_DUA -> "Sunnah Dua Recitation"
                IntentType.ISLAMIC_PRAYER -> "Prayer Times"
                IntentType.ISLAMIC_QIBLA -> "Qibla Direction"
                IntentType.ACCESSIBILITY_NAV -> "Navigate ${intent.navAction}"
                IntentType.ACCESSIBILITY_TYPE -> "Dictate Text"
                IntentType.EMERGENCY_STOP -> "Emergency Halt"
                else -> "VIP Conversation"
            },
            titleUrdu = when (intent.type) {
                IntentType.CALL_PHONE -> "کال: ${intent.contactName ?: "رابطہ"}"
                IntentType.SEND_SMS -> "پیغام: ${intent.contactName ?: "وصول کنندہ"}"
                IntentType.OPEN_APP -> "کھولیں: ${intent.targetApp ?: "ایپ"}"
                IntentType.SYSTEM_SETTING -> "سیٹنگز: ${intent.targetApp ?: "نظام"}"
                IntentType.SET_ALARM -> "الارم لگانا"
                IntentType.SET_TIMER -> "ٹائمر لگانا"
                IntentType.ISLAMIC_QURAN -> "تلاوت کلام پاک"
                IntentType.ISLAMIC_DUA -> "مسنون دعا"
                IntentType.ISLAMIC_PRAYER -> "اوقات نماز"
                IntentType.ISLAMIC_QIBLA -> "سمت قبلہ"
                IntentType.EMERGENCY_STOP -> "ہنگامی تعطل"
                else -> "معاون گفتگو"
            },
            intentType = intent.type,
            status = if (requiresConfirmation) TaskExecutionStatus.CONFIRMATION_REQUIRED else TaskExecutionStatus.PENDING,
            steps = steps,
            requiresConfirmation = requiresConfirmation
        )
    }

    companion object {
        private const val TAG = "JarvisAiEngine"
    }
}
