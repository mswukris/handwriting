package com.example.kris.handwriting.mesh

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import com.example.kris.handwriting.CustomShader
import com.example.kris.handwriting.IOpenGLObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer


class LegacyMesh(var context: Context, private val textureId: Int): IOpenGLObject {

    // Geometric variables
    private val width = 0f
    private val height = 0f
    var left = 0f
    var right = 0f
    var top = 0f
    var bottom = 0f
    private lateinit var vertices: FloatArray
    private lateinit var color: FloatArray
    private lateinit var uvs: FloatArray
    private lateinit var indices: IntArray

    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null
    private var uvBuffer: FloatBuffer? = null
    private var drawListBuffer: IntBuffer? = null
    private var lifeCounterAlpha: Float
    private var lifeCounterSize: Float

    private fun setupTriangle() {
        // We have create the vertices of our view.
        vertices = floatArrayOf(
            left, bottom,
            left, top,
            right, bottom,
            right, top
        )
        indices = intArrayOf(0, 1, 2, 3, 0) // loop in the android official tutorial opengles why different order.
        color = floatArrayOf(
            1f,
            1f,
            0f,
            1f
        )

        // The vertex buffer.
        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer!!.put(vertices)
        vertexBuffer!!.position(0)

        // initialize byte buffer for the draw list
        val dlb = ByteBuffer.allocateDirect(indices.size * 4)
        dlb.order(ByteOrder.nativeOrder())
        drawListBuffer = dlb.asIntBuffer()
        drawListBuffer!!.put(indices)
        drawListBuffer!!.position(0)
        val cb = ByteBuffer.allocateDirect(color.size * 4)
        cb.order(ByteOrder.nativeOrder())
        colorBuffer = cb.asFloatBuffer()
        colorBuffer!!.put(color)
        colorBuffer!!.position(0)
    }

    private fun setupImage() {
        // Create our UV coordinates.
        uvs = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
        )
        // The texture buffer
        val bb = ByteBuffer.allocateDirect(uvs.size * 4)
        bb.order(ByteOrder.nativeOrder())
        uvBuffer = bb.asFloatBuffer()
        uvBuffer!!.put(uvs)
        uvBuffer!!.position(0)
    }

    override fun draw(m: FloatArray?) {
        calcPoints()
        setColorBuffer(1 - (1 - 1 / lifeCounterAlpha), color[1], color[2], 1 / lifeCounterAlpha)
        val mPositionHandle = GLES20.glGetAttribLocation(CustomShader.sp_mouse_swipe, "vPosition")
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        GLES20.glVertexAttribPointer(
            mPositionHandle, 2,
            GLES20.GL_FLOAT, false,
            0, vertexBuffer
        )

        val colorHandle = GLES20.glGetUniformLocation(CustomShader.sp_mouse_swipe, "a_color")
        GLES20.glUniform4f(colorHandle, color[0], color[1], color[2], color[3])
//        int mTexCoordLoc = GLES20.glGetAttribLocation(CustomShader.INSTANCE.getSp_mouse_swipe(), "a_texCoord")
//        GLES20.glEnableVertexAttribArray(mTexCoordLoc);
//        GLES20.glVertexAttribPointer(mTexCoordLoc, 2, GLES20.GL_FLOAT,false,0, uvBuffer)

        val mtrxhandle = GLES20.glGetUniformLocation(CustomShader.sp_mouse_swipe, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0)
//        int mSamplerLoc = GLES20.glGetUniformLocation(CustomShader.INSTANCE.getSp_mouse_swipe(), "s_texture")

        // Set the sampler texture unit to 0, where we have saved the texture.
//        GLES20.glUniform1i(mSamplerLoc, 0)

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, indices.size, GLES20.GL_UNSIGNED_INT, drawListBuffer)
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        GLES20.glDisableVertexAttribArray(mPositionHandle)
//        GLES20.glDisableVertexAttribArray(mTexCoordLoc)

        lifeCounterAlpha += lifeCounterAlpha / 2.5.toFloat()
        lifeCounterSize += lifeCounterSize / 30
        left += lifeCounterSize
        right -= lifeCounterSize
        bottom += lifeCounterSize
        top -= lifeCounterSize
        Log.d("<^>", "$left,$top,$right,$bottom")
    }

    private fun calcPoints() {
        vertices = floatArrayOf(
            left, top,
            left, bottom,
            right, bottom,
            right, top
        )

        // The vertex buffer.
        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer()
        vertexBuffer!!.put(vertices)
        vertexBuffer!!.position(0)
    }

    private fun setColorBuffer(R: Float, G: Float, B: Float, A: Float) {
        color = floatArrayOf(R, G, B, A)
        val cb = ByteBuffer.allocateDirect(color.size * 4)
        cb.order(ByteOrder.nativeOrder())
        colorBuffer = cb.asFloatBuffer()
        colorBuffer!!.put(color)
        colorBuffer!!.position(0)
    }

    init {
        setupTriangle()
        setupImage()
        lifeCounterAlpha = 1f
        lifeCounterSize = 1f
    }
}