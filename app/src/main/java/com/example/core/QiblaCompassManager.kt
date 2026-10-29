package com.example.core

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs

class QiblaCompassManager(private val context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _azimuth = MutableStateFlow(0f)
    val azimuth: StateFlow<Float> = _azimuth.asStateFlow()

    private val _isAligned = MutableStateFlow(false)
    val isAligned: StateFlow<Boolean> = _isAligned.asStateFlow()

    private var targetQiblaBearing = 265f // Calculated dynamically from user coordinates

    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var hasAccelerometer = false
    private var hasMagnetometer = false

    fun start(qiblaBearing: Float) {
        targetQiblaBearing = qiblaBearing
        if (rotationSensor != null) {
            sensorManager?.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager?.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager?.registerListener(this, magnetSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)
            updateAzimuth(Math.toDegrees(orientation[0].toDouble()).toFloat())
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            hasAccelerometer = true
            tryComputeOrientation()
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            hasMagnetometer = true
            tryComputeOrientation()
        }
    }

    private fun tryComputeOrientation() {
        if (hasAccelerometer && hasMagnetometer) {
            if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                SensorManager.getOrientation(rotationMatrix, orientation)
                updateAzimuth(Math.toDegrees(orientation[0].toDouble()).toFloat())
            }
        }
    }

    private fun updateAzimuth(degrees: Float) {
        var azimuthDeg = (degrees + 360f) % 360f
        _azimuth.value = azimuthDeg

        // Check alignment with Kaaba bearing (within +- 3 degrees)
        val diff = abs(azimuthDeg - targetQiblaBearing)
        _isAligned.value = diff <= 3f || diff >= 357f
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
