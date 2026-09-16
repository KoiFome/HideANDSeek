package com.UD.hideandseek.ECS.systems

import android.content.Context
import com.UD.hideandseek.ECS.ECS
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import com.UD.hideandseek.ECS.Components



class SensorSystem (context: Context) : ECS.System, SensorEventListener{
    private val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private var hasRotationVector = false
    private val accelValues = FloatArray(3)
    private val magValues = FloatArray(3)

    @Volatile private var lastAzimuth = 0f
    @Volatile private var lastPitch = 0f
    @Volatile private var lastRoll = 0f

    fun register() {

        val rotationVector = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        if (rotationVector != null) {
            hasRotationVector = true
            sm.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_GAME)
        } else {
            // PLAN B: Si no hay Rotation Vector, registramos obligatoriamente los dos individuales
            hasRotationVector = false
            sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
            sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let {
                sm.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
            }
        }
    }

    fun unregister() = sm.unregisterListener(this)

    override fun onSensorChanged(event: SensorEvent) {
        if (hasRotationVector && event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {

            // PLAN A: Procesamiento limpio y rápido con el vector de rotación
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            updateOrientationAngles()

        } else if (!hasRotationVector) {
            // PLAN B: Copiar los datos crudos de los dos sensores y fusionarlos manualmente
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, accelValues, 0, 3)
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, magValues, 0, 3)
            }

            // Solo calculamos si ambos sensores ya tienen datos
            if (SensorManager.getRotationMatrix(rotationMatrix, null, accelValues, magValues)) {
                updateOrientationAngles()
            }
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

    private fun updateOrientationAngles() {
        SensorManager.getOrientation(rotationMatrix, orientation)
        lastAzimuth = (Math.toDegrees(orientation[0].toDouble()).toFloat() + 360f) % 360f
        lastPitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        lastRoll = Math.toDegrees(orientation[2].toDouble()).toFloat()
    }


}