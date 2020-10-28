package com.example.kris.handwriting.util


data class ColorV4(val R: Float, val G: Float, val B: Float, var A: Float) {

    fun setAlpha(a: Float) {
        A = a
    }

    fun gradientWhite(scaleFactor: Float): ColorV4 {
        return ColorV4(
            1 - R * scaleFactor,
            1 - G * scaleFactor,
            1 - B * scaleFactor,
            A
        )
    }

    fun gradientTransparent(scaleFactor: Float): ColorV4 {
        return ColorV4(
            R,
            G,
            B,
            A * scaleFactor
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
