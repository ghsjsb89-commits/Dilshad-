package com.example.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.SmsManager
import android.telecom.TelecomManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.model.InstalledAppItem
import com.example.service.JarvisAccessibilityService

class PhoneActionExecutor(private val context: Context) {

    // 1. App Launching & Discovery
    fun getInstalledApps(): List<InstalledAppItem> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfos = pm.queryIntentActivities(intent, 0)
        return resolveInfos.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            val label = resolveInfo.loadLabel(pm).toString()
            if (pkg == context.packageName) null
            else InstalledAppItem(name = label, packageName = pkg)
        }.sortedBy { it.name }
    }

    fun openApp(queryOrPackage: String): Pair<Boolean, String> {
        val pm = context.packageManager
        val lower = queryOrPackage.trim().lowercase()

        // Check well-known shortcuts
        val shortcutPkg = when {
            lower.contains("youtube") -> "com.google.android.youtube"
            lower.contains("whatsapp") -> "com.whatsapp"
            lower.contains("chrome") -> "com.android.chrome"
            lower.contains("maps") || lower.contains("map") -> "com.google.android.apps.maps"
            lower.contains("settings") || lower.contains("setting") -> "com.android.settings"
            lower.contains("camera") -> "camera"
            else -> null
        }

        if (shortcutPkg == "camera") {
            return openCamera()
        }

        if (shortcutPkg != null) {
            val launchIntent = pm.getLaunchIntentForPackage(shortcutPkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return Pair(true, "Opened $queryOrPackage successfully.")
            }
        }

        // Search through installed apps
        val apps = getInstalledApps()
        val matchedApp = apps.find { it.name.lowercase().contains(lower) || it.packageName.lowercase() == lower }
        if (matchedApp != null) {
            val launchIntent = pm.getLaunchIntentForPackage(matchedApp.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return Pair(true, "Opened ${matchedApp.name} successfully.")
            }
        }

        // Try direct intent resolution if it's a known schema
        if (lower.contains("setting")) {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return Pair(true, "Opened Android Settings.")
        }

        return Pair(false, "Application '$queryOrPackage' is not installed on this device.")
    }

    // 2. System Settings & Features
    fun openSystemSetting(settingType: String): Pair<Boolean, String> {
        val action = when (settingType.lowercase()) {
            "wifi", "wi-fi" -> Settings.ACTION_WIFI_SETTINGS
            "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
            "display", "brightness" -> Settings.ACTION_DISPLAY_SETTINGS
            "sound", "volume" -> Settings.ACTION_SOUND_SETTINGS
            "date", "time" -> Settings.ACTION_DATE_SETTINGS
            "accessibility" -> Settings.ACTION_ACCESSIBILITY_SETTINGS
            else -> Settings.ACTION_SETTINGS
        }
        return try {
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Pair(true, "Opened $settingType settings.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open setting $settingType", e)
            Pair(false, "Could not open $settingType settings: ${e.localizedMessage}")
        }
    }

    fun openCamera(): Pair<Boolean, String> {
        return try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Pair(true, "Camera opened.")
            } else {
                Pair(false, "No camera application found.")
            }
        } catch (e: Exception) {
            Pair(false, "Failed to launch Camera: ${e.localizedMessage}")
        }
    }

    // 3. Contacts & Calling
    fun findContacts(nameQuery: String): List<Pair<String, String>> {
        val results = mutableListOf<Pair<String, String>>()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return results
        }

        val contentResolver = context.contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$nameQuery%")

        try {
            contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIndex) ?: ""
                    val number = cursor.getString(numberIndex) ?: ""
                    if (name.isNotBlank() && number.isNotBlank()) {
                        results.add(Pair(name, number))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to query contacts", e)
        }
        return results
    }

    @SuppressLint("MissingPermission")
    fun initiateCall(phoneNumber: String): Pair<Boolean, String> {
        val cleanNumber = phoneNumber.replace(" ", "").replace("-", "")
        val hasCallPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

        return try {
            val intent = if (hasCallPerm) {
                Intent(Intent.ACTION_CALL, Uri.parse("tel:$cleanNumber")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanNumber")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(intent)
            if (hasCallPerm) {
                Pair(true, "Calling $phoneNumber...")
            } else {
                Pair(true, "Dialer opened for $phoneNumber. Grant CALL_PHONE for direct calling.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initiate call", e)
            Pair(false, "Call failed: ${e.localizedMessage}")
        }
    }

    @SuppressLint("MissingPermission")
    fun endCall(): Pair<Boolean, String> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val telecom = context.getSystemService(Context.TELECOM_SERVICE) as? TelecomManager
            if (telecom != null && ContextCompat.checkSelfPermission(context, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_GRANTED) {
                try {
                    val ended = telecom.endCall()
                    if (ended) return Pair(true, "Active call terminated.")
                } catch (e: Exception) {
                    Log.w(TAG, "TelecomManager endCall failed: ${e.localizedMessage}")
                }
            }
        }
        // Fallback: Accessibility back or notification collapse
        val backSuccess = JarvisAccessibilityService.performBack()
        return if (backSuccess) {
            Pair(true, "Call screen dismissed via Accessibility Service.")
        } else {
            Pair(false, "Ending active calls directly requires default dialer or specific OEM permissions.")
        }
    }

    // 4. SMS Messaging
    fun sendDirectSms(phoneNumber: String, message: String): Pair<Boolean, String> {
        val hasSmsPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED

        return if (hasSmsPerm) {
            try {
                val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
                Pair(true, "Message sent to $phoneNumber.")
            } catch (e: Exception) {
                Log.e(TAG, "SMS Manager failed", e)
                Pair(false, "Failed to send SMS: ${e.localizedMessage}")
            }
        } else {
            // Safe fallback: Prepare SMS via Intent
            try {
                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phoneNumber")).apply {
                    putExtra("sms_body", message)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                Pair(true, "SMS drafted in default messaging app. Grant SEND_SMS for instant direct dispatch.")
            } catch (e: Exception) {
                Pair(false, "Could not draft SMS: ${e.localizedMessage}")
            }
        }
    }

    // 5. Alarms & Timers
    fun setAlarm(hour: Int, minute: Int, message: String = "Muslim Jarvis Alarm"): Pair<Boolean, String> {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                Pair(true, "Alarm set for %02d:%02d ($message).".format(hour, minute))
            } else {
                Pair(false, "Clock application not found on device.")
            }
        } catch (e: Exception) {
            Pair(false, "Alarm creation failed: ${e.localizedMessage}")
        }
    }

    fun setTimer(seconds: Int, message: String = "Muslim Jarvis Timer"): Pair<Boolean, String> {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, seconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, message)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
                val mins = seconds / 60
                val secs = seconds % 60
                Pair(true, "Timer started for ${if (mins > 0) "$mins min " else ""}${if (secs > 0) "$secs sec" else ""}.")
            } else {
                Pair(false, "Timer application not found on device.")
            }
        } catch (e: Exception) {
            Pair(false, "Timer creation failed: ${e.localizedMessage}")
        }
    }

    // 6. Accessibility Automation
    fun executeAccessibilityAction(actionType: String): Pair<Boolean, String> {
        if (!JarvisAccessibilityService.isConnected()) {
            return Pair(false, "Accessibility Service is not enabled. Please enable 'Muslim Jarvis VIP' in Android Accessibility settings.")
        }
        val success = when (actionType.lowercase()) {
            "back" -> JarvisAccessibilityService.performBack()
            "home" -> JarvisAccessibilityService.performHome()
            "notifications" -> JarvisAccessibilityService.performNotifications()
            "quick_settings" -> JarvisAccessibilityService.performQuickSettings()
            "scroll_down" -> JarvisAccessibilityService.performScrollDown()
            "scroll_up" -> JarvisAccessibilityService.performScrollUp()
            else -> false
        }
        return if (success) Pair(true, "Action '$actionType' executed.")
        else Pair(false, "Action '$actionType' could not be performed on the current screen.")
    }

    fun typeTextIntoFocused(text: String): Pair<Boolean, String> {
        if (!JarvisAccessibilityService.isConnected()) {
            return Pair(false, "Accessibility Service is not enabled. Please enable 'Muslim Jarvis VIP' in Android Accessibility settings.")
        }
        val success = JarvisAccessibilityService.typeTextIntoFocused(text)
        return if (success) Pair(true, "Typed '$text' into active input.")
        else Pair(false, "No active editable input field found on screen.")
    }

    companion object {
        private const val TAG = "PhoneActionExecutor"
    }
}
