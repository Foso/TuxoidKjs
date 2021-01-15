import App.Companion.DIR_DOWN
import App.Companion.DIR_LEFT
import App.Companion.DIR_NONE
import App.Companion.DIR_RIGHT
import App.Companion.DIR_UP

fun opposite_dir(dir: Int): Int {
    when (dir) {
        DIR_UP -> {
            return DIR_DOWN;
        }

        DIR_DOWN -> {
            return DIR_UP;

        }
        DIR_LEFT -> {
            return DIR_RIGHT;
        }

        DIR_RIGHT -> {
            return DIR_LEFT;
        }
        else -> {
            return DIR_NONE
        }
    }
}