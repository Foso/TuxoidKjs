package ui.menu


class Option(
    val line: Boolean,
    val check: Int,
    val name: String,
    val hotkey: String,
    val effect_id: Int,
    val on: () -> Boolean
)