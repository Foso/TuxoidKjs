import kotlinx.browser.document

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.round
import kotlin.random.Random

external interface Block {

    val just_moved: Boolean
    var animation_delay: Int
    val moving_offset: dynamic
    val gets_removed_in: Int
    val pushing: Boolean
    val face_dir: Int
    val moving: Boolean
    var fine_offset_y: Int
    var fine_offset_x: Int
    var animation_frame: Int
    val id: Int

}


external interface Visual {
    fun update_animation(currX: Int, currY: Int)
    fun update_all_animations()
    fun init_animation()
    fun open_dbx(dbxConfirm: Int, i: Int)
    fun open_dbx(dbxConfirm: Int)
    fun error_dbx(errEmptyname: Int)

    val vol_bar: VolumeBar
    val dbx: Dbx
    val blue: Rgb
    val black: Rgb
    val dark_grey: Rgb
    val white: Rgb
    val med_grey: Rgb
    val light_grey: Rgb
    var berti_blink_time: Int
    val buttons_pressed: Array<Boolean>
    val menu1: dynamic
}

@JsExport
fun add_text(text: String, pos_x: Int, pos_y: Int, that: dynamic) {
    var txt = document.createElement("p").asDynamic();
    txt.innerHTML = text;
    txt.style.position = "absolute";
    txt.style.left = pos_x.toString() + "px";
    txt.style.top = pos_y.toString() + "px";
    txt.style.fontFamily = "Tahoma";
    txt.style.fontSize = "12px";
    that.dbx.appendChild(txt);
}

@JsExport
fun add_number(a_num: Int, pos_x: Int, pos_y: Int, width: dynamic, height: dynamic, that: dynamic) {
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
    that.dbx.appendChild(num);
}




@JsExport
fun kt_can_see_tile(eye_x: Int, eye_y: Int, tile_x: Int, tile_y: Int): Boolean {
    var diff_x = tile_x - eye_x;
    var diff_y = tile_y - eye_y;

    var walk1_x: Int;
    var walk1_y: Int;
    var walk2_x: Int;
    var walk2_y: Int;

    if (diff_x == 0) {
        if (diff_y == 0) {
            return true;
        } else if (diff_y > 0) {
            walk1_x = 0;
            walk1_y = 1;
            walk2_x = 0;
            walk2_y = 1;
        } else {// diff_y < 0
            walk1_x = 0;
            walk1_y = -1;
            walk2_x = 0;
            walk2_y = -1;
        }
    } else if (diff_x > 0) {
        if (diff_y == 0) {
            walk1_x = 1;
            walk1_y = 0;
            walk2_x = 1;
            walk2_y = 0;
        } else if (diff_y > 0) {
            if (diff_y > diff_x) {
                walk1_x = 0;
                walk1_y = 1;
                walk2_x = 1;
                walk2_y = 1;
            } else if (diff_y == diff_x) {
                walk1_x = 1;
                walk1_y = 1;
                walk2_x = 1;
                walk2_y = 1;
            } else {// diff_y < diff_x
                walk1_x = 1;
                walk1_y = 0;
                walk2_x = 1;
                walk2_y = 1;
            }
        } else {// diff_y < 0
            if (diff_y * (-1) > diff_x) {
                walk1_x = 0;
                walk1_y = -1;
                walk2_x = 1;
                walk2_y = -1;
            } else if (diff_y * (-1) == diff_x) {
                walk1_x = 1;
                walk1_y = -1;
                walk2_x = 1;
                walk2_y = -1;
            } else {// diff_y < diff_x
                walk1_x = 1;
                walk1_y = 0;
                walk2_x = 1;
                walk2_y = -1;
            }
        }
    } else {// diff_x < 0
        if (diff_y == 0) {
            walk1_x = -1;
            walk1_y = 0;
            walk2_x = -1;
            walk2_y = 0;
        } else if (diff_y > 0) {
            if (diff_y > diff_x * (-1)) {
                walk1_x = 0;
                walk1_y = 1;
                walk2_x = -1;
                walk2_y = 1;
            } else if (diff_y == diff_x * (-1)) {
                walk1_x = -1;
                walk1_y = 1;
                walk2_x = -1;
                walk2_y = 1;
            } else {// diff_y < diff_x
                walk1_x = -1;
                walk1_y = 0;
                walk2_x = -1;
                walk2_y = 1;
            }
        } else {// diff_y < 0
            if (diff_y > diff_x) {
                walk1_x = -1;
                walk1_y = 0;
                walk2_x = -1;
                walk2_y = -1;
            } else if (diff_y == diff_x) {
                walk1_x = -1;
                walk1_y = -1;
                walk2_x = -1;
                walk2_y = -1;
            } else {// diff_y < diff_x
                walk1_x = 0;
                walk1_y = -1;
                walk2_x = -1;
                walk2_y = -1;
            }
        }
    }
    var x_offset = 0;
    var y_offset = 0;
    var x_ratio1: Int;
    var y_ratio1: Int;
    var x_ratio2: Int;
    var y_ratio2: Int;
    var diff1: Int;
    var diff2: Int;

    while (true) {
        if (diff_x != 0) {
            x_ratio1 = (x_offset + walk1_x) / diff_x;
            x_ratio2 = (x_offset + walk2_x) / diff_x;
        } else {
            x_ratio1 = 1;
            x_ratio2 = 1;
        }
        if (diff_y != 0) {
            y_ratio1 = (y_offset + walk1_y) / diff_y;
            y_ratio2 = (y_offset + walk2_y) / diff_y;
        } else {
            y_ratio1 = 1;
            y_ratio2 = 1;
        }

        diff1 = abs(x_ratio1 - y_ratio1);
        diff2 = abs(x_ratio2 - y_ratio2);

        if (diff1 <= diff2) {
            x_offset += walk1_x;
            y_offset += walk1_y;
        } else {
            x_offset += walk2_x;
            y_offset += walk2_y;
        }

        if (x_offset == diff_x && y_offset == diff_y) {
            return true;
        }
        if (game.level_array[eye_x + x_offset][eye_y + y_offset].id != 0 && game.level_array[eye_x + x_offset][eye_y + y_offset].id != -1 && !game.level_array[eye_x + x_offset][eye_y + y_offset].is_small) {
            return false;
        }
    }
    // Code here is unreachable

}


