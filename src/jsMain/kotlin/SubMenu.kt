import App.Companion.DEFAULT_VOLUME

@JsExport
class Option(val line:Boolean,val check:Int,val name:String,val hotkey:String,val effect_id:Int,val on: ()->Boolean)


@JsExport
class SubMenu(a_width: Int, val a_dd_width: Int, a_name: String, a_arr_options: Array<dynamic>) {
    val width = a_width
    val offset_line = 9;
    val offset_text = 17;

    val dd_width = a_dd_width
    var dd_height = 6
    var name: String = ""
    var options: Array<dynamic>

    init {
        for (element in a_arr_options) {
            if (element.line) {
                this.dd_height += this.offset_line
            } else {
                this.dd_height += this.offset_text
            }
        }
        this.name = a_name;
        this.options = a_arr_options;
    }
}

class Menu(a_offset_x: Int, a_offset_y: Int, a_height: Int, a_submenu_list:Array<SubMenu>){
    val offset_x = a_offset_x;
    val offset_y = a_offset_y;
    val height = a_height;
    var width = 0;
    var submenu_open = -1;

    init {
        for(element in a_submenu_list){
            this.width += element.width
        }
    }

    val submenu_list = a_submenu_list;
}


class VolumeBar(){
    val offset_x = 400;
    val offset_y = 2;
    val height = 17;
    val width = 100;
    var volume = DEFAULT_VOLUME;

    val colour_1 = js("{r:0, g:255, b: 0}")// Low volume colour: Green
    val colour_2 = js("{r:255, g:0, b: 0}");// High volume colour: Red
    val colour_3 = js("{r:255, g:255, b: 255}");// Rest of the volume bar: White
    val colour_4 = js("{r:0, g:0, b: 0}");// Inbetween bars: Black

    val colour_5 = js("{r:50, g:50, b:50}");// "off" colour, some grey...
}