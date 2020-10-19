import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.BOTTOM
import org.w3c.dom.CENTER
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.LEFT
import org.w3c.dom.MIDDLE
import org.w3c.dom.RIGHT
import kotlin.js.Date
import kotlin.math.min
import kotlin.math.round

external var md5: dynamic

lateinit var res: MyRes

var vis: KtVisual = KtVisual()
lateinit var game: KtGame
var ERR_SUCCESS = 0
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


var DIR_NONE = -1;
var DIR_UP = 0;
var DIR_LEFT = 1;
var DIR_DOWN = 2;
var DIR_RIGHT = 3;
var LEV_START_DELAY = 1
var UPS: Int = 60

var AUTHOR = "Benjamin";
var JOYSTICK_SIZE = 0.4;// In terms of the smaller of the two screen dimensions

var SCREEN_WIDTH = 537;
var SCREEN_HEIGHT = 408;
var LEV_DIMENSION_X = 21;
var LEV_DIMENSION_Y = 13;
var RENDER_FULL = 0;
var RENDER_TOP = 1;
var RENDER_BOTTOM = 2;
var RENDER_BOTTOM_BORDER = 3;
var LEV_OFFSET_X = 16;
var LEV_OFFSET_Y = 79;

var LEV_STOP_DELAY: Int = 1
var ANIMATION_DURATION: Int =
    round((8.toDouble() * UPS / 60)).toInt();// How many times the game has to render before the image changes

lateinit var MYJOYCTX: CanvasRenderingContext2D

var DEFAULT_VOLUME = 0.7;
lateinit var input: MyInput
lateinit var MYCANVAS: HTMLCanvasElement
lateinit var MYCTX: CanvasRenderingContext2D
var IS_TOUCH_DEVICE: Boolean = false
var true_width: Double = SCREEN_WIDTH.toDouble()
var true_height: Double = SCREEN_HEIGHT.toDouble()
lateinit var MyJOYSTICK: HTMLCanvasElement


fun initCanvas() {
    MYCANVAS = document.createElement("canvas") as HTMLCanvasElement;
    MYCTX = MYCANVAS.getContext("2d") as CanvasRenderingContext2D;
    MYCANVAS.width = SCREEN_WIDTH;
    MYCANVAS.height = SCREEN_HEIGHT;
    MYCANVAS.className = "canv";
    document.body?.appendChild(MYCANVAS);
}

class Application(){
        val res = MyRes()
}

fun main() {
    val app = Application()
    res = app.res
    vis.init_menus()
    requestAnimationFrame()
    initInput()
    initCanvas()
    checkIfTouch()
    if (IS_TOUCH_DEVICE) {
        UPS = 15
        initTouch()
    }
    game = KtGame()

}

fun requestAnimationFrame() {
    var lastTime = 0;
    js(
        "  \n" +
                "    var vendors = ['ms', 'moz', 'webkit', 'o'];\n" +
                "    for(var x = 0; x < vendors.length && !window.requestAnimationFrame; ++x) {\n" +
                "        window.requestAnimationFrame = window[vendors[x]+'RequestAnimationFrame'];\n" +
                "        window.cancelAnimationFrame = window[vendors[x]+'CancelAnimationFrame']\n" +
                "                                   || window[vendors[x]+'CancelRequestAnimationFrame'];\n" +
                "    }\n" +
                " \n" +
                "    if (!window.requestAnimationFrame)\n" +
                "        window.requestAnimationFrame = function(callback, element) {\n" +
                "            var currTime = new Date().getTime();\n" +
                "            var timeToCall = Math.max(0, 16 - (currTime - lastTime));\n" +
                "            var id = window.setTimeout(function() { callback(currTime + timeToCall); },\n" +
                "              timeToCall);\n" +
                "            lastTime = currTime + timeToCall;\n" +
                "            return id;\n" +
                "        };\n" +
                " \n" +
                "    if (!window.cancelAnimationFrame)\n" +
                "        window.cancelAnimationFrame = function(id) {\n" +
                "            clearTimeout(id);\n" +
                "        };"
    )

}

