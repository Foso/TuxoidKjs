import App.Companion.DBX_CHARTS
import App.Companion.DBX_CHPASS
import App.Companion.DBX_CONFIRM
import App.Companion.DBX_LOAD
import App.Companion.DBX_LOADLVL
import App.Companion.DBX_SAVE
import App.Companion.DIR_DOWN
import App.Companion.DIR_LEFT
import App.Companion.DIR_NONE
import App.Companion.DIR_RIGHT
import App.Companion.DIR_UP
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.Element
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.TouchEvent
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import org.w3c.dom.get
import kotlin.js.Date
import kotlin.js.json
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.sqrt
import kotlin.properties.Delegates


/**
 *
//////////////////////////////////////////////////////////////////////////////////////////////////////
// INPUT CLASS
// Everything that has to do with keyboard and mouse input goes here
//////////////////////////////////////////////////////////////////////////////////////////////////////
 */
class MyInput(val game: KtGame, val MYCANVAS: HTMLCanvasElement) {
    var last_joystick_render by Delegates.notNull<Double>()
    var joystick_dir: Int = DIR_NONE

    val that = this


    fun init() {

        if (IS_TOUCH_DEVICE) {
            this.joystick_dir = DIR_NONE;
            this.last_joystick_render = Date.now();
        }
        // Handle keyboard controls (GLOBAL)
        document.addEventListener("keydown", { this.handle_keydown_global(it as KeyboardEvent) }, false);

        document.addEventListener("keyup", { handle_keyup_global(it as KeyboardEvent) }, false);

        // Handle mouse controls (GLOBAL)

        document.addEventListener("mousemove", { handle_mousemove_global(it as MouseEvent) }, false);

        document.addEventListener("mousedown", { handle_mousedown_global(it as MouseEvent) }, false);

        document.addEventListener("mouseup", { handle_mouseup_global(it as MouseEvent) }, false);

        // Handle touch events

        document.addEventListener("touchstart", { handle_touch_global(it as TouchEvent) }, false);

        document.addEventListener("touchmove", { handle_touch_global(it as TouchEvent) }, false);

        document.addEventListener("touchend", { handle_touchend_global(it as TouchEvent) }, false);

        // Handle mouse controls (MYCANVAS)
        App.MYCANVAS.addEventListener("mousemove", { handle_mousemove(it as MouseEvent) }, false);

        App.MYCANVAS.addEventListener("mousedown", { handle_mousedown(it as MouseEvent) }, false);

        App.MYCANVAS.addEventListener("mouseup", { handle_mouseup(it as MouseEvent) }, false);

        App.MYCANVAS.addEventListener("mouseout", { handle_mouseout(it as MouseEvent) }, false);
    }

    // Public:
    var keys_down = arrayOf<Boolean>();
    var mouse_pos = js("{x: 0, y: 0}");
    var mouse_pos_global: dynamic = js(" {x: 0, y: 0}");
    var mouse_lastclick = js(" {x: 0, y: 0}");
    var mouse_down = false;
    var lastclick_button = -1;
    var menu_pressed = -1;
    var lastklick_option: dynamic = null;
    val test = json(Pair("x", "Hallo"))

    fun handle_keydown_global(evt: KeyboardEvent) {
        game.remove_soundrestriction();
        keys_down[evt.keyCode] = true;
        if (keys_down[37]) {
            game.walk_dir = DIR_LEFT;
        } else if (keys_down[38]) {
            game.walk_dir = DIR_UP;
        } else if (keys_down[39]) {
            game.walk_dir = DIR_RIGHT;
        } else if (keys_down[40]) {
            game.walk_dir = DIR_DOWN;
        }

        if (vis.dbx.firstChild) {// If a dialog box is open
            if (keys_down[13]) {// Enter
                vis.dbx.enterfun();
            } else if (keys_down[27]) {// Esc
                vis.dbx.cancelfun();
            }
        }
    }

    fun handle_keyup_global(evt: KeyboardEvent) {
        keys_down[evt.keyCode] = false
        //js("delete keys_down[evt.keyCode];")
    }

