import App.Companion.DIR_DOWN
import App.Companion.DIR_LEFT
import App.Companion.DIR_NONE
import App.Companion.DIR_RIGHT
import App.Companion.DIR_UP
import App.Companion.ERR_EMPTYNAME
import App.Companion.ERR_EXISTS
import App.Companion.ERR_NOSAVE
import App.Companion.ERR_NOTFOUND
import App.Companion.ERR_SUCCESS
import App.Companion.ERR_WRONGPW
import App.Companion.LEV_START_DELAY
import App.Companion.UPS
import kotlinx.browser.localStorage
import kotlin.js.Date
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.random.Random

var DEBUG = true;
val GAME_MODE_ENTRY =0
val GAME_MODE_MENU =1
val GAME_MODE_PLAY =2


@JsExport
class KtGame(val volumeBar: VolumeBar, val res: MyRes) {
    var INTRO_DURATION = 2;// In seconds

    var savegame = SaveGame();

    var move_speed = round((1 * 60 / UPS).toDouble());
    var door_removal_delay = round((8 * UPS / 60).toDouble());

    var fpsInterval = 1000 / UPS;
    var then = Date.now();
    var now: Double = 0.0;

    var initialized = false;
    var wait_timer = INTRO_DURATION * UPS;
    var paused = false;
    var update_drawn = false;

    var mode = 0;// 0 is entry, 1 is menu and play
    var level_number = 0;
    var level_array = arrayOf(arrayOf<KtEntity>());

    var level_unlocked = 0;
    var level_ended = 0;// 0 is not ended. 1 is won. 2 is died.
    var wow = true;// true is WOW!, false is Yeah!

    var berti_positions = arrayOf(Tile(0, 0));

    var single_steps = true;
    var walk_dir = DIR_NONE;

    var steps_taken = 0;
    var num_bananas = 0;

    var last_updated = Date.now();
    var delta_updated = Date.now();

    var buttons_activated = Array<Boolean>(5) { false };
    // var buttons_activated[0] = buttons_activated[2] == false;
    // var buttons_activated[1] = true;

    var sound = !DEBUG;
    var soundrestriction_removed = false;

    var update_tick = 0;
    var prime_movement = false;

    init {
        buttons_activated[0] = buttons_activated[2] == false;
        buttons_activated[1] = true;
    }


    fun load_level(lev_number: Int) {
        mode = 1;
        update_tick = 0;

        steps_taken = 0;
        num_bananas = 0;
        level_ended = 0;
        level_array = js("new Array()")
        level_number = lev_number;
        wait_timer = LEV_START_DELAY * UPS;
        walk_dir = DIR_NONE;

        if (level_unlocked < lev_number) {
            level_unlocked = lev_number;
        }

        buttons_activated[2] = lev_number < level_unlocked as Int && lev_number != 0

        buttons_activated[0] = lev_number > 1

        for (i in 0 until LEV_DIMENSION_X) {
            level_array[i] = js("new Array()")
        }

        var berti_counter = 0;
        berti_positions = js("new Array()")

        for (y in 0 until LEV_DIMENSION_Y) {
            for (x in 0 until LEV_DIMENSION_X) {
                level_array[x][y] = KtEntity(game);
                level_array[x][y].init(res.levels[lev_number][x][y]);

                if (res.levels[lev_number][x][y] == 4) {
                    num_bananas++;
                } else if (res.levels[lev_number][x][y] == 1) {
                    level_array[x][y].berti_id = berti_counter;
                    berti_positions[berti_counter] = js("{x: x, y: y}");
                    berti_counter++;
                }
            }
        }

        vis.init_animation();

        if (berti_counter > 0) {
            play_sound(8);
        }
    }


    fun play_sound(id: Int) {
        if (!sound) return;
        if (res.sounds[id].currentTime != 0) res.sounds[id].currentTime = 0;
        res.sounds[id].play();
    }

    fun dir_to_coords(curr_x: Int, curr_y: Int, dir: Int): dynamic {
        var new_x = curr_x;
        var new_y = curr_y;

        when (dir) {
            DIR_UP -> {
                new_y--;
            }
            DIR_DOWN -> {
                new_y++;
            }
            DIR_LEFT -> {
                new_x--;
            }
            DIR_RIGHT -> {
                new_x++;
            }
        }

        return js("{x: new_x, y: new_y}");

    }


