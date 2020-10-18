import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.events.MouseEvent
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round
import kotlin.random.Random



@JsExport
class KtVisual() {

    var berti_blink_time = 0;
    var last_rendered = 0;
    var fps_delay = 0;
    var static_ups = 0;
    var static_fps = 0;

    var buttons_pressed = arrayOf<dynamic>();



// Animations:
    var offset_key_x = 3;
    var offset_key_y = 4
    var offset_banana_x = 4;
    var offset_banana_y = 4;

    var offset_wow_x = -20;
    var offset_wow_y = -44;

    var offset_yeah_x = -20;
    var offset_yeah_y = -44;

    var offset_argl_x = -20;
    var offset_argl_y = -44;


    var vol_bar = VolumeBar()

    lateinit var menu1 : Menu

    // Menu stuff:
    var black = js("{r:0, g:0, b: 0}");
    var dark_grey = js("{r:64, g:64, b:64}");
    var med_grey = js("{r:128, g:128, b:128}");
    var light_grey = js("{r:212, g:208, b:200}");
    var white = js("{r:255, g:255, b: 255}");
    var blue = js("{r:10, g:36, b:106}");
    var dbx = document.createElement("div").asDynamic();

    fun HAS_STORAGE(): Boolean {
        return true
    }

    fun error_dbx(errno: Int) {
        if (dbx.errfield === null) return;
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
        dbx.errfield.innerHTML = err_string;
    }
    fun init_menus(){

        val arr_options1 = arrayOf(
            Option(false, 0, "New", "F2", 0, { true }),
            Option(false, 0, "Load Game...","", 1, { HAS_STORAGE() }),
            Option(false, 0, "Save","", 2, { game.savegame.progressed && HAS_STORAGE()}),
            Option(false, 1, "Pause", "", 3, { true })
        )

        val arr_options2 = arrayOf(
            Option(false, 1, "Single steps", "F5", 4, { true }),
            Option(false, 1, "Sound", "", 5, { true }),
            Option(true, 0, "", "", -1, { true }),
            Option(false, 0, "Load Level", "", 6, { HAS_STORAGE()}),
            Option(false, 0, "Change Password", "", 7, {game.savegame.username !== null && HAS_STORAGE()}),
            Option(true, 0, "", "", -1, { true }),
            Option(
                false, 0, "Charts", "", 8, { HAS_STORAGE()})
            )

        val sub_m1 = SubMenu(43, 100, "Game", arr_options1);
        val sub_m2 = SubMenu(55, 150, "Options", arr_options2);


        menu1 = Menu(1, 2, 17, arrayOf(sub_m1, sub_m2));

    }

    init {
        buttons_pressed[2] == false
        buttons_pressed[1] == false
        buttons_pressed[0] == false

        dbx.style.position = "fixed";
        dbx.style.zIndex = 100;
        dbx.style.display = "none";
        document.body?.appendChild(dbx);
        dbx.drag_pos = js("{x:0, y:0}");
        dbx.drag = false;
        dbx.arr_btn = arrayOf<dynamic>();
        dbx.arr_input = arrayOf<dynamic>();
        dbx.lvlselect = null;
        dbx.errfield = null;
    }

    fun open_dbx(dbx_id: dynamic, opt: Int? = 0) {
        close_dbx();
        when (dbx_id) {
            DBX_CONFIRM -> {
                dbx_confirm(opt)
            }
            DBX_SAVE -> {
                dbx_save(opt)
            }
            DBX_LOAD -> {
                dbx_load()
            }
            DBX_CHPASS -> {
                dbx_chpass();
            }
            DBX_LOADLVL -> {
                dbx_loadlvl()
            }
            DBX_CHARTS -> {
                dbx_charts()
            }
        }

        dbx.style.display = "inline";

        if (dbx.arr_input[0]) {
            dbx.arr_input[0].focus();
        }
    }

    fun close_dbx() {
        dbx.style.display = "none";

        // IMPORTANT MEMORY LEAK PREVENTION
        for (i in (dbx.arr_btn.length as Int - 1) downTo 0) {
            dbx.arr_btn[i].pressed = null;
            dbx.arr_btn[i].onmousedown = null;
            dbx.arr_btn[i].onmouseup = null;
            dbx.arr_btn[i].onmouseout = null;
            dbx.arr_btn[i].onmouseover = null;
            dbx.arr_btn[i].onclick = null;
            dbx.arr_btn[i] = null;
            dbx.enterfun = null;
            dbx.cancelfun = null;
        }

        dbx.arr_btn = arrayOf<dynamic>();

        for (i in (dbx.arr_btn.length as Int - 1) downTo 0) {
            dbx.arr_input[i] = null;
        }

        dbx.arr_input = arrayOf<dynamic>();

        dbx.lvlselect = null;
        dbx.errfield = null;

        dbx.enterfun = null;
        dbx.cancelfun = null;

        while (dbx.firstChild) {
        dbx.removeChild(dbx.firstChild);
        }
    }

    fun dbx_save(opt: dynamic) {
        add_title("Save game");

        dbx.style.width = "256px";
        dbx.style.height = "213px";
        dbx.style.left = js("Math.max(Math.floor(window.innerWidth-256)/2, 0)+\"px\";")
        dbx.style.top = js("Math.max(Math.floor(window.innerHeight-213)/2, 0)+\"px\";")
        dbx.style.background = "url(" + res.images[174].src + ')';

        add_text("Player name:", 20, 35);
        add_input(100, 35, 120, 15, "text");
        add_text("Password:", 20, 60);
        add_input(100, 60, 120, 15, "password");


        var f_o: () -> Unit = {};
        var f_c: () -> Unit = {};

        if (opt == 0) {// "Save game"
            f_o = {
                if (game.dbxcall_save(dbx.arr_input[0].value, dbx.arr_input[1].value)) {
                    close_dbx();
                }
            };
            f_c = { close_dbx(); };
        } else if (opt == 1) {// "New Game" -> yes, save
            f_o = {
                if (game.dbxcall_save(dbx.arr_input[0].value, dbx.arr_input[1].value)) {
                    game.clear_savegame();close_dbx();
                }
            };
            f_c = { game.clear_savegame();close_dbx(); };
        } else if (opt == 2) {// "Load Game" -> yes, save
            f_o = {
                if (game.dbxcall_save(dbx.arr_input[0].value, dbx.arr_input[1].value)) {
                    open_dbx(DBX_LOAD);
                }
            };
            f_c = { open_dbx(DBX_LOAD); };
        }

        dbx.enterfun = f_o;
        dbx.cancelfun = f_c;

        add_button(181, 40, 160, f_o);// ok
        add_button(177, 160, 160, f_c);// cancel

        add_errfield(20, 85);
    }


