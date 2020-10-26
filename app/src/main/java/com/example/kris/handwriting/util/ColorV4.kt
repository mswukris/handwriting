package com.example.kris.handwriting.util


data class ColorV4(val R: Float, val G: Float, val B: Float, val A: Float) {

    fun gradientWhite(scaleFactor: Float): ColorV4 {
        return ColorV4(
            1 - R * scaleFactor,
            1 - G * scaleFactor,
            1 - B * scaleFactor,
            A
        )
    }

    fun gradientBlack(scaleFactor: Float): ColorV4 {
        return ColorV4(
            R * scaleFactor,
            G * scaleFactor,
            B * scaleFactor,
            A
        )
    }

    fun interpolate(b: ColorV4, t: Float): ColorV4 {
        return ColorV4(
            R + (b.R - R) * t,
            G + (b.G - G) * t,
            B + (b.B - B) * t,
            A + (b.A - A) * t
        )
    }

}
