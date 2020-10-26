package com.example.kris.handwriting.util

import android.util.Log
import com.example.kris.handwriting.mesh.MeshPoint
import java.util.*

object Smoother {
    private val tmp = ArrayList<MeshPoint>()

    fun resolve(input: ArrayList<MeshPoint>, output: ArrayList<MeshPoint>) {
        var input = input
        var output = output
        output.clear()
        if (input.size <= 2) { //simple copy
            output.addAll(input)
            return
        }

        //simplify with squared tolerance
        if (simplifyTolerance > 0 && input.size > 3) {
            simplify(input, simplifyTolerance * simplifyTolerance, tmp)
            input = tmp
        }

        //perform smooth operations
        if (iterations <= 0) { //no smooth, just copy input to output
            output.addAll(input)
        } else if (iterations == 1) { //1 iteration, smooth to output
            smooth(input, output)
        } else { //multiple iterations.. ping-pong between arrays
            var iters = iterations
            //subsequent iterations
            do {
                smooth(input, output)
                tmp.clear()
                tmp.addAll(output)
                val old = output
                input = tmp
                output = old
            } while (--iters > 0)
        }
    }

    var iterations = 2
    var simplifyTolerance = 35.0

    fun linearInterpolation(a: Float, b: Float, t: Float): Float {
        val out = Math.abs(a - b) * t + a
        Log.d("<^>", " $a $b $t out: $out")
        return out
    }

    fun smooth(input: ArrayList<MeshPoint>, output: ArrayList<MeshPoint>) {
        //expected size
        output.clear()
        output.ensureCapacity(input.size * 2)
        //first element
        output.add(input[0])
        //average elements
        for (i in 0 until input.size - 1) {
            val p0 = input[i]
            val p1 = input[i + 1]
            val Q = MeshPoint(
                0.75f * p0.point.x + 0.25f * p1.point.x,
                0.75f * p0.point.y + 0.25f * p1.point.y,
                p0.color.interpolate(p1.color, 0.25f),
                p0.age * 0.75f + p1.age * .25f
            )
            val R = MeshPoint(
                0.25f * p0.point.x + 0.75f * p1.point.x,
                0.25f * p0.point.y + 0.75f * p1.point.y,
                p0.color.interpolate(p1.color, 0.75f),
                p0.age * 0.25f + p1.age * .75f
            )
            output.add(Q)
            output.add(R)
        }
        //last element
        output.add(input[input.size - 1])
    }

    //simple distance-based simplification
    //adapted from simplify.js
    fun simplify(points: ArrayList<MeshPoint>, sqTolerance: Double, out: ArrayList<MeshPoint>) {
        val len = points.size
        var point: MeshPoint
        var prevPoint = points[0]
        out.clear()
        out.add(prevPoint)
        for (i in 1 until len) {
            point = points[i]
            if (point.getDistSqrt(prevPoint) > sqTolerance) {
                out.add(point)
                prevPoint = point
            }
        }
//        if (!prevPoint.equals(point)) {
//            out.add(point)
//        }
    }

}