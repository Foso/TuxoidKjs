import GameSettings.Companion.JOYSTICK_SIZE
import data.savegame.SaveGameDataRepository
import data.savegame.SaveGameDataSource
import data.sound.SoundDataSource
import data.sound.SoundRepository
import de.jensklingenberg.bananiakt.MyImage
import de.jensklingenberg.bananiakt.model.GameMode
import kotlinx.browser.document
import kotlinx.browser.window
import model.Block
import model.Color.Companion.black
import model.Color.Companion.blue
import model.Color.Companion.dark_grey
import model.Color.Companion.light_grey
import model.Color.Companion.med_grey
import model.Color.Companion.toRgbString
import model.Color.Companion.white
import org.w3c.dom.BOTTOM
import org.w3c.dom.CENTER
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.LEFT
import org.w3c.dom.MIDDLE
import org.w3c.dom.TOP
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import ui.volumebar.VolumeBar
import kotlin.js.Date
import kotlin.math.floor
import kotlin.math.min
import kotlin.random.Random

enum class GameState(val value: Int) {
    RUNNING(0), WON(1), DIED(2)
}

class App : KeyListener {
    companion object {


        var DEFAULT_VOLUME = 0.7


        var DBX_CONFIRM = 0
        var DBX_SAVE = 1
        var DBX_LOAD = 2
        var DBX_CHPASS = 3
        var DBX_LOADLVL = 4
        var DBX_CHARTS = 5

        var DIR_NONE = -1
        var DIR_UP = 0
        var DIR_LEFT = 1
        var DIR_DOWN = 2
        var DIR_RIGHT = 3
        var LEV_START_DELAY = 1
        var UPS: Int = 60

        lateinit var ktGame: KtGame
        private val MYCANVAS: HTMLCanvasElement = document.createElement("canvas") as HTMLCanvasElement
        val MYCTX: CanvasRenderingContext2D = MYCANVAS.getContext("2d") as CanvasRenderingContext2D
    }

    var dbx = document.createElement("div").asDynamic()

    private val gameHandler: GameHandler = GameHandlerImpl()

    private val vol_bar = VolumeBar()
    private val res = MyRes()
    private val saveGameDataSource: SaveGameDataSource = SaveGameDataRepository()
    private val soundDataSource: SoundDataSource = SoundRepository(res)
    private val game = KtGame(vol_bar, res, saveGameDataSource, soundDataSource, gameHandler)

    init {
        ktGame = game
        initCanvas()
        checkIfTouch()
        if (IS_TOUCH_DEVICE) {
            UPS = 15
            initTouch()
        }
    }


    private val input: MyInput = MyInput(MYCANVAS, saveGameDataSource, soundDataSource, vol_bar, gameHandler)
    val vis = KtVisual(vol_bar, input, res, saveGameDataSource, soundDataSource, game, dbx)

    fun initCanvas() {


        MYCANVAS.apply {
            width = GameSettings.SCREEN_WIDTH
            height = GameSettings.SCREEN_HEIGHT
            className = "canv"
        }
        document.body?.appendChild(MYCANVAS)
    }

    fun initTouch() {
        // Joystick creation
        MyJOYSTICK = document.createElement("canvas") as HTMLCanvasElement
        MYJOYCTX = MyJOYSTICK.getContext("2d") as CanvasRenderingContext2D
        var mindim = min(window.innerWidth, window.innerHeight)
        MyJOYSTICK.apply {
            width = (mindim * JOYSTICK_SIZE).toInt()
            height = (mindim * JOYSTICK_SIZE).toInt()
            className = "joystick"
        }
        document.body?.appendChild(MyJOYSTICK)

        window.onresize = {// On mobile, make game fullscreen
            val ratio_1 = window.innerWidth / true_width
            val ratio_2 = window.innerHeight / true_height
            if (ratio_1 < ratio_2) {
                MYCANVAS.style.apply {
                    height = ""
                    width = "100%"
                }
            } else {
                MYCANVAS.style.height = "100%"
                MYCANVAS.style.width = ""
            }

            var rect = MYCANVAS.getBoundingClientRect()
            var style = window.getComputedStyle(MYCANVAS)
            true_width = rect.width + style.getPropertyValue("border-left-width")
                .toInt() + style.getPropertyValue("border-right-width")
                .toInt()
            true_height = rect.height + style.getPropertyValue("border-top-width")
                .toInt() + style.getPropertyValue("border-bottom-width")
                .toInt()


            var mindim = min(window.innerWidth, window.innerHeight)
            MyJOYSTICK.width = (mindim * JOYSTICK_SIZE).toInt()
            MyJOYSTICK.height = (mindim * JOYSTICK_SIZE).toInt()

            render_joystick()

        }
        window.onresize = {}
    }


