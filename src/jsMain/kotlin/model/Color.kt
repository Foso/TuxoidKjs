package model

class Color{
    companion object{
        var black = js("{r:0, g:0, b: 0}")
        var dark_grey = js("{r:64, g:64, b:64}")
        var med_grey = js("{r:128, g:128, b:128}")
        var light_grey = js("{r:212, g:208, b:200}")
        var white = js("{r:255, g:255, b: 255}")
        var blue = js("{r:10, g:36, b:106}")
        fun toRgbString(color:dynamic):String{
            return "rgb(" + color.r + ", " + color.g + ", " + color.b + ")"
        }
    }
}