    fun dbx_charts() {
        game.play_sound(4);

        add_title("Charts");

        dbx.style.width = "322px";
        dbx.style.height = "346px";
        dbx.style.left = js(" Math.max(Math.floor(window.innerWidth-322)/2, 0)+\"px\";")
        dbx.style.top = js(" Math.max(Math.floor(window.innerHeight-346)/2, 0)+\"px\";")
        dbx.style.background = "url(" + res.images[176].src + ')';

        var uc = localStorage.getItem("user_count")?.toInt() ?: 0;
        var user_arr = arrayListOf<dynamic>();

        for (i in 0 until uc) {
            var prefix = "player" + i + "_";
            var rl = localStorage.getItem(prefix + "reached_level")?.toInt() ?: 0;
            var st = 0;
            for (j in 1 until rl) {
                st += localStorage.getItem(prefix + "steps_lv" + j)?.toInt() ?: 0;
            }
            user_arr.add(js("{name: localStorage.getItem(prefix+\"username\"), reached: rl, steps: st}"))
        }

        //SORT

        add_text("rank", 21, 37);
        add_text("level", 57, 37);
        add_text("steps", 100, 37);
        add_text("name", 150, 37);

        for (i in 0 until uc) {
            add_number((i + 1), 20, 65 + 18 * i, 20, 20);
            add_number(user_arr[i].reached, 50, 65 + 18 * i, 30, 20);
            add_number(user_arr[i].steps, 95, 65 + 18 * i, 40, 20);
            add_text(user_arr[i].name, 155, 65 + 18 * i);
        }
        var f_o = { close_dbx(); };

        dbx.enterfun = f_o;
        dbx.cancelfun = f_o;

        add_button(181, 125, 300, f_o);// okay


    }


    fun dbx_loadlvl() {
        add_title("Load level");

        dbx.style.width = "197px";
        dbx.style.height = "273px";
        this.dbx.style.left = js(" Math.max(Math.floor(window.innerWidth-197)/2, 0)+\"px\";")
        this.dbx.style.top = js(" Math.max(Math.floor(window.innerHeight-273)/2, 0)+\"px\";")
        dbx.style.background = "url(" + res.images[175].src + ')';

        add_lvlselect(20, 80, 158, 109);

        var f_o = {
            if (dbx.lvlselect.value > 0) {
                game.load_level(dbx.lvlselect.value);
                close_dbx();
            }
        };
        var f_c = { close_dbx(); };

        dbx.enterfun = f_o;
        dbx.cancelfun = f_c;

        add_button(181, 25, 220, f_o);// ok
        add_button(177, 105, 220, f_c);// cancel

        add_text("Player name:", 20, 30);
        if (game.savegame.username == null) {
            add_text("- none -", 100, 30);
        } else {
            add_text(game.savegame.username, 100, 30);
        }

        add_text("Level, steps:", 20, 50);

    }


    fun dbx_chpass() {
        add_title("Change password");

        dbx.style.width = "256px";
        dbx.style.height = "213px";
        dbx.style.left = js("this. Math.max(Math.floor(window.innerWidth-256)/2, 0)+\"px\";")
        dbx.style.top =js(" Math.max(Math.floor(window.innerHeight-213)/2, 0)+\"px\";")

        dbx.style.background = "url(" + res.images[174].src + ')';

        add_text("Old password:", 20, 35);
        add_input(100, 35, 120, 15, "password");
        add_text("New password:", 20, 60);
        add_input(100, 60, 120, 15, "password");

        var f_o = {
            if (game.dbxcall_chpass(dbx.arr_input[0].value, dbx.arr_input[1].value)) {
                close_dbx();
            }
        };
        var f_c = { close_dbx(); };

        dbx.enterfun = f_o;
        dbx.cancelfun = f_c;

        add_button(181, 40, 160, f_o);// ok
        add_button(177, 160, 160, f_c);// cancel

        add_errfield(20, 85);
    }


    fun dbx_load() {
        add_title("Load game");

        dbx.style.width = "256px";
        dbx.style.height = "213px";
        dbx.style.left =js(" Math.max(Math.floor(window.innerWidth-256)/2, 0)+\"px\";")
        dbx.style.top =js(" Math.max(Math.floor(window.innerHeight-213)/2, 0)+\"px\";\n")
        dbx.style.background = "url(" + res.images[174].src + ')';

        add_text("Player name:", 20, 35);
        add_input(100, 35, 120, 15, "text");
        add_text("Password:", 20, 60);
        add_input(100, 60, 120, 15, "password");

        var f_o = {
            if (game.dbxcall_load(dbx.arr_input[0].value, dbx.arr_input[1].value)) {
                close_dbx();
            }
        };
        var f_c = { close_dbx(); };

        dbx.enterfun = f_o;
        dbx.cancelfun = f_c;

        add_button(181, 40, 160, f_o);// ok
        add_button(177, 160, 160, f_c);// cancel

        add_errfield(20, 85);
    }