    fun renderToolbar(res: MyRes, myCanvas: MyCanvas) {

        val toolbar = Toolbar(res, myCanvas)
        toolbar.renderStepsDisplay(gameHandler.getStepsTaken().toString())
        toolbar.renderLevelCounter(game.level_number.toString())

    }


    fun kt_render_buttons(MYCTX: CanvasRenderingContext2D, input: MyInput, res: MyRes, game: KtGame) {
        var over_button = false
        if (input.mouse_down) {
            if (input.mouse_pos.y >= 35 && input.mouse_pos.y <= 65) {
                when {
                    input.mouse_pos.x >= 219 && input.mouse_pos.x <= 249 && input.lastclick_button == 0 -> {
                        vis.buttons_pressed[0] = true
                        over_button = true
                    }
                    input.mouse_pos.x >= 253 && input.mouse_pos.x <= 283 && input.lastclick_button == 1 -> {
                        vis.buttons_pressed[1] = true
                        over_button = true
                    }
                    input.mouse_pos.x >= 287 && input.mouse_pos.x <= 317 && input.lastclick_button == 2 -> {
                        vis.buttons_pressed[2] = true
                        over_button = true
                    }
                }
            }
        }
        if (!over_button) {
            vis.buttons_pressed[0] = vis.buttons_pressed[1] == vis.buttons_pressed[2] == false
        }

        if (game.buttons_activated[0]) {
            if (vis.buttons_pressed[0]) {
                MYCTX.drawImage(res.images[26], 219, 35)// << pressed
            } else {
                MYCTX.drawImage(res.images[23], 219, 35)// << up
            }
        } else {
            MYCTX.drawImage(res.images[29], 219, 35)// << disabled
        }

        if (vis.buttons_pressed[1]) {
            MYCTX.drawImage(res.images[25], 253, 35)// Berti pressed
        } else {
            if (vis.berti_blink_time < 100) {
                MYCTX.drawImage(res.images[22], 253, 35)// Berti up
                if (vis.berti_blink_time == 0) {
                    vis.berti_blink_time = 103//Math.floor(100+(Math.random()*100)+1);
                } else {
                    vis.berti_blink_time--
                }
            } else {
                MYCTX.drawImage(res.images[28], 253, 35)// Berti up blink
                if (vis.berti_blink_time == 100) {
                    vis.berti_blink_time = floor((Random.nextDouble() * 95) + 5).toInt()
                } else {
                    vis.berti_blink_time--
                }
            }
        }

        if (game.buttons_activated[2]) {
            if (vis.buttons_pressed[2]) {
                MYCTX.drawImage(res.images[27], 287, 35)// >> pressed
            } else {
                MYCTX.drawImage(res.images[24], 287, 35)// >> up
            }
        } else {
            MYCTX.drawImage(res.images[30], 287, 35)// >> disabled
        }
    }


