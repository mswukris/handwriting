package com.example.kris.handwriting.mesh

import com.example.kris.handwriting.CustomGLSurface
import com.example.kris.handwriting.IOpenGLObject
import com.example.kris.handwriting.util.ColorV4
import com.example.kris.handwriting.util.Vector
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.collections.ArrayList

abstract class MeshBase(var screenHeight: Float, var glSurface: CustomGLSurface): IOpenGLObject {

    var meshPointQueue: ConcurrentLinkedQueue<MeshPoint> = ConcurrentLinkedQueue()
    var indices = IntArray(0)
    var colorArray = FloatArray(0)

    var segments: ArrayList<MeshPoint> = ArrayList()

    var swipeBuffer: FloatBuffer = ByteBuffer.allocateDirect(0).asFloatBuffer()
    var indexBuffer: IntBuffer = ByteBuffer.allocateDirect(0).asIntBuffer()
    var colorBuffer: FloatBuffer = ByteBuffer.allocateDirect(0).asFloatBuffer()

    abstract fun calPoints()

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
            colorArray[j + 3] = 1f
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
}