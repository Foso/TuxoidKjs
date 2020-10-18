import kotlinx.browser.window
import org.w3c.dom.Audio
import org.w3c.dom.Image

external var EXTERNAL_LEVELS: dynamic
var NUM_RESOURCES = 197;
var IMAGE_DIR = "images/";
var SOUND_DIR = "sound/";

var MENU_HEIGHT = 20;





/*//////////////////////////////////////////////////////////////////////////////////////////////////////
// RESOURCES CLASS
// Images, sounds, level. Just resources.
/////////////////////////////////////////////////////////////
/////////////////////////////////////////
 */

class MyRes {
    var that = this;
    var resources_loaded = 0;
    var already_loading = false;

    fun on_loaded() {
        resources_loaded++;
    }

    var images = arrayOf<dynamic>();
    var sounds = arrayOf<dynamic>();
    var levels = EXTERNAL_LEVELS;// External loading

    fun ready(): Boolean {
        return (resources_loaded == NUM_RESOURCES);
    }

    fun load() {

        if (already_loading) {
            return;
        }
        already_loading = true;
        ////////////////////////////////////////////////////////
        // Images: /////////////////////////////////////////////
        ////////////////////////////////////////////////////////
        // Background image
        images[0] = Image();
        images[0].onload = on_loaded();
        images[0].src = IMAGE_DIR + "background.png";

        // Entry Image
        images[1] = Image();
        images[1].onload = on_loaded();
        images[1].src = IMAGE_DIR + "entry.png";

        for (i in 0 until 9) {// From 2 to 10 garbage
            that.images[2 + i] = Image();
            that.images[2 + i].onload = on_loaded();
            that.images[2 + i].src = IMAGE_DIR + "garbage_" + i + ".png";
        }

        for (i in 0 until 11) {// From 11 to 21 digits
            that.images[11 + i] = Image();
            that.images[11 + i].onload = on_loaded();
            that.images[11 + i].src = IMAGE_DIR + "digits_" + i + ".png";
        }

        for (i in 0 until 3) {// From 22 to 30 buttons
            for (j in 0 until 3) {
                images[22 + 3 * i + j] = Image();
                images[22 + 3 * i + j].onload = on_loaded();
                images[22 + 3 * i + j].src = IMAGE_DIR + "userbutton_" + i + "-" + j + ".png";
            }
        }

        for (i in 0 until 9) {
            images[31 + i] = Image();
            images[31 + i].onload = on_loaded();
            images[31 + i].src = IMAGE_DIR + "stone_" + i + ".png";
        }

        // Numbers 39 and 40 contain no images due to a miscalculation
        for (i in 0 until 6) {// From 41 to 58 doors
            for (j in 0 until 3) {
                images[41 + 3 * i + j] = Image();
                images[41 + 3 * i + j].onload = on_loaded();
                images[41 + 3 * i + j].src = IMAGE_DIR + "doors_" + j + "-" + i + ".png";
            }
        }

        for(i in 0 until 13){// From 59 to 110 player (berti)
            for(j in 0 until 4){// Reversed order for ease of access
                images[59+4*i+j] = Image();
                images[59+4*i+j].onload = on_loaded();
                images[59+4*i+j].src = IMAGE_DIR+"player_"+j+"-"+i+".png";
            }
        }

        for(i in 0 until 9){// From 111 to 146 monster 1(purple)
            for(j in 0 until 4){// Reversed order for ease of access
                that.images[111+4*i+j] = Image();
                that.images[111+4*i+j].onload = on_loaded();
                that.images[111+4*i+j].src = IMAGE_DIR+"monster1_"+j+"-"+i+".png";
            }
        }

        for(i in 0 until 5){// From 147 to 166 monster 2(green)
            for(j in 0 until 4){// Reversed order for ease of access
                that.images[147+4*i+j] =  Image();
                that.images[147+4*i+j].onload = on_loaded();
                that.images[147+4*i+j].src = IMAGE_DIR+"monster2_"+j+"-"+i+".png";
            }
        }

        that.images[167] =  Image();
        that.images[167].onload = on_loaded();
        that.images[167].src = IMAGE_DIR+"argl.png";

        that.images[168] =  Image();
        that.images[168].onload = on_loaded();
        that.images[168].src = IMAGE_DIR+"wow.png";

        that.images[169] =  Image();
        that.images[169].onload = on_loaded();
        that.images[169].src = IMAGE_DIR+"yeah.png";

        that.images[170] =  Image();
        that.images[170].onload = on_loaded();
        that.images[170].src = IMAGE_DIR+"exit.png";

        that.images[171] =  Image();
        that.images[171].onload = on_loaded();
        that.images[171].src = IMAGE_DIR+"check_b.png";

        that.images[172] =  Image();
        that.images[172].onload = on_loaded();
        that.images[172].src = IMAGE_DIR+"check_w.png";

        that.images[173] =  Image();
        that.images[173].onload = on_loaded();
        that.images[173].src = IMAGE_DIR+"dbx_confirm.png";

        that.images[174] =  Image();
        that.images[174].onload = on_loaded();
        that.images[174].src = IMAGE_DIR+"dbx_saveload.png";

        that.images[175] =  Image();
        that.images[175].onload = on_loaded();
        that.images[175].src = IMAGE_DIR+"dbx_loadlvl.png";

        that.images[176] =  Image();
        that.images[176].onload = on_loaded();
        that.images[176].src = IMAGE_DIR+"dbx_charts.png";

        that.images[177] =  Image();
        that.images[177].onload = on_loaded();
        that.images[177].src = IMAGE_DIR+"btn_c-up.png";

        that.images[178] =  Image();
        that.images[178].onload = on_loaded();
        that.images[178].src = IMAGE_DIR+"btn_c-down.png";

        that.images[179] =  Image();
        that.images[179].onload = on_loaded();
        that.images[179].src = IMAGE_DIR+"btn_n-up.png";

        that.images[180] =  Image();
        that.images[180].onload = on_loaded();
        that.images[180].src = IMAGE_DIR+"btn_n-down.png";

        that.images[181] =  Image();
        that.images[181].onload = on_loaded();
        that.images[181].src = IMAGE_DIR+"btn_o-up.png";

        that.images[182] =  Image();
        that.images[182].onload = on_loaded();
        that.images[182].src = IMAGE_DIR+"btn_o-down.png";

        that.images[183] =  Image();
        that.images[183].onload = on_loaded();
        that.images[183].src = IMAGE_DIR+"btn_y-up.png";

        that.images[184] =  Image();
        that.images[184].onload = on_loaded();
        that.images[184].src = IMAGE_DIR+"btn_y-down.png";


        ////////////////////////////////////////////////////////
        // Sounds: /////////////////////////////////////////////
        ////////////////////////////////////////////////////////

        for(i in 0 until soundarray.size){
            sounds[i] =  Audio();
            sounds[i].oncanplaythrough = on_loaded();
            sounds[i].src = SOUND_DIR+soundarray[i];
        }

        ////////////////////////////////////////////////////////
        // Level: //////////////////////////////////////////////
        ////////////////////////////////////////////////////////

        // levels is now loaded externally
        if(that.levels !== null){
            on_loaded();
        }
    }


}