fun initTouch() {
    // Joystick creation
    MyJOYSTICK = document.createElement("canvas") as HTMLCanvasElement;
    MYJOYCTX = MyJOYSTICK.getContext("2d") as CanvasRenderingContext2D;
    var mindim = min(window.innerWidth, window.innerHeight);
    MyJOYSTICK.width = (mindim * JOYSTICK_SIZE).toInt();
    MyJOYSTICK.height = (mindim * JOYSTICK_SIZE).toInt();
    MyJOYSTICK.className = "joystick";
    document.body?.appendChild(MyJOYSTICK);

    window.onresize = {// On mobile, make game fullscreen
        val ratio_1 = window.innerWidth / true_width;
        val ratio_2 = window.innerHeight / true_height;
        if (ratio_1 < ratio_2) {
            MYCANVAS.style.height = "";
            MYCANVAS.style.width = "100%";
        } else {
            MYCANVAS.style.height = "100%";
            MYCANVAS.style.width = "";
        }

        var rect = MYCANVAS.getBoundingClientRect();
        var style = window.getComputedStyle(MYCANVAS);
        true_width = rect.width + style.getPropertyValue("border-left-width")
            .toInt() + style.getPropertyValue("border-right-width")
            .toInt();
        true_height = rect.height + style.getPropertyValue("border-top-width")
            .toInt() + style.getPropertyValue("border-bottom-width")
            .toInt();


        var mindim = min(window.innerWidth, window.innerHeight);
        MyJOYSTICK.width = (mindim * JOYSTICK_SIZE).toInt();
        MyJOYSTICK.height = (mindim * JOYSTICK_SIZE).toInt();

        render_joystick();

    };
    window.onresize = {}
}

fun checkIfTouch() {
    IS_TOUCH_DEVICE =
        true == js("(\"ontouchstart\" in window || window.DocumentTouch && document instanceof DocumentTouch);")

}

fun Double.toFixed(digits: Int): Double = this.asDynamic().toFixed(digits)
fun Float.format(digits: Int): String = this.asDynamic().toFixed(digits)

fun render_fps() {
    var now = Date.now();

    if (now - vis.fps_delay >= 250) {
        var delta_rendered = now - vis.last_rendered;
        vis.static_ups = ((1000 / game.delta_updated).toFixed(2));
        vis.static_fps = ((1000 / delta_rendered).toFixed(2));

        vis.fps_delay = now;
    }

    MYCTX.fillStyle = "rgb(255, 0, 0)";
    MYCTX.font = "12px Helvetica";
    MYCTX.textAlign = CanvasTextAlign.RIGHT;
    MYCTX.textBaseline = CanvasTextBaseline.BOTTOM;
    MYCTX.fillText(
        "UPS: " + vis.static_ups + " FPS:" + vis.static_fps + " ", SCREEN_WIDTH.toDouble(),
        SCREEN_HEIGHT.toDouble()
    );

    vis.last_rendered = now;
};


fun initInput() {
    input = MyInput()
}

fun render() {
    game.now = Date.now();
    var elapsed = game.now - game.then;
    if (elapsed > game.fpsInterval) {
        game.then = game.now - (elapsed % game.fpsInterval);
        ktupdate()
    }

    if (game.update_drawn) {// This prevents the game from rendering the same thing twice
        window.requestAnimationFrame { render() };
        return;
    }

    game.update_drawn = true;
    if (res.ready()) {
        MYCTX.drawImage(res.images[0], 0.0, 0.0);// Background
        MYCTX.drawImage(res.images[9], 22.0, 41.0);// Steps
        MYCTX.drawImage(res.images[10], 427.0, 41.0);// Ladder
        render_displays();
        kt_render_buttons();

        if (game.mode == 0) {// Title image
            MYCTX.drawImage(res.images[1], (LEV_OFFSET_X + 4).toDouble(), (LEV_OFFSET_Y + 4).toDouble());
            MYCTX.fillStyle = "rgb(0, 0, 0)";
            MYCTX.font = "bold 12px Helvetica";
            MYCTX.textAlign = CanvasTextAlign.LEFT;
            MYCTX.textBaseline = CanvasTextBaseline.BOTTOM;
            MYCTX.fillText("JavaScript remake by ", 140.0, 234.0);
        } else if (game.mode == 1) {
            render_field();
        } else if (game.mode == 2) {// Won!
            MYCTX.drawImage(res.images[170], LEV_OFFSET_X + 4, LEV_OFFSET_Y + 4);
        }
        render_vol_bar();
        kt_render_menu();
    } else {
        MYCTX.fillStyle = "rgb(" + vis.light_grey.r + ", " + vis.light_grey.g + ", " + vis.light_grey.b + ")";
        MYCTX.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);// Options box
        MYCTX.fillStyle = "rgb(0, 0, 0)";
        MYCTX.font = "36px Helvetica";
        MYCTX.textAlign = CanvasTextAlign.CENTER
        MYCTX.textBaseline = CanvasTextBaseline.MIDDLE;
        MYCTX.fillText("Loading...", (SCREEN_WIDTH / 2).toDouble(), (SCREEN_HEIGHT / 2).toDouble());
    }
    if (DEBUG) render_fps();
    window.requestAnimationFrame({ render() });
    // js("")
}