    fun render_field(game: KtGame, res: MyRes) {
        render_field_subset(true, res)// Consumables in the background
        render_field_subset(false, res)// The rest in the foreground

        MYCTX.drawImage(
            res.images[0],
            0.0,
            391.0,
            537.0,
            4.0,
            0.0,
            (LEV_OFFSET_Y + 24 * LEV_DIMENSION_Y).toDouble(),
            537.0,
            4.0
        )// Bottom border covering blocks
        MYCTX.drawImage(
            res.images[0],
            520.0,
            LEV_OFFSET_Y.toDouble(),
            4.0,
            (391 - LEV_OFFSET_Y).toDouble(),
            (LEV_OFFSET_X + 24 * LEV_DIMENSION_X).toDouble(),
            LEV_OFFSET_Y.toDouble(),
            4.0,
            (391 - LEV_OFFSET_Y).toDouble()
        )// Right border covering blocks

        when (game.levelState) {
            GameState.WON -> {// Berti cheering, wow or yeah
                for (i in game.berti_positions.indices) {
                    if (game.wow) {
                        MYCTX.drawImage(
                            res.images[168],
                            LEV_OFFSET_X + 24 * game.berti_positions[i].x + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.x as Int + vis.offset_wow_x,
                            LEV_OFFSET_Y + 24 * game.berti_positions[i].y + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.y as Int + vis.offset_wow_y
                        )
                    } else {
                        MYCTX.drawImage(
                            res.images[169],
                            LEV_OFFSET_X + 24 * game.berti_positions[i].x + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.x as Int + vis.offset_yeah_x,
                            LEV_OFFSET_Y + 24 * game.berti_positions[i].y + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.y as Int + vis.offset_yeah_y
                        )
                    }
                }
            }
            GameState.DIED -> {// Berti dies in a pool of blood
                for (i in game.berti_positions.indices) {
                    MYCTX.drawImage(
                        res.images[167],
                        LEV_OFFSET_X + 24 * game.berti_positions[i].x + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.x as Int + vis.offset_argl_x,
                        LEV_OFFSET_Y + 24 * game.berti_positions[i].y + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.y as Int + vis.offset_argl_y
                    )
                }
            }
        }
    }