    fun dbx_confirm(opt: dynamic) {
        add_title("Confirm");

        dbx.style.width = "256px";
        dbx.style.height = "154px";
        this.dbx.style.left = js("Math.max(Math.floor(window.innerWidth-256)/2, 0)+\"px\";\n")
        this.dbx.style.top =  js("Math.max(Math.floor(window.innerHeight-154)/2, 0)+\"px\";")
        dbx.style.background = "url(" + res.images[173].src + ')';

        var f_y: () -> Unit = {};
        var f_n: () -> Unit = {};
        var f_c = { close_dbx(); };

        if (opt == 0) {// "New Game"
            f_y = { open_dbx(DBX_SAVE, 1); };
            f_n = { game.clear_savegame();close_dbx(); };
        } else if (opt == 1) {// "Load Game"
            f_y = { open_dbx(DBX_SAVE, 2); };
            f_n = { open_dbx(DBX_LOAD); };
        }

        dbx.enterfun = f_y;
        dbx.cancelfun = f_c;

        add_button(183, 20, 100, f_y);// yes
        add_button(179, 100, 100, f_n);// no
        add_button(177, 180, 100, f_c);// cancel

        add_text("Do you want to save the game?", 40, 35);
    }


    fun add_errfield(pos_x: dynamic, pos_y: dynamic) {
        var ef = document.createElement("p").asDynamic();
        ef.innerHTML = "";
        ef.style.position = "absolute";
        js("ef.style.left = pos_x+\"px\";")
        js("ef.style.top = pos_y+\"px\";")
        ef.style.fontFamily = "Tahoma";
        ef.style.fontSize = "12px";
        ef.style.color = "#FF0000";
        dbx.appendChild(ef);

        dbx.errfield = ef;
    }

    fun add_button(img: dynamic, pos_x: dynamic, pos_y: dynamic, click_effect: dynamic) {
        var btn = document.createElement("img").asDynamic();
        btn.src = res.images[img].src;
        btn.style.position = "absolute";
        btn.style.width = res.images[img].width + "px";
        btn.style.height = res.images[img].height + "px";
        btn.style.left = pos_x + "px";
        btn.style.top = pos_y + "px";

        btn.pressed = false;
        btn.onmousedown =
            { evt: MouseEvent -> btn.src = res.images[img + 1].src; btn.pressed = true; evt.preventDefault(); };
        btn.onmouseup = { evt: MouseEvent -> btn.src = res.images[img].src; btn.pressed = false; };
        btn.onmouseout = { evt: MouseEvent -> btn.src = res.images[img].src; };
        btn.onmouseover =
            { evt: MouseEvent -> if (btn.pressed && input.mouse_down) btn.src = res.images[img + 1].src; };

        btn.onclick = click_effect;

        dbx.appendChild(btn);
        dbx.arr_btn[dbx.arr_btn.length] = btn;
    }

    fun add_lvlselect(pos_x: dynamic, pos_y: dynamic, width: dynamic, height: dynamic) {

        var select = document.createElement("select").asDynamic();
        select.size = 2;

        select.innerHTML = "";
        for (i in 1 until game.savegame.reached_level) {
            select.innerHTML += "<option value=\"" + i + "\">\n" + i + ", " + game.savegame.arr_steps[i] + "</option>";
        }
        if (game.savegame.reached_level <= 50) {
            select.innerHTML += "<option value=\"" + game.savegame.reached_level + "\">\n" + game.savegame.reached_level + ", -</option>";
        }


        select.style.position = "absolute";
        select.style.left = pos_x + "px";
        select.style.top = pos_y + "px";
        select.style.width = width + "px";
        select.style.height = height + "px";
        select.style.fontFamily = "Tahoma";
        select.style.fontSize = "12px";

        dbx.appendChild(select);
        dbx.lvlselect = select;
    }

    fun add_text(text: String?, pos_x: Int, pos_y: Int) {
        var txt = document.createElement("p").asDynamic();
        txt.innerHTML = text;
        txt.style.position = "absolute";
        txt.style.left = pos_x.toString() + "px";
        txt.style.top = pos_y.toString() + "px";
        txt.style.fontFamily = "Tahoma";
        txt.style.fontSize = "12px";
        dbx.appendChild(txt);
    }

    fun add_number(a_num: Int, pos_x: Int, pos_y: Int, width: dynamic, height: dynamic) {
        var num = document.createElement("p").asDynamic();
        num.innerHTML = a_num;
        num.style.position = "absolute";
        num.style.left = pos_x.toString() + "px";
        num.style.top = pos_y.toString() + "px";
        num.style.width = width + "px";
        num.style.height = height + "px";
        num.style.fontFamily = "Tahoma";
        num.style.fontSize = "12px";
        num.style.textAlign = "right";
        dbx.appendChild(num);
    }

    fun add_title(text: String) {
        var txt = document.createElement("p").asDynamic();
        txt.innerHTML = text;
        txt.style.position = "absolute";
        txt.style.left = "5px";
        txt.style.top = "-13px";
        txt.style.fontFamily = "Tahoma";
        txt.style.fontSize = "14px";
        txt.style.color = "white";
        txt.style.fontWeight = "bold";
        dbx.appendChild(txt);
    }

    fun add_input(pos_x: dynamic, pos_y: dynamic, width: dynamic, height: dynamic, type: dynamic) {
        var txt = document.createElement("input").asDynamic();
        //txt.innerHTML = text;
        txt.type = type;
        txt.style.position = "absolute";
        txt.style.left = pos_x + "px";
        pos_y += 10;// Because of padding
        txt.style.top = pos_y + "px";
        txt.style.width = width + "px";
        txt.style.height = height + "px";
        txt.style.fontFamily = "Tahoma";
        txt.style.fontSize = "12px";

        dbx.appendChild(txt);
        dbx.arr_input[dbx.arr_input.length] = txt;
    }

    fun update_all_animations(){
        for(y in 0 until LEV_DIMENSION_Y){
            for (x in 0 until LEV_DIMENSION_X){
                update_animation(x, y);
            }
        }
    }

