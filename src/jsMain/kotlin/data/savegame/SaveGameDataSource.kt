package data.savegame

import de.jensklingenberg.bananiakt.model.SaveGame

interface SaveGameDataSource {

    fun update_savegame(lev: Int, steps: Int)
    fun getSaveGame(): SaveGame

    fun clear_savegame()
    fun store_savegame(): Int
    fun name_savegame(uname: String, pass: String): Int
    fun retrieve_savegame(uname: String, pass: String): Int
}