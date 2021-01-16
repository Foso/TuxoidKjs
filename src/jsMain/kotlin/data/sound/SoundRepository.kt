package data.sound

import DEBUG
import MyRes

class SoundRepository(val res: MyRes) : SoundDataSource {

    var sound = !DEBUG;

    override fun play_sound(id: Int) {
        if (!sound) return;
        if (res.sounds[id].currentTime != 0) res.sounds[id].currentTime = 0;
        res.sounds[id].play();
    }

   override fun toggle_sound() {
        if (sound) {
            sound = false;
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
}