package com.example.kris.handwriting.paint

import android.opengl.GLES20
import com.example.kris.handwriting.CustomGLSurface
import com.example.kris.handwriting.CustomShader
import com.example.kris.handwriting.legacy.MeshPoint
import kotlin.collections.ArrayList

class Pen(screenHeight: Float, surface: CustomGLSurface) : PaintBase(screenHeight, surface){

    override fun calPoints() {
        glSurface.queueEvent {
            val out = ArrayList<MeshPoint>()
            segments.clear()
            if (meshPointQueue.size > 3) {
                smoother.resolve(ArrayList(meshPointQueue), out)
                for (i in out.indices) {
                    segments.add(MeshPoint(convertToGLCoords(out[i].point), out[i].color, out[i].age))
                }
            }
        }
    }

    override fun draw(m: FloatArray?) {
        calPoints()

        GLES20.glUseProgram(CustomShader.sp_mouse_swipe)

        val mtrxhandle = GLES20.glGetUniformLocation(CustomShader.sp_mouse_swipe, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0)
        GLES20.glUseProgram(CustomShader.sp_mouse_swipe)

        setupBuffers()
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glEnable(GLES20.GL_LINE_WIDTH)

        val mPositionHandle = GLES20.glGetAttribLocation(CustomShader.sp_mouse_swipe, "vPosition")
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(mPositionHandle, 2, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        val colorHandle = GLES20.glGetAttribLocation(CustomShader.sp_mouse_swipe, "a_color")
        GLES20.glEnableVertexAttribArray(colorHandle)
        GLES20.glVertexAttribPointer(colorHandle, 4, GLES20.GL_FLOAT, false, 0, colorBuffer)

        val pointSizeHandle = GLES20.glGetUniformLocation(CustomShader.sp_mouse_swipe, "pointSize")
        GLES20.glUniform1f(pointSizeHandle, strokeThickness.toFloat())

        GLES20.glDrawElements(GLES20.GL_POINTS, indexArray.size, GLES20.GL_UNSIGNED_INT, indexBuffer)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
    }
}