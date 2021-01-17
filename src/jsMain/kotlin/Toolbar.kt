import kotlin.math.floor
import kotlin.math.min
import kotlin.random.Random

class Toolbar(val res: MyRes, val myCanvas: MyCanvas) {

    fun renderLevelCounter(levelNumber: String) {
        val level_string = levelNumber
        val level_length = min(level_string.length - 1, 4)

        for (i in level_length downTo 0) {

            val newChar = level_string[i].toString()
            val imageId2 = 11 + newChar.toInt()//newChar

            myCanvas.drawImage(res.images[imageId2], (506 - (level_length - i) * 13).toDouble(), 41.0)
        }

        for (i in (level_length + 1) until 5) {
            myCanvas.drawImage(res.images[21], 506 - i * 13, 41)
        }
    }

    fun renderStepsDisplay(steps_string: String) {

        val steps_length = min(steps_string.length - 1, 4)

        for (i in steps_length downTo 0) {
            val newChar = steps_string[i].toString()
            val imageId2 = 11 + newChar.toInt()//newChar
            myCanvas.drawImage(res.images[imageId2], (101 - (steps_length - i) * 13).toDouble(), 41.0)
        }

        for (i in (steps_length + 1) until 5) {
            myCanvas.drawImage(res.images[21], (101 - i * 13).toDouble(), 41.0)
        }
    }

    fun kt_render_buttons(input: MyInput, game: KtGame, vis: KtVisual) {
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
                myCanvas.drawImage(res.images[26], 219, 35)// << pressed
            } else {
                myCanvas.drawImage(res.images[23], 219, 35)// << up
            }
        } else {
            myCanvas.drawImage(res.images[29], 219, 35)// << disabled
        }

        if (vis.buttons_pressed[1]) {
            myCanvas.drawImage(res.images[25], 253, 35)// Berti pressed
        } else {
            if (vis.berti_blink_time < 100) {
                myCanvas.drawImage(res.images[22], 253, 35)// Berti up
                if (vis.berti_blink_time == 0) {
                    vis.berti_blink_time = 103//Math.floor(100+(Math.random()*100)+1);
                } else {
                    vis.berti_blink_time--
                }
            } else {
                myCanvas.drawImage(res.images[28], 253, 35)// Berti up blink
                if (vis.berti_blink_time == 100) {
                    vis.berti_blink_time = floor((Random.nextDouble() * 95) + 5).toInt()
                } else {
                    vis.berti_blink_time--
                }
            }
        }

        if (game.buttons_activated[2]) {
            if (vis.buttons_pressed[2]) {
                myCanvas.drawImage(res.images[27], 287, 35)// >> pressed
            } else {
                myCanvas.drawImage(res.images[24], 287, 35)// >> up
            }
        } else {
            myCanvas.drawImage(res.images[30], 287, 35)// >> disabled
        }
    }
}