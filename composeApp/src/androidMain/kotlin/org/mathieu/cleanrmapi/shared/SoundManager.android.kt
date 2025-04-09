package org.mathieu.cleanrmapi.shared

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberSoundManager(): SoundManager {
    val context = LocalContext.current
    return remember {
        SoundManager().apply {
            init(context)
        }
    }
}


actual class SoundManager actual constructor() {

    private var mediaPlayer: MediaPlayer? = null

    private var context: Context? = null

    fun init(context: Context) {
        this.context = context
    }

    actual fun playSound(url: String) {
        val ctx = context ?: return
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            setOnPreparedListener { start() }
            prepareAsync()
        }
    }
}