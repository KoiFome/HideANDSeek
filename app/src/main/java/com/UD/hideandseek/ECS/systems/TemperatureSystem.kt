package com.UD.hideandseek.ECS.systems

import com.UD.hideandseek.ECS.Components
import com.UD.hideandseek.ECS.ECS
import com.UD.hideandseek.ECS.GameStatus

class TemperatureSystem : ECS.System {

    // Usamos explícitamente ECS.Entity para cumplir con el contrato de la interfaz
    override fun update(entities: List<ECS.Entity>, dt: Float) {
        // 1. Buscamos la entidad usando ECS.Entity
        val e = entities.firstOrNull { it.has<Components.TemperatureComponent>() } ?: return

        val or = e.get<Components.OrientationComponent>() ?: return
        val tg = e.get<Components.TargetComponent> () ?: return
        val tp = e.get<Components.TemperatureComponent>() !!
        val gs = e.get<Components.GameStateComponent>()!!

        if (gs.status != GameStatus.PLAYING) return

        val diff = angularDistance(or.azimuth, tg.azimuth)
        tp.angularDistance = diff
        tp.state = when {
            diff <= 15f -> TempState.FRIO
            diff <= 45f -> TempState.TIBIO
            else        -> TempState.FRIO
        }

        if (diff <= tg.toleranceDeg) {
            gs.status = GameStatus.WON
            gs.precisionAtFound = diff
        }
    }

    private fun angularDistance(a: Float, b: Float): Float {
        val d = kotlin.math.abs(a - b) % 360f
        return if (d > 180f) 360f - d else d
    }
}