    fun handle_mousemove_global(evt: MouseEvent) {

        mouse_pos_global = js("{x: evt.clientX, y: evt.clientY}");
        if (vis !== null && vis.dbx !== null && vis.dbx.style.display != "none") {
            if (vis.dbx.drag) {
                var temp_x = (mouse_pos_global.x - vis.dbx.drag_pos.x);
                var temp_y = (mouse_pos_global.y - vis.dbx.drag_pos.y);
                if (temp_x < 0) temp_x = 0.0;
                if (temp_y < 0) temp_y = 0.0;

                vis.dbx.style.left = temp_x;
                vis.dbx.style.top = temp_y;
            }
        }
    }

    fun handle_mousedown_global(evt: MouseEvent) {
        game.remove_soundrestriction();
        mouse_down = true;
        if (vis !== null && vis.dbx !== null && vis.dbx.style.display != "none") {
            val x1 = mouse_pos_global.x - vis.dbx.style.left as Int
            val y1 = mouse_pos_global.y - vis.dbx.style.top as Int
            var rel_pos = js(" {x: x1, y: y1}");

            if (rel_pos.x > 0 && rel_pos.x < vis.dbx.style.width as Int && rel_pos.y > 0 && rel_pos.y < 20) {
                evt.preventDefault();// Prevents from selecting the canvas
                vis.dbx.drag = true;
                vis.dbx.drag_pos = rel_pos;
            }
        }
    }


    fun handle_mouseup_global(evt: MouseEvent) {
        mouse_down = false;
        if (vis !== null && vis.dbx !== null && vis.dbx.style.display != "none") {
            vis.dbx.drag = false;
        }
    }

    fun handle_mousemove(evt: MouseEvent) {
        var rect = MYCANVAS.getBoundingClientRect();
        var style = window.getComputedStyle(MYCANVAS);


        val x1 = round(
            (evt.clientX - rect.left.toDouble() - style.getPropertyValue("border-left-width").replace("px", "")
                .toDouble()) / true_width as Double * MYCANVAS.width.toDouble()
        )
        val y1 = round(
            (evt.clientY - rect.top - style.getPropertyValue("border-top-width").replace("px", "")
                .toInt()) / true_height * MYCANVAS.height
        )
        // console.log("HIER   " + x1)

        mouse_pos = js("{x: x1, y: y1}");


        if (lastclick_button == 3) {
            game.set_volume((mouse_pos.x - vis.vol_bar.offset_x) / vis.vol_bar.width);
        }

        if (menu_pressed == 0) {
            calc_opened(vis.menu1, mouse_pos.x, mouse_pos.y);
        }
    }

    fun handle_mousedown(evt: MouseEvent) {
        evt.preventDefault();// Prevents from selecting the canvas
        val x1 = mouse_pos.x
        val y1 = mouse_pos.y
        mouse_lastclick = js("{x: x1, y: y1}")

        if (mouse_pos.y >= 35 && mouse_pos.y <= 65) {
            if (mouse_pos.x >= 219 && mouse_pos.x <= 249) {
                lastclick_button = 0;
            } else if (mouse_pos.x >= 253 && mouse_pos.x <= 283) {
                lastclick_button = 1;
            } else if (mouse_pos.x >= 287 && mouse_pos.x <= 317) {
                lastclick_button = 2;
            }
        }
        if (mouse_pos.x >= vis.vol_bar.offset_x && mouse_pos.y >= vis.vol_bar.offset_y &&
            mouse_pos.x <= vis.vol_bar.offset_x + vis.vol_bar.width && mouse_pos.y <= vis.vol_bar.offset_y + vis.vol_bar.height
        ) {
            game.set_volume((mouse_pos.x - vis.vol_bar.offset_x) / vis.vol_bar.width);
            lastclick_button = 3;
        }

        if (mouse_pos.x >= vis.menu1.offset_x && mouse_pos.x <= vis.menu1.offset_x + vis.menu1.width &&
            mouse_pos.y >= vis.menu1.offset_y && mouse_pos.y <= vis.menu1.offset_y + vis.menu1.height
        ) {
            if (menu_pressed == -1) {
                menu_pressed = 0;
                calc_opened(vis.menu1, mouse_pos.x, mouse_pos.y);
            } else {
                menu_pressed = -1;
                vis.menu1.submenu_open = -1;
            }
        } else {
            var menubutton_pressed = false;

            if (menu_pressed != -1) {
                lastklick_option = calc_option(vis.menu1, mouse_pos.x, mouse_pos.y);
                if (lastklick_option !== null) {
                    menubutton_pressed = true;
                }
            }

            if (!menubutton_pressed) {
                menu_pressed = -1;
                vis.menu1.submenu_open = -1;
            }
        }
    }