fun CanvasRenderingContext2D.fillRect(x: Int, y: Int, w: Int, h: Int) {
    fillRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
}

fun CanvasRenderingContext2D.drawImage(image: dynamic, dx: Int, dy: Int) {
    drawImage(image, dx.toDouble(), dy.toDouble())
}


@JsExport
fun render_displays() {

    val steps_string = game.steps_taken.toString();
    val steps_length = min(steps_string.length - 1, 4);

    for (i in steps_length downTo 0) {
        val imageId = js("11+parseInt(steps_string.charAt(i))")
        MYCTX.drawImage(res.images[imageId], (101 - (steps_length - i) * 13).toDouble(), 41.0);
    }

    for (i in (steps_length + 1) until 5) {
        MYCTX.drawImage(res.images[21], (101 - i * 13).toDouble(), 41.0);
    }

    val level_string = game.level_number.toString();
    val level_length = min(level_string.length - 1, 4);

    for (i in level_length downTo 0) {
        val imageId = js("11+parseInt(level_string.charAt(i))")

        MYCTX.drawImage(res.images[imageId], (506 - (level_length - i) * 13).toDouble(), 41.0);
    }

    for (i in (level_length + 1) until 5) {
        MYCTX.drawImage(res.images[21], 506 - i * 13, 41);
    }

}


class Tile(
    val x: Int,
    val y: Int
) {

}


@JsExport
fun kt_update_entities() {
    var tick = (game.update_tick * 60 / UPS);
    var synced_move = (tick % (12 / game.move_speed)) == 0.0

    // The player moves first at all times to ensure the best response time and remove directional quirks.
// The player moves first at all times to ensure the best response time and remove directional quirks.
    for (position in game.berti_positions) {
        game.level_array[position.x][position.y].register_input(position.x, position.y, !synced_move);
    }

    if (synced_move) {
        // NPC logic and stop walking logic.
        for (y in 0 until LEV_DIMENSION_Y) {
            for (x in 0 until LEV_DIMENSION_X) {
                if (game.level_array[x][y].id == 2) {// MENU Berti
                    game.level_array[x][y].move_randomly(x, y);
                } else if (game.level_array[x][y].id == 7 || game.level_array[x][y].id == 10) {// Purple and green monster
                    game.level_array[x][y].chase_berti(x, y);
                }

                if (game.level_array[x][y].just_moved) {
                    game.level_array[x][y].just_moved = false;
                    vis.update_animation(x, y);
                }
            }
        }
    }

    // After calculating who moves where, the entities actually get updated.
    for (y in 0 until LEV_DIMENSION_Y) {
        for (x in 0 until LEV_DIMENSION_X) {
            game.level_array[x][y].update_entity(x, y);
        }
    }

    // Gameover condition check.
    for (position in game.berti_positions) {
        game.level_array[position.x][position.y].check_enemy_proximity(position.x, position.y);
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
                when (game.level_ended) {
                    0 -> {
                        game.update_tick++;
                        kt_update_entities();
                    }
                    1 -> {
                        game.update_savegame(game.level_number, game.steps_taken);
                        game.next_level();
                    }
                    2 -> {
                        game.reset_level();
                    }
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