    fun can_see_tile(eye_x: Int, eye_y: Int, tile_x: Int, tile_y: Int): Boolean {
        val diff_x = tile_x - eye_x;
        val diff_y = tile_y - eye_y;

        var walk1_x: Int;
        var walk1_y: Int;
        var walk2_x: Int;
        var walk2_y: Int;

        if (diff_x == 0) {
            when {
                diff_y == 0 -> {
                    return true;
                }
                diff_y > 0 -> {
                    walk1_x = 0;
                    walk1_y = 1;
                    walk2_x = 0;
                    walk2_y = 1;
                }
                else -> {// diff_y < 0
                    walk1_x = 0;
                    walk1_y = -1;
                    walk2_x = 0;
                    walk2_y = -1;
                }
            }
        } else if (diff_x > 0) {
            if (diff_y == 0) {
                walk1_x = 1;
                walk1_y = 0;
                walk2_x = 1;
                walk2_y = 0;
            } else if (diff_y > 0) {
                when {
                    diff_y > diff_x -> {
                        walk1_x = 0;
                        walk1_y = 1;
                        walk2_x = 1;
                        walk2_y = 1;
                    }
                    diff_y == diff_x -> {
                        walk1_x = 1;
                        walk1_y = 1;
                        walk2_x = 1;
                        walk2_y = 1;
                    }
                    else -> {// diff_y < diff_x
                        walk1_x = 1;
                        walk1_y = 0;
                        walk2_x = 1;
                        walk2_y = 1;
                    }
                }
            } else {// diff_y < 0
                when {
                    diff_y * (-1) > diff_x -> {
                        walk1_x = 0;
                        walk1_y = -1;
                        walk2_x = 1;
                        walk2_y = -1;
                    }
                    diff_y * (-1) == diff_x -> {
                        walk1_x = 1;
                        walk1_y = -1;
                        walk2_x = 1;
                        walk2_y = -1;
                    }
                    else -> {// diff_y < diff_x
                        walk1_x = 1;
                        walk1_y = 0;
                        walk2_x = 1;
                        walk2_y = -1;
                    }
                }
            }
        } else {// diff_x < 0
            if (diff_y == 0) {
                walk1_x = -1;
                walk1_y = 0;
                walk2_x = -1;
                walk2_y = 0;
            } else if (diff_y > 0) {
                if (diff_y > diff_x * (-1)) {
                    walk1_x = 0;
                    walk1_y = 1;
                    walk2_x = -1;
                    walk2_y = 1;
                } else if (diff_y == diff_x * (-1)) {
                    walk1_x = -1;
                    walk1_y = 1;
                    walk2_x = -1;
                    walk2_y = 1;
                } else {// diff_y < diff_x
                    walk1_x = -1;
                    walk1_y = 0;
                    walk2_x = -1;
                    walk2_y = 1;
                }
            } else {// diff_y < 0
                if (diff_y > diff_x) {
                    walk1_x = -1;
                    walk1_y = 0;
                    walk2_x = -1;
                    walk2_y = -1;
                } else if (diff_y == diff_x) {
                    walk1_x = -1;
                    walk1_y = -1;
                    walk2_x = -1;
                    walk2_y = -1;
                } else {// diff_y < diff_x
                    walk1_x = 0;
                    walk1_y = -1;
                    walk2_x = -1;
                    walk2_y = -1;
                }
            }
        }


        var x_offset = 0;
        var y_offset = 0;
        var x_ratio1: Int;
        var y_ratio1: Int;
        var x_ratio2: Int;
        var y_ratio2: Int;
        var diff1: Int;
        var diff2: Int;

        while (true) {
            if (diff_x != 0) {
                x_ratio1 = (x_offset + walk1_x) / diff_x;
                x_ratio2 = (x_offset + walk2_x) / diff_x;
            } else {
                x_ratio1 = 1;
                x_ratio2 = 1;
            }
            if (diff_y != 0) {
                y_ratio1 = (y_offset + walk1_y) / diff_y;
                y_ratio2 = (y_offset + walk2_y) / diff_y;
            } else {
                y_ratio1 = 1;
                y_ratio2 = 1;
            }

            diff1 = abs(x_ratio1 - y_ratio1);
            diff2 = abs(x_ratio2 - y_ratio2);

            if (diff1 <= diff2) {
                x_offset += walk1_x;
                y_offset += walk1_y;
            } else {
                x_offset += walk2_x;
                y_offset += walk2_y;
            }

            if (x_offset == diff_x && y_offset == diff_y) {
                return true;
            }
            console.log("EXER  " + eye_x)
            console.log("EXERx_offset  " + x_offset)
            console.log("EXERy  " + eye_y)
            console.log("EXERy_offset  " + y_offset)
            x_offset = if (x_offset < 0) {
                return false;

            } else {
                x_offset
            }
            y_offset = if (y_offset < 0) {
                return false;

            } else {
                y_offset
            }
            if (game.level_array[eye_x + x_offset][eye_y + y_offset].id != 0 && game.level_array[eye_x + x_offset][eye_y + y_offset].id != -1 && !game.level_array[eye_x + x_offset][eye_y + y_offset].is_small) {
                return false;
            }
        }
        // Code here is unreachable

    }


