package cc.crab55e.metsChat.util

import java.awt.Color

class ColorCodeToColor(val colorCode: String) {
    val color: Color
    init {
            val colorCode = colorCode.removePrefix("#")
            val intColor = Integer.parseInt(colorCode, 16)
            color = Color(intColor, false)
    }
}