import kotlinx.browser.window
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.random.Random


external fun myTest(): dynamic

external fun CLASS_entity()




@JsExport
fun kt_chase_berti(curr_x: Int, curr_y: Int, tthis: dynamic) {
    if (!tthis.moving) {
        tthis.time_since_noise++;

        var closest_dist = LEV_DIMENSION_X + LEV_DIMENSION_Y + 1;
        var closest_berti = -1;

        for (i in 0 until game.berti_positions.size) {
            var face_right_direction =
                (tthis.face_dir == DIR_DOWN && game.berti_positions[i].y >= curr_y) ||
                        (tthis.face_dir == DIR_UP && game.berti_positions[i].y <= curr_y) ||
                        (tthis.face_dir == DIR_LEFT && game.berti_positions[i].x <= curr_x) ||
                        (tthis.face_dir == DIR_RIGHT && game.berti_positions[i].x >= curr_x);

            if (face_right_direction && game.can_see_tile(
                    curr_x,
                    curr_y,
                    game.berti_positions[i].x,
                    game.berti_positions[i].y
                )
            ) {
                var distance =
                    abs(game.berti_positions[i].x - curr_x) + abs(game.berti_positions[i].y - curr_y);// Manhattan distance
                if (distance < closest_dist || (distance == closest_dist && Random.nextDouble() < 0.50)) {
                    closest_dist = distance;
                    closest_berti = i;
                }
            }


        }

        if (closest_berti == -1 || Random.nextDouble() < 0.02) { // Can't see berti OR he randomly gets distracted THEN Random search
            tthis.sees_berti = false;
            tthis.move_randomly(curr_x, curr_y);
        } else {// Chasing code here.
            if (!tthis.sees_berti) {
                tthis.sees_berti = true;

                if (tthis.time_since_noise > ceil(Random.nextDouble() * 10) + 3) {
                    tthis.time_since_noise = 0;
                    if (tthis.id == 7) {
                        game.play_sound(2);
                    } else if (tthis.id == 10) {
                        game.play_sound(3);
                    }
                }
            }

            var diff_x = game.berti_positions[closest_berti].x - curr_x;
            var diff_y = game.berti_positions[closest_berti].y - curr_y;

            var dir1: Int;
            var dir2: Int;

            if (diff_x == 0) {
                if (diff_y == 0) {// This should NEVER happen.
                    window.alert("001: Something went mighty wrong! Blame the programmer!");
                    tthis.move_randomly(curr_x, curr_y);
                    return;
                } else if (diff_y > 0) {
                    dir1 = DIR_DOWN
                    dir2 = DIR_DOWN;
                } else {// diff_y < 0
                    dir1 = DIR_UP
                    dir2 = DIR_UP;
                }
            } else if (diff_x > 0) {
                if (diff_y == 0) {
                    dir1 = DIR_RIGHT
                    dir2 = DIR_RIGHT;
                } else if (diff_y > 0) {
                    dir1 = DIR_RIGHT;
                    dir2 = DIR_DOWN;
                } else {// diff_y < 0
                    dir1 = DIR_RIGHT
                    dir2 = DIR_UP;
                }
            } else {// diff_x < 0
                if (diff_y == 0) {
                    dir1 = DIR_LEFT
                    dir2 = DIR_LEFT;
                } else if (diff_y > 0) {
                    dir1 = DIR_LEFT;
                    dir2 = DIR_DOWN;
                } else {// diff_y < 0
                    dir1 = DIR_LEFT
                    dir2 = DIR_UP;
                }
            }

            if (dir1 != dir2) {
                var total_distance = abs(diff_x) + abs(diff_y);
                var percentage_x = abs(diff_x / total_distance);
                if (Random.nextDouble() >= percentage_x) {
                    var swapper = dir1;
                    dir1 = dir2;
                    dir2 = swapper;
                }

                if (game.walkable(curr_x, curr_y, dir1)) {
                    game.start_move(curr_x, curr_y, dir1);
                } else if (game.walkable(curr_x, curr_y, dir2)) {
                    game.start_move(curr_x, curr_y, dir2);
                } else {
                    tthis.move_randomly(curr_x, curr_y);
                }
            } else {
                if (game.walkable(curr_x, curr_y, dir1)) {
                    game.start_move(curr_x, curr_y, dir1);
                } else {
                    tthis.move_randomly(curr_x, curr_y);
                }
            }


        }
    }
}


