package com.example.kris.handwriting.legacy

import com.example.kris.handwriting.util.ColorV4
import com.example.kris.handwriting.util.Util.colorIntensityArray
import com.example.kris.handwriting.util.Vector
import kotlin.math.floor

class MeshPoint {
    var point: Vector
    var color: ColorV4
    var age: Float = 1f

    constructor(point: Vector, color: ColorV4, age: Float) {
        this.point = point
        this.color = color
        this.age = age
    }

    constructor(x: Double, y: Double) {
        this.point = Vector(x, y)
        this.color = ColorV4(0f, 0f, 0f, 1f)
    }

    constructor(x: Double, y: Double, color: ColorV4, age: Float) {
        this.point = Vector(x, y)
        this.color = color
        this.age = age
    }

    fun getOlder(){
        age++
    }

    fun getDistSqrt(p2: MeshPoint): Double {
        return Vector.getDistSqrt(this.point, p2.point)
    }

    companion object {
        fun getDistanceColor(prePt: MeshPoint, curPt: MeshPoint, screenHypotenuse: Float): ColorV4 {
            val distance = Vector.getHypotenuse(prePt.point, curPt.point)
            val relativeDistance = screenHypotenuse / 10.toDouble()
            val index: Int
            index = if (distance > relativeDistance) {
                colorIntensityArray.size - 3
            } else {
                floor(distance * (colorIntensityArray.size / 3) / relativeDistance).toInt() * 3
            }
            return ColorV4(colorIntensityArray[index], colorIntensityArray[index + 1], colorIntensityArray[index + 2], 1f)
        }
    }
}
