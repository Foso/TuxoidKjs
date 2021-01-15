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

fun render_vol_bar() {
    var vb = vis.vol_bar;
    var switcher = false;

    for (i in 0 until vb.width) {
        var line_height: Int = 0

        if (switcher) {
            switcher = false;
            MYCTX.fillStyle = "rgb(" + vb.colour_4.r + ", " + vb.colour_4.g + ", " + vb.colour_4.b + ")";
        } else {
            switcher = true;
            var ratio2 = i / vb.width.toDouble();
            line_height = round((vb.height * ratio2).toDouble()).toInt();

            if (i < ceil(vb.volume * vb.width)) {
                if (game.sound) {
                    var ratio1 = 1 - ratio2;
                    MYCTX.fillStyle =
                        "rgb(" + round(vb.colour_1.r * ratio1 + vb.colour_2.r * ratio2) + ", " + round(vb.colour_1.g * ratio1 + vb.colour_2.g * ratio2) + ", " + round(
                            vb.colour_1.b * ratio1 + vb.colour_2.b * ratio2
                        ) + ")";
                } else {
                    MYCTX.fillStyle = "rgb(" + vb.colour_5.r + ", " + vb.colour_5.g + ", " + vb.colour_5.b + ")";
                }
            } else {
                MYCTX.fillStyle = "rgb(" + vb.colour_3.r + ", " + vb.colour_3.g + ", " + vb.colour_3.b + ")";
            }

        }
        MYCTX.fillRect(vb.offset_x + i, vb.offset_y + vb.height - line_height, 1, line_height);

    }
}
