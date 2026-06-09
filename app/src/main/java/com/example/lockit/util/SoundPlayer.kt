package com.example.lockit.util

import android.content.Context
import android.media.MediaPlayer

/**
 * Plays short one-shot sound effects from res/raw.
 *
 * The resource is resolved by name at runtime, so the app compiles and runs even when the
 * sound file is not present yet — drop e.g. `unlock.mp3` into app/src/main/res/raw and it
 * will start playing automatically. Missing file = silent no-op.
 */
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
            // Sound is non-critical — never let a playback issue break login.
        }
    }
}
