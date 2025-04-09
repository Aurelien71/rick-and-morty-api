package org.mathieu.cleanrmapi.shared

import androidx.compose.runtime.Composable

/**
 * Composable function that returns a [SoundManager] instance.
 *
 * @return A [SoundManager] instance.
 */
@Composable
expect fun rememberSoundManager(): SoundManager

/**
 * Manager used to manage sounds (for mutliplatform management).
 *
 * @return A [SoundManager] instance.
 */
expect class SoundManager() {
    /**
     * Plays a sound from a given URL.
     *
     * @param url The URL of the sound to play.
     */
    fun playSound(url: String)
}