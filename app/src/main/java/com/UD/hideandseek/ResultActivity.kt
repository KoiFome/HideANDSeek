package com.UD.hideandseek

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.UD.hideandseek.ECS.GameStatus
import com.UD.hideandseek.databinding.ActivityResultBinding
import kotlin.jvm.java

class ResultActivity : AppCompatActivity() {

    private lateinit var binding : ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val status = intent.getStringExtra(EXTRA_STATUS) ?: GameStatus.LOST.name
        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val elapsed = intent.getFloatExtra(EXTRA_ELAPSED, 0f)
        val precision = intent.getFloatExtra(EXTRA_PRECISION, 0f)
        val total = intent.getFloatExtra(EXTRA_TOTAL, 60f)

        render(status, score, elapsed, precision, total)
        setupButtons()
    }

    private fun render(
        status: String,
        score: Int,
        elapsed: Float,
        precision: Float,
        total: Float
    ){

        val precisionCalculus : Int = (100f - precision / 90f * 100f).toInt()
        val won = status == GameStatus.WON.name

        binding.txtResultTitle.text = getString(
            if(won) R.string.result_found
            else R.string.result_not_found
        )

        binding.imgResult.setImageResource(
            if(won) R.drawable.ic_victory else R.drawable.ic_loser
        )

        binding.txtTotalTime.text = formatTime(elapsed)
        binding.txtScore.text = getString(R.string.result_score_value, score)

        binding.txtPrecision.text = if(won){
            getString(R.string.result_precision_value, precisionCalculus)
        }else{
            getString(R.string.result_precision_none)
        }

        binding.txtBestTime.text = formatTime(loadBestTime())
    }

    private fun setupButtons(){
        binding.btnPlayAgain.setOnClickListener {
            // Reinicia el juego: termina esta Activity y lanza GameActivity
            startActivity(Intent(this, GameActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
            finish()
        }

        binding.btnMenu.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            })
        }

        binding.btnShare.setOnClickListener {
            shareScore()
        }
    }


    private fun shareScore() {
        val score = intent.getIntExtra(EXTRA_SCORE, 0)
        val elapsed = intent.getFloatExtra(EXTRA_ELAPSED, 0f)
        val text = getString(R.string.share_text, score, formatTime(elapsed))

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(sendIntent, getString(R.string.share_chooser)))
    }
    private fun formatTime(seconds: Float): String {
        val s = seconds.toInt().coerceAtLeast(0)
        return "%02d:%02d".format(s / 60, s % 60)
    }

    private fun loadBestTime(): Float {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getFloat(KEY_BEST_TIME, 0f)
    }

    companion object {
        const val EXTRA_STATUS    = "STATUS"
        const val EXTRA_SCORE     = "SCORE"
        const val EXTRA_ELAPSED   = "ELAPSED"
        const val EXTRA_PRECISION = "PRECISION"
        const val EXTRA_TOTAL     = "TOTAL"

        const val PREFS_NAME    = "hideandseek_prefs"
        const val KEY_BEST_TIME = "best_time"
    }
}