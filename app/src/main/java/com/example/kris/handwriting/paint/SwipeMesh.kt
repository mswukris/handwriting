package com.example.kris.handwriting.paint

import android.opengl.GLES20
import com.example.kris.handwriting.CustomGLSurface
import com.example.kris.handwriting.CustomShader
import com.example.kris.handwriting.IOpenGLObject
import com.example.kris.handwriting.legacy.MeshPoint
import com.example.kris.handwriting.util.Smoother
import com.example.kris.handwriting.util.Vector
import java.util.*
import kotlin.collections.ArrayList

class SwipeMesh(screenHeight: Float, surface: CustomGLSurface) : PaintBase(screenHeight, surface), IOpenGLObject {
    private val ageOfDeath = 60

    var smoother = Smoother

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
                            convertToGLCoords(Vector.add(B, Vector.scale(perpV, 40 * (i.toFloat() / out.size).toDouble()))),
                            nextMeshPoint.color.gradientWhite(i.toFloat() / out.size),
                            nextMeshPoint.age
                        )
                        D = MeshPoint(
                            convertToGLCoords(Vector.sub(B, Vector.scale(perpV, 40 * (i.toFloat() / out.size).toDouble()))),
                            nextMeshPoint.color.gradientWhite(i.toFloat() / out.size),
                            nextMeshPoint.age
                        )
                        segments.add(C)
                        segments.add(D)
                    }
                }
//                if (meshPointQueue.size > 50) {
//                    meshPointQueue.poll()
//                }
            }
        }
    }

    override fun draw(m: FloatArray?) {
//        Log.d("gl_swipe", "before");
        GLES20.glUseProgram(CustomShader.sp_mouse_swipe)

        val mtrxhandle = GLES20.glGetUniformLocation(CustomShader.sp_mouse_swipe, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0)
        GLES20.glUseProgram(CustomShader.sp_mouse_swipe)
//        Log.d("gl_swipe","after");
        setupBuffers()
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        val mPositionHandle = GLES20.glGetAttribLocation(CustomShader.sp_mouse_swipe, "vPosition")
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, swipeBuffer)

        val colorHandle = GLES20.glGetAttribLocation(CustomShader.sp_mouse_swipe, "a_color")
        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, indices.size, GLES20.GL_UNSIGNED_INT, indexBuffer)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }

    init {
        ageTimer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
//                for (meshPoint in meshPointQueue) {
//                    meshPoint.getOlder()
//                    removeCounter++
//                    if (meshPoint.age > ageOfDeath) {
//                        meshPointQueue.poll()
//                        removeCounter = 0
//                    }
//                }
                calPoints()
            }
        }, 0, 1000L / 120L)
    }
}