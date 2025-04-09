package org.mathieu.cleanrmapi.shared

import androidx.compose.runtime.Composable

@Composable
expect fun rememberSoundManager(): SoundManager

expect class SoundManager() {
    fun playSound(url: String)
}