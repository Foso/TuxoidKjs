package data.sound

interface SoundDataSource{
    fun play_sound(id: Int)
    fun toggle_sound()
    fun isSoundOn() : Boolean
    fun remove_soundrestriction()
    fun addVolumeChangeListener(volumeChangeListener: VolumeChangeListener)
    fun set_volume(vol: Double)
}