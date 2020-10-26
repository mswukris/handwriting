package com.example.kris.handwriting.util


data class ColorV4(val R: Float, val G: Float, val B: Float, val A: Float) {

    fun interpolate(b: ColorV4, t: Float): ColorV4 {
        return ColorV4(
        R + (b.R - R) * t,
        G + (b.G - G) * t,
        B + (b.B - B) * t,
        A + (b.A - A) * t
        )
    }

}
