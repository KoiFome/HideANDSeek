import com.UD.hideandseek.ECS.ECS
import com.UD.hideandseek.ECS.Components
import com.UD.hideandseek.ECS.GameStatus

class TimerSystem : ECS.System {
    override fun update(entities: List<ECS.Entity>, dt: Float) {
        entities.forEach { e ->

            //return@foreach funciona como un continue en un ciclo clasico
            val t = e.get<Components.TimerComponent>() ?: return@forEach
            val gs = e.get<Components.GameStateComponent>()?: return@forEach
            if (gs.status != GameStatus.PLAYING) return@forEach
            t.remaining = (t.remaining - dt).coerceAtLeast(0f)
            gs.elapsed += dt
            if (t.remaining <= 0f) gs.status = GameStatus.LOST
        }
    }
}