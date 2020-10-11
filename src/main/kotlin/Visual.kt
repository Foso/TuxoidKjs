import kotlin.math.floor

external interface Block {

    val gets_removed_in: Int
    val pushing: Boolean
    val face_dir: Int
    val moving: Boolean
    var fine_offset_y: dynamic
    var fine_offset_x: dynamic
    var animation_frame: Int
    val id: Int

}


@JsExport
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
                1,2 -> {
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

@JsExport
fun kt_update_animation(x: Int, y: Int) {
    var block = game.level_array[x][y] as Block;
    when(block.id){
        1,2->{
            kt_update_animation_case2(x,y,block)
        }
        7->{
            // Purple monster (Monster 2)
            kt_update_animation_case7(x,y,block);
        }
        10->{
            // Green monster (Monster 2)
            kt_update_animation_case10(x,y,block);
        }
        19->{
            // Door 1
            if(block.gets_removed_in >= 0){
                block.animation_frame = 43 - floor(block.gets_removed_in/game.door_removal_delay*2).toInt();
            }
        }
        20->{
            // Door 2
            if(block.gets_removed_in >= 0){
                block.animation_frame = 46-floor(block.gets_removed_in/game.door_removal_delay*2).toInt();
            }
        }
        21->{
            // Door 3
            if(block.gets_removed_in >= 0){
                block.animation_frame = 49-floor(block.gets_removed_in/game.door_removal_delay*2).toInt();
            }
        }
        22->{
            // Door 4
            if(block.gets_removed_in >= 0){
                block.animation_frame = 52-floor(block.gets_removed_in/game.door_removal_delay*2).toInt();
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