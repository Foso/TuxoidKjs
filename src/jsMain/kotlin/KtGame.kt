import App.Companion.DIR_DOWN
import App.Companion.DIR_LEFT
import App.Companion.DIR_NONE
import App.Companion.DIR_RIGHT
import App.Companion.DIR_UP
import App.Companion.LEV_START_DELAY
import App.Companion.UPS
import KtVisual.Companion.ERR_EMPTYNAME
import KtVisual.Companion.ERR_SUCCESS
import data.savegame.SaveGameDataSource
import data.sound.SoundDataSource
import data.sound.VolumeChangeListener
import de.jensklingenberg.bananiakt.Tile
import ui.volumebar.VolumeBar
import kotlin.js.Date
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.round
import kotlin.random.Random

var DEBUG = true

interface GameHandler {


    fun increaseBanana()
    fun decreaseBanana()
    fun getBananas(): Int
    fun resetBanas()
    fun getStepsTaken(): Int
    fun resetSteps()
    fun increaseSteps()
    fun isPaused(): Boolean
    fun setPause(pause: Boolean)
    fun toggle_paused()
}


class GameHandlerImpl() : GameHandler {

    var num_bananas = 0

    var steps_taken = 0
    var paused = false


    override fun increaseBanana() {
        num_bananas++
    }

    override fun decreaseBanana() {
        num_bananas--
    }

    override fun getBananas(): Int {
        return num_bananas
    }

    override fun resetBanas() {
        num_bananas = 0
    }

    override fun getStepsTaken(): Int {
        return steps_taken
    }

    override fun resetSteps() {
        steps_taken = 0
    }

    override fun increaseSteps() {
        steps_taken++
    }

    override fun isPaused(): Boolean {
        return paused
    }

    override fun setPause(pause: Boolean) {
        paused = pause
    }

    override fun toggle_paused() {
        paused = !paused
    }

}

