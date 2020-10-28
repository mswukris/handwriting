package com.example.kris.handwriting.paint

import com.example.kris.handwriting.CustomGLSurface
import com.example.kris.handwriting.legacy.MeshPoint
import com.example.kris.handwriting.util.Vector
import kotlin.collections.ArrayList

class Pen(screenHeight: Float, surface: CustomGLSurface) : PaintBase(screenHeight, surface){

    override fun calPoints() {
        glSurface.queueEvent {
            val out = ArrayList<MeshPoint>()
            segments.clear()
            if (meshPointQueue.size > 3) {
                smoother.resolve(ArrayList(meshPointQueue), out)
                var A: Vector
                var B: Vector
                var C: MeshPoint?
                var D: MeshPoint?
                for (i in out.indices) {
                    if (i == 0 || i == out.size - 1) {
                        segments.add(MeshPoint(convertToGLCoords(out[i].point), out[i].color, out[i].age))
                    } else {
                        val currentMeshPoint = out[i]
                        val nextMeshPoint = out[i + 1]
                        A = currentMeshPoint.point
                        B = nextMeshPoint.point
                        val perpV = Vector.calNorPerpV(A, B)
                        C = MeshPoint(
                            convertToGLCoords(
                                Vector.add(B, Vector.scale(perpV, strokeThickness.toDouble()))
                            ),
                            nextMeshPoint.color,
                            nextMeshPoint.age
                        )
                        D = MeshPoint(
                            convertToGLCoords(
                                Vector.sub(B, Vector.scale(perpV, strokeThickness.toDouble()))
                            ),
                            nextMeshPoint.color,
                            nextMeshPoint.age
                        )
                        segments.add(C)
                        segments.add(D)
                    }
                }
            }
        }
    }
}