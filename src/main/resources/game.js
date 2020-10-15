// Please note that this code is quite old, my first large project in JavaScript
// and therefore the code structure is not very good.

"use strict";
var DEBUG = true;
var AUTHOR = "Benjamin";

// GLOBAL CONSTANTS
var JOYSTICK_SIZE = 0.4;// In terms of the smaller of the two screen dimensions
var IS_TOUCH_DEVICE = true == ("ontouchstart" in window || window.DocumentTouch && document instanceof DocumentTouch);

var UPS = IS_TOUCH_DEVICE ? 15 : 60;// Reduced framerate on mobile
var NUM_RESOURCES = 197;
var IMAGE_DIR = "images/";
var SOUND_DIR = "sound/";
var SCREEN_WIDTH = 537;
var SCREEN_HEIGHT = 408;
var LEV_OFFSET_X = 16;
var LEV_OFFSET_Y = 79;
var LEV_DIMENSION_X = 21;
var LEV_DIMENSION_Y = 13;
var MENU_HEIGHT = 20;
var INTRO_DURATION = 6;// In seconds
if(DEBUG) INTRO_DURATION = 2;
var LEV_START_DELAY = 2;
if(DEBUG) LEV_START_DELAY = 1;
var LEV_STOP_DELAY = 2;
if(DEBUG) LEV_STOP_DELAY = 1;
var ANIMATION_DURATION = Math.round(8*UPS/60);// How many times the game has to render before the image changes

var DEFAULT_VOLUME = 0.7;

var DIR_NONE = -1;
var DIR_UP = 0;
var DIR_LEFT = 1;
var DIR_DOWN = 2;
var DIR_RIGHT = 3;

var RENDER_FULL = 0;
var RENDER_TOP = 1;
var RENDER_BOTTOM = 2;
var RENDER_BOTTOM_BORDER = 3;

var DBX_CONFIRM = 0;
var DBX_SAVE = 1;
var DBX_LOAD = 2;
var DBX_CHPASS = 3;
var DBX_LOADLVL = 4;
var DBX_CHARTS = 5;

var ERR_SUCCESS = 0;
var ERR_EXISTS = 1;
var ERR_NOSAVE = 2;
var ERR_WRONGPW = 3;
var ERR_NOTFOUND = 4;
var ERR_EMPTYNAME = 5;

// Check storage
var HAS_STORAGE = (function(){try {return 'localStorage' in window && window['localStorage'] !== null && window['localStorage'] !== undefined;} catch (e) {return false;}})();

// Canvas creation
var CANVAS = document.createElement("canvas");
var CTX = CANVAS.getContext("2d");
CANVAS.width = SCREEN_WIDTH;
CANVAS.height = SCREEN_HEIGHT;
CANVAS.true_width = SCREEN_WIDTH;
CANVAS.true_height = SCREEN_HEIGHT;
CANVAS.className = "canv";
document.body.appendChild(CANVAS);

var JOYSTICK;
var JOYCTX;
if(IS_TOUCH_DEVICE){
	// Joystick creation
	JOYSTICK = document.createElement("canvas");
	JOYCTX = JOYSTICK.getContext("2d");
	var mindim = Math.min(window.innerWidth, window.innerHeight);
	JOYSTICK.width = mindim*JOYSTICK_SIZE;
	JOYSTICK.height = mindim*JOYSTICK_SIZE;
	JOYSTICK.className = "joystick";
	document.body.appendChild(JOYSTICK);

	window.onresize = function(event) {// On mobile, make game fullscreen
		var ratio_1 = window.innerWidth/CANVAS.true_width;
		var ratio_2 = window.innerHeight/CANVAS.true_height;
		if(ratio_1 < ratio_2){
			CANVAS.style.height = "";
			CANVAS.style.width = "100%";
		}else{
			CANVAS.style.height = "100%";
			CANVAS.style.width = "";
		}
		
		var rect = CANVAS.getBoundingClientRect();
		var style = window.getComputedStyle(CANVAS);
		CANVAS.true_width = rect.width + parseInt(style.getPropertyValue('border-left-width')) +parseInt(style.getPropertyValue('border-right-width'));
		CANVAS.true_height = rect.height + parseInt(style.getPropertyValue('border-top-width')) +parseInt(style.getPropertyValue('border-bottom-width'));
		
			
		var mindim = Math.min(window.innerWidth, window.innerHeight);
		JOYSTICK.width = mindim*JOYSTICK_SIZE;
		JOYSTICK.height = mindim*JOYSTICK_SIZE;
		
		render_joystick();
		
	};
	window.onresize(null);
}