    fun handle_mouseup(evt: MouseEvent) {
        if (mouse_pos.y >= 35 && mouse_pos.y <= 65) {
            if (mouse_pos.x >= 219 && mouse_pos.x <= 249 && lastclick_button == 0 && game.buttons_activated[0]) {
                //alert("<<");
                game.prev_level();
            } else if (mouse_pos.x >= 253 && mouse_pos.x <= 283 && lastclick_button == 1 && game.buttons_activated[1]) {
                //alert("Berti");
                game.reset_level();
            } else if (mouse_pos.x >= 287 && mouse_pos.x <= 317 && lastclick_button == 2 && game.buttons_activated[2]) {
                //alert(">>");
                game.next_level();
            }
        }

        if (menu_pressed == 0 && lastklick_option !== null && !lastklick_option.line) {
            var up_option = calc_option(vis.menu1, mouse_pos.x, mouse_pos.y);
            if (up_option === lastklick_option && lastklick_option.on()) {
                when (lastklick_option.effect_id) {
                    0 -> {
                        if (game.savegame.progressed) {
                            vis.open_dbx(DBX_CONFIRM, 0);
                        } else {
                            game.clear_savegame();
                        }
                    }
                    1 -> {
                        if (game.savegame.progressed) {
                            vis.open_dbx(DBX_CONFIRM, 1);
                        } else {
                            vis.open_dbx(DBX_LOAD);
                        }
                    }
                    2 -> {
                        if (game.savegame.username !== null) {
                            game.store_savegame();
                        } else {
                            vis.open_dbx(DBX_SAVE);
                        }
                    }
                    3 -> {
                        game.toggle_paused();
                    }
                    4 -> {
                        game.toggle_single_steps();
                    }
                    5 -> {
                        game.toggle_sound();
                    }
                    6 -> {
                        vis.open_dbx(DBX_LOADLVL);
                    }
                    7 -> {
                        vis.open_dbx(DBX_CHPASS);
                    }
                    8 -> {
                        vis.open_dbx(DBX_CHARTS);
                    }
                }
                menu_pressed = -1;
                vis.menu1.submenu_open = -1;
            }

        }
        lastclick_button = -1;
        lastklick_option = null;

    }

    fun handle_mouseout(evt: dynamic) {

    }


    fun calc_opened(a_menu: dynamic, mouse_x: dynamic, mouse_y: dynamic) {
        if (mouse_y < a_menu.offset_y || mouse_y > a_menu.offset_y + a_menu.height) {
            return;
        }
        if (mouse_x < a_menu.offset_x || mouse_x > a_menu.offset_x + a_menu.width) {
            return;
        }

        var submenu_offset = 0;

        for (i in 0 until a_menu.submenu_list.length) {
            submenu_offset += a_menu.submenu_list[i].width as Int;
            if (mouse_x < a_menu.offset_x + submenu_offset) {
                a_menu.submenu_open = i;
                return;
            }
        }
    }

