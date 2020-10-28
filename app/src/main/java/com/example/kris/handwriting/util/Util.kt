package com.example.kris.handwriting.util

object Util {

    val colorIntensityArray = floatArrayOf(
        0.345098f, 0.172549f, 0.513725f,
        0.380915f, 0.161046f, 0.519216f,
        0.416732f, 0.149542f, 0.524706f,
        0.452549f, 0.138039f, 0.530196f,
        0.488366f, 0.126536f, 0.535686f,
        0.524183f, 0.115033f, 0.541176f,
        0.560000f, 0.103529f, 0.546667f,
        0.595817f, 0.092026f, 0.552157f,
        0.631634f, 0.080523f, 0.557647f,
        0.667451f, 0.069020f, 0.563137f,
        0.703268f, 0.057516f, 0.568627f,
        0.739085f, 0.046013f, 0.574118f,
        0.774902f, 0.034510f, 0.579608f,
        0.810719f, 0.023007f, 0.585098f,
        0.846536f, 0.011503f, 0.590588f,
        0.882353f, 0.000000f, 0.596078f
    )

    /**
     * Tension: 1 is high, 0 normal, -1 is low
     * Bias: 0 is even,
     * positive is towards first segment,
     * negative towards the other
     */
    fun hermiteInterpolate(
        pointA: Float, pointB: Float,
        pointC: Float, pointD: Float,
        mu: Float
    ): Float {
        val mu3: Float
        val a0: Float
        val a1: Float
        val a2: Float
        val a3: Float

        val mu2: Float = mu * mu
        mu3 = mu2 * mu

        var m0: Float = (pointB - pointA) * 1 / 2
        m0 += (pointC - pointB) * 1 / 2

        var m1: Float = (pointC - pointB) * 1 / 2
        m1 += (pointD - pointC) * 1 / 2

        a0 = 2 * mu3 - 3 * mu2 + 1
        a1 = mu3 - 2 * mu2 + mu
        a2 = mu3 - mu2
        a3 = -2 * mu3 + 3 * mu2
        return a0 * pointB + a1 * m0 + a2 * m1 + a3 * pointC
    }

    fun InterpolateHermite4pt3oX(x0: Float, x1: Float, x2: Float, x3: Float, t: Float): Float {
        val c1 = .5f * (x2 - x0)
        val c2 = x0 - 2.5f * x1 + 2 * x2 - .5f * x3
        val c3 = .5f * (x3 - x0) + 1.5f * (x1 - x2)
        return ((c3 * t + c2) * t + c1) * t + x1
    }
}