// GLOBAL VARIABLES

// MD5 digest for passwords
var md5 = new CLASS_md5();

// Game
var game = new untitled23.KtGame();//CLASS_game();

// Resources
var res = new untitled23.MyRes();
res.load();

// Input mechanics
var input = new untitled23.MyInput();//CLASS_input;

// Visual
var vis = new CLASS_visual();
vis.init_menus();






/*//////////////////////////////////////////////////////////////////////////////////////////////////////
// GAME CLASS
// Holds entities and the game itself
//////////////////////////////////////////////////////////////////////////////////////////////////////*/



/*//////////////////////////////////////////////////////////////////////////////////////////////////////
// VISUAL CLASS
// Everything in here is related to graphical output. Also, menus and dialog boxes
//////////////////////////////////////////////////////////////////////////////////////////////////////*/

function CLASS_visual(){
	var that = this;

    var ktvisual = new untitled23.KtVisual(that);
	this.berti_blink_time = 0;
	this.last_rendered = 0;
	this.fps_delay = 0;
	this.static_ups = 0;
	this.static_fps = 0;
	
	this.buttons_pressed = new Array();
	this.buttons_pressed[0] = this.buttons_pressed[1] = this.buttons_pressed[2] = false;
	
	// Animations:
	this.offset_key_x = 3;
	this.offset_key_y = 4;
	this.offset_banana_x = 4;
	this.offset_banana_y = 4;
	
	this.offset_wow_x = -20;
	this.offset_wow_y = -44;
	
	this.offset_yeah_x = -20;
	this.offset_yeah_y = -44;
	
	this.offset_argl_x = -20;
	this.offset_argl_y = -44;
	
	this.init_animation = function(){
	        ktvisual.init_animation(game);
	}
	
	this.update_animation = function(x, y){
		ktvisual.update_animation(x,y);
	}
	
	this.update_all_animations = function(){
		ktvisual.update_all_animations();
	}
	
	// Volume bar:
	this.vol_bar = new untitled23.VolumeBar();
	

	
	// Menu stuff:
	this.black = {r:0, g:0, b: 0};
	this.dark_grey = {r:64, g:64, b:64};
	this.med_grey = {r:128, g:128, b:128};
	this.light_grey = {r:212, g:208, b:200};
	this.white = {r:255, g:255, b: 255};
	this.blue = {r:10, g:36, b:106};
	

	this.menu1;

	this.init_menus = function(){
		ktvisual.init_menus(that);
	}
	
	// Dialog box stuff:

		function add_button(img, pos_x, pos_y, click_effect){
		ktvisual.add_button(img, pos_x, pos_y, click_effect);
		}


	
	function add_text(text, pos_x, pos_y){
	ktvisual.add_text(text, pos_x, pos_y)
	}
	
	function add_number(a_num, pos_x, pos_y, width, height){
	ktvisual.add_number(a_num, pos_x, pos_y, width, height);

	}
	
	function add_title(text){
		ktvisual.add_title(text);
	}


	
	function add_input(pos_x, pos_y, width, height, type){
		ktvisual.add_input(pos_x, pos_y, width, height, type);
	}

function add_lvlselect(pos_x, pos_y, width, height){
		ktvisual.add_lvlselect(pos_x, pos_y, width, height);
	}


	function add_errfield(pos_x, pos_y){
				ktvisual.add_errfield(pos_x, pos_y);

	}
	
	this.dbx = document.createElement("div");
	this.dbx.style.position = "fixed";
	this.dbx.style.zIndex = 100;
	this.dbx.style.display = "none";
	document.body.appendChild(this.dbx);
	
	this.dbx.drag_pos = {x:0, y:0};
	this.dbx.drag = false;
	this.dbx.arr_btn = new Array();
	this.dbx.arr_input = new Array();
	this.dbx.lvlselect = null;
	this.dbx.errfield = null;
	
	this.dbx.enterfun = null;
	this.dbx.cancelfun = null;
	
	this.error_dbx = function(errno){
		untitled23.kt_error_dbx(errno,that);
	}
	
	this.open_dbx = function(dbx_id, opt){
		that.close_dbx();
		opt = (typeof opt !== 'undefined') ? opt : 0;
	
		switch(dbx_id){
			case DBX_CONFIRM:
			ktvisual.dbx_confirm(opt);

				break;
			case DBX_SAVE:
				ktvisual.dbx_save(opt);
				break;
			case DBX_LOAD:
				ktvisual.dbx_load();

				break;
			case DBX_CHPASS:
				ktvisual.dbx_chpass();
				break;
			case DBX_LOADLVL:
				ktvisual.dbx_loadlvl();
				break;
			case DBX_CHARTS:
				ktvisual.dbx_charts();
				break;
			default:
				break;
		}
		that.dbx.style.display = "inline";
		
		if(that.dbx.arr_input[0]){
			that.dbx.arr_input[0].focus();
		}
	}
	
	this.close_dbx = function(){
		ktvisual.dbx_close();
	}
	
}

