package ui.menu


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