    fun init_animation() {

        for (y in 0 until LEV_DIMENSION_Y) {
            // console.log("HALLO"+y+" "+LEV_DIMENSION_Y)
            for (x in 0 until LEV_DIMENSION_X) {
                val block = game.level_array[x][y] as Block;
                when (block.id) {
                    -1 -> {
                        // DUMMY BLOCK (invisible). Prevents entities from moving to already occupied spaces.
                        break
                    }
                    1, 2 -> {
                        // 1: Berti (entry point)
                        // 2: AUTO Menu Berti
                        block.animation_frame = 59;
                        break
                    }
                    3 -> {// Solid block
                        block.animation_frame = 31;
                        break
                    }
                    4 -> {
                        // Banana
                        block.animation_frame = 2;
                        block.fine_offset_x = offset_banana_x;
                        block.fine_offset_y = offset_banana_y;
                        break
                    }
                    5 -> {
                        // Light block
                        block.animation_frame = 32;
                        break
                    }
                    6 -> {
                        // Heavy block
                        block.animation_frame = 33;
                        break
                    }
                    7 -> {
                        // Purple monster (Monster 2)
                        block.animation_frame = 111;
                        break
                    }
                    8, 9 -> {
                        // NOTHING
                        break
                    }

                    10 -> {
                        // Green monster (Monster 2)
                        block.animation_frame = 147;
                        break
                    }
                    11, 12 -> {
                        // NOTHING
                        break
                    }
                    13 -> {
                        // Key 1
                        block.animation_frame = 3;
                        block.fine_offset_x = offset_key_x;
                        block.fine_offset_y = offset_key_y;
                        break
                    }
                    14 -> {
                        // Key 2
                        block.animation_frame = 4;
                        block.fine_offset_x = offset_key_x;
                        block.fine_offset_y = offset_key_y;
                        break
                    }
                    15 -> {
                        // Key 3
                        block.animation_frame = 5;
                        block.fine_offset_x = offset_key_x;
                        block.fine_offset_y = offset_key_y;
                        break
                    }
                    16 -> {
                        // Key 4
                        block.animation_frame = 6;
                        block.fine_offset_x = offset_key_x;
                        block.fine_offset_y = offset_key_y;
                        break
                    }
                    17 -> {
                        // Key 5
                        block.animation_frame = 7;
                        block.fine_offset_x = offset_key_x;
                        block.fine_offset_y = offset_key_y;
                        break
                    }
                    18 -> {
                        // Key 6
                        block.animation_frame = 8;
                        block.fine_offset_x = offset_key_x;
                        block.fine_offset_y = offset_key_y;
                        break
                    }
                    19 -> {
                        // Door 1
                        block.animation_frame = 41;
                        break
                    }
                    20 -> {
                        // Door 2
                        block.animation_frame = 44;
                        break
                    }
                    21 -> {
                        // Door 3
                        block.animation_frame = 47;
                        break
                    }
                    22 -> {
                        // Door 4
                        block.animation_frame = 50;
                        break
                    }
                    23 -> {
                        // Door 5
                        block.animation_frame = 53;
                        break
                    }
                    24 -> {
                        // Door 6
                        block.animation_frame = 56;
                        break
                    }
                    else -> {
                        break
                    }
                }
            }
        }
    }


    fun kt_update_animation_case2(x: Int, y: Int, block: Block) {
        block.fine_offset_x = 0;
        if (game.level_ended == 0) {
            if (block.moving) {
                block.fine_offset_x = -1;
                if (block.pushing) {
                    when (block.face_dir) {
                        DIR_UP -> {
                            if (block.animation_frame < 87 || block.animation_frame > 90) {
                                block.animation_frame = 87;
                            }
                        }
                        DIR_DOWN -> {
                            if (block.animation_frame < 91 || block.animation_frame > 94) {
                                block.animation_frame = 91;
                            }
                        }
                        DIR_LEFT -> {
                            if (block.animation_frame < 79 || block.animation_frame > 82) {
                                block.animation_frame = 79;
                            }
                        }
                        DIR_RIGHT -> {
                            if (block.animation_frame < 83 || block.animation_frame > 86) {
                                block.animation_frame = 83;
                            }
                        }
                    }
                } else {
                    when (block.face_dir) {
                        DIR_UP -> {
                            if (block.animation_frame < 71 || block.animation_frame > 74) {
                                block.animation_frame = 71;
                            }
                        }
                        DIR_DOWN -> {
                            if (block.animation_frame < 75 || block.animation_frame > 78) {
                                block.animation_frame = 75;
                            }
                        }
                        DIR_LEFT -> {
                            if (block.animation_frame < 63 || block.animation_frame > 66) {
                                block.animation_frame = 63;
                            }
                        }
                        DIR_RIGHT -> {
                            if (block.animation_frame < 67 || block.animation_frame > 70) {
                                block.animation_frame = 67;
                            }
                        }
                    }
                }
            } else {
                block.animation_frame = 59;
            }
        } else if (game.level_ended == 1) {
            block.animation_frame = 61;
        } else if (game.level_ended == 2) {
            block.animation_frame = 62;
        }

    }