/*//////////////////////////////////////////////////////////////////////////////////////////////////////
// UPDATING PROCESS
// Here, the behaviour of the game is calculated, once per UPS (update per second)
//////////////////////////////////////////////////////////////////////////////////////////////////////*/


var update_entities = function(){
	var tick = (game.update_tick*60/UPS);
	var synced_move = tick % (12/game.move_speed) == 0;
	
	// The player moves first at all times to ensure the best response time and remove directional quirks.
	for(var i = 0; i < game.berti_positions.length; i++){
		game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].register_input(game.berti_positions[i].x, game.berti_positions[i].y, !synced_move);
	}
	
	if(synced_move){
		// NPC logic and stop walking logic.
		for(var y = 0; y < LEV_DIMENSION_Y; y++){
			for(var x = 0; x < LEV_DIMENSION_X; x++){
				if(game.level_array[x][y].id == 2){// MENU Berti
					game.level_array[x][y].move_randomly(x,y);
				}else if(game.level_array[x][y].id == 7 || game.level_array[x][y].id == 10){// Purple and green monster
					game.level_array[x][y].chase_berti(x,y);
				}
				
				if(game.level_array[x][y].just_moved){
					game.level_array[x][y].just_moved = false;
					vis.update_animation(x,y);
				}
			}
		}
	}

	// After calculating who moves where, the entities actually get updated.
	for(var y = 0; y < LEV_DIMENSION_Y; y++){
		for(var x = 0; x < LEV_DIMENSION_X; x++){
			game.level_array[x][y].update_entity(x,y);
		}
	}
	
	// Gameover condition check.
	for(var i = 0; i < game.berti_positions.length; i++){
		game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].check_enemy_proximity(game.berti_positions[i].x, game.berti_positions[i].y);
	}
}

/*//////////////////////////////////////////////////////////////////////////////////////////////////////
// RENDERING PROCESS
// All visual things get handled here. Visual variables go into the object "vis".
// Runs with 60 FPS on average (depending on browser).
//////////////////////////////////////////////////////////////////////////////////////////////////////*/

// Render scene
var render = function () {
	game.now = Date.now();
    var elapsed = game.now - game.then;
	if (elapsed > game.fpsInterval) {
        game.then = game.now - (elapsed % game.fpsInterval);
		//update();
		untitled23.ktupdate();
	}
	
	//CTX.fillStyle="red";
	//CTX.fillRect(0, 0, SCREEN_WIDTH, MENU_HEIGHT);
	//CTX.clearRect(0, MENU_HEIGHT, SCREEN_WIDTH, SCREEN_HEIGHT-MENU_HEIGHT);
	
	if (game.update_drawn) {// This prevents the game from rendering the same thing twice
		window.requestAnimationFrame(render);
		return;
	}
	game.update_drawn = true;

	if (res.ready()) {
		CTX.drawImage(res.images[0], 0, 0);// Background
		CTX.drawImage(res.images[9], 22, 41);// Steps
		CTX.drawImage(res.images[10], 427, 41);// Ladder
		render_displays();
		render_buttons();
		if(game.mode == 0){// Title image
			CTX.drawImage(res.images[1], LEV_OFFSET_X+4, LEV_OFFSET_Y+4);
			
			CTX.fillStyle = "rgb(0, 0, 0)";
			CTX.font = "bold 12px Helvetica";
			CTX.textAlign = "left";
			CTX.textBaseline = "bottom";
			CTX.fillText("JavaScript remake by " + AUTHOR, 140, 234);
		}else if(game.mode == 1){
			render_field();
		}else if(game.mode == 2){// Won!
			CTX.drawImage(res.images[170], LEV_OFFSET_X+4, LEV_OFFSET_Y+4);
		}
		render_vol_bar();
		render_menu();
	}else{
		CTX.fillStyle = "rgb("+vis.light_grey.r+", "+vis.light_grey.g+", "+vis.light_grey.b+")";
		CTX.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);// Options box
		CTX.fillStyle = "rgb(0, 0, 0)";
		CTX.font = "36px Helvetica";
		CTX.textAlign = "center";
		CTX.textBaseline = "middle";
		CTX.fillText("Loading...", SCREEN_WIDTH/2,SCREEN_HEIGHT/2);
	}
	if(DEBUG) render_fps();
	
	window.requestAnimationFrame(render);
};