@JsExport
class KtVisual(that: dynamic){


    fun kt_init_animation(that: dynamic, game: dynamic) {

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
                        block.fine_offset_x = that.offset_banana_x;
                        block.fine_offset_y = that.offset_banana_y;
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
                        block.fine_offset_x = that.offset_key_x;
                        block.fine_offset_y = that.offset_key_y;
                        break
                    }
                    14 -> {
                        // Key 2
                        block.animation_frame = 4;
                        block.fine_offset_x = that.offset_key_x;
                        block.fine_offset_y = that.offset_key_y;
                        break
                    }
                    15 -> {
                        // Key 3
                        block.animation_frame = 5;
                        block.fine_offset_x = that.offset_key_x;
                        block.fine_offset_y = that.offset_key_y;
                        break
                    }
                    16 -> {
                        // Key 4
                        block.animation_frame = 6;
                        block.fine_offset_x = that.offset_key_x;
                        block.fine_offset_y = that.offset_key_y;
                        break
                    }
                    17 -> {
                        // Key 5
                        block.animation_frame = 7;
                        block.fine_offset_x = that.offset_key_x;
                        block.fine_offset_y = that.offset_key_y;
                        break
                    }
                    18 -> {
                        // Key 6
                        block.animation_frame = 8;
                        block.fine_offset_x = that.offset_key_x;
                        block.fine_offset_y = that.offset_key_y;
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
    fun kt_update_animation(x: Int, y: Int) {
        var block = game.level_array[x][y] as Block;
        when (block.id) {
            1, 2 -> {
                kt_update_animation_case2(x, y, block)
            }
            7 -> {
                // Purple monster (Monster 2)
                kt_update_animation_case7(x, y, block);
            }
            10 -> {
                // Green monster (Monster 2)
                kt_update_animation_case10(x, y, block);
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
fun kt_update_animation_case10(x: Int, y: Int, block: Block) {
    // Green monster (Monster 2)
    block.fine_offset_x = 0;
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

@JsExport
fun kt_update_animation_case7(x: Int, y: Int, block: Block) {
    // Purple monster (Monster 2)
    block.fine_offset_x = 0;
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

@JsExport
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

@JsExport
fun kt_render_block(x: Int, y: Int, render_option: dynamic) {
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
fun render_vol_bar() {
    var vb = vis.vol_bar;
    var switcher = false;

    for(i in 0 until vb.width){
        var line_height:Int=0

        if(switcher){
            switcher = false;
            CTX.fillStyle = "rgb("+vb.colour_4.r+", "+vb.colour_4.g+", "+vb.colour_4.b+")";
        }else{
            switcher = true;
            var ratio2 = i/ vb.width.toDouble();
            line_height = round((vb.height*ratio2).toDouble()).toInt();

            if(i < ceil(vb.volume*vb.width)){
                if(game.sound){
                    var ratio1 = 1-ratio2;
                    CTX.fillStyle = "rgb("+round(vb.colour_1.r*ratio1+vb.colour_2.r*ratio2)+", "+round(vb.colour_1.g*ratio1+vb.colour_2.g*ratio2)+", "+round(vb.colour_1.b*ratio1+vb.colour_2.b*ratio2)+")";
                }else{
                    CTX.fillStyle = "rgb("+vb.colour_5.r+", "+vb.colour_5.g+", "+vb.colour_5.b+")";
                }
            }else{
                CTX.fillStyle = "rgb("+vb.colour_3.r+", "+vb.colour_3.g+", "+vb.colour_3.b+")";
            }

        }
        CTX.fillRect(vb.offset_x+i, vb.offset_y+vb.height-line_height, 1, line_height);

    }
}

@JsExport
fun kt_render_menu() {
    var submenu_offset = 0.0;
    // The font is the same for the whole menu... Segoe UI is also nice
    CTX.font = "11px Tahoma";
    CTX.textAlign = "left";
    CTX.textBaseline = "top";

    for(i in 0 until vis.menu1.submenu_list.length){
        var sm = vis.menu1.submenu_list[i];
        if(i == vis.menu1.submenu_open){
            CTX.fillStyle = "rgb("+vis.light_grey.r+", "+vis.light_grey.g+", "+vis.light_grey.b+")";
            CTX.fillRect(vis.menu1.offset_x + submenu_offset, vis.menu1.offset_y + vis.menu1.height + 1, sm.dd_width, sm.dd_height);// Options box

            CTX.fillStyle = "rgb("+vis.med_grey.r+", "+vis.med_grey.g+", "+vis.med_grey.b+")";
            CTX.fillRect(vis.menu1.offset_x + submenu_offset, vis.menu1.offset_y, sm.width, 1);
            CTX.fillRect(vis.menu1.offset_x + submenu_offset, vis.menu1.offset_y, 1, vis.menu1.height);
            CTX.fillRect(vis.menu1.offset_x + submenu_offset + sm.dd_width - 2, vis.menu1.offset_y + vis.menu1.height + 2, 1, sm.dd_height - 2);// Options box
            CTX.fillRect(vis.menu1.offset_x + submenu_offset + 1, vis.menu1.offset_y + vis.menu1.height + sm.dd_height - 1, sm.dd_width - 2, 1);// Options box

            CTX.fillStyle = "rgb("+vis.white.r+", "+vis.white.g+", "+vis.white.b+")";
            CTX.fillRect(vis.menu1.offset_x + submenu_offset, vis.menu1.offset_y + vis.menu1.height, sm.width, 1);
            CTX.fillRect(vis.menu1.offset_x + submenu_offset + sm.width - 1, vis.menu1.offset_y, 1, vis.menu1.height);
            CTX.fillRect(vis.menu1.offset_x + submenu_offset + 1, vis.menu1.offset_y + vis.menu1.height + 2, 1, sm.dd_height - 3);// Options box
            CTX.fillRect(vis.menu1.offset_x + submenu_offset + 1, vis.menu1.offset_y + vis.menu1.height + 2, sm.dd_width - 3, 1);// Options box

            CTX.fillStyle = "rgb("+vis.dark_grey.r+", "+vis.dark_grey.g+", "+vis.dark_grey.b+")";
            CTX.fillRect(vis.menu1.offset_x + submenu_offset + sm.dd_width - 1, vis.menu1.offset_y + vis.menu1.height + 1, 1, sm.dd_height);// Options box
            CTX.fillRect(vis.menu1.offset_x + submenu_offset, vis.menu1.offset_y + vis.menu1.height + sm.dd_height, sm.dd_width - 1, 1);// Options box

            //input.mouse_pos.x
            var option_offset = vis.menu1.offset_y + vis.menu1.height + 4;
            CTX.fillStyle = "rgb("+vis.black.r+", "+vis.black.g+", "+vis.black.b+")";

            for(j in 0 until sm.options.length){
                var next_offset:Int;
                var check_image = 171;
                if(sm.options[j].line){
                    next_offset = option_offset + sm.offset_line;

                    CTX.fillStyle = "rgb("+vis.med_grey.r+", "+vis.med_grey.g+", "+vis.med_grey.b+")";
                    CTX.fillRect(vis.menu1.offset_x + submenu_offset + 3 , option_offset + 3, sm.dd_width - 6, 1);// Separator line
                    CTX.fillStyle = "rgb("+vis.white.r+", "+vis.white.g+", "+vis.white.b+")";
                    CTX.fillRect(vis.menu1.offset_x + submenu_offset + 3 , option_offset + 4, sm.dd_width - 6, 1);// Separator line

                }else{
                    next_offset = option_offset + sm.offset_text;
                }

                if(!sm.options[j].line && input.mouse_pos.x > vis.menu1.offset_x + submenu_offset && input.mouse_pos.x < vis.menu1.offset_x + submenu_offset + sm.dd_width &&
                    input.mouse_pos.y > option_offset && input.mouse_pos.y < next_offset){
                    CTX.fillStyle = "rgb("+vis.blue.r+", "+vis.blue.g+", "+vis.blue.b+")";
                    CTX.fillRect(vis.menu1.offset_x + submenu_offset + 3 , option_offset, sm.dd_width - 6, sm.offset_text);// Options box
                    CTX.fillStyle = "rgb("+vis.white.r+", "+vis.white.g+", "+vis.white.b+")";

                    check_image = 172;
                }else if(!sm.options[j].on()){
                    CTX.fillStyle = "rgb("+vis.white.r+", "+vis.white.g+", "+vis.white.b+")";
                    CTX.fillText(sm.options[j].name, vis.menu1.offset_x + submenu_offset + 21, option_offset + 2);
                }else{
                    CTX.fillStyle = "rgb("+vis.black.r+", "+vis.black.g+", "+vis.black.b+")";
                }

                if(sm.options[j].on()){
                    CTX.fillText(sm.options[j].name, vis.menu1.offset_x + submenu_offset + 20, option_offset + 1);
                }else{
                    CTX.fillStyle = "rgb("+vis.med_grey.r+", "+vis.med_grey.g+", "+vis.med_grey.b+")";
                    CTX.fillText(sm.options[j].name, vis.menu1.offset_x + submenu_offset + 20, option_offset + 1);
                }

                if(sm.options[j].check != 0){
                    if((sm.options[j].effect_id == 3 && game.paused) || (sm.options[j].effect_id == 4 && game.single_steps) || (sm.options[j].effect_id == 5 && game.sound)){
                        CTX.drawImage(res.images[check_image], vis.menu1.offset_x + submenu_offset + 6, option_offset + 6);// Background
                    }
                }

                option_offset = next_offset;

            }
        }
        CTX.fillStyle = "rgb("+vis.black.r+", "+vis.black.g+", "+vis.black.b+")";
        CTX.fillText(sm.name, vis.menu1.offset_x + submenu_offset + 6, vis.menu1.offset_y + 3);
        submenu_offset += sm.width as Double;
    }

}