class KtGame(
    val volumeBar: VolumeBar,
    val res: MyRes,
    val saveGameDataSource: SaveGameDataSource,
    val soundDataSource: SoundDataSource,
    val gameHandler: GameHandler
) :
    VolumeChangeListener {

    var INTRO_DURATION = 2// In seconds
    var single_steps = true

    var savegame = saveGameDataSource.getSaveGame()
    val move_speed = round((1 * 60 / UPS).toDouble())
    var door_removal_delay = round((8 * UPS / 60).toDouble())
    var fpsInterval = 1000 / UPS
    var then = Date.now()
    var now: Double = 0.0
    var initialized = false
    var wait_timer = INTRO_DURATION * UPS
    var update_drawn = false
    var mode: GameMode = GameMode.ENTRY// 0 is entry, 1 is menu and play
    var level_number = 0
    var level_array = arrayOf(arrayOf<KtEntity>())
    private var level_unlocked = 0
    var levelState: GameState = GameState.RUNNING// 0 is not ended. 1 is won. 2 is died.
    var wow = true// true is WOW!, false is Yeah!
    var berti_positions = arrayOf(Tile(0, 0))
    var walk_dir = DIR_NONE
    var last_updated = Date.now()
    var delta_updated = Date.now()

    var buttons_activated = Array<Boolean>(5) { false }
    // var buttons_activated[0] = buttons_activated[2] == false;
    // var buttons_activated[1] = true;

    var sound = !DEBUG

    var update_tick = 0
    var prime_movement = false

    init {
        buttons_activated[0] = buttons_activated[2] == false
        buttons_activated[1] = true
        soundDataSource.addVolumeChangeListener(this)
    }


    fun load_level(lev_number: Int) {
        mode = GameMode.MENU
        update_tick = 0

        gameHandler.resetSteps()
        gameHandler.resetBanas()
        levelState = GameState.RUNNING
        level_array = js("new Array()")
        level_number = lev_number
        wait_timer = LEV_START_DELAY * UPS
        walk_dir = DIR_NONE

        if (level_unlocked < lev_number) {
            level_unlocked = lev_number
        }

        buttons_activated[2] = lev_number < level_unlocked as Int && lev_number != 0

        buttons_activated[0] = lev_number > 1

        for (i in 0 until LEV_DIMENSION_X) {
            level_array[i] = js("new Array()")
        }

        var berti_counter = 0
        berti_positions = js("new Array()")

        for (y in 0 until LEV_DIMENSION_Y) {
            for (x in 0 until LEV_DIMENSION_X) {
                level_array[x][y] = KtEntity(this)
                level_array[x][y].init(res.levels[lev_number][x][y])

                if (res.levels[lev_number][x][y] == 4) {
                    gameHandler.increaseBanana()
                } else if (res.levels[lev_number][x][y] == 1) {
                    level_array[x][y].berti_id = berti_counter
                    berti_positions[berti_counter] = js("{x: x, y: y}")
                    berti_counter++
                }
            }
        }

        vis.init_animation()

        if (berti_counter > 0) {
            soundDataSource.play_sound(8)
        }
    }


    fun dir_to_coords(curr_x: Int, curr_y: Int, dir: Int): dynamic {
        var new_x = curr_x
        var new_y = curr_y

        when (dir) {
            DIR_UP -> {
                new_y--
            }
            DIR_DOWN -> {
                new_y++
            }
            DIR_LEFT -> {
                new_x--
            }
            DIR_RIGHT -> {
                new_x++
            }
        }

        return js("{x: new_x, y: new_y}")

    }





    fun next_level() {
        if (level_number >= 50 || level_number < 0) {
            mode = GameMode.PLAY
            gameHandler.resetSteps()
            soundDataSource.play_sound(6)
            buttons_activated[0] = false
            buttons_activated[2] = false
            return
        }
        load_level(level_number + 1)// Prevent overflow here
        if (level_number > level_unlocked) {
            level_unlocked = level_number
        }
    }


    fun prev_level() {
        if (level_number >= 1) {
            load_level(level_number - 1)
        }
    }
    fun kt_update_entities(input: MyInput) {
        var tick = (update_tick * 60 / UPS)
        var synced_move = (tick % (12 / move_speed)) == 0.0

        // The player moves first at all times to ensure the best response time and remove directional quirks.
        // The player moves first at all times to ensure the best response time and remove directional quirks.
        for (position in berti_positions) {
            level_array[position.x][position.y].register_input(position.x, position.y, !synced_move, input)
        }

        if (synced_move) {
            // NPC logic and stop walking logic.
            for (y in 0 until LEV_DIMENSION_Y) {
                for (x in 0 until LEV_DIMENSION_X) {
                    when (level_array[x][y].id) {
                        2 -> {// MENU Berti
                            level_array[x][y].move_randomly(x, y)
                        }
                        7, 10 -> {// Purple and green monster
                            level_array[x][y].chase_berti(x, y)
                        }
                    }

                    if (level_array[x][y].just_moved) {
                        level_array[x][y].just_moved = false
                        vis.update_animation(x, y)
                    }
                }
            }
        }

        // After calculating who moves where, the entities actually get updated.
        for (y in 0 until LEV_DIMENSION_Y) {
            for (x in 0 until LEV_DIMENSION_X) {
                level_array[x][y].updateEntity(x, y)
            }
        }

        // Gameover condition check.
        for (position in berti_positions) {
            level_array[position.x][position.y].check_enemy_proximity(position.x, position.y)
        }
    }

    fun reset_level() {
        when (mode) {
            GameMode.ENTRY -> {
                load_level(3)
            }
            GameMode.MENU -> {
                if (level_number == 0) {
                    load_level(1)
                } else {
                    load_level(level_number)
                }
            }
        }
    }


    fun toggle_single_steps() {
        if (single_steps) {
            walk_dir = DIR_NONE
            single_steps = false
        } else {
            single_steps = true
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
        var dst = dir_to_coords(src_x, src_y, dir)
        level_array[src_x][src_y].moving = true
        level_array[src_x][src_y].face_dir = dir

        if (level_array[src_x][src_y].id == 1) {
            gameHandler.increaseSteps()
        }

        if ((level_array[src_x][src_y].id == 1 || level_array[src_x][src_y].id == 2) && level_array[dst.x][dst.y].consumable) {
            // Om nom nom start
        } else if (level_array[dst.x][dst.y].moving) {
            // It's moving out of place by itself, don't do anything
        } else if (level_array[dst.x][dst.y].id != 0) {
            level_array[src_x][src_y].pushing = true
            start_move(dst.x, dst.y, dir)
        } else {
            level_array[dst.x][dst.y].init(-1)// DUMMYBLOCK, invisible and blocks everything.
        }

        vis.update_animation(src_x, src_y)


    }

    fun move(src_x: Int, src_y: Int, dir: Int) {
        var dst = dir_to_coords(src_x, src_y, dir)
        level_array[src_x][src_y].moving = false
        level_array[src_x][src_y].moving_offset = js("{x: 0, y: 0}")
        level_array[src_x][src_y].pushing = false

        if ((level_array[src_x][src_y].id == 1 || level_array[src_x][src_y].id == 2) && level_array[dst.x][dst.y].consumable) {
            when (level_array[dst.x][dst.y].id) {// Done Om nom nom
                4 -> {
                    gameHandler.decreaseBanana()
                    if (gameHandler.getBananas() <= 0) {
                        wait_timer = LEV_STOP_DELAY * UPS
                        levelState = GameState.WON
                        if (Random.nextDouble() < 0.50) {
                            wow = true
                            soundDataSource.play_sound(10)// wow
                        } else {
                            wow = false
                            soundDataSource.play_sound(11)// yeah
                        }
                        vis.update_all_animations()
                    } else {
                        soundDataSource.play_sound(7)// Om nom nom
                    }
                }
                13 -> {
                    remove_door(19)
                }
                14 -> {
                    remove_door(20)
                }
                15 -> {
                    remove_door(21)
                }
                16 -> {
                    remove_door(22)
                }
                17 -> {
                    remove_door(24)
                }
                else -> {
                    console.log("003: Something went mighty wrong! Blame the programmer! " + level_array[dst.x][dst.y].id)
                }
            }
        } else if (level_array[dst.x][dst.y].id != -1 && level_array[dst.x][dst.y].id != 0) {
            move(dst.x, dst.y, dir)
        } else if (sound) {// we need another logic to determine this correctly...DEBUG!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            var dst2 = dir_to_coords(dst.x, dst.y, dir)
            if ((level_array[src_x][src_y].id == 5 || level_array[src_x][src_y].id == 6) &&
                (!is_in_bounds(dst2.x, dst2.y) || level_array[dst2.x][dst2.y].id == 3)
            ) {
                soundDataSource.play_sound(5)
            }
        }

        var swapper = level_array[dst.x][dst.y]
        level_array[dst.x][dst.y] = level_array[src_x][src_y]
        level_array[src_x][src_y] = swapper

        var back_dir = opposite_dir(dir)
        var before_src = dir_to_coords(src_x, src_y, back_dir)

        var possibilities = arrayOf(DIR_UP, DIR_DOWN, DIR_LEFT, DIR_RIGHT)
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

        var before_src2 = dir_to_coords(src_x, src_y, possibilities[0])
        var before_src3 = dir_to_coords(src_x, src_y, possibilities[1])

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
            level_array[src_x][src_y].init(-1)
        } else {
            level_array[src_x][src_y].init(0)
        }
        if (level_array[dst.x][dst.y].id == 1) {// Rectify the position of berti
            berti_positions[level_array[dst.x][dst.y].berti_id] = dst
        }

    }

    fun remove_door(id: Int) {
        soundDataSource.play_sound(9)
        for (y in 0 until LEV_DIMENSION_Y) {
            for (x in 0 until LEV_DIMENSION_X) {
                if (level_array[x][y].id == id) {
                    level_array[x][y].gets_removed_in = door_removal_delay
                }
            }
        }

    }

    // Whether you can walk from a tile in a certain direction, boolean
    fun walkable(curr_x: Int, curr_y: Int, dir: Int): Boolean {
        var dst = dir_to_coords(curr_x, curr_y, dir)

        if (!is_in_bounds(dst.x, dst.y)) {// Can't go out of boundaries
            return false
        }

        if (level_array[dst.x][dst.y].id == 0) {// Blank space is always walkable
            return true
        } else if (!level_array[dst.x][dst.y].moving) {
            if ((level_array[curr_x][curr_y].id == 1 || level_array[curr_x][curr_y].id == 2) && level_array[dst.x][dst.y].consumable) {// Berti and MENU Berti can pick up items.
                return true
            } else {
                if (level_array[curr_x][curr_y].can_push && level_array[dst.x][dst.y].pushable) {
                    return walkable(dst.x, dst.y, dir)
                } else {
                    return false
                }
            }
        } else if (level_array[dst.x][dst.y].face_dir == dir || (level_array[curr_x][curr_y].is_small && level_array[dst.x][dst.y].is_small)) {// If the block is already moving away in the right direction
            return true
        } else {
            return false
        }
    }

    fun is_in_bounds(tile_x: Int, tile_y: Int): Boolean {
        return (tile_x >= 0 && tile_y >= 0 && tile_x < LEV_DIMENSION_X && tile_y < LEV_DIMENSION_Y)
    }


    fun dbxcall_load(uname: String?, pass: String): Boolean {
        if (uname === null || uname == "") {
            vis.error_dbx(ERR_EMPTYNAME)
            return false
        }

        var result = saveGameDataSource.retrieve_savegame(uname, pass)
        if (result == ERR_SUCCESS) {
            level_unlocked = savegame.reached_level
            if (level_unlocked >= 50) {
                load_level(50)
            } else {
                load_level(level_unlocked)
            }
        } else {
            vis.error_dbx(result)
            return false
        }

        return true
    }


    // Those calls are on a higher abstraction levels and can be safely used by dialog boxes:
    fun dbxcall_save(uname: String?, pass: String): Boolean {
        var result: Int
        if (uname === null || uname == "") {
            vis.error_dbx(ERR_EMPTYNAME)
            return false
        }

        if (savegame.username === null) {
            result = saveGameDataSource.name_savegame(uname, pass)
            if (result != ERR_SUCCESS) {
                vis.error_dbx(result)
                return false
            }
        }

        result = saveGameDataSource.store_savegame()
        if (result != ERR_SUCCESS) {
            vis.error_dbx(result)
            return false
        }

        return true
    }

    fun createNewGame() {
        saveGameDataSource.clear_savegame()
        level_unlocked = 1
        load_level(level_unlocked)
    }

    override fun onSoundChanged(vol: Double) {
        volumeBar.volume = vol
        val newVol = vol.pow(3.0)// LOGARITHMIC!

        for (element in res.sounds) {
            element.volume = newVol
        }
    }

}