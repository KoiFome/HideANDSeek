package com.UD.hideandseek

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.UD.hideandseek.databinding.ActivityDashboardBinding

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtons()
        loadStats()
    }

    private fun setupButtons(){

        binding.btnNewGame.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }
        binding.btnRanking.setOnClickListener {
            showComingSoon(getString(R.string.dashboard_ranking))
        }
        binding.btnSettings.setOnClickListener {
            showComingSoon(getString(R.string.dashboard_settings))
        }

        binding.btnHowToPlay.setOnClickListener {
            showHowToPlay()
        }
    }

    private fun loadStats(){
        //We still have no Rooms or JSON to parse into our views so...yeah
        binding.txtBestTime.text = getString(R.string.dashboard_no_record)
        binding.txtBestScore.text = getString(R.string.dashboard_no_record)
    }

    private fun showHowToPlay(){
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(R.string.dashboard_how_to_play)
            .setMessage(R.string.dashboard_how_to_play_body)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showComingSoon(feature: String) {
        android.widget.Toast.makeText(
            this,
            getString(R.string.dashboard_coming_soon, feature),
            android.widget.Toast.LENGTH_SHORT
        ).show()
    }

}