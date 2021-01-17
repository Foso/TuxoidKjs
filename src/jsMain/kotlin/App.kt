import GameSettings.Companion.JOYSTICK_SIZE
import data.savegame.SaveGameDataRepository
import data.savegame.SaveGameDataSource
import data.sound.SoundDataSource
import data.sound.SoundRepository
import de.jensklingenberg.bananiakt.MyImage
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
import org.w3c.dom.events.KeyboardEvent
import org.w3c.dom.events.MouseEvent
import ui.menu.kt_render_buttons
import ui.menu.kt_render_menu
import ui.menu.render_field
import ui.volumebar.VolumeBar
import kotlin.js.Date
import kotlin.math.min

enum class GameMode(val value: Int) {
    ENTRY(0), MENU(1), PLAY(2)
}

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
        lateinit var MYCTX: CanvasRenderingContext2D
        lateinit var ktGame: KtGame

    }

    private val gameHandler: GameHandler = GameHandlerImpl()
    private val MYCANVAS: HTMLCanvasElement = document.createElement("canvas") as HTMLCanvasElement
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
    val vis = KtVisual(vol_bar, input, res, saveGameDataSource, soundDataSource, game)

    fun initCanvas() {

        MYCTX = MYCANVAS.getContext("2d") as CanvasRenderingContext2D
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



    fun render_displays(res: MyRes, myCanvas: MyCanvas) {

        val steps_string = gameHandler.getStepsTaken().toString()
        val steps_length = min(steps_string.length - 1, 4)

        for (i in steps_length downTo 0) {
            val newChar = steps_string[i].toString()
            val imageId2 = 11 + newChar.toInt()//newChar
            MYCTX.drawImage(res.images[imageId2], (101 - (steps_length - i) * 13).toDouble(), 41.0)
        }

        for (i in (steps_length + 1) until 5) {
            myCanvas.drawImage(res.images[21], (101 - i * 13).toDouble(), 41.0)
        }

        val level_string = game.level_number.toString()
        val level_length = min(level_string.length - 1, 4)

        for (i in level_length downTo 0) {

            val newChar = level_string[i].toString()
            val imageId2 = 11 + newChar.toInt()//newChar
            val imageId = js("11+parseInt(level_string.charAt(i))")

            MYCTX.drawImage(res.images[imageId2], (506 - (level_length - i) * 13).toDouble(), 41.0)
        }

        for (i in (level_length + 1) until 5) {
            MYCTX.drawImage(res.images[21], 506 - i * 13, 41)
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
            render_displays(res, TestCanvas(MYCTX))
            kt_render_buttons(MYCTX, input, res, game, vis)

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
            vis.render_vol_bar()
            kt_render_menu(input, res, gameHandler)
        } else {
            MYCTX.apply {
                fillStyle = "rgb(" + vis.light_grey.r + ", " + vis.light_grey.g + ", " + vis.light_grey.b + ")"
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

        if (vis.dbx.firstChild) {// If a dialog box is open
            when (evt.keyCode) {
                Key.ENTER.value -> {// Enter
                    vis.dbx.enterfun()
                }
                Key.ESCAPE.value -> {// Esc
                    vis.dbx.cancelfun()
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