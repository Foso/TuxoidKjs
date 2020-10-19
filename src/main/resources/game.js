// Please note that this code is quite old, my first large project in JavaScript
// and therefore the code structure is not very good.

"use strict";

// GLOBAL CONSTANTS



// Check storage
var HAS_STORAGE = (function(){try {return 'localStorage' in window && window['localStorage'] !== null && window['localStorage'] !== undefined;} catch (e) {return false;}})();

// Canvas creation



// GLOBAL VARIABLES

// MD5 digest for passwords
var md5 = new CLASS_md5();

// Game

// Resources
//var res = new untitled23.MyRes();
//res.load();


// Visual

untitled23.main();

/*//////////////////////////////////////////////////////////////////////////////////////////////////////
// RENDERING PROCESS
// All visual things get handled here. Visual variables go into the object "vis".
// Runs with 60 FPS on average (depending on browser).
//////////////////////////////////////////////////////////////////////////////////////////////////////*/


// Use window.requestAnimationFrame, get rid of browser differences.


//untitled23.render();// Render thread