import de.jensklingenberg.bananiakt.EXTERNAL_LEVELS
import org.w3c.dom.Audio
import org.w3c.dom.Image


class MyRes {
    var NUM_RESOURCES = 197;
    var IMAGE_DIR = "images/";
    var SOUND_DIR = "sound/";

    var soundarray = arrayOf(
        "about.mp3",
        "argl.mp3",
        "attack1.mp3",
        "attack2.mp3",
        "chart.mp3",
        "click.mp3",
        "gameend.mp3",
        "getpoint.mp3",
        "newplane.mp3",
        "opendoor.mp3",
        "wow.mp3",
        "yeah.mp3"
    )


    var resources_loaded = 0;
    var already_loading = false;
    var levels = EXTERNAL_LEVELS
    var images = arrayOf<dynamic>();
    var sounds = arrayOf<dynamic>();

    init {
        load()
    }


    fun on_loaded() {
        resources_loaded++;
    }




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
            images[2 + i] = Image();
            images[2 + i].onload = on_loaded();
            images[2 + i].src = IMAGE_DIR + "garbage_" + i + ".png";
        }

        for (i in 0 until 11) {// From 11 to 21 digits
            images[11 + i] = Image();
            images[11 + i].onload = on_loaded();
            images[11 + i].src = IMAGE_DIR + "digits_" + i + ".png";
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

        for (i in 0 until 13) {// From 59 to 110 player (berti)
            for (j in 0 until 4) {// Reversed order for ease of access
                images[59 + 4 * i + j] = Image();
                images[59 + 4 * i + j].onload = on_loaded();
                images[59 + 4 * i + j].src = IMAGE_DIR + "player_" + j + "-" + i + ".png";
            }
        }

        for (i in 0 until 9) {// From 111 to 146 monster 1(purple)
            for (j in 0 until 4) {// Reversed order for ease of access
                images[111 + 4 * i + j] = Image();
                images[111 + 4 * i + j].onload = on_loaded();
                images[111 + 4 * i + j].src = IMAGE_DIR + "monster1_" + j + "-" + i + ".png";
            }
        }

        for (i in 0 until 5) {// From 147 to 166 monster 2(green)
            for (j in 0 until 4) {// Reversed order for ease of access
                images[147 + 4 * i + j] = Image();
                images[147 + 4 * i + j].onload = on_loaded();
                images[147 + 4 * i + j].src = IMAGE_DIR + "monster2_" + j + "-" + i + ".png";
            }
        }

        images[167] = Image();
        images[167].onload = on_loaded();
        images[167].src = IMAGE_DIR + "argl.png";

        images[168] = Image();
        images[168].onload = on_loaded();
        images[168].src = IMAGE_DIR + "wow.png";

        images[169] = Image();
        images[169].onload = on_loaded();
        images[169].src = IMAGE_DIR + "yeah.png";

        images[170] = Image();
        images[170].onload = on_loaded();
        images[170].src = IMAGE_DIR + "exit.png";

        images[171] = Image();
        images[171].onload = on_loaded();
        images[171].src = IMAGE_DIR + "check_b.png";

        images[172] = Image();
        images[172].onload = on_loaded();
        images[172].src = IMAGE_DIR + "check_w.png";

        images[173] = Image();
        images[173].onload = on_loaded();
        images[173].src = IMAGE_DIR + "dbx_confirm.png";

        images[174] = Image();
        images[174].onload = on_loaded();
        images[174].src = IMAGE_DIR + "dbx_saveload.png";

        images[175] = Image();
        images[175].onload = on_loaded();
        images[175].src = IMAGE_DIR + "dbx_loadlvl.png";

        images[176] = Image();
        images[176].onload = on_loaded();
        images[176].src = IMAGE_DIR + "dbx_charts.png";

        images[177] = Image();
        images[177].onload = on_loaded();
        images[177].src = IMAGE_DIR + "btn_c-up.png";

        images[178] = Image();
        images[178].onload = on_loaded();
        images[178].src = IMAGE_DIR + "btn_c-down.png";

        images[179] = Image();
        images[179].onload = on_loaded();
        images[179].src = IMAGE_DIR + "btn_n-up.png";

        images[180] = Image();
        images[180].onload = on_loaded();
        images[180].src = IMAGE_DIR + "btn_n-down.png";

        images[181] = Image();
        images[181].onload = on_loaded();
        images[181].src = IMAGE_DIR + "btn_o-up.png";

        images[182] = Image();
        images[182].onload = on_loaded();
        images[182].src = IMAGE_DIR + "btn_o-down.png";

        images[183] = Image();
        images[183].onload = on_loaded();
        images[183].src = IMAGE_DIR + "btn_y-up.png";

        images[184] = Image();
        images[184].onload = on_loaded();
        images[184].src = IMAGE_DIR + "btn_y-down.png";


        ////////////////////////////////////////////////////////
        // Sounds: /////////////////////////////////////////////
        ////////////////////////////////////////////////////////

        for (i in soundarray.indices) {
            sounds[i] = Audio();
            sounds[i].oncanplaythrough = on_loaded();
            sounds[i].src = SOUND_DIR + soundarray[i];
        }

        ////////////////////////////////////////////////////////
        // Level: //////////////////////////////////////////////
        ////////////////////////////////////////////////////////

        // levels is now loaded externally
        on_loaded()
    }


}