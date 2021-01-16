package ui.menu

class Menu(a_offset_x: Int, a_offset_y: Int, a_height: Int, a_submenu_list: Array<SubMenu>) {
    val offset_x = a_offset_x;
    val offset_y = a_offset_y;
    val height = a_height;
    var width = 0;
    var submenu_open = -1;

    init {
        for (element in a_submenu_list) {
            this.width += element.width
        }
    }

    val submenu_list = a_submenu_list;
}