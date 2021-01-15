package ui.menu

import ANIMATION_DURATION
import App.Companion.DIR_DOWN
import App.Companion.DIR_LEFT
import App.Companion.DIR_RIGHT
import App.Companion.DIR_UP
import KtGame
import LEV_DIMENSION_X
import LEV_DIMENSION_Y
import LEV_OFFSET_X
import LEV_OFFSET_Y
import MYCTX
import RENDER_BOTTOM
import RENDER_BOTTOM_BORDER
import RENDER_FULL
import RENDER_TOP
import drawImage
import fillRect
import fillText
import game
import input
import org.w3c.dom.CanvasTextAlign
import org.w3c.dom.CanvasTextBaseline
import org.w3c.dom.LEFT
import org.w3c.dom.TOP
import model.Block
import org.w3c.dom.CanvasRenderingContext2D
import res
import vis
import kotlin.math.floor
import kotlin.random.Random




fun kt_render_buttons(MYCTX: CanvasRenderingContext2D) {
    var over_button = false;
    if (input.mouse_down) {
        if (input.mouse_pos.y >= 35 && input.mouse_pos.y <= 65) {
            when {
                input.mouse_pos.x >= 219 && input.mouse_pos.x <= 249 && input.lastclick_button == 0 -> {
                    vis.buttons_pressed[0] = true;
                    over_button = true;
                }
                input.mouse_pos.x >= 253 && input.mouse_pos.x <= 283 && input.lastclick_button == 1 -> {
                    vis.buttons_pressed[1] = true;
                    over_button = true;
                }
                input.mouse_pos.x >= 287 && input.mouse_pos.x <= 317 && input.lastclick_button == 2 -> {
                    vis.buttons_pressed[2] = true;
                    over_button = true;
                }
            }
        }
    }
    if (!over_button) {
        vis.buttons_pressed[0] = vis.buttons_pressed[1] == vis.buttons_pressed[2] == false;
    }

    if (game.buttons_activated[0]) {
        if (vis.buttons_pressed[0]) {
            MYCTX.drawImage(res.images[26], 219, 35);// << pressed
        } else {
            MYCTX.drawImage(res.images[23], 219, 35);// << up
        }
    } else {
        MYCTX.drawImage(res.images[29], 219, 35);// << disabled
    }

    if (vis.buttons_pressed[1]) {
        MYCTX.drawImage(res.images[25], 253, 35);// Berti pressed
    } else {
        if (vis.berti_blink_time < 100) {
            MYCTX.drawImage(res.images[22], 253, 35);// Berti up
            if (vis.berti_blink_time == 0) {
                vis.berti_blink_time = 103;//Math.floor(100+(Math.random()*100)+1);
            } else {
                vis.berti_blink_time--;
            }
        } else {
            MYCTX.drawImage(res.images[28], 253, 35);// Berti up blink
            if (vis.berti_blink_time == 100) {
                vis.berti_blink_time = floor((Random.nextDouble() * 95) + 5).toInt();
            } else {
                vis.berti_blink_time--;
            }
        }
    }

    if (game.buttons_activated[2]) {
        if (vis.buttons_pressed[2]) {
            MYCTX.drawImage(res.images[27], 287, 35);// >> pressed
        } else {
            MYCTX.drawImage(res.images[24], 287, 35);// >> up
        }
    } else {
        MYCTX.drawImage(res.images[30], 287, 35);// >> disabled
    }
}


