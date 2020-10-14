import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlin.js.Date
import kotlin.math.abs

import kotlin.math.pow
import kotlin.math.roundToInt

import kotlin.random.Random

var soundarray = arrayOf(
    "about.mp3",
    "argl.mp3",
    "attack1.mp3",
    "attack2.mp3",
    "chart.mp3",
    "click.mp3",
    "gameend.mp3",
    "getpoint.mp3",
    "newplane.mp3",
    "opendoor.mp3",
    "wow.mp3",
    "yeah.mp3"
)


var ERR_SUCCESS = 0;
var ERR_EXISTS = 1;
var ERR_NOSAVE = 2;
var ERR_WRONGPW = 3;
var ERR_NOTFOUND = 4;
var ERR_EMPTYNAME = 5;

var DBX_CONFIRM = 0;
var DBX_SAVE = 1;
var DBX_LOAD = 2;
var DBX_CHPASS = 3;
var DBX_LOADLVL = 4;
var DBX_CHARTS = 5;
external var md5: dynamic


/**
 * This needed
 */
@JsExport
fun saveGame(test: dynamic) {
    // window.alert(test.usernumber)
    test.usernumber = -1
    test.username = null;
    test.password = null;
    test.reached_level = 1;
    test.progressed = false;
    test.arr_steps = arrayOf<Int>()
    for (i in 0 until 50) {
        test.arr_steps[i] = 0
    }


}


external interface Unknown {
    fun load_level(i: Int)
    fun play_sound(i: Int)

    val door_removal_delay: dynamic
    val level_array: Array<dynamic>
    var level_unlocked: Int
    var savegame: SaveGame
}


@JsExport
fun reset_level(that: dynamic) {
    if (that.mode == 0) {
        that.load_level(0);
    } else if (that.mode == 1) {
        if (that.level_number == 0) {
            that.load_level(1);
        } else {
            that.load_level(that.level_number);
        }
    }
}



@JsExport
fun get_adjacent_tiles(tile_x:Int, tile_y:Int,that:dynamic): Array<dynamic> {

    var result = arrayListOf<dynamic>()
    for(i in -1 until 1){
        for(j in -1 until 1){
            if(i != 0 || j != 0){
                if(that.is_in_bounds(tile_x+i, tile_y+j)){
                    result.add(js("{x:(tile_x+i), y:(tile_y+j)}"))
                }
            }
        }
    }


    return result.toTypedArray()
}