    fun render_block(x: Int, y: Int, render_option: dynamic, res: MyRes) {
        var block = ktGame.level_array[x][y] as Block

        var offset_x = block.moving_offset.x as Int
        var offset_y = block.moving_offset.y as Int

        var needs_update = false
        while (block.animation_delay >= ANIMATION_DURATION && !block.just_moved) {
            block.animation_delay -= ANIMATION_DURATION
            needs_update = true
        }

        if (ktGame.level_array[x][y].id <= 0) return// Optimization (empty and dummy block can't be drawn)

        if (needs_update) {
            when (ktGame.level_array[x][y].id) {
                /*case -1://DUMMY BLOCK (invisible). Prevents entities from moving to already occupied spaces.
                    break;*/

                1, 2 -> {
                    // 1: Berti
                    // 2: AUTO ui.menu.Menu Berti
                    when (block.animation_frame) {
                        in 63..65 -> {
                            block.animation_frame += 1
                        }
                        66 -> {
                            block.animation_frame = 63
                        }
                        in 67..69 -> {
                            block.animation_frame += 1
                        }
                        70 -> {
                            block.animation_frame = 67
                        }
                        in 71..73 -> {
                            block.animation_frame += 1
                        }
                        74 -> {
                            block.animation_frame = 71
                        }
                        in 75..77 -> {
                            block.animation_frame += 1
                        }
                        78 -> {
                            block.animation_frame = 75
                        }
                        in 79..81 -> {
                            block.animation_frame += 1
                        }
                        82 -> {
                            block.animation_frame = 79
                        }
                        in 83..85 -> {
                            block.animation_frame += 1
                        }
                        86 -> {
                            block.animation_frame = 83
                        }
                        in 87..89 -> {
                            block.animation_frame += 1
                        }
                        90 -> {
                            block.animation_frame = 87
                        }
                        in 91..93 -> {
                            block.animation_frame += 1
                        }
                        94 -> {
                            block.animation_frame = 91
                        }
                    }
                }
                7 -> {
                    // Purple monster (Monster 2)
                    when (block.animation_frame) {
                        in 111..113 -> {
                            block.animation_frame += 1
                        }
                        114 -> {
                            block.animation_frame = 111
                        }
                        in 115..117 -> {
                            block.animation_frame += 1
                        }
                        118 -> {
                            block.animation_frame = 115
                        }
                        in 119..121 -> {
                            block.animation_frame += 1
                        }
                        122 -> {
                            block.animation_frame = 119
                        }
                        in 123..125 -> {
                            block.animation_frame += 1
                        }
                        126 -> {
                            block.animation_frame = 123
                        }
                        in 127..129 -> {
                            block.animation_frame += 1
                        }
                        130 -> {
                            block.animation_frame = 127
                        }
                        in 131..133 -> {
                            block.animation_frame += 1
                        }
                        134 -> {
                            block.animation_frame = 131
                        }
                        in 135..137 -> {
                            block.animation_frame += 1
                        }
                        138 -> {
                            block.animation_frame = 135
                        }
                        in 139..141 -> {
                            block.animation_frame += 1
                        }
                        142 -> {
                            block.animation_frame = 139
                        }
                        in 143..145 -> {
                            block.animation_frame += 1
                        }
                        146 -> {
                            block.animation_frame = 143
                        }
                    }
                }

                10 -> {
                    // Green monster (Monster 2)
                    when (block.animation_frame) {
                        in 147..149 -> {
                            block.animation_frame += 1
                        }
                        150 -> {
                            block.animation_frame = 147
                        }
                        in 151..153 -> {
                            block.animation_frame += 1
                        }
                        154 -> {
                            block.animation_frame = 151
                        }
                        in 155..157 -> {
                            block.animation_frame += 1
                        }
                        158 -> {
                            block.animation_frame = 155
                        }
                        in 159..161 -> {
                            block.animation_frame += 1
                        }
                        162 -> {
                            block.animation_frame = 159
                        }
                        in 163..165 -> {
                            block.animation_frame += 1
                        }
                        166 -> {
                            block.animation_frame = 163
                        }
                    }
                }

            }
        }

        //drawImage reference: context.drawImage(img,sx,sy,swidth,sheight,x,y,width,height);
        if (block.animation_frame >= 0) {
            if (render_option == RENDER_FULL) {// Render the full block
                MYCTX.drawImage(
                    res.images[block.animation_frame],
                    LEV_OFFSET_X + 24 * x + offset_x + block.fine_offset_x,
                    LEV_OFFSET_Y + 24 * y + offset_y + block.fine_offset_y
                )
            } else if (render_option == RENDER_TOP) {// Render top
                if (block.face_dir == DIR_DOWN) {
                    MYCTX.drawImage(
                        res.images[block.animation_frame],
                        0.0,
                        0.0,
                        res.images[block.animation_frame].width,
                        res.images[block.animation_frame].height - offset_y,
                        (LEV_OFFSET_X + 24 * x + offset_x + block.fine_offset_x).toDouble(),
                        (LEV_OFFSET_Y + 24 * y + offset_y + block.fine_offset_y).toDouble(),
                        res.images[block.animation_frame].width,
                        res.images[block.animation_frame].height - offset_y
                    )
                } else if (block.face_dir == DIR_UP) {
                    MYCTX.drawImage(
                        res.images[block.animation_frame],
                        0.0,
                        0.0,
                        res.images[block.animation_frame].width,
                        res.images[block.animation_frame].height - offset_y - 24,
                        (LEV_OFFSET_X + 24 * x + offset_x + block.fine_offset_x).toDouble(),
                        (LEV_OFFSET_Y + 24 * y + offset_y + block.fine_offset_y).toDouble(),
                        res.images[block.animation_frame].width,
                        res.images[block.animation_frame].height - offset_y - 24
                    )
                }
            } else if (render_option == RENDER_BOTTOM) {// Render bottom
                var imgsize_offset: Int = res.images[block.animation_frame].height - 24

                if (block.face_dir == DIR_DOWN) {
                    MYCTX.drawImage(
                        res.images[block.animation_frame],
                        0.0,
                        res.images[block.animation_frame].height - offset_y - imgsize_offset,
                        res.images[block.animation_frame].width,
                        (offset_y + imgsize_offset).toDouble(),
                        (LEV_OFFSET_X + 24 * x + offset_x + block.fine_offset_x).toDouble(),
                        (LEV_OFFSET_Y + 24 * y + 24 + block.fine_offset_y).toDouble(),
                        res.images[block.animation_frame].width,
                        (offset_y + imgsize_offset).toDouble()
                    )
                } else if (block.face_dir == DIR_UP) {
                    MYCTX.drawImage(
                        res.images[block.animation_frame],
                        0.0,
                        (-offset_y).toDouble(),
                        res.images[block.animation_frame].width,
                        res.images[block.animation_frame].height + offset_y,
                        (LEV_OFFSET_X + 24 * x + offset_x + block.fine_offset_x).toDouble(),
                        (LEV_OFFSET_Y + 24 * y + block.fine_offset_y).toDouble(),
                        res.images[block.animation_frame].width,
                        res.images[block.animation_frame].height + offset_y
                    )
                }
            } else if (render_option == RENDER_BOTTOM_BORDER) {// Render the bottom 4 pixels
                MYCTX.drawImage(
                    res.images[block.animation_frame],
                    0.0,
                    24.0,
                    res.images[block.animation_frame].width - 4,
                    4.0,
                    (LEV_OFFSET_X + 24 * x + offset_x + block.fine_offset_x).toDouble(),
                    (LEV_OFFSET_Y + 24 * y + offset_y + block.fine_offset_y + 24).toDouble(),
                    res.images[block.animation_frame].width - 4,
                    4.0
                )
            }
        }


    }


