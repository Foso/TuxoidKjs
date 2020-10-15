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

external var vis: KtVisual
external var DIR_LEFT: Int
external var DIR_UP: Int
external var DIR_DOWN: Int

external var DIR_RIGHT: Int
external var DIR_NONE: Int
external var LEV_START_DELAY: Int
external var UPS: Int

external var res: MyRes
external var game: KtGame
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
external var input: MyInput
external fun update_entities()



external interface Tile {
    val x: Int
    val y: Int
}



external interface Rgb {
    val r: dynamic
    val g: dynamic
    val b: dynamic
}

@JsExport
fun kt_update_entities(){
    var tick = (game.update_tick*60/UPS);
    var synced_move = tick % (12/game.move_speed) //==0 ?

    // The player moves first at all times to ensure the best response time and remove directional quirks.

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