    fun update_animation(x: Int, y: Int) {

        var block = game.level_array[x][y] as Block;
        when (block.id) {
            1, 2 -> {
                kt_update_animation_case2(x, y, block)
            }
            7 -> {
                // Purple monster (Monster 2)
                block.fine_offset_x = 0
                if (game.level_ended == 0) {
                    if (block.moving) {
                        block.fine_offset_x = -1;
                        if (block.pushing) {
                            when (block.face_dir) {
                                DIR_UP -> {
                                    if (block.animation_frame < 139 || block.animation_frame > 142) {
                                        block.animation_frame = 139;
                                    }

                                }
                                DIR_DOWN -> {
                                    if (block.animation_frame < 143 || block.animation_frame > 146) {
                                        block.animation_frame = 143;
                                    }

                                }
                                DIR_LEFT -> {
                                    if (block.animation_frame < 131 || block.animation_frame > 134) {
                                        block.animation_frame = 131;
                                    }

                                }
                                DIR_RIGHT -> {
                                    if (block.animation_frame < 135 || block.animation_frame > 138) {
                                        block.animation_frame = 135;
                                    }
                                }
                            }
                        } else {
                            when (block.face_dir) {
                                DIR_UP -> {
                                    if (block.animation_frame < 123 || block.animation_frame > 126) {
                                        block.animation_frame = 123;
                                    }


                                }
                                DIR_DOWN -> {
                                    if (block.animation_frame < 127 || block.animation_frame > 130) {
                                        block.animation_frame = 127;
                                    }


                                }
                                DIR_LEFT -> {
                                    if (block.animation_frame < 115 || block.animation_frame > 118) {
                                        block.animation_frame = 115;
                                    }


                                }
                                DIR_RIGHT -> {
                                    if (block.animation_frame < 119 || block.animation_frame > 122) {
                                        block.animation_frame = 119;
                                    }

                                }
                            }
                        }

                    } else {
                        block.animation_frame = 111;
                    }
                } else {
                    block.animation_frame = 111;
                }
            }
            10 -> {
                // Green monster (Monster 2)
                block.fine_offset_x = 0
                if (game.level_ended == 0) {
                    if (block.moving) {
                        block.fine_offset_x = -1;
                        when (block.face_dir) {
                            DIR_UP -> {
                                if (block.animation_frame < 159 || block.animation_frame > 162) {
                                    block.animation_frame = 159;
                                }

                            }
                            DIR_DOWN -> {
                                if (block.animation_frame < 163 || block.animation_frame > 166) {
                                    block.animation_frame = 163;
                                }

                            }
                            DIR_LEFT -> {
                                if (block.animation_frame < 151 || block.animation_frame > 154) {
                                    block.animation_frame = 151;
                                }

                            }
                            DIR_RIGHT -> {
                                if (block.animation_frame < 155 || block.animation_frame > 158) {
                                    block.animation_frame = 155;
                                }

                            }
                        }
                    } else {
                        block.animation_frame = 147;
                    }
                } else {
                    block.animation_frame = 147;
                }
            }
            19 -> {
                // Door 1
                if (block.gets_removed_in >= 0) {
                    block.animation_frame = 43 - floor(block.gets_removed_in / game.door_removal_delay * 2).toInt();
                }
            }
            20 -> {
                // Door 2
                if (block.gets_removed_in >= 0) {
                    block.animation_frame = 46 - floor(block.gets_removed_in / game.door_removal_delay * 2).toInt();
                }
            }
            21 -> {
                // Door 3
                if (block.gets_removed_in >= 0) {
                    block.animation_frame = 49 - floor(block.gets_removed_in / game.door_removal_delay * 2).toInt();
                }
            }
            22 -> {
                // Door 4
                if (block.gets_removed_in >= 0) {
                    block.animation_frame = 52 - floor(block.gets_removed_in / game.door_removal_delay * 2).toInt();
                }
            }
            23 -> {
                // Door 5
                if (block.gets_removed_in >= 0) {
                    block.animation_frame = 55 - floor(block.gets_removed_in / game.door_removal_delay * 2).toInt();
                }
            }
            24 -> {
                if (block.gets_removed_in >= 0) {
                    block.animation_frame = 58 - floor(block.gets_removed_in / game.door_removal_delay * 2).toInt();
                }
            }

        }
    }
}