    fun render_field_subset(consumable: dynamic, res: MyRes) {
        for (y in 0 until LEV_DIMENSION_Y) {
            for (x in 0 until LEV_DIMENSION_X) {
                var block = ktGame.level_array[x][y]
                if (y > 0 && ktGame.level_array[x][y - 1].moving && ktGame.level_array[x][y - 1].face_dir == DIR_DOWN && ktGame.level_array[x][y - 1].consumable == consumable) {
                    render_block(x, y - 1, RENDER_BOTTOM, res)
                }

                if (y > 0 && (!ktGame.level_array[x][y - 1].moving) && ktGame.level_array[x][y - 1].consumable == consumable) {
                    if (x > 0 && ktGame.level_array[x - 1][y].face_dir != DIR_RIGHT) {
                        render_block(x, y - 1, RENDER_BOTTOM_BORDER, res)
                    }
                }

                if (block.consumable == consumable) {
                    if (!block.moving || block.face_dir == DIR_LEFT || block.face_dir == DIR_RIGHT) {
                        render_block(x, y, RENDER_FULL, res)
                    } else if (block.face_dir == DIR_DOWN) {
                        render_block(x, y, RENDER_TOP, res)
                    } else if (block.face_dir == DIR_UP) {
                        render_block(x, y, RENDER_BOTTOM, res)
                    }
                }

                if (y + 1 < LEV_DIMENSION_Y && ktGame.level_array[x][y + 1].moving && ktGame.level_array[x][y + 1].face_dir == DIR_UP && ktGame.level_array[x][y + 1].consumable == consumable) {
                    render_block(x, y + 1, RENDER_TOP, res)
                }
            }
        }
    }