function render_fps(){
	var now = Date.now();
	
	if(now - vis.fps_delay >= 250){
		var delta_rendered = now - vis.last_rendered;
		vis.static_ups = ((1000/game.delta_updated).toFixed(2));
		vis.static_fps = ((1000/delta_rendered).toFixed(2));
		
		vis.fps_delay = now;
	}
	
	CTX.fillStyle = "rgb(255, 0, 0)";
	CTX.font = "12px Helvetica";
	CTX.textAlign = "right";
	CTX.textBaseline = "bottom";
	CTX.fillText("UPS: " + vis.static_ups +" FPS:" + vis.static_fps + " ", SCREEN_WIDTH,SCREEN_HEIGHT);

	vis.last_rendered = now;
};

function render_menu(){
	untitled23.kt_render_menu();
}

function render_vol_bar(){
	untitled23.render_vol_bar();


};

function render_field(){
untitled23.render_field()
}


function render_field_subset(consumable){
	untitled23.render_field_subset(consumable);
}
function render_block(x, y, render_option){
    untitled23.render_block(x,y,render_option)
}

function render_buttons(){
untitled23.kt_render_buttons();

}

function render_displays(){

	var steps_string = game.steps_taken.toString();
    	var steps_length = Math.min(steps_string.length-1, 4);

    	for(var i = steps_length; i >= 0; i--){
    		CTX.drawImage(res.images[11+parseInt(steps_string.charAt(i))], 101-(steps_length-i)*13, 41);
    	}
    	for(var i = steps_length+1; i < 5; i++){
    		CTX.drawImage(res.images[21], 101-i*13, 41);
    	}

    	var level_string = game.level_number.toString();
    	var level_length = Math.min(level_string.length-1, 4);

    	for(var i = level_length; i >= 0; i--){
    		CTX.drawImage(res.images[11+parseInt(level_string.charAt(i))], 506-(level_length-i)*13, 41);
    	}
    	for(var i = level_length+1; i < 5; i++){
    		CTX.drawImage(res.images[21], 506-i*13, 41);
    	}
}

function render_joystick(x, y){
	var mid_x = JOYSTICK.width/2;
	var mid_y = JOYSTICK.height/2;
	
	JOYCTX.clearRect ( 0 , 0 , JOYSTICK.width, JOYSTICK.height );
	JOYCTX.globalAlpha = 0.5;// Set joystick half-opaque (1 = opaque, 0 = fully transparent)
	JOYCTX.beginPath();
	JOYCTX.arc(mid_x,mid_y,JOYSTICK.width/4+10,0,2*Math.PI);
	JOYCTX.stroke();
	
	if(typeof x !== 'undefined' && typeof y !== 'undefined'){
		var dist = Math.sqrt(Math.pow(x-mid_x,2)+Math.pow(y-mid_y,2));
		if(dist > JOYSTICK.width/4){
			x = mid_x + (x-mid_x)/dist*JOYSTICK.width/4;
			y = mid_y + (y-mid_y)/dist*JOYSTICK.width/4;
		}
		JOYCTX.beginPath();
		JOYCTX.arc(x, y, 10, 0,2*Math.PI, false);// a circle at the start
		JOYCTX.fillStyle = "red";
		JOYCTX.fill();
	}
}

// Use window.requestAnimationFrame, get rid of browser differences.
(function() {
    var lastTime = 0;
    var vendors = ['ms', 'moz', 'webkit', 'o'];
    for(var x = 0; x < vendors.length && !window.requestAnimationFrame; ++x) {
        window.requestAnimationFrame = window[vendors[x]+'RequestAnimationFrame'];
        window.cancelAnimationFrame = window[vendors[x]+'CancelAnimationFrame']
                                   || window[vendors[x]+'CancelRequestAnimationFrame'];
    }
 
    if (!window.requestAnimationFrame)
        window.requestAnimationFrame = function(callback, element) {
            var currTime = new Date().getTime();
            var timeToCall = Math.max(0, 16 - (currTime - lastTime));
            var id = window.setTimeout(function() { callback(currTime + timeToCall); },
              timeToCall);
            lastTime = currTime + timeToCall;
            return id;
        };
 
    if (!window.cancelAnimationFrame)
        window.cancelAnimationFrame = function(id) {
            clearTimeout(id);
        };
}());

render();// Render thread