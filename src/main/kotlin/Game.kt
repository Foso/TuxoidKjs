import kotlinx.browser.window
import kotlin.js.Date

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


}
interface Resources{
    fun ready():Boolean
    val levels: dynamic
    var images : Array<dynamic>
}


external interface Tile{
    val x : Int
    val y : Int
}

external interface Game{
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

external var vis : dynamic
external var DIR_LEFT: Int
external var DIR_UP: Int
external var DIR_DOWN: Int

external var DIR_RIGHT: Int
external var DIR_NONE: Int
external var LEV_START_DELAY: Int
external var UPS: Int

external var res : Resources
external var game : Game
external var CTX : dynamic

external var SCREEN_WIDTH : dynamic
external var SCREEN_HEIGHT : dynamic
external var LEV_DIMENSION_Y : Int
external var LEV_DIMENSION_X : Int
external var RENDER_FULL: Int
external var RENDER_TOP: Int
external var RENDER_BOTTOM: Int
external var RENDER_BOTTOM_BORDER: Int
external var LEV_OFFSET_X: Int
external var LEV_OFFSET_Y: Int

external var LEV_STOP_DELAY: Int
external var ANIMATION_DURATION: Int

external var DEFAULT_VOLUME : dynamic
external var input : dynamic
external fun update_entities()

var tt = arrayOf(Test(line = true,check = 0))
class Test(line:Boolean,check:Int)


class MyResource(){
    fun myrest(){
        window.alert("Alert123")
    }

    companion object{
        fun mycomp(){
            window.alert("mycomp")

        }
    }
}
/**
 *
 */
@JsExport
fun ktupdate(){

    if(res.ready()){
        if(!game.initialized){
            game.set_volume(DEFAULT_VOLUME);
            input.init();// Only init inputs after everything is loaded.
            game.play_sound(0);
            game.initialized = true;
        }
    }
    if(!game.paused){
        if(game.mode == 0){
            game.wait_timer--;
            if(game.wait_timer <= 0){
                game.load_level(0);
            }
        }else if(game.mode == 1){
            if(game.wait_timer <= 0){
                if(game.level_ended == 0){
                    game.update_tick++;
                    update_entities();
                }else if(game.level_ended == 1){
                    game.update_savegame(game.level_number, game.steps_taken);
                    game.next_level();
                }else if(game.level_ended == 2){
                    game.reset_level();
                }
            }else{
                game.wait_timer--;
            }
        }
    }

    var now = Date.now();
    game.delta_updated = now.minus(game.last_updated as Double) ;
    game.last_updated = now;

    game.update_drawn = false;
}
