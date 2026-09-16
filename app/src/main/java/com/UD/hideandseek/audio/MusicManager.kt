package com.UD.hideandseek.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.annotation.RawRes

object MusicManager {
    private var player: MediaPlayer? = null

    fun play(context : Context, @RawRes resId: Int) {
        if (player?.isPlaying == true) return
        stop()
        player = MediaPlayer.create(context.applicationContext, resId)?.apply {
            isLooping = true
            start()
        }
    }

    fun stop() {
        player?.run {
            if (isPlaying) stop()
            release()
        }
        player = null
    }
}