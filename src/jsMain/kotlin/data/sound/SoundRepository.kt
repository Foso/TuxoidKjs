package data.sound

import DEBUG
import MyRes

interface VolumeChangeListener {
    fun onSoundChanged(vol: Double)
}

class SoundRepository(val res: MyRes) : SoundDataSource {

    var sound = !DEBUG;
    var soundrestriction_removed = false;
    var volumeChangeListener: VolumeChangeListener? = null
    override fun play_sound(id: Int) {
        if (!sound) return;
        if (res.sounds[id].currentTime != 0) res.sounds[id].currentTime = 0;
        res.sounds[id].play();
    }

    override fun toggle_sound() {
        if (sound) {
            sound = false
            for (i in res.sounds.indices) {
                res.sounds[i].pause();
                res.sounds[i].currentTime = 0
            }
        } else {
            sound = true;
        }
    }

    override fun isSoundOn(): Boolean {
        return sound
    }

    // This is necessary because of mobile browsers. These browsers block sound playback
// unless it is triggered by a user input event. Play all sounds at the first input,
// then the restriction is lifted for further playbacks.
    override fun remove_soundrestriction() {
        if (soundrestriction_removed) return;
        for (i in res.sounds.indices) {
            if (res.sounds[i].paused) {
                res.sounds[i].play();
                res.sounds[i].pause();
                res.sounds[i].currentTime = 0
            }
        }
        soundrestriction_removed = true;
    }

    override fun addVolumeChangeListener(volumeChangeListener: VolumeChangeListener) {
        this.volumeChangeListener = volumeChangeListener
    }

    override fun set_volume(vol: Double) {

        var newVol = vol
        if (vol > 1) {
            newVol = 1.0;
        } else if (vol < 0) {
            newVol = 0.0;
        }
        volumeChangeListener?.onSoundChanged(newVol)

    }
}