@JsExport
fun render_block(x: Int, y: Int, render_option: dynamic) {
    var block = game.level_array[x][y] as Block;

    var offset_x = block.moving_offset.x as Int;
    var offset_y = block.moving_offset.y as Int;

    var needs_update = false;
    while (block.animation_delay >= ANIMATION_DURATION && !block.just_moved) {
        block.animation_delay -= ANIMATION_DURATION;
        needs_update = true;
    }

    if (game.level_array[x][y].id <= 0) return;// Optimization (empty and dummy block can't be drawn)

    if (needs_update) {
        when (game.level_array[x][y].id) {
            /*case -1://DUMMY BLOCK (invisible). Prevents entities from moving to already occupied spaces.
                break;*/

            1, 2 -> {
                // 1: Berti
                // 2: AUTO Menu Berti
                when (block.animation_frame) {
                    in 63..65 -> {
                        block.animation_frame += 1;
                    }
                    66 -> {
                        block.animation_frame = 63;
                    }
                    in 67..69 -> {
                        block.animation_frame += 1;
                    }
                    70 -> {
                        block.animation_frame = 67;
                    }
                    in 71..73 -> {
                        block.animation_frame += 1;
                    }
                    74 -> {
                        block.animation_frame = 71;
                    }
                    in 75..77 -> {
                        block.animation_frame += 1;
                    }
                    78 -> {
                        block.animation_frame = 75;
                    }
                    in 79..81 -> {
                        block.animation_frame += 1;
                    }
                    82 -> {
                        block.animation_frame = 79;
                    }
                    in 83..85 -> {
                        block.animation_frame += 1;
                    }
                    86 -> {
                        block.animation_frame = 83;
                    }
                    in 87..89 -> {
                        block.animation_frame += 1;
                    }
                    90 -> {
                        block.animation_frame = 87;
                    }
                    in 91..93 -> {
                        block.animation_frame += 1;
                    }
                    94 -> {
                        block.animation_frame = 91;
                    }
                }
            }
            7 -> {
                // Purple monster (Monster 2)
                when (block.animation_frame) {
                    in 111..113 -> {
                        block.animation_frame += 1;
                    }
                    114 -> {
                        block.animation_frame = 111;
                    }
                    in 115..117 -> {
                        block.animation_frame += 1;
                    }
                    118 -> {
                        block.animation_frame = 115;
                    }
                    in 119..121 -> {
                        block.animation_frame += 1;
                    }
                    122 -> {
                        block.animation_frame = 119;
                    }
                    in 123..125 -> {
                        block.animation_frame += 1;
                    }
                    126 -> {
                        block.animation_frame = 123;
                    }
                    in 127..129 -> {
                        block.animation_frame += 1;
                    }
                    130 -> {
                        block.animation_frame = 127;
                    }
                    in 131..133 -> {
                        block.animation_frame += 1;
                    }
                    134 -> {
                        block.animation_frame = 131;
                    }
                    in 135..137 -> {
                        block.animation_frame += 1;
                    }
                    138 -> {
                        block.animation_frame = 135;
                    }
                    in 139..141 -> {
                        block.animation_frame += 1;
                    }
                    142 -> {
                        block.animation_frame = 139;
                    }
                    in 143..145 -> {
                        block.animation_frame += 1;
                    }
                    146 -> {
                        block.animation_frame = 143;
                    }
                }
            }

            10 -> {
                // Green monster (Monster 2)
                when (block.animation_frame) {
                    in 147..149 -> {
                        block.animation_frame += 1;
                    }
                    150 -> {
                        block.animation_frame = 147;
                    }
                    in 151..153 -> {
                        block.animation_frame += 1;
                    }
                    154 -> {
                        block.animation_frame = 151;
                    }
                    in 155..157 -> {
                        block.animation_frame += 1;
                    }
                    158 -> {
                        block.animation_frame = 155;
                    }
                    in 159..161 -> {
                        block.animation_frame += 1;
                    }
                    162 -> {
                        block.animation_frame = 159;
                    }
                    in 163..165 -> {
                        block.animation_frame += 1;
                    }
                    166 -> {
                        block.animation_frame = 163;
                    }
                }
            }

        }
    }

    //drawImage reference: context.drawImage(img,sx,sy,swidth,sheight,x,y,width,height);
    if (block.animation_frame >= 0) {
        if (render_option == RENDER_FULL) {// Render the full block
            CTX.drawImage(
                res.images[block.animation_frame],
                LEV_OFFSET_X + 24 * x + offset_x + block.fine_offset_x,
                LEV_OFFSET_Y + 24 * y + offset_y + block.fine_offset_y
            );
        } else if (render_option == RENDER_TOP) {// Render top
            if (block.face_dir == DIR_DOWN) {
                CTX.drawImage(
                    res.images[block.animation_frame],
                    0,
                    0,
                    res.images[block.animation_frame].width,
                    res.images[block.animation_frame].height - offset_y,
                    LEV_OFFSET_X + 24 * x + offset_x + block.fine_offset_x,
                    LEV_OFFSET_Y + 24 * y + offset_y + block.fine_offset_y,
                    res.images[block.animation_frame].width,
                    res.images[block.animation_frame].height - offset_y
                );
            } else if (block.face_dir == DIR_UP) {
                CTX.drawImage(
                    res.images[block.animation_frame],
                    0,
                    0,
                    res.images[block.animation_frame].width,
                    res.images[block.animation_frame].height - offset_y - 24,
                    LEV_OFFSET_X + 24 * x + offset_x + block.fine_offset_x,
                    LEV_OFFSET_Y + 24 * y + offset_y + block.fine_offset_y,
                    res.images[block.animation_frame].width,
                    res.images[block.animation_frame].height - offset_y - 24
                );
            }
        } else if (render_option == RENDER_BOTTOM) {// Render bottom
            var imgsize_offset: Int = res.images[block.animation_frame].height - 24;

            if (block.face_dir == DIR_DOWN) {
                CTX.drawImage(
                    res.images[block.animation_frame],
                    0,
                    res.images[block.animation_frame].height - offset_y - imgsize_offset,
                    res.images[block.animation_frame].width,
                    offset_y + imgsize_offset,
                    LEV_OFFSET_X + 24 * x + offset_x + block.fine_offset_x,
                    LEV_OFFSET_Y + 24 * y + 24 + block.fine_offset_y,
                    res.images[block.animation_frame].width,
                    offset_y + imgsize_offset
                );
            } else if (block.face_dir == DIR_UP) {
                CTX.drawImage(
                    res.images[block.animation_frame],
                    0,
                    -offset_y,
                    res.images[block.animation_frame].width,
                    res.images[block.animation_frame].height + offset_y,
                    LEV_OFFSET_X + 24 * x + offset_x + block.fine_offset_x,
                    LEV_OFFSET_Y + 24 * y + block.fine_offset_y,
                    res.images[block.animation_frame].width,
                    res.images[block.animation_frame].height + offset_y
                );
            }
        } else if (render_option == RENDER_BOTTOM_BORDER) {// Render the bottom 4 pixels
            CTX.drawImage(
                res.images[block.animation_frame],
                0,
                24,
                res.images[block.animation_frame].width - 4,
                4,
                LEV_OFFSET_X + 24 * x + offset_x + block.fine_offset_x,
                LEV_OFFSET_Y + 24 * y + offset_y + block.fine_offset_y + 24,
                res.images[block.animation_frame].width - 4,
                4
            );
        }
    }


}

@JsExport
fun kt_render_buttons() {
    var over_button = false;
    if (input.mouse_down) {
        if (input.mouse_pos.y >= 35 && input.mouse_pos.y <= 65) {
            if (input.mouse_pos.x >= 219 && input.mouse_pos.x <= 249 && input.lastclick_button == 0) {
                vis.buttons_pressed[0] = true;
                over_button = true;
            } else if (input.mouse_pos.x >= 253 && input.mouse_pos.x <= 283 && input.lastclick_button == 1) {
                vis.buttons_pressed[1] = true;
                over_button = true;
            } else if (input.mouse_pos.x >= 287 && input.mouse_pos.x <= 317 && input.lastclick_button == 2) {
                vis.buttons_pressed[2] = true;
                over_button = true;
            }
        }
    }
    if (!over_button) {
        vis.buttons_pressed[0] = vis.buttons_pressed[1] == vis.buttons_pressed[2] == false;
    }

    if (game.buttons_activated[0]) {
        if (vis.buttons_pressed[0]) {
            CTX.drawImage(res.images[26], 219, 35);// << pressed
        } else {
            CTX.drawImage(res.images[23], 219, 35);// << up
        }
    } else {
        CTX.drawImage(res.images[29], 219, 35);// << disabled
    }

    if (vis.buttons_pressed[1]) {
        CTX.drawImage(res.images[25], 253, 35);// Berti pressed
    } else {
        if (vis.berti_blink_time < 100) {
            CTX.drawImage(res.images[22], 253, 35);// Berti up
            if (vis.berti_blink_time == 0) {
                vis.berti_blink_time = 103;//Math.floor(100+(Math.random()*100)+1);
            } else {
                vis.berti_blink_time--;
            }
        } else {
            CTX.drawImage(res.images[28], 253, 35);// Berti up blink
            if (vis.berti_blink_time == 100) {
                vis.berti_blink_time = floor((Random.nextDouble() * 95) + 5).toInt();
            } else {
                vis.berti_blink_time--;
            }
        }
    }

    if (game.buttons_activated[2]) {
        if (vis.buttons_pressed[2]) {
            CTX.drawImage(res.images[27], 287, 35);// >> pressed
        } else {
            CTX.drawImage(res.images[24], 287, 35);// >> up
        }
    } else {
        CTX.drawImage(res.images[30], 287, 35);// >> disabled
    }
}


