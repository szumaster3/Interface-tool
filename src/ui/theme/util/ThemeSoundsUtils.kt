package ui.theme.util

import java.io.File
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.FloatControl

object ThemeSoundsUtils{
    fun playSFX(path: String, volume: Float = 0.5f) {
        val file = File(path)
        if (!file.exists()) return

        AudioSystem.getAudioInputStream(file).use { stream ->
            val clip = AudioSystem.getClip()
            clip.open(stream)

            val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl

            val min = gainControl.minimum
            val max = gainControl.maximum
            val gain = min + (max - min) * volume
            gainControl.value = gain

            clip.start()
        }
    }
}