package com.example.kris.handwriting.paint

import android.opengl.GLES20
import com.example.kris.handwriting.CustomGLSurface
import com.example.kris.handwriting.CustomShader
import com.example.kris.handwriting.legacy.MeshPoint
import com.example.kris.handwriting.util.Smoother
import com.example.kris.handwriting.util.Vector
import kotlin.collections.ArrayList

class Pen(screenHeight: Float, surface: CustomGLSurface) : PaintBase(screenHeight, surface){

    var smoother = Smoother

    var strokeThickness = 40

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

    override fun draw(m: FloatArray?) {
        calPoints()

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
}