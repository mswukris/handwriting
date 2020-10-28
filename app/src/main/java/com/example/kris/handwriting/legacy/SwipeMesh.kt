package com.example.kris.handwriting.legacy

import com.example.kris.handwriting.CustomGLSurface
import com.example.kris.handwriting.IOpenGLObject
import com.example.kris.handwriting.paint.PaintBase
import com.example.kris.handwriting.util.Vector
import java.util.*
import kotlin.collections.ArrayList

class SwipeMesh(screenHeight: Float, surface: CustomGLSurface) : PaintBase(screenHeight, surface), IOpenGLObject {
    private val ageOfDeath = 60

    private val ageTimer: Timer = Timer()
    private var removeCounter = 0

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
                        val meshPoint = out[i]
                        segments.add(MeshPoint(convertToGLCoords(meshPoint.point), out[i].color, meshPoint.age))
                    } else {
                        val currentMeshPoint = out[i]
                        val nextMeshPoint = out[i + 1]
                        A = currentMeshPoint.point
                        B = nextMeshPoint.point
                        val perpV = Vector.calNorPerpV(A, B)
                        C = MeshPoint(
                            convertToGLCoords(Vector.add(B, Vector.scale(perpV, strokeThickness * (i.toFloat() / out.size).toDouble()))),
                            nextMeshPoint.color.gradientWhite(i.toFloat() / out.size),
                            nextMeshPoint.age
                        )
                        D = MeshPoint(
                            convertToGLCoords(Vector.sub(B, Vector.scale(perpV, strokeThickness * (i.toFloat() / out.size).toDouble()))),
                            nextMeshPoint.color.gradientWhite(i.toFloat() / out.size),
                            nextMeshPoint.age
                        )
                        segments.add(C)
                        segments.add(D)
                    }
                }
                if (meshPointQueue.size > 50) {
                    meshPointQueue.poll()
                }
            }
        }
    }

    init {
        ageTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                for (meshPoint in meshPointQueue) {
                    meshPoint.getOlder()
                    removeCounter++
                    if (meshPoint.age > ageOfDeath) {
                        meshPointQueue.poll()
                        removeCounter = 0
                    }
                }
                calPoints()
            }
        }, 0, 1000L / 120L)
    }
}