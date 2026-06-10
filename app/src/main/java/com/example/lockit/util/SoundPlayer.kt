package com.example.lockit.util

import android.content.Context
import android.media.MediaPlayer

object SoundPlayer {
    fun playRaw(context: Context, name: String) {
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        if (resId == 0) return
        try {
            MediaPlayer.create(context, resId)?.apply {
                setOnCompletionListener { it.release() }
                start()
            }
        } catch (e: Exception) {
        }
    }
}
