package com.UD.hideandseek.ECS


enum class TempState { FRIO, TIBIO, CALIENTE }
enum class GameStatus { PLAYING, WON, LOST }


class Components {

    data class OrientationComponent(
        var azimuth: Float = 0f,   // 0..360, 0 = Norte
        var pitch: Float = 0f,
        var roll: Float = 0f
    ) : Component

    data class TargetComponent(
        val azimuth: Float,
        val toleranceDeg: Float = 10f
    ) : Component

    data class TimerComponent(
        var total: Float,
        var remaining: Float
    ) : Component

    data class TemperatureComponent(
        var state: TempState = TempState.FRIO,
        var angularDistance: Float = 180f
    ) : Component

    data class GameStateComponent(
        var status: GameStatus = GameStatus.PLAYING,
        var elapsed: Float = 0f,
        var precisionAtFound: Float = 0f,
        var score: Int = 0
    ) : Component

}