@JsExport
fun can_see_tile(eye_x:Int, eye_y:Int, tile_x:Int, tile_y:Int): Boolean {
    var diff_x = tile_x - eye_x;
    var diff_y = tile_y - eye_y;

    var walk1_x:Int;
    var walk1_y:Int;
    var walk2_x:Int;
    var walk2_y:Int;

    if (diff_x==0){
        if(diff_y==0){
            return true;
        }else if(diff_y > 0){
            walk1_x = 0;
            walk1_y = 1;
            walk2_x = 0;
            walk2_y = 1;
        }else{// diff_y < 0
            walk1_x = 0;
            walk1_y = -1;
            walk2_x = 0;
            walk2_y = -1;
        }
    }else if(diff_x > 0){
        if(diff_y==0){
            walk1_x = 1;
            walk1_y = 0;
            walk2_x = 1;
            walk2_y = 0;
        }else if(diff_y > 0){
            if(diff_y > diff_x){
                walk1_x = 0;
                walk1_y = 1;
                walk2_x = 1;
                walk2_y = 1;
            }else if(diff_y == diff_x){
                walk1_x = 1;
                walk1_y = 1;
                walk2_x = 1;
                walk2_y = 1;
            }else{// diff_y < diff_x
                walk1_x = 1;
                walk1_y = 0;
                walk2_x = 1;
                walk2_y = 1;
            }
        }else{// diff_y < 0
            if(diff_y*(-1) > diff_x){
                walk1_x = 0;
                walk1_y = -1;
                walk2_x = 1;
                walk2_y = -1;
            }else if(diff_y*(-1) == diff_x){
                walk1_x = 1;
                walk1_y = -1;
                walk2_x = 1;
                walk2_y = -1;
            }else{// diff_y < diff_x
                walk1_x = 1;
                walk1_y = 0;
                walk2_x = 1;
                walk2_y = -1;
            }
        }
    }else{// diff_x < 0
        if(diff_y==0){
            walk1_x = -1;
            walk1_y = 0;
            walk2_x = -1;
            walk2_y = 0;
        }else if(diff_y > 0){
            if(diff_y > diff_x*(-1)){
                walk1_x = 0;
                walk1_y = 1;
                walk2_x = -1;
                walk2_y = 1;
            }else if(diff_y == diff_x*(-1)){
                walk1_x = -1;
                walk1_y = 1;
                walk2_x = -1;
                walk2_y = 1;
            }else{// diff_y < diff_x
                walk1_x = -1;
                walk1_y = 0;
                walk2_x = -1;
                walk2_y = 1;
            }
        }else{// diff_y < 0
            if(diff_y > diff_x){
                walk1_x = -1;
                walk1_y = 0;
                walk2_x = -1;
                walk2_y = -1;
            }else if(diff_y == diff_x){
                walk1_x = -1;
                walk1_y = -1;
                walk2_x = -1;
                walk2_y = -1;
            }else{// diff_y < diff_x
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

    while(true){
        if(diff_x != 0){
            x_ratio1 = (x_offset+walk1_x)/diff_x;
            x_ratio2 = (x_offset+walk2_x)/diff_x;
        }else{
            x_ratio1 = 1;
            x_ratio2 = 1;
        }
        if(diff_y != 0){
            y_ratio1 = (y_offset+walk1_y)/diff_y;
            y_ratio2 = (y_offset+walk2_y)/diff_y;
        }else{
            y_ratio1 = 1;
            y_ratio2 = 1;
        }

        diff1 = abs(x_ratio1-y_ratio1);
        diff2 = abs(x_ratio2-y_ratio2);

        if (diff1 <= diff2){
            x_offset += walk1_x;
            y_offset += walk1_y;
        }else{
            x_offset += walk2_x;
            y_offset += walk2_y;
        }

        if(x_offset == diff_x && y_offset == diff_y){
            return true;
        }
        if(game.level_array[eye_x + x_offset][eye_y + y_offset].id != 0 && game.level_array[eye_x + x_offset][eye_y + y_offset].id != -1 && !game.level_array[eye_x + x_offset][eye_y + y_offset].is_small){
            return false;
        }
    }
    // Code here is unreachable

}

@JsExport
fun opposite_dir(dir: Int): Int {
    when (dir) {
        DIR_UP -> {
            return DIR_DOWN;
        }

        DIR_DOWN -> {
            return DIR_UP;

        }
        DIR_LEFT -> {
            return DIR_RIGHT;
        }

        DIR_RIGHT -> {
            return DIR_LEFT;
        }
        else -> {
            return DIR_NONE
        }
    }
}

@JsExport
fun load_level(lev_number: Int, that: dynamic) {
    that.mode = 1;
    that.update_tick = 0;

    that.steps_taken = 0;
    that.num_bananas = 0;
    that.level_ended = 0;
    that.level_array = js("new Array()");
    that.level_number = lev_number;
    that.wait_timer = LEV_START_DELAY * UPS;
    that.walk_dir = DIR_NONE;

    if (that.level_unlocked < lev_number) {
        that.level_unlocked = lev_number;
    }

    if (lev_number < that.level_unlocked as Int && lev_number != 0) {
        that.buttons_activated[2] = true;
    } else {
        that.buttons_activated[2] = false;
    }

    if (lev_number > 1) {
        that.buttons_activated[0] = true;
    } else {
        that.buttons_activated[0] = false;
    }

    for (i in 0 until LEV_DIMENSION_X) {
        that.level_array[i] = js("new Array()")
    }

    var berti_counter = 0;
    that.berti_positions = js("new Array()")

    for (y in 0 until LEV_DIMENSION_Y) {
        for (x in 0 until LEV_DIMENSION_X) {
            that.level_array[x][y] = KtEntity();
            that.level_array[x][y].init(res.levels[lev_number][x][y]);

            if (res.levels[lev_number][x][y] == 4) {
                that.num_bananas++;
            } else if (res.levels[lev_number][x][y] == 1) {
                that.level_array[x][y].berti_id = berti_counter;
                that.berti_positions[berti_counter] = js("{x: x, y: y}");
                berti_counter++;
            }
        }
    }

    vis.init_animation();

    if (berti_counter > 0) {
        that.play_sound(8);
    }
}

@JsExport
fun set_volume(vol: Int) {
    var newVol = vol
    if (vol > 1) {
        newVol = 1;
    } else if (vol < 0) {
        newVol = 0;
    }
    vis.vol_bar.volume = newVol;
    newVol = newVol.toDouble().pow(3.0).roundToInt();// LOGARITHMIC!

    for (i in 0 until res.sounds.length as Int) {
        res.sounds[i].volume = vol;
    }

}


@JsExport
fun next_level(that: dynamic) {
    if (that.level_number >= 50 || that.level_number < 0) {
        game.mode = 2;
        game.steps_taken = 0;
        game.play_sound(6);
        that.buttons_activated[0] = false;
        that.buttons_activated[2] = false;
        return;
    }
    that.load_level(that.level_number + 1);// Prevent overflow here
    if (that.level_number > that.level_unlocked) {
        that.level_unlocked = that.level_number;
    }
}

@JsExport
fun toggle_paused(that: dynamic) {
    if (that.paused) {
        that.paused = false;
    } else {
        that.paused = true;
    }
}


@JsExport
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


@JsExport
fun start_move(src_x: Int, src_y: Int, dir: Int, that: dynamic) {
    var dst = that.dir_to_coords(src_x, src_y, dir);
    that.level_array[src_x][src_y].moving = true;
    that.level_array[src_x][src_y].face_dir = dir;

    if (that.level_array[src_x][src_y].id == 1) {
        that.steps_taken++;
    }

    if ((that.level_array[src_x][src_y].id == 1 || that.level_array[src_x][src_y].id == 2) && that.level_array[dst.x][dst.y].consumable) {
        // Om nom nom start
    } else if (that.level_array[dst.x][dst.y].moving) {
        // It's moving out of place by itself, don't do anything
    } else if (that.level_array[dst.x][dst.y].id != 0) {
        that.level_array[src_x][src_y].pushing = true;
        that.start_move(dst.x, dst.y, dir);
    } else {
        that.level_array[dst.x][dst.y].init(-1);// DUMMYBLOCK, invisible and blocks everything.
    }

    vis.update_animation(src_x, src_y);


}


@JsExport
class KtGame(val that: Unknown) {


    fun remove_door(id: Int) {
        that.play_sound(9);
        for (y in 0 until LEV_DIMENSION_Y) {
            for (x in 0 until LEV_DIMENSION_X) {
                if (that.level_array[x][y].id == id) {
                    that.level_array[x][y].gets_removed_in = that.door_removal_delay;
                }
            }
        }

    }

// Whether you can walk from a tile in a certain direction, boolean
    fun walkable(curr_x: Int, curr_y: Int, dir: dynamic, that: dynamic, tthis: dynamic): Boolean {
        var dst = that.dir_to_coords(curr_x, curr_y, dir);

        if (!tthis.is_in_bounds(dst.x, dst.y)) {// Can't go out of boundaries
            return false;
        }

        if (that.level_array[dst.x][dst.y].id == 0) {// Blank space is always walkable
            return true;
        } else if (!that.level_array[dst.x][dst.y].moving) {
            if ((that.level_array[curr_x][curr_y].id == 1 || that.level_array[curr_x][curr_y].id == 2) && that.level_array[dst.x][dst.y].consumable) {// Berti and MENU Berti can pick up items.
                return true;
            } else {
                if (that.level_array[curr_x][curr_y].can_push == 1 && that.level_array[dst.x][dst.y].pushable == 1) {
                    return that.walkable(dst.x, dst.y, dir);
                } else {
                    return false;
                }
            }
        } else if (that.level_array[dst.x][dst.y].face_dir == dir || (that.level_array[curr_x][curr_y].is_small && that.level_array[dst.x][dst.y].is_small)) {// If the block is already moving away in the right direction
            return true;
        } else {
            return false;
        }
    }

    fun is_in_bounds(tile_x:Int, tile_y:Int):Boolean{
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

        if (that.savegame.username === null) {
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
        if (that.savegame.password === md5pass) {
            that.savegame.password = md5.digest(newpass);
            localStorage.setItem("player" + that.savegame.usernumber + "_password", that.savegame.password!!);
            return ERR_SUCCESS;// Worked
        }
        return ERR_WRONGPW;// Wrong pass
    }

    fun update_savegame(lev: Int, steps: Int) {
        if (that.savegame.reached_level <= lev) {
            that.savegame.reached_level = lev + 1;
            that.savegame.progressed = true;
        }
        if (that.savegame.arr_steps[lev] == 0 || that.savegame.arr_steps[lev] > steps) {
            that.savegame.arr_steps[lev] = steps;
            that.savegame.progressed = true;
        }
    }

    fun clear_savegame() {
        that.savegame = SaveGame();
        that.level_unlocked = 1;
        that.load_level(that.level_unlocked);
    }


    fun store_savegame(): Int {
        if (localStorage.getItem("user_count") == null) {
            localStorage.setItem("user_count", "1");
            that.savegame.usernumber = 0
        } else if (that.savegame.usernumber == -1) {
            that.savegame.usernumber = localStorage.getItem("user_count")?.toInt() ?: -1;
            localStorage.setItem("user_count", (that.savegame.usernumber + 1).toString())
        }

        var prefix = "player" + that.savegame.usernumber + "_";
        that.savegame.username?.let { localStorage.setItem(prefix + "username", it) };
        that.savegame.password?.let { localStorage.setItem(prefix + "password", it) };
        localStorage.setItem(prefix + "reached_level", that.savegame.reached_level.toString());

        for (i in 1 until 50) {
            localStorage.setItem(prefix + "steps_lv" + i, that.savegame.arr_steps[i].toString());
        }

        that.savegame.progressed = false
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
        that.savegame.username = uname;
        that.savegame.password = md5.digest(pass)
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
                    that.savegame = SaveGame();
                    that.savegame.usernumber = i;0
                    that.savegame.username = uname;
                    that.savegame.password = md5pass;
                    that.savegame.reached_level = localStorage.getItem(prefix + "reached_level")!!.toInt();

                    for (i in 1 until 50) {
                        that.savegame.arr_steps[i] = localStorage.getItem(prefix + "steps_lv" + i)!!.toInt();
                    }
                    that.savegame.progressed = false;

                    that.level_unlocked = that.savegame.reached_level;
                    if (that.level_unlocked >= 50) {
                        that.load_level(50);
                    } else {
                        that.load_level(that.level_unlocked);
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

class SaveGame() {
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

class MyGame() {
    var that = this

    var saveGame = SaveGame()


}


interface Resources {
    fun ready(): Boolean
    val sounds: dynamic

    val levels: dynamic
    var images: Array<dynamic>
}


external interface Tile {
    val x: Int
    val y: Int
}

external interface Game {
    fun set_volume(defaultVolume: dynamic)
    fun play_sound(i: Int)
    fun load_level(i: Int)
    fun next_level()
    fun update_savegame(levelNumber: Int, stepsTaken: Int)
    fun reset_level()
    fun dir_to_coords(currX: Int, currY: Int, faceDir: dynamic): dynamic
    fun move(currX: Int, currY: Int, faceDir: dynamic)
    fun walkable(currX: Int, currY: Int, dirLeft: Int): Boolean
    fun start_move(currX: Int, currY: Int, dirLeft: Int)
    fun get_adjacent_tiles(currX: Int, currY: Int): Array<Tile>
    fun opposite_dir(faceDir: Any): Int
    fun can_see_tile(currX: Int, currY: Int, x: dynamic, y: dynamic): Boolean
    fun remove_soundrestriction()
    fun prev_level()
    fun clear_savegame()
    fun store_savegame()
    fun toggle_paused()
    fun toggle_single_steps()
    fun toggle_sound()

    val savegame: dynamic
    var wow: Boolean
    val berti_positions: Array<Tile>
    val sound: Boolean
    val buttons_activated: Array<Boolean>
    var walk_dir: Int
    val single_steps: Boolean
    var prime_movement: dynamic
    val move_speed: dynamic
    var door_removal_delay: Double
    var level_array: dynamic
    var update_drawn: Boolean
    var last_updated: dynamic
    var delta_updated: dynamic
    var steps_taken: Int
    val level_number: Int
    var update_tick: Int
    var level_ended: Int
    var wait_timer: Int
    var mode: Int
    val paused: Boolean
    var initialized: Boolean

}

external interface Rgb {
    val r: dynamic
    val g: dynamic
    val b: dynamic
}

external interface Dbx {
    fun enterfun()
    fun cancelfun()

    var drag_pos: dynamic
    var drag: Boolean
    val style: dynamic
    val firstChild: Boolean

}


external interface Visual {
    fun update_animation(currX: Int, currY: Int)
    fun update_all_animations()
    fun init_animation()
    fun open_dbx(dbxConfirm: Int, i: Int)
    fun open_dbx(dbxConfirm: Int)
    fun error_dbx(errEmptyname: Int)

    val vol_bar: dynamic
    val dbx: Dbx
    val blue: Rgb
    val black: Rgb
    val dark_grey: Rgb
    val white: Rgb
    val med_grey: Rgb
    val light_grey: Rgb
    var berti_blink_time: Int
    val buttons_pressed: Array<Boolean>
    val menu1: dynamic
}

external var vis: Visual
external var DIR_LEFT: Int
external var DIR_UP: Int
external var DIR_DOWN: Int

external var DIR_RIGHT: Int
external var DIR_NONE: Int
external var LEV_START_DELAY: Int
external var UPS: Int

external var res: Resources
external var game: Game
external var CTX: dynamic//CanvasRenderingContext2D

external var SCREEN_WIDTH: dynamic
external var SCREEN_HEIGHT: dynamic
external var LEV_DIMENSION_Y: Int
external var LEV_DIMENSION_X: Int
external var RENDER_FULL: Int
external var RENDER_TOP: Int
external var RENDER_BOTTOM: Int
external var RENDER_BOTTOM_BORDER: Int
external var LEV_OFFSET_X: Int
external var LEV_OFFSET_Y: Int

external var LEV_STOP_DELAY: Int
external var ANIMATION_DURATION: Int

external var DEFAULT_VOLUME: dynamic
external var input: dynamic
external fun update_entities()

var tt = arrayOf(Test(line = true, check = 0))

class Test(line: Boolean, check: Int)


class MyResource() {
    fun myrest() {
        window.alert("Alert123")
    }

    companion object {
        fun mycomp() {
            window.alert("mycomp")

        }
    }
}


@JsExport
fun toggle_single_steps(that: dynamic){
    if(that.single_steps){
        that.walk_dir = DIR_NONE;
        that.single_steps = false;
    }else{
        that.single_steps = true;
    }
}

/**
 *
 */
@JsExport
fun ktupdate() {

    if (res.ready()) {
        if (!game.initialized) {
            game.set_volume(DEFAULT_VOLUME);
            input.init();// Only init inputs after everything is loaded.
            game.play_sound(0);
            game.initialized = true;
        }
    }
    if (!game.paused) {
        if (game.mode == 0) {
            game.wait_timer--;
            if (game.wait_timer <= 0) {
                game.load_level(2);
            }
        } else if (game.mode == 1) {
            if (game.wait_timer <= 0) {
                if (game.level_ended == 0) {
                    game.update_tick++;
                    update_entities();
                } else if (game.level_ended == 1) {
                    game.update_savegame(game.level_number, game.steps_taken);
                    game.next_level();
                } else if (game.level_ended == 2) {
                    game.reset_level();
                }
            } else {
                game.wait_timer--;
            }
        }
    }

    var now = Date.now();
    game.delta_updated = now.minus(game.last_updated as Double);
    game.last_updated = now;

    game.update_drawn = false;
}



@JsExport
fun toggle_sound(that: dynamic){
    if(that.sound){
        that.sound = false;
        for(i in 0 until res.sounds.length){
            res.sounds[i].pause();
            res.sounds[i].currentTime=0
        }
    }else{
        that.sound = true;
    }
}



// This is necessary because of mobile browsers. These browsers block sound playback
// unless it is triggered by a user input event. Play all sounds at the first input,
// then the restriction is lifted for further playbacks.
@JsExport
fun remove_soundrestriction(that :dynamic){
    if(that.soundrestriction_removed) return;
    for(i in 0 until  res.sounds.length){
        if(res.sounds[i].paused) {
            res.sounds[i].play();
            res.sounds[i].pause();
            res.sounds[i].currentTime=0
        }
    }
    that.soundrestriction_removed = true;
}

@JsExport
fun play_sound(id:Int,that: dynamic){
    if(!that.sound) return;
    if(res.sounds[id].currentTime!=0) res.sounds[id].currentTime=0;
    res.sounds[id].play();
}


@JsExport
fun kt_move(src_x: Int, src_y: Int, dir: Int, that: dynamic) {
    var dst = that.dir_to_coords(src_x, src_y, dir);
    that.level_array[src_x][src_y].moving = false;
    that.level_array[src_x][src_y].moving_offset = js("{x: 0, y: 0}");
    that.level_array[src_x][src_y].pushing = false;

    if ((that.level_array[src_x][src_y].id == 1 || that.level_array[src_x][src_y].id == 2) && that.level_array[dst.x][dst.y].consumable) {
        when (that.level_array[dst.x][dst.y].id) {// Done Om nom nom
            4 -> {
                that.num_bananas--;
                if (that.num_bananas <= 0) {
                    that.wait_timer = LEV_STOP_DELAY * UPS;
                    that.level_ended = 1;
                    if (Random.nextDouble() < 0.50) {
                        game.wow = true;
                        that.play_sound(10);// wow
                    } else {
                        game.wow = false;
                        that.play_sound(11);// yeah
                    }
                    vis.update_all_animations();
                } else {
                    that.play_sound(7);// Om nom nom
                }
            }
            13 -> {
                remove_door(19, that);
            }
            14 -> {
                kt_remove_door(20, that);
            }
            15 -> {
                kt_remove_door(21, that);
            }
            16 -> {
                kt_remove_door(22, that);
            }
            17 -> {
                kt_remove_door(24, that);
            }
            else -> {
                console.log("003: Something went mighty wrong! Blame the programmer! " + that.level_array[dst.x][dst.y].id)
            }
        }
    } else if (that.level_array[dst.x][dst.y].id != -1 && that.level_array[dst.x][dst.y].id != 0) {
        kt_move(dst.x, dst.y, dir, that);
    } else if (that.sound) {// we need another logic to determine this correctly...DEBUG!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        var dst2 = that.dir_to_coords(dst.x, dst.y, dir);
        if ((that.level_array[src_x][src_y].id == 5 || that.level_array[src_x][src_y].id == 6) &&
            (!that.is_in_bounds(dst2.x, dst2.y) || that.level_array[dst2.x][dst2.y].id == 3)
        ) {
            that.play_sound(5);
        }
    }

    var swapper = that.level_array[dst.x][dst.y];
    that.level_array[dst.x][dst.y] = that.level_array[src_x][src_y];
    that.level_array[src_x][src_y] = swapper;

    var back_dir = that.opposite_dir(dir);
    var before_src = that.dir_to_coords(src_x, src_y, back_dir);

    var possibilities = arrayOf(DIR_UP, DIR_DOWN, DIR_LEFT, DIR_RIGHT);
    for (i in 0 until possibilities.size) {
        if (possibilities[i] == dir || possibilities[i] == back_dir) {
            //TODO: Find out how this works in Kotlin
            js(
                "" +
                        "possibilities.splice(i, 1);\n" +
                        "            i--;"
            )
        }
    }

    var before_src2 = that.dir_to_coords(src_x, src_y, possibilities[0]);
    var before_src3 = that.dir_to_coords(src_x, src_y, possibilities[1]);

    if (
        (that.is_in_bounds(
            before_src.x,
            before_src.y
        ) && (that.level_array[before_src.x][before_src.y].moving && that.level_array[before_src.x][before_src.y].face_dir == dir)) ||
        that.level_array[dst.x][dst.y].is_small && ((that.is_in_bounds(
            before_src2.x,
            before_src2.y
        ) && (that.level_array[before_src2.x][before_src2.y].is_small && that.level_array[before_src2.x][before_src2.y].moving && that.level_array[before_src2.x][before_src2.y].face_dir == possibilities[1])) ||
                (that.is_in_bounds(
                    before_src3.x,
                    before_src3.y
                ) && (that.level_array[before_src3.x][before_src3.y].is_small && that.level_array[before_src3.x][before_src3.y].moving && that.level_array[before_src3.x][before_src3.y].face_dir == possibilities[0])))
    ) {
        that.level_array[src_x][src_y].init(-1);
    } else {
        that.level_array[src_x][src_y].init(0);
    }
    if (that.level_array[dst.x][dst.y].id == 1) {// Rectify the position of berti
        that.berti_positions[that.level_array[dst.x][dst.y].berti_id] = dst;
    }

}

@JsExport
fun kt_error_dbx(errno: Int, that: dynamic) {
    if (that.dbx.errfield === null) return;
    var err_string = "";
    when (errno) {
        ERR_EXISTS -> {
            err_string = "Error - the account already exists.";
        }

        ERR_NOSAVE -> {
            err_string = "Error - there are no savegames to load!";
        }

        ERR_WRONGPW -> {
            err_string = "Error - you used the wrong password.";
        }

        ERR_NOTFOUND -> {
            err_string = "Error - this username couldn't be found.";
        }

        ERR_EMPTYNAME -> {
            err_string = "Error - please fill in your uname.";
        }
        else -> {
            err_string = "Unknown error";
        }
    }
    that.dbx.errfield.innerHTML = err_string;
}