@JsExport
fun render_field() {
    render_field_subset(true);// Consumables in the background
    render_field_subset(false);// The rest in the foreground

    CTX.drawImage(
        res.images[0],
        0,
        391,
        537,
        4,
        0,
        LEV_OFFSET_Y + 24 * LEV_DIMENSION_Y,
        537,
        4
    );// Bottom border covering blocks
    CTX.drawImage(
        res.images[0],
        520,
        LEV_OFFSET_Y,
        4,
        391 - LEV_OFFSET_Y,
        LEV_OFFSET_X + 24 * LEV_DIMENSION_X,
        LEV_OFFSET_Y,
        4,
        391 - LEV_OFFSET_Y
    );// Right border covering blocks

    if (game.level_ended == 1) {// Berti cheering, wow or yeah
        for (i in game.berti_positions.indices) {
            if (game.wow) {
                CTX.drawImage(
                    res.images[168],
                    LEV_OFFSET_X + 24 * game.berti_positions[i].x + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.x as Int + vis.offset_wow_x,
                    LEV_OFFSET_Y + 24 * game.berti_positions[i].y + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.y as Int + vis.offset_wow_y
                );
            } else {
                CTX.drawImage(
                    res.images[169],
                    LEV_OFFSET_X + 24 * game.berti_positions[i].x + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.x as Int + vis.offset_yeah_x,
                    LEV_OFFSET_Y + 24 * game.berti_positions[i].y + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.y as Int + vis.offset_yeah_y
                );
            }
        }
    } else if (game.level_ended == 2) {// Berti dies in a pool of blood
        for (i in game.berti_positions.indices) {
            CTX.drawImage(
                res.images[167],
                LEV_OFFSET_X + 24 * game.berti_positions[i].x + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.x as Int + vis.offset_argl_x,
                LEV_OFFSET_Y + 24 * game.berti_positions[i].y + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.y as Int + vis.offset_argl_y
            );
        }
    }
}


@JsExport
fun render_field_subset(consumable: dynamic) {
    for (y in 0 until LEV_DIMENSION_Y) {
        for (x in 0 until LEV_DIMENSION_X) {
            var block = game.level_array[x][y] as KtEntity;
            if (y > 0 && game.level_array[x][y - 1].moving && game.level_array[x][y - 1].face_dir == DIR_DOWN && game.level_array[x][y - 1].consumable == consumable) {
                render_block(x, y - 1, RENDER_BOTTOM);
            }

            if (y > 0 && (!game.level_array[x][y - 1].moving) && game.level_array[x][y - 1].consumable == consumable) {
                if (x > 0 && game.level_array[x - 1][y].face_dir != DIR_RIGHT) {
                    render_block(x, y - 1, RENDER_BOTTOM_BORDER);
                }
            }

            if (block.consumable == consumable) {
                if (!block.moving || block.face_dir == DIR_LEFT || block.face_dir == DIR_RIGHT) {
                    render_block(x, y, RENDER_FULL);
                } else if (block.face_dir == DIR_DOWN) {
                    render_block(x, y, RENDER_TOP);
                } else if (block.face_dir == DIR_UP) {
                    render_block(x, y, RENDER_BOTTOM);
                }
            }

            if (y + 1 < LEV_DIMENSION_Y && game.level_array[x][y + 1].moving && game.level_array[x][y + 1].face_dir == DIR_UP && game.level_array[x][y + 1].consumable == consumable) {
                render_block(x, y + 1, RENDER_TOP);
            }
        }
    }
}


@JsExport
fun render_vol_bar() {
    var vb = vis.vol_bar;
    var switcher = false;

    for (i in 0 until vb.width) {
        var line_height: Int = 0

        if (switcher) {
            switcher = false;
            CTX.fillStyle = "rgb(" + vb.colour_4.r + ", " + vb.colour_4.g + ", " + vb.colour_4.b + ")";
        } else {
            switcher = true;
            var ratio2 = i / vb.width.toDouble();
            line_height = round((vb.height * ratio2).toDouble()).toInt();

            if (i < ceil(vb.volume * vb.width)) {
                if (game.sound) {
                    var ratio1 = 1 - ratio2;
                    CTX.fillStyle =
                        "rgb(" + round(vb.colour_1.r * ratio1 + vb.colour_2.r * ratio2) + ", " + round(vb.colour_1.g * ratio1 + vb.colour_2.g * ratio2) + ", " + round(
                            vb.colour_1.b * ratio1 + vb.colour_2.b * ratio2
                        ) + ")";
                } else {
                    CTX.fillStyle = "rgb(" + vb.colour_5.r + ", " + vb.colour_5.g + ", " + vb.colour_5.b + ")";
                }
            } else {
                CTX.fillStyle = "rgb(" + vb.colour_3.r + ", " + vb.colour_3.g + ", " + vb.colour_3.b + ")";
            }

        }
        CTX.fillRect(vb.offset_x + i, vb.offset_y + vb.height - line_height, 1, line_height);

    }
}