    fun kt_render_menu(input: MyInput, res: MyRes, gameHandler: GameHandler) {
        var submenu_offset = 0.0
        // The font is the same for the whole menu... Segoe UI is also nice
        MYCTX.font = "11px Tahoma"
        MYCTX.textAlign = CanvasTextAlign.LEFT
        MYCTX.textBaseline = CanvasTextBaseline.TOP

        for (i in 0 until vis.menu1.submenu_list.size) {
            var sm = vis.menu1.submenu_list[i]
            if (i == vis.menu1.submenu_open) {
                MYCTX.fillStyle = toRgbString(light_grey)
                MYCTX.fillRect(
                    vis.menu1.offset_x + submenu_offset,
                    (vis.menu1.offset_y + vis.menu1.height + 1).toDouble(),
                    sm.dd_width.toDouble(),
                    sm.dd_height.toDouble()
                )// Options box

                MYCTX.fillStyle = toRgbString(med_grey)
                MYCTX.fillRect(vis.menu1.offset_x + submenu_offset, vis.menu1.offset_y.toDouble(), sm.width, 1)
                MYCTX.fillRect(vis.menu1.offset_x + submenu_offset, vis.menu1.offset_y.toDouble(), 1, vis.menu1.height)
                MYCTX.fillRect(
                    vis.menu1.offset_x + submenu_offset + sm.dd_width - 2,
                    (vis.menu1.offset_y + vis.menu1.height + 2).toDouble(),
                    1.0,
                    (sm.dd_height - 2).toDouble()
                )// Options box
                MYCTX.fillRect(
                    vis.menu1.offset_x + submenu_offset + 1,
                    vis.menu1.offset_y + vis.menu1.height + sm.dd_height - 1,
                    sm.dd_width - 2,
                    1
                )// Options box

                MYCTX.fillStyle = toRgbString(white)
                MYCTX.fillRect(vis.menu1.offset_x + submenu_offset, vis.menu1.offset_y + vis.menu1.height, sm.width, 1)
                MYCTX.fillRect(
                    vis.menu1.offset_x + submenu_offset + sm.width - 1,
                    vis.menu1.offset_y,
                    1,
                    vis.menu1.height
                )
                MYCTX.fillRect(
                    vis.menu1.offset_x + submenu_offset + 1,
                    vis.menu1.offset_y + vis.menu1.height + 2,
                    1,
                    sm.dd_height - 3
                )// Options box
                MYCTX.fillRect(
                    vis.menu1.offset_x + submenu_offset + 1,
                    vis.menu1.offset_y + vis.menu1.height + 2,
                    sm.dd_width - 3,
                    1
                )// Options box

                MYCTX.fillStyle = toRgbString(dark_grey)
                MYCTX.fillRect(
                    vis.menu1.offset_x + submenu_offset + sm.dd_width - 1,
                    vis.menu1.offset_y + vis.menu1.height + 1,
                    1,
                    sm.dd_height
                )// Options box
                MYCTX.fillRect(
                    vis.menu1.offset_x + submenu_offset,
                    vis.menu1.offset_y + vis.menu1.height + sm.dd_height,
                    sm.dd_width - 1,
                    1
                )// Options box

                //input.mouse_pos.x
                var option_offset = vis.menu1.offset_y + vis.menu1.height + 4
                MYCTX.fillStyle = toRgbString(black)

                for (j in 0 until sm.options.size) {
                    var next_offset: Int
                    var check_image = 171
                    if (sm.options[j].line) {
                        next_offset = option_offset + sm.offset_line

                        MYCTX.fillStyle = toRgbString(med_grey)
                        MYCTX.fillRect(
                            vis.menu1.offset_x + submenu_offset + 3,
                            option_offset + 3,
                            sm.dd_width - 6,
                            1
                        )// Separator line
                        MYCTX.fillStyle = toRgbString(white)
                        MYCTX.fillRect(
                            vis.menu1.offset_x + submenu_offset + 3,
                            option_offset + 4,
                            sm.dd_width - 6,
                            1
                        )// Separator line

                    } else {
                        next_offset = option_offset + sm.offset_text
                    }

                    if (!sm.options[j].line && input.mouse_pos.x > vis.menu1.offset_x + submenu_offset && input.mouse_pos.x < vis.menu1.offset_x + submenu_offset + sm.dd_width &&
                        input.mouse_pos.y > option_offset && input.mouse_pos.y < next_offset
                    ) {
                        MYCTX.fillStyle = toRgbString(blue)
                        MYCTX.fillRect(
                            vis.menu1.offset_x + submenu_offset + 3,
                            option_offset,
                            sm.dd_width - 6,
                            sm.offset_text
                        )// Options box
                        MYCTX.fillStyle = toRgbString(white)

                        check_image = 172
                    } else if (!sm.options[j].on()) {
                        MYCTX.fillStyle = toRgbString(white)
                        MYCTX.fillText(sm.options[j].name, vis.menu1.offset_x + submenu_offset + 21, option_offset + 2)
                    } else {
                        MYCTX.fillStyle = toRgbString(black)
                    }

                    if (sm.options[j].on()) {
                        MYCTX.fillText(sm.options[j].name, vis.menu1.offset_x + submenu_offset + 20, option_offset + 1)
                    } else {
                        MYCTX.fillStyle = toRgbString(med_grey)
                        MYCTX.fillText(sm.options[j].name, vis.menu1.offset_x + submenu_offset + 20, option_offset + 1)
                    }

                    if (sm.options[j].check != 0) {
                        if ((sm.options[j].effect_id == 3 && gameHandler.isPaused()) || (sm.options[j].effect_id == 4 && ktGame.single_steps) || (sm.options[j].effect_id == 5 && ktGame.sound)) {
                            MYCTX.drawImage(
                                res.images[check_image],
                                vis.menu1.offset_x + submenu_offset + 6,
                                (option_offset + 6).toDouble()
                            )// Background
                        }
                    }

                    option_offset = next_offset

                }
            }
            MYCTX.fillStyle = toRgbString(black)
            MYCTX.fillText(sm.name, vis.menu1.offset_x + submenu_offset + 6, vis.menu1.offset_y + 3)
            submenu_offset += sm.width as Double
        }
    }


