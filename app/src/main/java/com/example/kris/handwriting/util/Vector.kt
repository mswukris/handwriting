package com.example.kris.handwriting.util

import com.example.kris.handwriting.legacy.MousePoint
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.sqrt

class Vector {
    var x: Double
    var y: Double

    constructor() {
        y = 0.0
        x = y
    }

    constructor(x: Double, y: Double) {
        this.x = x
        this.y = y
    }

    constructor(mousePoint: MousePoint) {
        x = mousePoint.x.toDouble()
        y = mousePoint.y.toDouble()
    }

    // Scale vector by a constant ...
    override fun toString(): String {
        return "Vector($x, $y)"
    }

    fun length(): Double {
        return sqrt(x * x + y * y)
    }

    // Dot product of two vectors
    fun dotProduct(v2: Vector): Double {
        return x * v2.x + y * v2.y
    }

    // Normalize
    fun normalize(): Vector {
        val length = length()
        if (length != 0.0) {
            return Vector(x / length, y / length)
        }
        return Vector()
    }

    fun dist(v2: Vector): Double {
        val dx = x - v2.x
        val dy = y - v2.y
        return sqrt(dx * dx + dy * dy)
    }

    fun equals(v2: Vector): Boolean {
        return (dist(v2) < 0.01f)
    }

    companion object {
        // v1 + v2
        fun add(v1: Vector, v2: Vector): Vector {
            return Vector(v1.x + v2.x, v1.y + v2.y)
        }

        // v1 - v2
        fun sub(v1: Vector, v2: Vector): Vector {
            return Vector(v1.x - v2.x, v1.y - v2.y)
        }

        // Scale v1 by scaleFactor
        fun scale(v1: Vector, scaleFactor: Double): Vector {
            return Vector(v1.x * scaleFactor, v1.y * scaleFactor)
        }

        // p1 -> p2
        // Find Normalized perpendicular vector: Rotate +90 degrees
        fun calNorPerpV(p1: Vector, p2: Vector): Vector {
            val dir = sub(p2, p1)
            val nDir = dir.normalize()
            return Vector(-1 * nDir.y, nDir.x)
        }

        fun getDistSqrt(v1:Vector, v2: Vector): Double {
            val dx = v1.x - v2.x
            val dy = v1.y - v2.y
            return dx * dx + dy * dy
        }

        fun getHypotenuse(v1:Vector, v2: Vector): Double {
            val dx = abs(v1.x - v2.x)
            val dy = abs(v1.y - v2.y)
            return hypot(dx, dy)
        }
    }
}