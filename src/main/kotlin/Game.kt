import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlin.js.Date
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

class SaveGame() {
    var usernumber: Int = -1
    var username: String = ""
    var password: String = ""
    var reached_level: Int = 1
    var progressed = false
    var arr_steps = arrayOf<Int>()
}

class MyGame() {
    var that = this

    var saveGame = SaveGame()

    fun update_savegame(lev: Int, steps: Int) {
        if (saveGame.reached_level <= lev) {
            saveGame.reached_level = lev + 1
            saveGame.progressed = true
        }
        if (saveGame.arr_steps[lev] == 0 || saveGame.arr_steps[lev] > steps) {
            saveGame.arr_steps[lev] = steps
            saveGame.progressed = true
        }
    }

    fun store_savegame(): Int {
        if (localStorage.getItem("user_count") == null) {
            localStorage.setItem("user_count", "1");
            saveGame.usernumber = 0
        } else if (saveGame.usernumber == -1) {
            saveGame.usernumber = localStorage.getItem("user_count")?.toInt() ?: -1;
            localStorage.setItem("user_count", (saveGame.usernumber + 1).toString())
        }

        var prefix = "player" + saveGame.usernumber + "_";
        localStorage.setItem(prefix + "username", saveGame.username);
        localStorage.setItem(prefix + "password", saveGame.password);
        localStorage.setItem(prefix + "reached_level", saveGame.reached_level.toString());

        for (i in 1 until 50) {
            localStorage.setItem(prefix + "steps_lv" + i, saveGame.arr_steps[i].toString());
        }

        saveGame.progressed = false
        return ERR_SUCCESS

    }

    fun name_savegame(uname: String, pass: String) {
        kt_name_savegame(uname, pass, that)
    }
}

@JsExport
fun kt_name_savegame(uname: String, pass: String, that: dynamic): Int {
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

interface Resources {
    fun ready(): Boolean
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
    fun opposite_dir(faceDir: Any): Any
    fun can_see_tile(currX: Int, currY: Int, x: dynamic, y: dynamic): Boolean

    var wow: Boolean
    val berti_positions: Array<Tile>
    val sound: Boolean
    val buttons_activated: Array<Boolean>
    val walk_dir: Int
    val single_steps: Boolean
    var prime_movement: dynamic
    val move_speed: dynamic
    var door_removal_delay: Double
    var level_array: dynamic
    var update_drawn: Boolean
    var last_updated: dynamic
    var delta_updated: dynamic
    val steps_taken: Int
    val level_number: Int
    var update_tick: Int
    var level_ended: Int
    var wait_timer: Int
    val mode: Int
    val paused: Boolean
    var initialized: Boolean

}

external interface Rgb {
    val r: dynamic
    val g: dynamic
    val b: dynamic
}

external interface Visual {
    fun update_animation(currX: Int, currY: Int)
    fun update_all_animations()
    fun init_animation()

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
                game.load_level(3);
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
fun kt_remove_door(id: Int, that: dynamic) {
    that.play_sound(9);
    for (y in 0 until LEV_DIMENSION_Y) {
        for (x in 0 until LEV_DIMENSION_X) {
            if (that.level_array[x][y].id == id) {
                that.level_array[x][y].gets_removed_in = that.door_removal_delay;
            }
        }
    }

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
                that.remove_door(19);
            }
            14 -> {
                that.remove_door(20);
            }
            15 -> {
                that.remove_door(21);
            }
            16 -> {
                that.remove_door(22);
            }
            17 -> {
                that.remove_door(24);
            }
            else -> {
                window.alert("003: Something went mighty wrong! Blame the programmer!")
            }
        }
    }else if(that.level_array[dst.x][dst.y].id != -1 && that.level_array[dst.x][dst.y].id != 0){
        that.move(dst.x, dst.y, dir);
    }else if(that.sound){// we need another logic to determine this correctly...DEBUG!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        var dst2 = that.dir_to_coords(dst.x, dst.y, dir);
        if(	(that.level_array[src_x][src_y].id == 5 || that.level_array[src_x][src_y].id == 6) &&
            (!that.is_in_bounds(dst2.x, dst2.y) || that.level_array[dst2.x][dst2.y].id == 3)){
            that.play_sound(5);
        }
    }

    var swapper = that.level_array[dst.x][dst.y];
    that.level_array[dst.x][dst.y] = that.level_array[src_x][src_y];
    that.level_array[src_x][src_y] = swapper;

    var back_dir = that.opposite_dir(dir);
    var before_src = that.dir_to_coords(src_x, src_y, back_dir);

    var possibilities = arrayOf(DIR_UP, DIR_DOWN, DIR_LEFT, DIR_RIGHT);
    for(i in 0 until possibilities.size){
        if(possibilities[i] == dir || possibilities[i] == back_dir){
            //TODO: Find out how this works in Kotlin
            js("" +
                    "possibilities.splice(i, 1);\n" +
                    "            i--;")
        }
    }

    var before_src2 = that.dir_to_coords(src_x, src_y, possibilities[0]);
    var before_src3 = that.dir_to_coords(src_x, src_y, possibilities[1]);

    if(
        (that.is_in_bounds(before_src.x, before_src.y) && (that.level_array[before_src.x][before_src.y].moving && that.level_array[before_src.x][before_src.y].face_dir == dir)) ||
        that.level_array[dst.x][dst.y].is_small && ((that.is_in_bounds(before_src2.x, before_src2.y) && (that.level_array[before_src2.x][before_src2.y].is_small &&  that.level_array[before_src2.x][before_src2.y].moving && that.level_array[before_src2.x][before_src2.y].face_dir == possibilities[1])) ||
                (that.is_in_bounds(before_src3.x, before_src3.y) && (that.level_array[before_src3.x][before_src3.y].is_small &&  that.level_array[before_src3.x][before_src3.y].moving && that.level_array[before_src3.x][before_src3.y].face_dir == possibilities[0])))
    ){
        that.level_array[src_x][src_y].init(-1);
    }else{
        that.level_array[src_x][src_y].init(0);
    }
    if(that.level_array[dst.x][dst.y].id == 1){// Rectify the position of berti
        that.berti_positions[that.level_array[dst.x][dst.y].berti_id] = dst;
    }

}