fun render_field(game: KtGame) {
    render_field_subset(true);// Consumables in the background
    render_field_subset(false);// The rest in the foreground

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
    );// Bottom border covering blocks
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
    );// Right border covering blocks

    when (game.level_ended) {
        1 -> {// Berti cheering, wow or yeah
            for (i in game.berti_positions.indices) {
                if (game.wow) {
                    MYCTX.drawImage(
                        res.images[168],
                        LEV_OFFSET_X + 24 * game.berti_positions[i].x + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.x as Int + vis.offset_wow_x,
                        LEV_OFFSET_Y + 24 * game.berti_positions[i].y + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.y as Int + vis.offset_wow_y
                    );
                } else {
                    MYCTX.drawImage(
                        res.images[169],
                        LEV_OFFSET_X + 24 * game.berti_positions[i].x + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.x as Int + vis.offset_yeah_x,
                        LEV_OFFSET_Y + 24 * game.berti_positions[i].y + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.y as Int + vis.offset_yeah_y
                    );
                }
            }
        }
        2 -> {// Berti dies in a pool of blood
            for (i in game.berti_positions.indices) {
                MYCTX.drawImage(
                    res.images[167],
                    LEV_OFFSET_X + 24 * game.berti_positions[i].x + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.x as Int + vis.offset_argl_x,
                    LEV_OFFSET_Y + 24 * game.berti_positions[i].y + game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.y as Int + vis.offset_argl_y
                );
            }
        }
    }
}



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
                // 2: AUTO ui.menu.Menu Berti
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
            MYCTX.drawImage(
                res.images[block.animation_frame],
                LEV_OFFSET_X + 24 * x + offset_x + block.fine_offset_x,
                LEV_OFFSET_Y + 24 * y + offset_y + block.fine_offset_y
            );
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
                );
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
                );
            }
        } else if (render_option == RENDER_BOTTOM) {// Render bottom
            var imgsize_offset: Int = res.images[block.animation_frame].height - 24;

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
                );
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
                );
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
            );
        }
    }


}


