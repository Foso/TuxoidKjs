package data.savegame

import App
import de.jensklingenberg.bananiakt.SaveGame
import kotlinx.browser.localStorage
import md5

class SaveGameDataRepository: SaveGameDataSource {
    var savegame = SaveGame();
    override fun update_savegame(lev: Int, steps: Int) {
        if (savegame.reached_level <= lev) {
            savegame.reached_level = lev + 1;
            savegame.progressed = true;
        }
        if (savegame.arr_steps[lev] == 0 || savegame.arr_steps[lev] > steps) {
            savegame.arr_steps[lev] = steps;
            savegame.progressed = true;
        }
    }

    override fun getSaveGame(): SaveGame {
        return savegame
    }

    override fun clear_savegame() {
        savegame = SaveGame();

    }



   override fun store_savegame(): Int {
        if (localStorage.getItem("user_count") == null) {
            localStorage.setItem("user_count", "1");
            savegame.usernumber = 0
        } else if (savegame.usernumber == -1) {
            savegame.usernumber = localStorage.getItem("user_count")?.toInt() ?: -1;
            localStorage.setItem("user_count", (savegame.usernumber + 1).toString())
        }

        var prefix = "player" + savegame.usernumber + "_";
        savegame.username?.let { localStorage.setItem(prefix + "username", it) };
        savegame.password?.let { localStorage.setItem(prefix + "password", it) };
        localStorage.setItem(prefix + "reached_level", savegame.reached_level.toString());

        for (i in 1 until 50) {
            localStorage.setItem(prefix + "steps_lv" + i, savegame.arr_steps[i].toString());
        }

        savegame.progressed = false
        return App.ERR_SUCCESS

    }

    override fun name_savegame(uname: String, pass: String): Int {
        var user_count = localStorage.getItem("user_count");
        if (user_count != null) {
            val userCountNum = user_count.toInt()
            for (i in 0 until userCountNum) {
                var prefix = "player" + i + "_";
                if (localStorage.getItem(prefix + "username") == uname) {
                    return App.ERR_EXISTS;// Failed already exists
                }
            }
        }
        savegame.username = uname;
        savegame.password = md5.digest(pass)
        return App.ERR_SUCCESS;// Worked
    }

    override  fun retrieve_savegame(uname: String, pass: String): Int {
        var user_count = localStorage.getItem("user_count")?.toIntOrNull() ?: 0;
        if (user_count == 0) {
            return App.ERR_NOSAVE;// There are no save games
        }

        val md5pass = md5.digest(pass);

        for (i in 0 until user_count) {
            var prefix = "player" + i + "_";
            if (localStorage.getItem(prefix + "username") == uname) {
                if (localStorage.getItem(prefix + "password") == md5pass) {
                    savegame = SaveGame();
                    savegame.usernumber = i;0
                    savegame.username = uname;
                    savegame.password = md5pass;
                    savegame.reached_level = localStorage.getItem(prefix + "reached_level")!!.toInt();

                    for (i in 1 until 50) {
                        savegame.arr_steps[i] = localStorage.getItem(prefix + "steps_lv" + i)!!.toInt();
                    }
                    savegame.progressed = false;



                    return App.ERR_SUCCESS;// Success!
                } else {
                    return App.ERR_WRONGPW;// Wrong password!
                }
            }
        }
        return App.ERR_NOTFOUND;// There's no such name
    }
}