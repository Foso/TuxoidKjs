package de.jensklingenberg.bananiakt.model
class SaveGame {
    var usernumber: Int = -1
    var username: String? = null
    var password: String? = null
    var reached_level: Int = 1
    var progressed = false
    var arr_steps = arrayOf<Int>()

    init {
        for (i in 0 until 50) {
            arr_steps[i] = 0
        }
    }
}