package com.example.kris.handwriting.paint

import android.opengl.GLES20
import com.example.kris.handwriting.CustomGLSurface
import com.example.kris.handwriting.CustomShader
import com.example.kris.handwriting.IOpenGLObject
import com.example.kris.handwriting.legacy.MeshPoint
import com.example.kris.handwriting.util.ColorV4
import com.example.kris.handwriting.util.Smoother
import com.example.kris.handwriting.util.Vector
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.ArrayList

abstract class PaintBase(var screenHeight: Float, var glSurface: CustomGLSurface): IOpenGLObject {

    var meshPointQueue: ConcurrentLinkedQueue<MeshPoint> = ConcurrentLinkedQueue()
    var indices = IntArray(0)
    var colorArray = FloatArray(0)

    var segments: ArrayList<MeshPoint> = ArrayList()

    var swipeBuffer: FloatBuffer = ByteBuffer.allocateDirect(0).asFloatBuffer()
    var indexBuffer: IntBuffer = ByteBuffer.allocateDirect(0).asIntBuffer()
    var colorBuffer: FloatBuffer = ByteBuffer.allocateDirect(0).asFloatBuffer()

    var smoother = Smoother
    var strokeThickness = 40

    abstract fun calPoints()

    override fun draw(m: FloatArray?) {
        calPoints()

        GLES20.glUseProgram(CustomShader.sp_mouse_swipe)

        val mtrxhandle = GLES20.glGetUniformLocation(CustomShader.sp_mouse_swipe, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0)
        GLES20.glUseProgram(CustomShader.sp_mouse_swipe)

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

    private fun genVertexArray(): FloatArray {
        val inSize = segments.size
        val out = FloatArray(inSize * 2)
        if (segments.isEmpty()) return out
        for (i in 0 until inSize) {
            out[2 * i] = segments[i].point.x.toFloat()
            out[2 * i + 1] = segments[i].point.y.toFloat()
        }
        return out
    }

    fun setupBuffers() {
        val floatArray = genVertexArray()
        indices = IntArray(segments.size)
        colorArray = FloatArray(segments.size * 4)
        var j = 0
        for (i in segments.indices) {
            val segment = segments[i]
            indices[i] = i
            colorArray[j] = segment.color.R
            colorArray[j + 1] = segment.color.G
            colorArray[j + 2] = segment.color.B
            colorArray[j + 3] = segment.color.A
            j += 4
        }
        val bb = ByteBuffer.allocateDirect(floatArray.size * 4)
        bb.order(ByteOrder.nativeOrder())
        swipeBuffer = bb.asFloatBuffer()
        swipeBuffer.put(floatArray)
        swipeBuffer.position(0)

        val cb = ByteBuffer.allocateDirect(colorArray.size * 4)
        cb.order(ByteOrder.nativeOrder())
        colorBuffer = cb.asFloatBuffer()
        colorBuffer.put(colorArray)
        colorBuffer.position(0)

        val dlb = ByteBuffer.allocateDirect(indices.size * 4)
        dlb.order(ByteOrder.nativeOrder())
        indexBuffer = dlb.asIntBuffer()
        indexBuffer.put(indices)
        indexBuffer.position(0)
    }

    fun addPoint(point: Vector?, color: ColorV4?) {
        addPoint(MeshPoint(point!!, color!!, 1f))
    }

    fun addPoint(meshPoint: MeshPoint?) {
        if (meshPointQueue.isNotEmpty()) {
            for (meshPoint in meshPointQueue) {
                meshPoint.getOlder()
            }
        }
        meshPointQueue.add(meshPoint)
    }

    fun clearAllPoint() {
        meshPointQueue.clear()
    }

    fun checkSelected(selectedScreenPoint: Vector): Boolean {
        val point = convertToGLCoords(selectedScreenPoint)
        for (meshPoint in meshPointQueue) {
            if (selectedScreenPoint.equals(point)) {
                return true
            }
        }
        return false
    }

    fun convertToGLCoords(inVector: Vector): Vector {
        return Vector(inVector.x, screenHeight - inVector.y)
    }
}