@JsExport
fun kt_update_entity(curr_x: Int, curr_y: Int, tthis: dynamic) {
    tthis.animation_delay++;// This is an important link between the game logic and animation timing.

    if (tthis.moving) {
        when (tthis.face_dir) {
            DIR_UP -> {
                tthis.moving_offset.y -= game.move_speed;
            }
            DIR_DOWN -> {
                tthis.moving_offset.y += game.move_speed;
            }
            DIR_LEFT -> {
                tthis.moving_offset.x -= game.move_speed;
            }
            DIR_RIGHT -> {
                tthis.moving_offset.x += game.move_speed;
            }
            else -> {
                window.alert("002: Something went mighty wrong! Blame the programmer!");// This should never be executed
            }
        }
        if (tthis.moving_offset.x <= -24 || tthis.moving_offset.x >= 24 || tthis.moving_offset.y <= -24 || tthis.moving_offset.y >= 24) {
            game.move(curr_x, curr_y, tthis.face_dir);
            tthis.just_moved = true;
        }
    }

    if (tthis.gets_removed_in == 0) {
        if (tthis.moving) {
            var dst = game.dir_to_coords(curr_x, curr_y, tthis.face_dir);
            game.level_array[dst.x][dst.y].init(0);
        }
        game.level_array[curr_x][curr_y].init(0);
    } else if (tthis.gets_removed_in > 0) {
        tthis.gets_removed_in -= 1;
        vis.update_animation(curr_x, curr_y);
    }

}

external var IS_TOUCH_DEVICE: Boolean

@JsExport
fun kt_register_input(curr_x: Int, curr_y: Int, just_prime: dynamic, tthis: dynamic) {
    if (!tthis.moving) {
        if ((IS_TOUCH_DEVICE && input.joystick_dir == DIR_LEFT) || input.keys_down[37] || (!game.single_steps && game.walk_dir == DIR_LEFT) || (game.prime_movement && game.walk_dir == DIR_LEFT)) {
            game.prime_movement = just_prime;
            if (!just_prime && game.walkable(curr_x, curr_y, DIR_LEFT)) {
                game.start_move(curr_x, curr_y, DIR_LEFT);
            }
        } else if ((IS_TOUCH_DEVICE && input.joystick_dir == DIR_UP) || input.keys_down[38] || (!game.single_steps && game.walk_dir == DIR_UP) || (game.prime_movement && game.walk_dir == DIR_UP)) {
            game.prime_movement = just_prime;
            if (!just_prime && game.walkable(curr_x, curr_y, DIR_UP)) {
                game.start_move(curr_x, curr_y, DIR_UP);
            }
        } else if ((IS_TOUCH_DEVICE && input.joystick_dir == DIR_RIGHT) || input.keys_down[39] || (!game.single_steps && game.walk_dir == DIR_RIGHT) || (game.prime_movement && game.walk_dir == DIR_RIGHT)) {
            game.prime_movement = just_prime;
            if (!just_prime && game.walkable(curr_x, curr_y, DIR_RIGHT)) {
                game.start_move(curr_x, curr_y, DIR_RIGHT);
            }
        } else if ((IS_TOUCH_DEVICE && input.joystick_dir == DIR_DOWN) || input.keys_down[40] || (!game.single_steps && game.walk_dir == DIR_DOWN) || (game.prime_movement && game.walk_dir == DIR_DOWN)) {
            game.prime_movement = just_prime;
            if (!just_prime && game.walkable(curr_x, curr_y, DIR_DOWN)) {
                game.start_move(curr_x, curr_y, DIR_DOWN);
            }
        }
    }
}


@JsExport
// After each update, this function gets called for (every) Berti to see if he was caught!
fun kt_check_enemy_proximity(curr_x: Int, curr_y: Int, tthis: dynamic) {
    if (tthis.moving_offset.x != 0 || tthis.moving_offset.y != 0) return;
    var adj_array = game.get_adjacent_tiles(curr_x, curr_y);

    for (i in adj_array.indices) {
        if (game.level_array[adj_array[i].x][adj_array[i].y].id == 7 || game.level_array[adj_array[i].x][adj_array[i].y].id == 10) {// If there's an opponent on this adjacent tile
            var enemy_moving_offset_x = game.level_array[adj_array[i].x][adj_array[i].y].moving_offset.x;
            var enemy_moving_offset_y = game.level_array[adj_array[i].x][adj_array[i].y].moving_offset.y;
            if (enemy_moving_offset_x != 0 || enemy_moving_offset_y != 0) continue;

            if (abs(curr_x - adj_array[i].x) == 1 && abs(curr_y - adj_array[i].y) == 1) {// If the opponent is diagonally AND
                // there's an obstacle in the way
                if ((game.level_array[adj_array[i].x][curr_y].id != -1 && game.level_array[adj_array[i].x][curr_y].id != 0) ||
                    (game.level_array[curr_x][adj_array[i].y].id != -1 && game.level_array[curr_x][adj_array[i].y].id != 0)
                ) {
                    continue;// Don't perform a proximity check for this particular foe.
                }
            }

            // Got caught!
            game.play_sound(1);
            game.wait_timer = LEV_STOP_DELAY * UPS;
            game.level_ended = 2;
            vis.update_all_animations();
            return;
        }
    }
}