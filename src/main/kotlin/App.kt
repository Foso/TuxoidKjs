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

class App() {

    val vol_bar = VolumeBar()
    val res = MyRes()
    val game = KtGame(vol_bar, res)
    val input = MyInput(game, MYCANVAS)

    val vis = KtVisual(vol_bar, game)


    init {

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
    fun render(game: KtGame) {
        fun render_fps() {
            var now = Date.now();

            if (now - vis.fps_delay >= 250) {
                var delta_rendered = now - vis.last_rendered;
                vis.apply {
                    static_ups = ((1000 / game.delta_updated).toFixed(2));
                    static_fps = ((1000 / delta_rendered).toFixed(2));

                    fps_delay = now;
                }
            }

            MYCTX.apply {
                fillStyle = "rgb(255, 0, 0)";
                font = "12px Helvetica";
                textAlign = CanvasTextAlign.RIGHT;
                textBaseline = CanvasTextBaseline.BOTTOM;
                fillText(
                    "UPS: " + vis.static_ups + " FPS:" + vis.static_fps + " ", App.SCREEN_WIDTH.toDouble(),
                    App.SCREEN_HEIGHT.toDouble()
                );
            }

            vis.last_rendered = now;
        };

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


        fun render_displays(MYCTX: CanvasRenderingContext2D, res: MyRes) {

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

        game.now = Date.now();
        val elapsed = game.now - game.then;
        if (elapsed > game.fpsInterval) {
            game.then = game.now - (elapsed % game.fpsInterval);
            if (res.ready()) {
                if (!game.initialized) {
                    game.set_volume(DEFAULT_VOLUME);
                    input.init();// Only init inputs after everything is loaded.
                    game.play_sound(0);
                    game.initialized = true;
                }
            }
            if (!game.paused) {
                if (game.mode == GAME_MODE_ENTRY) {
                    game.wait_timer--;
                    if (game.wait_timer <= 0) {
                        game.load_level(3);
                    }
                } else if (game.mode == 1) {
                    if (game.wait_timer <= 0) {
                        when (game.level_ended) {
                            GAME_MODE_ENTRY -> {
                                game.update_tick++;
                                kt_update_entities();
                            }
                            GAME_MODE_MENU -> {
                                game.update_savegame(game.level_number, game.steps_taken);
                                game.next_level();
                            }
                            GAME_MODE_PLAY -> {
                                game.reset_level();
                            }
                        }
                    } else {
                        game.wait_timer--;
                    }
                }
            }
            var now = Date.now()
            game.delta_updated = now.minus(game.last_updated as Double)
            game.last_updated = now
            game.update_drawn = false
        }

        if (game.update_drawn) {// This prevents the game from rendering the same thing twice
            window.requestAnimationFrame { render(game) };
            return;
        }

        game.update_drawn = true;
        if (res.ready()) {
            MYCTX.apply {
                drawImage(res.images[0], 0.0, 0.0);// Background
                drawImage(res.images[9], 22.0, 41.0);// Steps
                drawImage(res.images[10], 427.0, 41.0);// Ladder
            }
            render_displays(MYCTX,res);
            kt_render_buttons();

            when (game.mode) {
                GAME_MODE_ENTRY -> {// Title image

                    MYCTX.apply {
                        drawImage(res.images[1], (LEV_OFFSET_X + 4).toDouble(), (LEV_OFFSET_Y + 4).toDouble());
                        fillStyle = "rgb(0, 0, 0)";
                        font = "bold 12px Helvetica";
                        textAlign = CanvasTextAlign.LEFT;
                        textBaseline = CanvasTextBaseline.BOTTOM;
                        fillText("JavaScript remake by ", 140.0, 234.0);
                    }
                }
                GAME_MODE_MENU -> {
                    render_field();
                }
                GAME_MODE_PLAY -> {// Won!
                    MYCTX.drawImage(res.images[170], LEV_OFFSET_X + 4, LEV_OFFSET_Y + 4);
                }
            }
            render_vol_bar();
            kt_render_menu();
        } else {
            MYCTX.apply {
                fillStyle = "rgb(" + vis.light_grey.r + ", " + vis.light_grey.g + ", " + vis.light_grey.b + ")";
                fillRect(0, 0, App.SCREEN_WIDTH, App.SCREEN_HEIGHT);// Options box
                fillStyle = "rgb(0, 0, 0)";
                font = "36px Helvetica";
                textAlign = CanvasTextAlign.CENTER
                textBaseline = CanvasTextBaseline.MIDDLE;
                fillText("Loading...", (App.SCREEN_WIDTH / 2).toDouble(), (App.SCREEN_HEIGHT / 2).toDouble());
            }
        }
        if (DEBUG) render_fps();
        window.requestAnimationFrame { render(game) };
        // js("")
    }

    companion object {

        var SCREEN_WIDTH = 537;
        var SCREEN_HEIGHT = 408;
        var DEFAULT_VOLUME = 0.7;
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
        lateinit var MYCANVAS: HTMLCanvasElement

        fun initCanvas() {
            MYCANVAS = document.createElement("canvas") as HTMLCanvasElement;
            MYCTX = MYCANVAS.getContext("2d") as CanvasRenderingContext2D;
            MYCANVAS.apply {
                width = App.SCREEN_WIDTH;
                height = App.SCREEN_HEIGHT;
                className = "canv";
            }
            document.body?.appendChild(MYCANVAS);
        }
    }
}