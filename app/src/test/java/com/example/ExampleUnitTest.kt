package com.example

import com.example.core.JarvisAiEngine
import com.example.data.IslamicRepository
import com.example.model.IntentType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPrayerTimesCalculation() {
        val prayers = IslamicRepository.calculatePrayerTimes(21.4225, 39.8262)
        assertEquals(6, prayers.size)
        assertTrue(prayers.any { it.nameEnglish == "Fajr" })
        assertTrue(prayers.any { it.nameEnglish == "Maghrib" })
        assertTrue(prayers.any { it.nameEnglish == "Isha" })
    }

    @Test
    fun testQiblaBearing() {
        // From Karachi (approx 24.86 N, 67.00 E), Qibla bearing should be west-southwest (~260-275 degrees)
        val bearing = IslamicRepository.calculateQiblaDirection(24.8607, 67.0011)
        assertTrue(bearing in 255.0f..280.0f)
    }

    @Test
    fun testAiEngineLocalUrduAndRomanParsing() = runBlocking {
        val engine = JarvisAiEngine()

        val intent1 = engine.analyzeCommand("YouTube kholo")
        assertEquals(IntentType.OPEN_APP, intent1.type)

        val intent2 = engine.analyzeCommand("Mama ko call karo")
        assertEquals(IntentType.CALL_PHONE, intent2.type)
        assertEquals("Mama", intent2.contactName)

        val intent3 = engine.analyzeCommand("Safar ki dua sunao")
        assertEquals(IntentType.ISLAMIC_DUA, intent3.type)

        val intent4 = engine.analyzeCommand("5 minute ka timer lagao")
        assertEquals(IntentType.SET_TIMER, intent4.type)
        assertEquals(300, intent4.timerSeconds)

        val intent5 = engine.analyzeCommand("Emergency stop")
        assertEquals(IntentType.EMERGENCY_STOP, intent5.type)
    }
}