    fun render() {

        game.now = Date.now()
        val elapsed = game.now - game.then
        if (elapsed > game.fpsInterval) {
            game.then = game.now - (elapsed % game.fpsInterval)
            if (res.ready()) {
                if (!game.initialized) {
                    soundDataSource.set_volume(DEFAULT_VOLUME)
                    input.init(this)// Only init inputs after everything is loaded.
                    soundDataSource.play_sound(0)
                    game.initialized = true
                }
            }
            if (!gameHandler.isPaused()) {
                when (game.mode) {
                    GameMode.ENTRY -> {
                        game.wait_timer--
                        if (game.wait_timer <= 0) {
                            game.load_level(10)
                        }
                    }
                    GameMode.MENU -> {
                        if (game.wait_timer <= 0) {
                            when (game.levelState) {
                                GameState.RUNNING -> {
                                    game.update_tick++
                                    game.kt_update_entities(input)
                                }
                                GameState.WON -> {
                                    saveGameDataSource.update_savegame(game.level_number, gameHandler.getStepsTaken())
                                    game.next_level()
                                }
                                GameState.DIED -> {
                                    game.reset_level()
                                }
                            }
                        } else {
                            game.wait_timer--
                        }
                    }
                }
            }
            var now = Date.now()
            game.apply {
                delta_updated = now.minus(game.last_updated as Double)
                last_updated = now
                update_drawn = false
            }
        }

        if (game.update_drawn) {// This prevents the game from rendering the same thing twice
            window.requestAnimationFrame { render() }
            return
        }

        game.update_drawn = true
        if (res.ready()) {
            MYCTX.apply {
                drawImage(res.images[MyImage.Background], 0.0, 0.0)// Background
                drawImage(res.images[MyImage.Steps], 22.0, 41.0)// Steps
                drawImage(res.images[MyImage.Ladder], 427.0, 41.0)// Ladder
            }
            renderToolbar(res, TestCanvas(MYCTX))
            kt_render_buttons(MYCTX, input, res, game)

            when (game.mode) {
                GameMode.ENTRY -> {// Title image

                    MYCTX.apply {
                        drawImage(res.images[1], (LEV_OFFSET_X + 4).toDouble(), (LEV_OFFSET_Y + 4).toDouble())
                        fillStyle = "rgb(0, 0, 0)"
                        font = "bold 12px Helvetica"
                        textAlign = CanvasTextAlign.LEFT
                        textBaseline = CanvasTextBaseline.BOTTOM
                        fillText("JavaScript remake by ", 140.0, 234.0)
                    }
                }
                GameMode.MENU -> {
                    render_field(game, res)
                }
                GameMode.PLAY -> {// Won!
                    MYCTX.drawImage(res.images[170], LEV_OFFSET_X + 4, LEV_OFFSET_Y + 4)
                }
            }
            vis.renderVolumeBar()
            kt_render_menu(input, res, gameHandler)
        } else {
            MYCTX.apply {
                fillStyle = toRgbString(light_grey)
                fillRect(0, 0, GameSettings.SCREEN_WIDTH, GameSettings.SCREEN_HEIGHT)// Options box
                fillStyle = "rgb(0, 0, 0)"
                font = "36px Helvetica"
                textAlign = CanvasTextAlign.CENTER
                textBaseline = CanvasTextBaseline.MIDDLE
                fillText(
                    "Loading...",
                    (GameSettings.SCREEN_WIDTH / 2).toDouble(),
                    (GameSettings.SCREEN_HEIGHT / 2).toDouble()
                )
            }
        }
        if (DEBUG) vis.render_fps(game)
        window.requestAnimationFrame { render() }
        // js("")
    }

    private fun handleOnKeyDown(evt: KeyboardEvent) {
        soundDataSource.remove_soundrestriction()

        when (evt.keyCode) {
            Key.ARROWLEFT.value -> {
                game.walk_dir = DIR_LEFT
            }
            Key.ARROWUP.value -> {
                game.walk_dir = DIR_UP
            }
            Key.ARROWRIGHT.value -> {
                game.walk_dir = DIR_RIGHT
            }
            Key.ARROWDWN.value -> {
                game.walk_dir = DIR_DOWN
            }
        }

        if (dbx.firstChild) {// If a dialog box is open
            when (evt.keyCode) {
                Key.ENTER.value -> {// Enter
                    dbx.enterfun()
                }
                Key.ESCAPE.value -> {// Esc
                    dbx.cancelfun()
                }
            }
        }
    }

    override fun onKeyDown(evt: KeyboardEvent) {
        handleOnKeyDown(evt)
    }

    override fun onMouseUp(evt: MouseEvent, mousePos: dynamic) {


    }


}