@JsExport
fun kt_render_menu() {
    var submenu_offset = 0.0;
    // The font is the same for the whole menu... Segoe UI is also nice
    CTX.font = "11px Tahoma";
    CTX.textAlign = "left";
    CTX.textBaseline = "top";

    for (i in 0 until vis.menu1.submenu_list.size) {
        var sm = vis.menu1.submenu_list[i];
        if (i == vis.menu1.submenu_open) {
            CTX.fillStyle = "rgb(" + vis.light_grey.r + ", " + vis.light_grey.g + ", " + vis.light_grey.b + ")";
            CTX.fillRect(
                vis.menu1.offset_x + submenu_offset,
                vis.menu1.offset_y + vis.menu1.height + 1,
                sm.dd_width,
                sm.dd_height
            );// Options box

            CTX.fillStyle = "rgb(" + vis.med_grey.r + ", " + vis.med_grey.g + ", " + vis.med_grey.b + ")";
            CTX.fillRect(vis.menu1.offset_x + submenu_offset, vis.menu1.offset_y, sm.width, 1);
            CTX.fillRect(vis.menu1.offset_x + submenu_offset, vis.menu1.offset_y, 1, vis.menu1.height);
            CTX.fillRect(
                vis.menu1.offset_x + submenu_offset + sm.dd_width - 2,
                vis.menu1.offset_y + vis.menu1.height + 2,
                1,
                sm.dd_height - 2
            );// Options box
            CTX.fillRect(
                vis.menu1.offset_x + submenu_offset + 1,
                vis.menu1.offset_y + vis.menu1.height + sm.dd_height - 1,
                sm.dd_width - 2,
                1
            );// Options box

            CTX.fillStyle = "rgb(" + vis.white.r + ", " + vis.white.g + ", " + vis.white.b + ")";
            CTX.fillRect(vis.menu1.offset_x + submenu_offset, vis.menu1.offset_y + vis.menu1.height, sm.width, 1);
            CTX.fillRect(vis.menu1.offset_x + submenu_offset + sm.width - 1, vis.menu1.offset_y, 1, vis.menu1.height);
            CTX.fillRect(
                vis.menu1.offset_x + submenu_offset + 1,
                vis.menu1.offset_y + vis.menu1.height + 2,
                1,
                sm.dd_height - 3
            );// Options box
            CTX.fillRect(
                vis.menu1.offset_x + submenu_offset + 1,
                vis.menu1.offset_y + vis.menu1.height + 2,
                sm.dd_width - 3,
                1
            );// Options box

            CTX.fillStyle = "rgb(" + vis.dark_grey.r + ", " + vis.dark_grey.g + ", " + vis.dark_grey.b + ")";
            CTX.fillRect(
                vis.menu1.offset_x + submenu_offset + sm.dd_width - 1,
                vis.menu1.offset_y + vis.menu1.height + 1,
                1,
                sm.dd_height
            );// Options box
            CTX.fillRect(
                vis.menu1.offset_x + submenu_offset,
                vis.menu1.offset_y + vis.menu1.height + sm.dd_height,
                sm.dd_width - 1,
                1
            );// Options box

            //input.mouse_pos.x
            var option_offset = vis.menu1.offset_y + vis.menu1.height + 4;
            CTX.fillStyle = "rgb(" + vis.black.r + ", " + vis.black.g + ", " + vis.black.b + ")";

            for (j in 0 until sm.options.size) {
                var next_offset: Int;
                var check_image = 171;
                if (sm.options[j].line) {
                    next_offset = option_offset + sm.offset_line;

                    CTX.fillStyle = "rgb(" + vis.med_grey.r + ", " + vis.med_grey.g + ", " + vis.med_grey.b + ")";
                    CTX.fillRect(
                        vis.menu1.offset_x + submenu_offset + 3,
                        option_offset + 3,
                        sm.dd_width - 6,
                        1
                    );// Separator line
                    CTX.fillStyle = "rgb(" + vis.white.r + ", " + vis.white.g + ", " + vis.white.b + ")";
                    CTX.fillRect(
                        vis.menu1.offset_x + submenu_offset + 3,
                        option_offset + 4,
                        sm.dd_width - 6,
                        1
                    );// Separator line

                } else {
                    next_offset = option_offset + sm.offset_text;
                }

                if (!sm.options[j].line && input.mouse_pos.x > vis.menu1.offset_x + submenu_offset && input.mouse_pos.x < vis.menu1.offset_x + submenu_offset + sm.dd_width &&
                    input.mouse_pos.y > option_offset && input.mouse_pos.y < next_offset
                ) {
                    CTX.fillStyle = "rgb(" + vis.blue.r + ", " + vis.blue.g + ", " + vis.blue.b + ")";
                    CTX.fillRect(
                        vis.menu1.offset_x + submenu_offset + 3,
                        option_offset,
                        sm.dd_width - 6,
                        sm.offset_text
                    );// Options box
                    CTX.fillStyle = "rgb(" + vis.white.r + ", " + vis.white.g + ", " + vis.white.b + ")";

                    check_image = 172;
                } else if (!sm.options[j].on()) {
                    CTX.fillStyle = "rgb(" + vis.white.r + ", " + vis.white.g + ", " + vis.white.b + ")";
                    CTX.fillText(sm.options[j].name, vis.menu1.offset_x + submenu_offset + 21, option_offset + 2);
                } else {
                    CTX.fillStyle = "rgb(" + vis.black.r + ", " + vis.black.g + ", " + vis.black.b + ")";
                }

                if (sm.options[j].on()) {
                    CTX.fillText(sm.options[j].name, vis.menu1.offset_x + submenu_offset + 20, option_offset + 1);
                } else {
                    CTX.fillStyle = "rgb(" + vis.med_grey.r + ", " + vis.med_grey.g + ", " + vis.med_grey.b + ")";
                    CTX.fillText(sm.options[j].name, vis.menu1.offset_x + submenu_offset + 20, option_offset + 1);
                }

                if (sm.options[j].check != 0) {
                    if ((sm.options[j].effect_id == 3 && game.paused) || (sm.options[j].effect_id == 4 && game.single_steps) || (sm.options[j].effect_id == 5 && game.sound)) {
                        CTX.drawImage(
                            res.images[check_image],
                            vis.menu1.offset_x + submenu_offset + 6,
                            option_offset + 6
                        );// Background
                    }
                }

                option_offset = next_offset;

            }
        }
        CTX.fillStyle = "rgb(" + vis.black.r + ", " + vis.black.g + ", " + vis.black.b + ")";
        CTX.fillText(sm.name, vis.menu1.offset_x + submenu_offset + 6, vis.menu1.offset_y + 3);
        submenu_offset += sm.width as Double;
    }

}