fun render_field_subset(consumable: dynamic) {
    for (y in 0 until LEV_DIMENSION_Y) {
        for (x in 0 until LEV_DIMENSION_X) {
            var block = game.level_array[x][y];
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


fun kt_render_menu() {
    var submenu_offset = 0.0;
    // The font is the same for the whole menu... Segoe UI is also nice
    MYCTX.font = "11px Tahoma";
    MYCTX.textAlign = CanvasTextAlign.LEFT;
    MYCTX.textBaseline = CanvasTextBaseline.TOP;

    for (i in 0 until vis.menu1.submenu_list.size) {
        var sm = vis.menu1.submenu_list[i];
        if (i == vis.menu1.submenu_open) {
            MYCTX.fillStyle = "rgb(" + vis.light_grey.r + ", " + vis.light_grey.g + ", " + vis.light_grey.b + ")";
            MYCTX.fillRect(
                vis.menu1.offset_x + submenu_offset,
                (vis.menu1.offset_y + vis.menu1.height + 1).toDouble(),
                sm.dd_width.toDouble(),
                sm.dd_height.toDouble()
            );// Options box

            MYCTX.fillStyle = "rgb(" + vis.med_grey.r + ", " + vis.med_grey.g + ", " + vis.med_grey.b + ")";
            MYCTX.fillRect(vis.menu1.offset_x + submenu_offset, vis.menu1.offset_y.toDouble(), sm.width, 1);
            MYCTX.fillRect(vis.menu1.offset_x + submenu_offset, vis.menu1.offset_y.toDouble(), 1, vis.menu1.height);
            MYCTX.fillRect(
                vis.menu1.offset_x + submenu_offset + sm.dd_width - 2,
                (vis.menu1.offset_y + vis.menu1.height + 2).toDouble(),
                1.0,
                (sm.dd_height - 2).toDouble()
            );// Options box
            MYCTX.fillRect(
                vis.menu1.offset_x + submenu_offset + 1,
                vis.menu1.offset_y + vis.menu1.height + sm.dd_height - 1,
                sm.dd_width - 2,
                1
            );// Options box

            MYCTX.fillStyle = "rgb(" + vis.white.r + ", " + vis.white.g + ", " + vis.white.b + ")";
            MYCTX.fillRect(vis.menu1.offset_x + submenu_offset, vis.menu1.offset_y + vis.menu1.height, sm.width, 1);
            MYCTX.fillRect(vis.menu1.offset_x + submenu_offset + sm.width - 1, vis.menu1.offset_y, 1, vis.menu1.height);
            MYCTX.fillRect(
                vis.menu1.offset_x + submenu_offset + 1,
                vis.menu1.offset_y + vis.menu1.height + 2,
                1,
                sm.dd_height - 3
            );// Options box
            MYCTX.fillRect(
                vis.menu1.offset_x + submenu_offset + 1,
                vis.menu1.offset_y + vis.menu1.height + 2,
                sm.dd_width - 3,
                1
            );// Options box

            MYCTX.fillStyle = "rgb(" + vis.dark_grey.r + ", " + vis.dark_grey.g + ", " + vis.dark_grey.b + ")";
            MYCTX.fillRect(
                vis.menu1.offset_x + submenu_offset + sm.dd_width - 1,
                vis.menu1.offset_y + vis.menu1.height + 1,
                1,
                sm.dd_height
            );// Options box
            MYCTX.fillRect(
                vis.menu1.offset_x + submenu_offset,
                vis.menu1.offset_y + vis.menu1.height + sm.dd_height,
                sm.dd_width - 1,
                1
            );// Options box

            //input.mouse_pos.x
            var option_offset = vis.menu1.offset_y + vis.menu1.height + 4;
            MYCTX.fillStyle = "rgb(" + vis.black.r + ", " + vis.black.g + ", " + vis.black.b + ")";

            for (j in 0 until sm.options.size) {
                var next_offset: Int;
                var check_image = 171;
                if (sm.options[j].line) {
                    next_offset = option_offset + sm.offset_line;

                    MYCTX.fillStyle = "rgb(" + vis.med_grey.r + ", " + vis.med_grey.g + ", " + vis.med_grey.b + ")";
                    MYCTX.fillRect(
                        vis.menu1.offset_x + submenu_offset + 3,
                        option_offset + 3,
                        sm.dd_width - 6,
                        1
                    );// Separator line
                    MYCTX.fillStyle = "rgb(" + vis.white.r + ", " + vis.white.g + ", " + vis.white.b + ")";
                    MYCTX.fillRect(
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
                    MYCTX.fillStyle = "rgb(" + vis.blue.r + ", " + vis.blue.g + ", " + vis.blue.b + ")";
                    MYCTX.fillRect(
                        vis.menu1.offset_x + submenu_offset + 3,
                        option_offset,
                        sm.dd_width - 6,
                        sm.offset_text
                    );// Options box
                    MYCTX.fillStyle = "rgb(" + vis.white.r + ", " + vis.white.g + ", " + vis.white.b + ")";

                    check_image = 172;
                } else if (!sm.options[j].on()) {
                    MYCTX.fillStyle = "rgb(" + vis.white.r + ", " + vis.white.g + ", " + vis.white.b + ")";
                    MYCTX.fillText(sm.options[j].name, vis.menu1.offset_x + submenu_offset + 21, option_offset + 2);
                } else {
                    MYCTX.fillStyle = "rgb(" + vis.black.r + ", " + vis.black.g + ", " + vis.black.b + ")";
                }

                if (sm.options[j].on()) {
                    MYCTX.fillText(sm.options[j].name, vis.menu1.offset_x + submenu_offset + 20, option_offset + 1);
                } else {
                    MYCTX.fillStyle = "rgb(" + vis.med_grey.r + ", " + vis.med_grey.g + ", " + vis.med_grey.b + ")";
                    MYCTX.fillText(sm.options[j].name, vis.menu1.offset_x + submenu_offset + 20, option_offset + 1);
                }

                if (sm.options[j].check != 0) {
                    if ((sm.options[j].effect_id == 3 && game.paused) || (sm.options[j].effect_id == 4 && game.single_steps) || (sm.options[j].effect_id == 5 && game.sound)) {
                        MYCTX.drawImage(
                            res.images[check_image],
                            vis.menu1.offset_x + submenu_offset + 6,
                            (option_offset + 6).toDouble()
                        );// Background
                    }
                }

                option_offset = next_offset;

            }
        }
        MYCTX.fillStyle = "rgb(" + vis.black.r + ", " + vis.black.g + ", " + vis.black.b + ")";
        MYCTX.fillText(sm.name, vis.menu1.offset_x + submenu_offset + 6, vis.menu1.offset_y + 3);
        submenu_offset += sm.width as Double;
    }

}