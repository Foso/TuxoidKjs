package data.sound

interface SoundDataSource{
    fun play_sound(id: Int)
    fun toggle_sound()
    fun isSoundOn() : Boolean
}