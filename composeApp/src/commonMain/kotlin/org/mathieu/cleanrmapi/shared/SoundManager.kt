package org.mathieu.cleanrmapi.shared

import androidx.compose.runtime.Composable

@Composable
expect fun rememberSoundManager(): SoundManager

/**
 * Manager used to manage sounds (for mutliplatform).
 * @param url The URL of the audio file to play.
 * @return A [SoundManager] instance.
 */
expect class SoundManager() {
    fun playSound(url: String)
}