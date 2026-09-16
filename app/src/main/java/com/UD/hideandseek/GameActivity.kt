package com.UD.hideandseek


import TimerSystem
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.UD.hideandseek.ECS.Components
import com.UD.hideandseek.ECS.ECS
import com.UD.hideandseek.ECS.GameStatus
import com.UD.hideandseek.ECS.TempState
import com.UD.hideandseek.ECS.systems.*
import com.UD.hideandseek.audio.MusicManager
import com.UD.hideandseek.databinding.ActivityGameBinding
import com.UD.hideandseek.score.ScoreCalculator
import kotlin.random.Random

class GameActivity : AppCompatActivity() {

    private  lateinit var biding : ActivityGameBinding
    private lateinit var world: ECS.World
    private lateinit var sensorSystem: SensorSystem
    private var lastFrame = 0L
    private var gameEnded = false

    private var gameStartTime = 0L

    private val totalTime = 60f
    private val toleranceDeg = 10f


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        biding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(biding.root)

        setUpBottons()
        buildWorld()


    }

    private fun buildWorld(){
        val targetAz = Random.nextFloat() * 360f

        world = ECS.World().apply {
            addSystem(TimerSystem())
            addSystem(TemperatureSystem())
            sensorSystem = SensorSystem(this@GameActivity).also { addSystem(it) }
            addEntity(ECS.Entity().apply {

                add(Components.OrientationComponent(),
                    Components.OrientationComponent::class.java)
                add(Components.TargetComponent(targetAz, toleranceDeg),
                    Components.TargetComponent::class.java)
                add(Components.TimerComponent(totalTime, totalTime),
                    Components.TimerComponent::class.java)
                add(Components.TemperatureComponent(),
                    Components.TemperatureComponent::class.java)
                add(Components.GameStateComponent(),
                    Components.GameStateComponent::class.java)

            })

        }
        sensorSystem.register()
        gameEnded = false

    }

    private fun setUpBottons(){

        biding.btnRestart.setOnClickListener { restartGame() }

        biding.btnBack.setOnClickListener {
            finish()
        }

        biding.btnHint.setOnClickListener {
            biding.txtInstruction.text = getString(R.string.hint_shown)
        }
    }

    private fun restartGame(){
        sensorSystem.unregister()
        buildWorld()
        lastFrame = System.nanoTime()
        biding.txtInstruction.text = getString(R.string.instruction_rotate)
    }


    override fun onResume() {
        super.onResume()
        lastFrame = System.nanoTime()
        MusicManager.play(this, R.raw.bg_music)
        startLoop()
    }

    override fun onPause() {
        super.onPause()
        MusicManager.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorSystem.unregister()
    }

    //Function to start running the game
    private fun startLoop(){

        biding.root.postOnAnimation (object : Runnable{
            override fun run() {
                if(gameEnded) return

                val now = System.nanoTime()
                val dt = ((now - lastFrame) / 1_000_000_000f)
                    .coerceAtMost(0.05f)
                lastFrame = now
                world.update(dt)
                renderWorld()

                if(!gameEnded) biding.root.postOnAnimation(this)

            }
        })
    }

    private fun renderWorld(){
        val e = world.entities.first()
        val orientation = e.get<Components.TargetComponent>()!!
        val target = e.get<Components.TargetComponent>()!!
        val temp = e.get<Components.TemperatureComponent>()!!
        val timer = e.get<Components.TimerComponent>()!!
        val gs = e.get<Components.GameStateComponent>()!!

        //- Compass
        biding.compass.azimuth = orientation.azimuth
        biding.compass.targetAzimuth = target.azimuth
        biding.compass.temperature = temp.state


        // -Timer
        val seconds = timer.remaining.toInt().coerceAtLeast(gameStartTime.toInt())
        biding.txtTimer.text = "%02d:%02d".format(seconds / 60, seconds % 60)

        //-Termic State
        biding.txtTemp.text = when (temp.state) {
            TempState.FRIO     -> getString(R.string.temp_cold)
            TempState.TIBIO    -> getString(R.string.temp_mild)
            TempState.CALIENTE -> getString(R.string.temp_hot)
        }

        //- Visibility

        biding.txtProximity.visibility =
            if (temp.state == TempState.CALIENTE) android.view.View.VISIBLE
            else android.view.View.GONE

        // - End of the Game
        if(gs.status != GameStatus.PLAYING && !gameEnded) {
            gameEnded = true
            endGame(gs,timer)
        }

    }

    private fun endGame(gs: Components.GameStateComponent , t : Components.TimerComponent){
        gs.score = ScoreCalculator.compute(gs.elapsed, t.total, gs.precisionAtFound)
        startActivity(Intent(this, ResultActivity::class.java).apply {
            putExtra(ResultActivity.EXTRA_STATUS, gs.status.name)
            putExtra(ResultActivity.EXTRA_SCORE, gs.score)
            putExtra(ResultActivity.EXTRA_ELAPSED, gs.elapsed)
            putExtra(ResultActivity.EXTRA_PRECISION, gs.precisionAtFound)
            putExtra(ResultActivity.EXTRA_TOTAL, t.total)
        })
        finish()
    }

}