import kotlin.math.ceil
import kotlin.math.round


interface IVolumeBar {
    val offset_x: Int
    val offset_y: Int
    val height: Int
    val width: Int
    var volume: Double
    val colour_1: dynamic
    val colour_2: dynamic
    val colour_3: dynamic
    val colour_4: dynamic
    val colour_5: dynamic
}

class VolumeBar : IVolumeBar {
    override val offset_x = 400;
    override val offset_y = 2;
    override val height = 17;
    override val width = 100;
    override var volume = App.DEFAULT_VOLUME;

    override val colour_1 = js("{r:0, g:255, b: 0}")// Low volume colour: Green
    override val colour_2 = js("{r:255, g:0, b: 0}");// High volume colour: Red
    override val colour_3 = js("{r:255, g:255, b: 255}");// Rest of the volume bar: White
    override val colour_4 = js("{r:0, g:0, b: 0}");// Inbetween bars: Black

    override val colour_5 = js("{r:50, g:50, b:50}");// "off" colour, some grey...
}
