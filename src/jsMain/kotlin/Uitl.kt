import App.Companion.DIR_DOWN
import App.Companion.DIR_LEFT
import App.Companion.DIR_NONE
import App.Companion.DIR_RIGHT
import App.Companion.DIR_UP
import kotlin.math.abs

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


fun can_see_tile(eye_x: Int, eye_y: Int, tile_x: Int, tile_y: Int, level_array: Array<Array<KtEntity>>): Boolean {
    val diff_x = tile_x - eye_x
    val diff_y = tile_y - eye_y

    var walk1_x: Int
    var walk1_y: Int
    var walk2_x: Int
    var walk2_y: Int

    when {
        diff_x == 0 -> {
            when {
                diff_y == 0 -> {
                    return true
                }
                diff_y > 0 -> {
                    walk1_x = 0
                    walk1_y = 1
                    walk2_x = 0
                    walk2_y = 1
                }
                else -> {// diff_y < 0
                    walk1_x = 0
                    walk1_y = -1
                    walk2_x = 0
                    walk2_y = -1
                }
            }
        }
        diff_x > 0 -> {
            when {
                diff_y == 0 -> {
                    walk1_x = 1
                    walk1_y = 0
                    walk2_x = 1
                    walk2_y = 0
                }
                diff_y > 0 -> {
                    when {
                        diff_y > diff_x -> {
                            walk1_x = 0
                            walk1_y = 1
                            walk2_x = 1
                            walk2_y = 1
                        }
                        diff_y == diff_x -> {
                            walk1_x = 1
                            walk1_y = 1
                            walk2_x = 1
                            walk2_y = 1
                        }
                        else -> {// diff_y < diff_x
                            walk1_x = 1
                            walk1_y = 0
                            walk2_x = 1
                            walk2_y = 1
                        }
                    }
                }
                else -> {// diff_y < 0
                    when {
                        diff_y * (-1) > diff_x -> {
                            walk1_x = 0
                            walk1_y = -1
                            walk2_x = 1
                            walk2_y = -1
                        }
                        diff_y * (-1) == diff_x -> {
                            walk1_x = 1
                            walk1_y = -1
                            walk2_x = 1
                            walk2_y = -1
                        }
                        else -> {// diff_y < diff_x
                            walk1_x = 1
                            walk1_y = 0
                            walk2_x = 1
                            walk2_y = -1
                        }
                    }
                }
            }
        }
        else -> {// diff_x < 0
            when {
                diff_y == 0 -> {
                    walk1_x = -1
                    walk1_y = 0
                    walk2_x = -1
                    walk2_y = 0
                }
                diff_y > 0 -> {
                    when {
                        diff_y > diff_x * (-1) -> {
                            walk1_x = 0
                            walk1_y = 1
                            walk2_x = -1
                            walk2_y = 1
                        }
                        diff_y == diff_x * (-1) -> {
                            walk1_x = -1
                            walk1_y = 1
                            walk2_x = -1
                            walk2_y = 1
                        }
                        else -> {// diff_y < diff_x
                            walk1_x = -1
                            walk1_y = 0
                            walk2_x = -1
                            walk2_y = 1
                        }
                    }
                }
                else -> {// diff_y < 0
                    when {
                        diff_y > diff_x -> {
                            walk1_x = -1
                            walk1_y = 0
                            walk2_x = -1
                            walk2_y = -1
                        }
                        diff_y == diff_x -> {
                            walk1_x = -1
                            walk1_y = -1
                            walk2_x = -1
                            walk2_y = -1
                        }
                        else -> {// diff_y < diff_x
                            walk1_x = 0
                            walk1_y = -1
                            walk2_x = -1
                            walk2_y = -1
                        }
                    }
                }
            }
        }
    }


    var x_offset = 0
    var y_offset = 0
    var x_ratio1: Int
    var y_ratio1: Int
    var x_ratio2: Int
    var y_ratio2: Int
    var diff1: Int
    var diff2: Int

    while (true) {
        if (diff_x != 0) {
            x_ratio1 = (x_offset + walk1_x) / diff_x
            x_ratio2 = (x_offset + walk2_x) / diff_x
        } else {
            x_ratio1 = 1
            x_ratio2 = 1
        }
        if (diff_y != 0) {
            y_ratio1 = (y_offset + walk1_y) / diff_y
            y_ratio2 = (y_offset + walk2_y) / diff_y
        } else {
            y_ratio1 = 1
            y_ratio2 = 1
        }

        diff1 = abs(x_ratio1 - y_ratio1)
        diff2 = abs(x_ratio2 - y_ratio2)

        if (diff1 <= diff2) {
            x_offset += walk1_x
            y_offset += walk1_y
        } else {
            x_offset += walk2_x
            y_offset += walk2_y
        }

        if (x_offset == diff_x && y_offset == diff_y) {
            return true
        }
        console.log("EXER  " + eye_x)
        console.log("EXERx_offset  " + x_offset)
        console.log("EXERy  " + eye_y)
        console.log("EXERy_offset  " + y_offset)
        x_offset = if (x_offset < 0) {
            return false

        } else {
            x_offset
        }
        y_offset = if (y_offset < 0) {
            return false

        } else {
            y_offset
        }
        if (level_array[eye_x + x_offset][eye_y + y_offset].id != 0 && level_array[eye_x + x_offset][eye_y + y_offset].id != -1 && !level_array[eye_x + x_offset][eye_y + y_offset].is_small) {
            return false
        }
    }
    // Code here is unreachable

}