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
	        ktvisual.kt_init_animation(that,game);
	}
	
	this.update_animation = function(x, y){
		ktvisual.kt_update_animation(x,y);
	}
	
	this.update_all_animations = function(){
		for(var y = 0; y < LEV_DIMENSION_Y; y++){
			for(var x = 0; x < LEV_DIMENSION_X; x++){
				that.update_animation(x, y);
			}
		}
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
		var tautology = function(){return true;};
	
		var arr_options1 = [
		{line:false, check:0, name:"New", hotkey:"F2", effect_id:0, on:tautology},
		{line:false, check:0, name:"Load Game...", hotkey:"", effect_id:1, on:function(){return HAS_STORAGE;}},
		{line:false, check:0, name:"Save", hotkey:"", effect_id:2, on:function(){return (game.savegame.progressed && HAS_STORAGE);}},
		{line:false, check:1, name:"Pause", hotkey:"", effect_id:3, on:tautology}
		];
		
		var arr_options2 = [
		{line:false, check:1, name:"Single steps", hotkey:"F5", effect_id:4, on:tautology},
		{line:false, check:1, name:"Sound", hotkey:"", effect_id:5, on:tautology},
		{line:true, check:0, name:"", hotkey:"", effect_id:-1, on:tautology},
		{line:false, check:0, name:"Load Level", hotkey:"", effect_id:6, on:function(){return HAS_STORAGE;}},
		{line:false, check:0, name:"Change Password", hotkey:"", effect_id:7, on:function(){return (game.savegame.username !== null && HAS_STORAGE);}},
		{line:true, check:0, name:"", hotkey:"", effect_id:-1, on:tautology},
		{line:false, check:0, name:"Charts", hotkey:"", effect_id:8, on:function(){return HAS_STORAGE;}}
		];
		
		var sub_m1 = new untitled23.SubMenu(43, 100, "Game", arr_options1);
		var sub_m2 = new untitled23.SubMenu(55, 150, "Options", arr_options2);
		
		that.menu1 = new untitled23.Menu(1, 2, 17, [sub_m1, sub_m2]);
	}
	
	// Dialog box stuff:
	
	function add_button(img, pos_x, pos_y, click_effect){
		var btn = document.createElement("img");
		btn.src = res.images[img].src;
		btn.style.position = "absolute";
		btn.style.width = res.images[img].width+"px";
		btn.style.height = res.images[img].height+"px";
		btn.style.left = pos_x+"px";
		btn.style.top = pos_y+"px";
		
		btn.pressed = false;
		btn.onmousedown = function(evt){btn.src = res.images[img+1].src; btn.pressed = true; evt.preventDefault();};
		btn.onmouseup = function(evt){btn.src = res.images[img].src; btn.pressed = false;};
		btn.onmouseout = function(evt){btn.src = res.images[img].src;};
		btn.onmouseover = function(evt){if(btn.pressed && input.mouse_down) btn.src = res.images[img+1].src;};
		btn.onclick = click_effect;
		
		that.dbx.appendChild(btn);
		that.dbx.arr_btn[that.dbx.arr_btn.length] = btn;
	}
	
	function add_text(text, pos_x, pos_y){
	untitled23.add_text(text, pos_x, pos_y,that)
	}
	
	function add_number(a_num, pos_x, pos_y, width, height){
	untitled23.add_text(a_num, pos_x, pos_y, width, height,that);

	}
	
	function add_title(text){
		var txt = document.createElement("p");
		txt.innerHTML = text;
		txt.style.position = "absolute";
		txt.style.left = "5px";
		txt.style.top = "-13px";
		txt.style.fontFamily = "Tahoma";
		txt.style.fontSize = "14px";
		txt.style.color = "white";
		txt.style.fontWeight = "bold";
		that.dbx.appendChild(txt);
	}
	
	function add_input(pos_x, pos_y, width, height, type){
		var txt = document.createElement("input");
		//txt.innerHTML = text;
		txt.type = type;
		txt.style.position = "absolute";
		txt.style.left = pos_x+"px";
		pos_y += 10;// Because of padding
		txt.style.top = pos_y+"px";
		txt.style.width = width+"px";
		txt.style.height = height+"px";
		txt.style.fontFamily = "Tahoma";
		txt.style.fontSize = "12px";
		
		that.dbx.appendChild(txt);
		that.dbx.arr_input[that.dbx.arr_input.length] = txt;
		
		//window.setTimeout( function() {txt.focus();}, 10);
	}
	
	function add_lvlselect(pos_x, pos_y, width, height){
		var select = document.createElement("select");
		select.size = 2;
		
		select.innerHTML = "";
		for(var i = 1; i < game.savegame.reached_level; i++){
			select.innerHTML += "<option value=\""+i+"\">\n"+i+", "+game.savegame.arr_steps[i]+"</option>";
		}
		if(game.savegame.reached_level <= 50){
			select.innerHTML += "<option value=\""+game.savegame.reached_level+"\">\n"+game.savegame.reached_level+", -</option>";
		}
		
		
		select.style.position = "absolute";
		select.style.left = pos_x+"px";
		select.style.top = pos_y+"px";
		select.style.width = width+"px";
		select.style.height = height+"px";
		select.style.fontFamily = "Tahoma";
		select.style.fontSize = "12px";
		
		that.dbx.appendChild(select);
		that.dbx.lvlselect = select;
	}
	
	function add_errfield(pos_x, pos_y){
		var ef = document.createElement("p");
		ef.innerHTML = "";
		ef.style.position = "absolute";
		ef.style.left = pos_x+"px";
		ef.style.top = pos_y+"px";
		ef.style.fontFamily = "Tahoma";
		ef.style.fontSize = "12px";
		ef.style.color = "#FF0000";
		that.dbx.appendChild(ef);
		
		that.dbx.errfield = ef;
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
				add_title("Confirm");
			
				that.dbx.style.width = "256px";
				that.dbx.style.height = "154px";
				that.dbx.style.left = Math.max(Math.floor(window.innerWidth-256)/2, 0)+"px";
				that.dbx.style.top = Math.max(Math.floor(window.innerHeight-154)/2, 0)+"px";
				that.dbx.style.background = 'url('+res.images[173].src+')';
				
				var f_y;
				var f_n;
				var f_c = function(){that.close_dbx();};
				
				if(opt == 0){// "New Game"
					f_y = function(){that.open_dbx(DBX_SAVE, 1);};
					f_n = function(){game.clear_savegame();that.close_dbx();};
				}else if(opt == 1){// "Load Game" 
					f_y = function(){that.open_dbx(DBX_SAVE, 2);};
					f_n = function(){that.open_dbx(DBX_LOAD);};
				}
				
				that.dbx.enterfun = f_y;
				that.dbx.cancelfun = f_c;
				
				add_button(183, 20, 100, f_y);// yes
				add_button(179, 100, 100, f_n);// no
				add_button(177, 180, 100, f_c);// cancel
				
				add_text("Do you want to save the game?", 40, 35);
				break;
			case DBX_SAVE:
				add_title("Save game");
			
				that.dbx.style.width = "256px";
				that.dbx.style.height = "213px";
				that.dbx.style.left = Math.max(Math.floor(window.innerWidth-256)/2, 0)+"px";
				that.dbx.style.top = Math.max(Math.floor(window.innerHeight-213)/2, 0)+"px";
				that.dbx.style.background = 'url('+res.images[174].src+')';
				
				add_text("Player name:", 20, 35);
				add_input(100, 35, 120, 15, "text");
				add_text("Password:", 20, 60);
				add_input(100, 60, 120, 15, "password");
				
				var f_o;
				var f_c;
				
				if(opt == 0){// "Save game"
					f_o = function(){if(game.dbxcall_save(that.dbx.arr_input[0].value, that.dbx.arr_input[1].value)){that.close_dbx();}};
					f_c = function(){that.close_dbx();};
				}else if(opt == 1){// "New Game" -> yes, save 
					f_o = function(){if(game.dbxcall_save(that.dbx.arr_input[0].value, that.dbx.arr_input[1].value)){game.clear_savegame();that.close_dbx();}};
					f_c = function(){game.clear_savegame();that.close_dbx();};
				}else if(opt == 2){// "Load Game" -> yes, save
					f_o = function(){if(game.dbxcall_save(that.dbx.arr_input[0].value, that.dbx.arr_input[1].value)){that.open_dbx(DBX_LOAD);}};
					f_c = function(){that.open_dbx(DBX_LOAD);};
				}
				
				that.dbx.enterfun = f_o;
				that.dbx.cancelfun = f_c;
				
				add_button(181, 40, 160, f_o);// ok
				add_button(177, 160, 160, f_c);// cancel
				
				add_errfield(20, 85);
				break;
			case DBX_LOAD:
				add_title("Load game");
			
				that.dbx.style.width = "256px";
				that.dbx.style.height = "213px";
				that.dbx.style.left = Math.max(Math.floor(window.innerWidth-256)/2, 0)+"px";
				that.dbx.style.top = Math.max(Math.floor(window.innerHeight-213)/2, 0)+"px";
				that.dbx.style.background = 'url('+res.images[174].src+')';
				
				add_text("Player name:", 20, 35);
				add_input(100, 35, 120, 15, "text");
				add_text("Password:", 20, 60);
				add_input(100, 60, 120, 15, "password");
				
				var f_o = function(){if(game.dbxcall_load(that.dbx.arr_input[0].value, that.dbx.arr_input[1].value)){that.close_dbx();}};
				var f_c = function(){that.close_dbx();};
				
				that.dbx.enterfun = f_o;
				that.dbx.cancelfun = f_c;
				
				add_button(181, 40, 160, f_o);// ok
				add_button(177, 160, 160, f_c);// cancel
				
				add_errfield(20, 85);
				break;
			case DBX_CHPASS:
				add_title("Change password");
			
				that.dbx.style.width = "256px";
				that.dbx.style.height = "213px";
				that.dbx.style.left = Math.max(Math.floor(window.innerWidth-256)/2, 0)+"px";
				that.dbx.style.top = Math.max(Math.floor(window.innerHeight-213)/2, 0)+"px";
				that.dbx.style.background = 'url('+res.images[174].src+')';
				
				add_text("Old password:", 20, 35);
				add_input(100, 35, 120, 15, "password");
				add_text("New password:", 20, 60);
				add_input(100, 60, 120, 15, "password");
				
				var f_o = function(){if(game.dbxcall_chpass(that.dbx.arr_input[0].value, that.dbx.arr_input[1].value)){that.close_dbx();}};
				var f_c = function(){that.close_dbx();};
				
				that.dbx.enterfun = f_o;
				that.dbx.cancelfun = f_c;
				
				add_button(181, 40, 160, f_o);// ok
				add_button(177, 160, 160, f_c);// cancel
				
				add_errfield(20, 85);
				break;
			case DBX_LOADLVL:
				add_title("Load level");
			
				that.dbx.style.width = "197px";
				that.dbx.style.height = "273px";
				that.dbx.style.left = Math.max(Math.floor(window.innerWidth-197)/2, 0)+"px";
				that.dbx.style.top = Math.max(Math.floor(window.innerHeight-273)/2, 0)+"px";
				that.dbx.style.background = 'url('+res.images[175].src+')';
				
				add_lvlselect(20, 80, 158, 109);
				
				var f_o = function(){if(parseInt(that.dbx.lvlselect.value) > 0) {game.load_level(parseInt(that.dbx.lvlselect.value)); that.close_dbx();}};
				var f_c = function(){that.close_dbx();};
				
				that.dbx.enterfun = f_o;
				that.dbx.cancelfun = f_c;
				
				add_button(181, 25, 220, f_o);// ok
				add_button(177, 105, 220, f_c);// cancel
				
				add_text("Player name:", 20, 30);
				if(game.savegame.username === null){
					add_text("- none -", 100, 30);
				}else{
					add_text(game.savegame.username, 100, 30);
				}
				
				add_text("Level, steps:", 20, 50);
				
				
				break;
			case DBX_CHARTS:
				game.play_sound(4);
				
				add_title("Charts");
				
				that.dbx.style.width = "322px";
				that.dbx.style.height = "346px";
				that.dbx.style.left = Math.max(Math.floor(window.innerWidth-322)/2, 0)+"px";
				that.dbx.style.top = Math.max(Math.floor(window.innerHeight-346)/2, 0)+"px";
				that.dbx.style.background = 'url('+res.images[176].src+')';
				
				var uc = localStorage.getItem("user_count");
				var user_arr = new Array();
				
				for(var i = 0; i < uc; i++){
					var prefix = "player"+i+"_";
					var rl = parseInt(localStorage.getItem(prefix+"reached_level"));
					var st = 0;
					for(var j = 1; j < rl; j++){
						st += parseInt(localStorage.getItem(prefix+"steps_lv"+j));
					}
					user_arr[i] = {name: localStorage.getItem(prefix+"username"), reached: rl, steps: st}
				}
				
				user_arr.sort(function(a,b){return (b.reached-a.reached == 0)?(a.steps - b.steps):(b.reached-a.reached);});
				
				add_text("rank", 21, 37);
				add_text("level", 57, 37);
				add_text("steps", 100, 37);
				add_text("name", 150, 37);
				
				for(var i = 0; i < uc && i < 10; i++){
					add_number((i+1), 20, 65+18*i, 20, 20);
					add_number(user_arr[i].reached, 50, 65+18*i, 30, 20);
					add_number(user_arr[i].steps, 95, 65+18*i, 40, 20);
					add_text(user_arr[i].name, 155, 65+18*i);
				}
				
				var f_o = function(){that.close_dbx();};
				
				that.dbx.enterfun = f_o;
				that.dbx.cancelfun = f_o;
				
				add_button(181, 125, 300, f_o);// okay
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
		that.dbx.style.display = "none";
		
		// IMPORTANT MEMORY LEAK PREVENTION
		for(var i = that.dbx.arr_btn.length-1; i >= 0; i--){
			that.dbx.arr_btn[i].pressed = null;
			that.dbx.arr_btn[i].onmousedown = null;
			that.dbx.arr_btn[i].onmouseup = null;
			that.dbx.arr_btn[i].onmouseout = null;
			that.dbx.arr_btn[i].onmouseover = null;
			that.dbx.arr_btn[i].onclick = null;
			that.dbx.arr_btn[i] = null;
		}
		that.dbx.arr_btn = new Array();
		
		for(var i = that.dbx.arr_input.length-1; i >= 0; i--){
			that.dbx.arr_input[i] = null;
		}
		that.dbx.arr_input = new Array();
		
		that.dbx.lvlselect = null;
		that.dbx.errfield = null;
		
		that.dbx.enterfun = null;
		that.dbx.cancelfun = null;
		
		while (that.dbx.firstChild) {
			that.dbx.removeChild(that.dbx.firstChild);
		}
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
	var submenu_offset = 0;
	// The font is the same for the whole menu... Segoe UI is also nice
	CTX.font = "11px Tahoma";
	CTX.textAlign = "left";
	CTX.textBaseline = "top";
	
	for(var i = 0; i < vis.menu1.submenu_list.length; i++){
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
			
			for(var j = 0; j < sm.options.length; j++){
				var next_offset;
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
		submenu_offset += sm.width;
	}
}

function render_vol_bar(){
	var vb = vis.vol_bar;
	var switcher = false;
	
	
	for(var i = 0; i < vb.width; i+= 1){
		if(switcher){
			switcher = false;
			CTX.fillStyle = "rgb("+vb.colour_4.r+", "+vb.colour_4.g+", "+vb.colour_4.b+")";
		}else{
			switcher = true;
			var ratio2 = i/vb.width;
			var line_height = Math.round(vb.height*ratio2);
		
			if(i < Math.ceil(vb.volume*vb.width)){
				if(game.sound){
					var ratio1 = 1-ratio2;
					CTX.fillStyle = "rgb("+Math.round(vb.colour_1.r*ratio1+vb.colour_2.r*ratio2)+", "+Math.round(vb.colour_1.g*ratio1+vb.colour_2.g*ratio2)+", "+Math.round(vb.colour_1.b*ratio1+vb.colour_2.b*ratio2)+")";
				}else{
					CTX.fillStyle = "rgb("+vb.colour_5.r+", "+vb.colour_5.g+", "+vb.colour_5.b+")";
				}
			}else{
				CTX.fillStyle = "rgb("+vb.colour_3.r+", "+vb.colour_3.g+", "+vb.colour_3.b+")";
			}
		}
		CTX.fillRect(vb.offset_x+i, vb.offset_y+vb.height-line_height, 1, line_height);
	}

};

function render_field(){
	render_field_subset(true);// Consumables in the background
	render_field_subset(false);// The rest in the foreground
	
	CTX.drawImage(res.images[0], 0, 391, 537, 4, 0, LEV_OFFSET_Y+24*LEV_DIMENSION_Y, 537, 4);// Bottom border covering blocks
	CTX.drawImage(res.images[0], 520, LEV_OFFSET_Y, 4, 391-LEV_OFFSET_Y, LEV_OFFSET_X+24*LEV_DIMENSION_X, LEV_OFFSET_Y, 4, 391-LEV_OFFSET_Y);// Right border covering blocks
	
	if(game.level_ended == 1){// Berti cheering, wow or yeah
		for(var i = 0; i < game.berti_positions.length; i++){
			if(game.wow){
				CTX.drawImage(res.images[168],
				LEV_OFFSET_X+24*game.berti_positions[i].x+game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.x+vis.offset_wow_x,
				LEV_OFFSET_Y+24*game.berti_positions[i].y+game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.y+vis.offset_wow_y);
			}else{
				CTX.drawImage(res.images[169],
				LEV_OFFSET_X+24*game.berti_positions[i].x+game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.x+vis.offset_yeah_x,
				LEV_OFFSET_Y+24*game.berti_positions[i].y+game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.y+vis.offset_yeah_y);
			}
		}
	}else if(game.level_ended == 2){// Berti dies in a pool of blood
		for(var i = 0; i < game.berti_positions.length; i++){
			CTX.drawImage(res.images[167],
			LEV_OFFSET_X+24*game.berti_positions[i].x+game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.x+vis.offset_argl_x,
			LEV_OFFSET_Y+24*game.berti_positions[i].y+game.level_array[game.berti_positions[i].x][game.berti_positions[i].y].moving_offset.y+vis.offset_argl_y);
		}
	}
}
function render_field_subset(consumable){
	for(var y = 0; y < LEV_DIMENSION_Y; y++){
		for(var x = 0; x < LEV_DIMENSION_X; x++){
			var block = game.level_array[x][y];
			if(y > 0 && game.level_array[x][y-1].moving && game.level_array[x][y-1].face_dir == DIR_DOWN && game.level_array[x][y-1].consumable == consumable){
				render_block(x, y-1, RENDER_BOTTOM);
			}
			
			if(y > 0 && (!game.level_array[x][y-1].moving) && game.level_array[x][y-1].consumable == consumable){
				if(x > 0 && game.level_array[x-1][y].face_dir != DIR_RIGHT){
					render_block(x, y-1, RENDER_BOTTOM_BORDER);
				}
			}
		
			if(block.consumable == consumable){
				if(!block.moving || block.face_dir == DIR_LEFT || block.face_dir == DIR_RIGHT){
					render_block(x, y, RENDER_FULL);
				}else if(block.face_dir == DIR_DOWN){
					render_block(x, y, RENDER_TOP);
				}else if(block.face_dir == DIR_UP){
					render_block(x, y, RENDER_BOTTOM);
				}
			}
			
			if(y+1 < LEV_DIMENSION_Y && game.level_array[x][y+1].moving && game.level_array[x][y+1].face_dir == DIR_UP && game.level_array[x][y+1].consumable == consumable){
				render_block(x, y+1, RENDER_TOP);
			}
		}
	}
}
function render_block(x, y, render_option){
    untitled23.kt_render_block(x,y,render_option)
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