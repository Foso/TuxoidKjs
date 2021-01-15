external interface Block {

    val just_moved: Boolean
    var animation_delay: Int
    val moving_offset: dynamic
    val gets_removed_in: Int
    val pushing: Boolean
    val face_dir: Int
    val moving: Boolean
    var fine_offset_y: Int
    var fine_offset_x: Int
    var animation_frame: Int
    val id: Int

}