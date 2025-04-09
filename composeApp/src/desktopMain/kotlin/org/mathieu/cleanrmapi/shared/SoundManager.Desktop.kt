package org.mathieu.cleanrmapi.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.net.HttpURLConnection
import java.net.URI
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip

@Composable
actual fun rememberSoundManager(): SoundManager {
    return remember { SoundManager() }
}

actual class SoundManager actual constructor() {

    private var clip: Clip? = null

    actual fun playSound(url: String) {
        try {
            val uri = URI(url)
            val connection = uri.toURL().openConnection() as HttpURLConnection
            connection.connect()
            val inputStream = connection.inputStream

            val audioInputStream = AudioSystem.getAudioInputStream(inputStream)
            clip?.close()
            clip = AudioSystem.getClip().apply {
                open(audioInputStream)
                start()
            }
        } catch (e: Exception) {
            println("Erreur lecture audio: ${e.message}")
        }
    }
}