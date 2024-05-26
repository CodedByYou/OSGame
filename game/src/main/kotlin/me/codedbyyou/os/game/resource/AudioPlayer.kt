package me.codedbyyou.os.game.resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.FloatControl
import kotlin.math.log10

object AudioPlayer {
    private var bgmClip: Clip? = null
    private var buttonSound: Clip? = null
    private var ringSound: Clip? = null
    private var winSound: Clip? = null
    private var loseSound: Clip? = null
    private var introSound: Clip? = null

    init {
        try {
            buttonSound = AudioSystem.getClip()
            buttonSound?.open(AudioSystem.getAudioInputStream(
                object {}.javaClass.getResource
                ("/assets/sfx/minecraft_click.wav")
            ))
            ringSound = AudioSystem.getClip()
            ringSound?.open(AudioSystem.getAudioInputStream(
                object {}.javaClass.getResource
                ("/assets/sfx/campana.wav")
            ))
            winSound = AudioSystem.getClip()
            winSound?.open(AudioSystem.getAudioInputStream(
                object {}.javaClass.getResource("/assets/sfx/correct-answer.wav")
            ))
            loseSound = AudioSystem.getClip()
            loseSound?.open(AudioSystem.getAudioInputStream(
                object {}.javaClass.getResource
                ("/assets/sfx/fail.wav")
            ))
            introSound = AudioSystem.getClip()
            introSound?.open(AudioSystem.getAudioInputStream(
                object {}.javaClass.getResource
                ("/assets/sfx/intro.wav")
            ))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playBackgroundMusic() {
        try {
            if (bgmClip != null) {
                resumeBackgroundMusic()
                return
            }
            val audioInputStream = AudioSystem.getAudioInputStream(
                object {}.javaClass.getResource("/assets/sfx/c418_mc_music.wav")
            )
            val clip = AudioSystem.getClip()
            clip.open(audioInputStream)
            clip.loop(Clip.LOOP_CONTINUOUSLY)
            val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
            gainControl.value =  20f * (log10(Config.musicMultiplier.toDouble())).toFloat()
            bgmClip = clip
            resumeBackgroundMusic()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun playOneShotSound(name: String) {
        val clip = when (name) {
            "click" -> buttonSound
            "ring" -> ringSound
            "win" -> winSound
            "lose" -> loseSound
            "intro" -> introSound
            else -> return
        }

        val gainControl = clip!!.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
        gainControl.value = Config.sfxMultiplier
        if (clip.isRunning) {
            clip.stop()
        }
        clip.framePosition = if (name == "click") 25 else 0
        clip.start()
    }


    private fun resumeBackgroundMusic() {
        bgmClip?.start()
    }

    fun stopBackgroundMusic() {
        bgmClip?.stop()
    }

    fun setVolume(volume: Float) {
        bgmClip?.let { clip ->
            val gainControl = clip.getControl(FloatControl.Type.MASTER_GAIN) as FloatControl
            gainControl.value = 20f * (log10(volume.toDouble())).toFloat()
        }
    }
}
