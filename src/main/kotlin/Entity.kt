import kotlinx.browser.window
import kotlin.math.abs


external class CLASS_entity{

}


@JsExport
fun kt_update_entity(curr_x:Int, curr_y:Int,tthis:dynamic){
    tthis.animation_delay++;// This is an important link between the game logic and animation timing.

    if(tthis.moving){
        when(tthis.face_dir){
            DIR_UP->{
                tthis.moving_offset.y -= game.move_speed;
            }
            DIR_DOWN->{
                tthis.moving_offset.y += game.move_speed;
            }
            DIR_LEFT->{
                tthis.moving_offset.x -= game.move_speed;
            }
            DIR_RIGHT->{
                tthis.moving_offset.x += game.move_speed;
            }
            else->{
                window.alert("002: Something went mighty wrong! Blame the programmer!");// This should never be executed
            }
        }
        if(tthis.moving_offset.x <= -24 || tthis.moving_offset.x >= 24 || tthis.moving_offset.y <= -24 || tthis.moving_offset.y >= 24){
            game.move(curr_x, curr_y, tthis.face_dir);
            tthis.just_moved = true;
        }
    }

    if(tthis.gets_removed_in == 0){
        if(tthis.moving){
            var dst = game.dir_to_coords(curr_x, curr_y, tthis.face_dir);
            game.level_array[dst.x][dst.y].init(0);
        }
        game.level_array[curr_x][curr_y].init(0);
    }else if(tthis.gets_removed_in > 0){
        tthis.gets_removed_in -= 1;
        vis.update_animation(curr_x, curr_y);
    }

}

external var IS_TOUCH_DEVICE : Boolean

@JsExport
fun kt_register_input(curr_x:Int, curr_y:Int,just_prime:dynamic,tthis:dynamic){
    if(!tthis.moving){
        if((IS_TOUCH_DEVICE && input.joystick_dir == DIR_LEFT) || input.keys_down[37] || (!game.single_steps && game.walk_dir == DIR_LEFT) || (game.prime_movement && game.walk_dir == DIR_LEFT)){
            game.prime_movement = just_prime;
            if(!just_prime && game.walkable(curr_x, curr_y, DIR_LEFT)){
                game.start_move(curr_x, curr_y, DIR_LEFT);
            }
        }else if((IS_TOUCH_DEVICE && input.joystick_dir == DIR_UP) || input.keys_down[38] || (!game.single_steps && game.walk_dir == DIR_UP) || (game.prime_movement && game.walk_dir == DIR_UP)){
            game.prime_movement = just_prime;
            if(!just_prime && game.walkable(curr_x, curr_y, DIR_UP)){
                game.start_move(curr_x, curr_y, DIR_UP);
            }
        }else if((IS_TOUCH_DEVICE && input.joystick_dir == DIR_RIGHT) || input.keys_down[39] || (!game.single_steps && game.walk_dir == DIR_RIGHT) || (game.prime_movement && game.walk_dir == DIR_RIGHT)){
            game.prime_movement = just_prime;
            if(!just_prime && game.walkable(curr_x, curr_y, DIR_RIGHT)){
                game.start_move(curr_x, curr_y, DIR_RIGHT);
            }
        }else if((IS_TOUCH_DEVICE && input.joystick_dir == DIR_DOWN) || input.keys_down[40] || (!game.single_steps && game.walk_dir == DIR_DOWN) || (game.prime_movement && game.walk_dir == DIR_DOWN)){
            game.prime_movement = just_prime;
            if(!just_prime && game.walkable(curr_x, curr_y, DIR_DOWN)){
                game.start_move(curr_x, curr_y, DIR_DOWN);
            }
        }
    }
}

@JsExport
fun kt_load_level(lev_number:Int,that:dynamic){
    that.mode = 1;
    that.update_tick = 0;

    that.steps_taken = 0;
    that.num_bananas = 0;
    that.level_ended = 0;
    that.level_array = arrayOf<dynamic>();
    that.level_number = lev_number;
    that.wait_timer = LEV_START_DELAY*UPS;
    that.walk_dir = DIR_NONE;

    if(that.level_unlocked < lev_number){
        that.level_unlocked = lev_number;
    }



    if(lev_number < (that.level_unlocked as Int) && lev_number != 0){
        that.buttons_activated[2] = true;
    }else{
        that.buttons_activated[2] = false;
    }

    if(lev_number > 1){
        that.buttons_activated[0] = true;
    }else{
        that.buttons_activated[0] = false;
    }

    for(i in 0 until LEV_DIMENSION_X){
        that.level_array[i] = arrayOf<dynamic>()
    }

    var berti_counter = 0;
    that.berti_positions = arrayOf<dynamic>()

    for(y in 0 until LEV_DIMENSION_Y){
        for(x in 0 until LEV_DIMENSION_X){
            that.level_array[x][y] = CLASS_entity();
            that.level_array[x][y].init(res.levels[lev_number][x][y]);

            if(res.levels[lev_number][x][y] == 4){
                that.num_bananas++;
            }else if(res.levels[lev_number][x][y] == 1){
                that.level_array[x][y].berti_id = berti_counter;
                that.berti_positions[berti_counter] = js("{x: x, y: y}");
                berti_counter++;
            }
        }
    }
}

@JsExport
// After each update, this function gets called for (every) Berti to see if he was caught!
fun kt_check_enemy_proximity(curr_x:Int, curr_y:Int,tthis:dynamic){
    if(tthis.moving_offset.x != 0 || tthis.moving_offset.y != 0) return;
    var adj_array = game.get_adjacent_tiles(curr_x, curr_y);

    for(i in adj_array.indices) {
        if (game.level_array[adj_array[i].x][adj_array[i].y].id == 7 || game.level_array[adj_array[i].x][adj_array[i].y].id == 10) {// If there's an opponent on this adjacent tile
            var enemy_moving_offset_x = game.level_array[adj_array[i].x][adj_array[i].y].moving_offset.x;
            var enemy_moving_offset_y = game.level_array[adj_array[i].x][adj_array[i].y].moving_offset.y;
            if(enemy_moving_offset_x != 0 || enemy_moving_offset_y != 0) continue;

            if(abs(curr_x - adj_array[i].x) == 1 && abs(curr_y - adj_array[i].y) == 1){// If the opponent is diagonally AND
                // there's an obstacle in the way
                if((game.level_array[adj_array[i].x][curr_y].id != -1 && game.level_array[adj_array[i].x][curr_y].id != 0) ||
                    (game.level_array[curr_x][adj_array[i].y].id != -1 && game.level_array[curr_x][adj_array[i].y].id != 0)){
                    continue;// Don't perform a proximity check for this particular foe.
                }
            }

            // Got caught!
            game.play_sound(1);
            game.wait_timer = LEV_STOP_DELAY*UPS;
            game.level_ended = 2;
            vis.update_all_animations();
            return;
        }
    }
}