    fun next_level() {
        if (level_number >= 50 || level_number < 0) {
            mode = 2;
            steps_taken = 0;
            play_sound(6);
            buttons_activated[0] = false;
            buttons_activated[2] = false;
            return;
        }
        load_level(level_number + 1);// Prevent overflow here
        if (level_number > level_unlocked) {
            level_unlocked = level_number;
        }
    }


    fun set_volume(vol: Double) {
        var newVol = vol
        if (vol > 1) {
            newVol = 1.0;
        } else if (vol < 0) {
            newVol = 0.0;
        }
        volumeBar.volume = newVol;
        newVol = newVol.pow(3.0);// LOGARITHMIC!

        for (element in res.sounds) {
            element.volume = vol;
        }

    }

    fun prev_level() {
        if (level_number >= 1) {
            load_level(level_number - 1);
        }
    }


    fun reset_level() {
        if (mode == 0) {
            load_level(3);
        } else if (mode == 1) {
            if (level_number == 0) {
                load_level(1);
            } else {
                load_level(level_number);
            }
        }
    }

    fun toggle_paused() {
        paused = !paused
    }

    // This is necessary because of mobile browsers. These browsers block sound playback
// unless it is triggered by a user input event. Play all sounds at the first input,
// then the restriction is lifted for further playbacks.
    fun remove_soundrestriction() {
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


    fun toggle_single_steps() {
        if (single_steps) {
            walk_dir = DIR_NONE;
            single_steps = false;
        } else {
            single_steps = true;
        }
    }

    fun toggle_sound() {
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

    fun get_adjacent_tiles(tile_x: Int, tile_y: Int): Array<Tile> {

        var result = arrayListOf<dynamic>()
        for (i in -1 until 1) {
            for (j in -1 until 1) {
                if (i != 0 || j != 0) {
                    if (is_in_bounds(tile_x + i, tile_y + j)) {
                        result.add(js("{x:(tile_x+i), y:(tile_y+j)}"))
                    }
                }
            }
        }


        return result.toTypedArray()
    }




    fun start_move(src_x: Int, src_y: Int, dir: Int) {
        var dst = dir_to_coords(src_x, src_y, dir);
        level_array[src_x][src_y].moving = true;
        level_array[src_x][src_y].face_dir = dir;

        if (level_array[src_x][src_y].id == 1) {
            steps_taken++;
        }

        if ((level_array[src_x][src_y].id == 1 || level_array[src_x][src_y].id == 2) && level_array[dst.x][dst.y].consumable) {
            // Om nom nom start
        } else if (level_array[dst.x][dst.y].moving) {
            // It's moving out of place by itself, don't do anything
        } else if (level_array[dst.x][dst.y].id != 0) {
            level_array[src_x][src_y].pushing = true;
            start_move(dst.x, dst.y, dir);
        } else {
            level_array[dst.x][dst.y].init(-1);// DUMMYBLOCK, invisible and blocks everything.
        }

        vis.update_animation(src_x, src_y);


    }

    fun move(src_x: Int, src_y: Int, dir: Int) {
        var dst = dir_to_coords(src_x, src_y, dir);
        level_array[src_x][src_y].moving = false;
        level_array[src_x][src_y].moving_offset = js("{x: 0, y: 0}");
        level_array[src_x][src_y].pushing = false;

        if ((level_array[src_x][src_y].id == 1 || level_array[src_x][src_y].id == 2) && level_array[dst.x][dst.y].consumable) {
            when (level_array[dst.x][dst.y].id) {// Done Om nom nom
                4 -> {
                    num_bananas--;
                    if (num_bananas <= 0) {
                        wait_timer = LEV_STOP_DELAY * UPS;
                        level_ended = 1;
                        if (Random.nextDouble() < 0.50) {
                            game.wow = true;
                            play_sound(10);// wow
                        } else {
                            game.wow = false;
                            play_sound(11);// yeah
                        }
                        vis.update_all_animations();
                    } else {
                        play_sound(7);// Om nom nom
                    }
                }
                13 -> {
                    remove_door(19);
                }
                14 -> {
                    remove_door(20);
                }
                15 -> {
                    remove_door(21);
                }
                16 -> {
                    remove_door(22);
                }
                17 -> {
                    remove_door(24);
                }
                else -> {
                    console.log("003: Something went mighty wrong! Blame the programmer! " + level_array[dst.x][dst.y].id)
                }
            }
        } else if (level_array[dst.x][dst.y].id != -1 && level_array[dst.x][dst.y].id != 0) {
            move(dst.x, dst.y, dir);
        } else if (sound) {// we need another logic to determine this correctly...DEBUG!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            var dst2 = dir_to_coords(dst.x, dst.y, dir);
            if ((level_array[src_x][src_y].id == 5 || level_array[src_x][src_y].id == 6) &&
                (!is_in_bounds(dst2.x, dst2.y) || level_array[dst2.x][dst2.y].id == 3)
            ) {
                play_sound(5);
            }
        }

        var swapper = level_array[dst.x][dst.y];
        level_array[dst.x][dst.y] = level_array[src_x][src_y];
        level_array[src_x][src_y] = swapper;

        var back_dir = opposite_dir(dir);
        var before_src = dir_to_coords(src_x, src_y, back_dir);

        var possibilities = arrayOf(DIR_UP, DIR_DOWN, DIR_LEFT, DIR_RIGHT);
        for (i in possibilities.indices) {
            if (possibilities[i] == dir || possibilities[i] == back_dir) {
                //TODO: Find out how this works in Kotlin
                js(
                    "" +
                            "possibilities.splice(i, 1);\n" +
                            "            i--;"
                )
            }
        }

        var before_src2 = dir_to_coords(src_x, src_y, possibilities[0]);
        var before_src3 = dir_to_coords(src_x, src_y, possibilities[1]);

        if (
            (is_in_bounds(
                before_src.x,
                before_src.y
            ) && (level_array[before_src.x][before_src.y].moving && level_array[before_src.x][before_src.y].face_dir == dir)) ||
            level_array[dst.x][dst.y].is_small && ((is_in_bounds(
                before_src2.x,
                before_src2.y
            ) && (level_array[before_src2.x][before_src2.y].is_small && level_array[before_src2.x][before_src2.y].moving && level_array[before_src2.x][before_src2.y].face_dir == possibilities[1])) ||
                    (is_in_bounds(
                        before_src3.x,
                        before_src3.y
                    ) && (level_array[before_src3.x][before_src3.y].is_small && level_array[before_src3.x][before_src3.y].moving && level_array[before_src3.x][before_src3.y].face_dir == possibilities[0])))
        ) {
            level_array[src_x][src_y].init(-1);
        } else {
            level_array[src_x][src_y].init(0);
        }
        if (level_array[dst.x][dst.y].id == 1) {// Rectify the position of berti
            berti_positions[level_array[dst.x][dst.y].berti_id] = dst;
        }

    }

    fun remove_door(id: Int) {
        play_sound(9);
        for (y in 0 until LEV_DIMENSION_Y) {
            for (x in 0 until LEV_DIMENSION_X) {
                if (level_array[x][y].id == id) {
                    level_array[x][y].gets_removed_in = door_removal_delay;
                }
            }
        }

    }

    // Whether you can walk from a tile in a certain direction, boolean
    fun walkable(curr_x: Int, curr_y: Int, dir: Int): Boolean {
        var dst = dir_to_coords(curr_x, curr_y, dir);

        if (!is_in_bounds(dst.x, dst.y)) {// Can't go out of boundaries
            return false;
        }

        if (level_array[dst.x][dst.y].id == 0) {// Blank space is always walkable
            return true;
        } else if (!level_array[dst.x][dst.y].moving) {
            if ((level_array[curr_x][curr_y].id == 1 || level_array[curr_x][curr_y].id == 2) && level_array[dst.x][dst.y].consumable) {// Berti and MENU Berti can pick up items.
                return true;
            } else {
                if (level_array[curr_x][curr_y].can_push && level_array[dst.x][dst.y].pushable) {
                    return walkable(dst.x, dst.y, dir);
                } else {
                    return false;
                }
            }
        } else if (level_array[dst.x][dst.y].face_dir == dir || (level_array[curr_x][curr_y].is_small && level_array[dst.x][dst.y].is_small)) {// If the block is already moving away in the right direction
            return true;
        } else {
            return false;
        }
    }

    fun is_in_bounds(tile_x: Int, tile_y: Int): Boolean {
        return (tile_x >= 0 && tile_y >= 0 && tile_x < LEV_DIMENSION_X && tile_y < LEV_DIMENSION_Y)
    }

    fun dbxcall_chpass(pass: String, newpass: String): Boolean {
        var result = change_password(pass, newpass);
        if (result != ERR_SUCCESS) {
            vis.error_dbx(result);
            return false;
        }

        return true;
    }

    fun dbxcall_load(uname: String?, pass: String): Boolean {
        if (uname === null || uname == "") {
            vis.error_dbx(ERR_EMPTYNAME);
            return false;
        }

        var result = retrieve_savegame(uname, pass);
        if (result != ERR_SUCCESS) {
            vis.error_dbx(result);
            return false;
        }

        return true;
    }


    // Those calls are on a higher abstraction levels and can be safely used by dialog boxes:
    fun dbxcall_save(uname: String?, pass: String): Boolean {
        var result: Int;
        if (uname === null || uname == "") {
            vis.error_dbx(ERR_EMPTYNAME);
            return false;
        }

        if (savegame.username === null) {
            result = name_savegame(uname, pass);
            if (result != ERR_SUCCESS) {
                vis.error_dbx(result);
                return false;
            }
        }

        result = store_savegame();
        if (result != ERR_SUCCESS) {
            vis.error_dbx(result);
            return false;
        }

        return true;
    }

    fun change_password(pass: String, newpass: String): Int {
        val md5pass = md5.digest(pass);
        if (savegame.password === md5pass) {
            savegame.password = md5.digest(newpass);
            localStorage.setItem("player" + savegame.usernumber + "_password", savegame.password!!);
            return ERR_SUCCESS;// Worked
        }
        return ERR_WRONGPW;// Wrong pass
    }

    fun update_savegame(lev: Int, steps: Int) {
        if (savegame.reached_level <= lev) {
            savegame.reached_level = lev + 1;
            savegame.progressed = true;
        }
        if (savegame.arr_steps[lev] == 0 || savegame.arr_steps[lev] > steps) {
            savegame.arr_steps[lev] = steps;
            savegame.progressed = true;
        }
    }

    fun clear_savegame() {
        savegame = SaveGame();
        level_unlocked = 1;
        load_level(level_unlocked);
    }


    fun store_savegame(): Int {
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
        return ERR_SUCCESS

    }

    fun name_savegame(uname: String, pass: String): Int {
        var user_count = localStorage.getItem("user_count");
        if (user_count != null) {
            val userCountNum = user_count.toInt()
            for (i in 0 until userCountNum) {
                var prefix = "player" + i + "_";
                if (localStorage.getItem(prefix + "username") == uname) {
                    return ERR_EXISTS;// Failed already exists
                }
            }
        }
        savegame.username = uname;
        savegame.password = md5.digest(pass)
        return ERR_SUCCESS;// Worked
    }

    fun retrieve_savegame(uname: String, pass: String): Int {
        var user_count = localStorage.getItem("user_count")?.toIntOrNull() ?: 0;
        if (user_count == 0) {
            return ERR_NOSAVE;// There are no save games
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

                    level_unlocked = savegame.reached_level;
                    if (level_unlocked >= 50) {
                        load_level(50);
                    } else {
                        load_level(level_unlocked);
                    }

                    return ERR_SUCCESS;// Success!
                } else {
                    return ERR_WRONGPW;// Wrong password!
                }
            }
        }
        return ERR_NOTFOUND;// There's no such name
    }

}