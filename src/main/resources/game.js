// Please note that this code is quite old, my first large project in JavaScript
// and therefore the code structure is not very good.

"use strict";
var DEBUG = true;

// GLOBAL CONSTANTS
var JOYSTICK_SIZE = 0.4;// In terms of the smaller of the two screen dimensions
var IS_TOUCH_DEVICE = true == ("ontouchstart" in window || window.DocumentTouch && document instanceof DocumentTouch);

var UPS = IS_TOUCH_DEVICE ? 15 : 60;// Reduced framerate on mobile


var INTRO_DURATION = 6;// In seconds
if(DEBUG) INTRO_DURATION = 2;
var LEV_START_DELAY = 2;
if(DEBUG) LEV_START_DELAY = 1;
var LEV_STOP_DELAY = 2;
if(DEBUG) LEV_STOP_DELAY = 1;
var ANIMATION_DURATION = Math.round(8*UPS/60);// How many times the game has to render before the image changes


// Check storage
var HAS_STORAGE = (function(){try {return 'localStorage' in window && window['localStorage'] !== null && window['localStorage'] !== undefined;} catch (e) {return false;}})();

// Canvas creation
untitled23.main();


// GLOBAL VARIABLES

// MD5 digest for passwords
var md5 = new CLASS_md5();

// Game
var game = new untitled23.KtGame();//CLASS_game();

// Resources
var res = new untitled23.MyRes();
res.load();


// Visual
var vis = new  untitled23.KtVisual()//CLASS_visual();
vis.init_menus();


/*//////////////////////////////////////////////////////////////////////////////////////////////////////
// RENDERING PROCESS
// All visual things get handled here. Visual variables go into the object "vis".
// Runs with 60 FPS on average (depending on browser).
//////////////////////////////////////////////////////////////////////////////////////////////////////*/


// Use window.requestAnimationFrame, get rid of browser differences.


untitled23.render();// Render thread