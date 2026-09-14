package com.UD.hideandseek.ECS.systems

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.UD.hideandseek.ECS.Components



class SensorSystem (context: Context) : ECS.System, SensorEventListener{
    private val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)

    @Volatile private var lastAzimuth = 0f
    @Volatile private var lastPitch = 0f
    @Volatile private var lastRoll = 0f

    fun register() {
        // El PDF pide acelerómetro + magnetómetro; rotation_vector los fusiona
        // internamente junto al giroscopio. Registramos los tres para cumplir.
        sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
        sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let {
            sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun unregister() = sm.unregisterListener(this)

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientation)
            lastAzimuth = ((Math.toDegrees(orientation[0].toDouble()).toFloat()) + 360f) % 360f
            lastPitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
            lastRoll  = Math.toDegrees(orientation[2].toDouble()).toFloat()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun update(entities: List<ECS.Entity>, dt: Float) {
        entities.forEach { e ->
            e.get<Components.OrientationComponent>()?.apply {
                azimuth = lastAzimuth
                pitch = lastPitch
                roll = lastRoll
            }
        }
    }
}