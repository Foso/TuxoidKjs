import App.Companion.UPS
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import kotlin.math.min
import kotlin.math.round

external var md5: dynamic

lateinit var res: MyRes

lateinit var vis: KtVisual
lateinit var game: KtGame
var JOYSTICK_SIZE = 0.4;// In terms of the smaller of the two screen dimensions


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

lateinit var input: MyInput
lateinit var MYCTX: CanvasRenderingContext2D
var IS_TOUCH_DEVICE: Boolean = false
var true_width: Double = App.SCREEN_WIDTH.toDouble()
var true_height: Double = App.SCREEN_HEIGHT.toDouble()
lateinit var MyJOYSTICK: HTMLCanvasElement




fun main() {
    App.initCanvas()
    val app = App()
    input = app.input
    game = app.game
    vis = app.vis
    res = app.res
   // vis.init_menus()
    requestAnimationFrame()


    checkIfTouch()
    if (IS_TOUCH_DEVICE) {
        UPS = 15
        app.initTouch()
    }

    app.render(app.game)
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



fun checkIfTouch() {
    IS_TOUCH_DEVICE =
        true == js("(\"ontouchstart\" in window || window.DocumentTouch && document instanceof DocumentTouch);")

}

fun Double.toFixed(digits: Int): Double = this.asDynamic().toFixed(digits)
fun Float.format(digits: Int): String = this.asDynamic().toFixed(digits)




fun CanvasRenderingContext2D.fillRect(x: Int, y: Int, w: Int, h: Int) {
    fillRect(x.toDouble(), y.toDouble(), w.toDouble(), h.toDouble())
}

fun CanvasRenderingContext2D.drawImage(image: dynamic, dx: Int, dy: Int) {
    drawImage(image, dx.toDouble(), dy.toDouble())
}