    fun calc_option(a_menu: dynamic, mouse_x: dynamic, mouse_y: dynamic): dynamic {
        if (a_menu.submenu_open != -1) {
            var submenu_offset = 0;
            for (i in 0 until a_menu.submenu_list.length) {
                var sm = a_menu.submenu_list[i];
                if (i == a_menu.submenu_open) {
                    var option_offset = a_menu.offset_y + a_menu.height + 4;
                    for (j in 0 until sm.options.length) {
                        var next_offset: Int;
                        if (sm.options[j].line) {
                            next_offset = option_offset + sm.offset_line;
                        } else {
                            next_offset = option_offset + sm.offset_text;
                        }
                        if (mouse_x > a_menu.offset_x + submenu_offset && mouse_x < a_menu.offset_x + submenu_offset + sm.dd_width &&
                            mouse_y > option_offset && mouse_y < next_offset
                        ) {
                            return sm.options[j];
                        }

                        option_offset = next_offset;
                    }
                }
                submenu_offset += sm.width as Int;
            }
        }
        return null
    }

    fun handle_touch_global(evt: TouchEvent) {
        game.remove_soundrestriction();
        //evt.preventDefault();
        var touches = evt.changedTouches;
        var rect = (MyJOYSTICK as HTMLCanvasElement).getBoundingClientRect();
        var style = window.getComputedStyle(MyJOYSTICK as Element);

        var changed = false;

        var mid_x = MyJOYSTICK.width / 2;
        var mid_y = MyJOYSTICK.height / 2;

        for (i in 0 until touches.length) {

            var x = round(
                touches[i]?.clientX!!.toDouble() - rect.left as Double - style.getPropertyValue("border-left-width")
                    .toDouble()
            );
            var y = round(
                touches[i]?.clientY!!.toDouble() - rect.top as Double - style.getPropertyValue("border-top-width")
                    .toDouble()
            );

            if (x >= 0 && x <= MyJOYSTICK.width && y >= 0 && y <= MyJOYSTICK.height) {
                if (x >= y) {
                    if (-x + MyJOYSTICK.height >= y) {
                        joystick_dir = DIR_UP;
                        changed = true;
                    } else {
                        joystick_dir = DIR_RIGHT;
                        changed = true;
                    }
                } else {
                    if (-x + MyJOYSTICK.width >= y) {
                        joystick_dir = DIR_LEFT;
                        changed = true;
                    } else {
                        joystick_dir = DIR_DOWN;
                        changed = true;
                    }
                }
            }

            if (Date.now() - last_joystick_render > 15) {
                render_joystick(x, y);
                last_joystick_render = Date.now();
            }

        }

        if (!changed) {
            render_joystick();
            joystick_dir = DIR_NONE;
        }
    }

    fun handle_touchend_global(evt: dynamic) {
        render_joystick();
        joystick_dir = DIR_NONE;
    }


}

fun render_joystick(x: Double? = null, y: Double? = null) {
    var newX = x
    var newY = y
    val mid_x = MyJOYSTICK.width / 2;
    val mid_y = MyJOYSTICK.height / 2;

    MYJOYCTX.clearRect(0.0, 0.0, MyJOYSTICK.width.toDouble(), MyJOYSTICK.height.toDouble());
    MYJOYCTX.globalAlpha = 0.5;// Set joystick half-opaque (1 = opaque, 0 = fully transparent)
    MYJOYCTX.beginPath();
    MYJOYCTX.arc(mid_x.toDouble(), mid_y.toDouble(), (MyJOYSTICK.width / 4 + 10).toDouble(), 0.0, 2 * PI);
    MYJOYCTX.stroke();

    if (newX != null && newY != null) {
        var dist = sqrt((newX - mid_x).pow(2.0) + (newY - mid_y).pow(2.0));
        if (dist > MyJOYSTICK.width / 4) {
            newX = mid_x + (newX - mid_x) / dist * MyJOYSTICK.width / 4;
            newY = mid_y + (newY - mid_y) / dist * MyJOYSTICK.width / 4;
        }
        MYJOYCTX.beginPath();
        MYJOYCTX.arc(newX, newY, 10.0, 0.0, 2 * PI, false);// a circle at the start
        MYJOYCTX.fillStyle = "red";
        MYJOYCTX.fill();
    }
}