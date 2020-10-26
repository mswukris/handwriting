package com.example.kris.handwriting.background

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLUtils
import android.util.Log
import com.example.kris.handwriting.util.ColorV4
import com.example.kris.handwriting.CustomShader.sp_background
import com.example.kris.handwriting.IOpenGLObject
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.IntBuffer
import java.util.*

class BackgroundMesh(private var screenHeight: Float, private var screenWidth: Float, private val texture: Bitmap) : IOpenGLObject {
    var currentColor: ColorV4
    private val backgroundColors: ArrayList<ColorV4> = ArrayList()
    private val colorTimer: Timer = Timer()
    private val startColor: Int = 0
    private val endColor: Int = 1
    private val interpolatePercent: Float = 0.0f
    private lateinit var textureIDs: IntArray
    private lateinit var vertices: FloatArray
    private lateinit var uvs: FloatArray
    private lateinit var indices: IntArray
    private var uvBuffer: FloatBuffer? = null
    private var vertexBuffer: FloatBuffer? = null
    private var drawListBuffer: IntBuffer? = null
    private fun init() {
        setupImage()
        initTextures()
    }

    private fun initTextures() {
        textureIDs = IntArray(1)
        GLES20.glGenTextures(1, textureIDs, 0)
        // Retrieve our image from resources.
        // Bind texture to texturename
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIDs[0])
        // Set filtering
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_LINEAR
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR
        )
        // Set wrapping mode
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, texture, 0)
        texture.recycle()
    }

    private fun setupImage() {
        // Create our UV coordinates.
        Log.d("image width", "" + texture.width)
        Log.d("screen width", "" + screenWidth)
        Log.d("image height", "" + texture.height)
        Log.d("screen height", "" + screenHeight)
        var ratio = 0f
        if (texture.width > screenWidth) {
            ratio = screenWidth / texture.width
        }
        uvs = floatArrayOf(
            ratio, 0.5f,  //bottom left
            ratio, 1.0f,  //top left
            1.0f - ratio, 0.5f,  //bottom right
            1.0f - ratio, 1f //top right
        )
        // The texture buffer
        val bb = ByteBuffer.allocateDirect(uvs.size * 4)
        bb.order(ByteOrder.nativeOrder())
        uvBuffer = bb.asFloatBuffer()
        uvBuffer!!.put(uvs)
        uvBuffer!!.position(0)
    }

    override fun draw(m: FloatArray?) {

//        Log.d("gl_background","before")
        GLES20.glUseProgram(sp_background)
        //        Log.d("gl_background","after")
        val mtrxhandle = GLES20.glGetUniformLocation(sp_background, "MVP")
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, m, 0)
        GLES20.glClearColor(currentColor.R, currentColor.G, currentColor.B, 1f)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        val mPositionHandle = GLES20.glGetAttribLocation(sp_background, "inVertex")
        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle)
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(
            mPositionHandle, 2,
            GLES20.GL_FLOAT, false,
            0, vertexBuffer
        )
        val mTexCoordLoc = GLES20.glGetAttribLocation(sp_background, "inTextureCoordinate")
        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mTexCoordLoc)
        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer(
            mTexCoordLoc, 2, GLES20.GL_FLOAT,
            false,
            0, uvBuffer
        )
        // Get handle to shape's transformation matrix‘


        // Get handle to textures locations
        val mSamplerLoc = GLES20.glGetUniformLocation(sp_background, "texture")

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i(mSamplerLoc, 0)
        // Draw the triangle
        GLES20.glDrawElements(
            GLES20.GL_TRIANGLE_STRIP, indices.size,
            GLES20.GL_UNSIGNED_INT, drawListBuffer
        )
        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle)
        GLES20.glDisableVertexAttribArray(mTexCoordLoc)
    }

    fun setScreenWidth(screenWidth: Float) {
        this.screenWidth = screenWidth
        createBackgroundMesh()
    }

    private fun createBackgroundMesh() {
        // We have create the vertices of our view.
        vertices = floatArrayOf(
            0f, 0f, 0f, screenHeight,
            screenWidth, 0f,
            screenWidth, screenHeight
        )
        indices = intArrayOf(0, 1, 2, 3)

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
    }

    fun setScreenHeight(screenHeight: Float) {
        this.screenHeight = screenHeight
        createBackgroundMesh()
    }

    init {
        backgroundColors.add(ColorV4(0.792f, 1f, 0.952f, 1f)) //blue
        backgroundColors.add(ColorV4(0.870f, 0.741f, 1f, 1f)) //purple
        backgroundColors.add(ColorV4(1f, 0.615f, 0.819f, 1f)) //pink
        currentColor = ColorV4(0.792f, 1f, 0.952f, 1f) //default blue start
        init()

//        colorTimer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                if(interpolatePercent >= 1){
//                    startColor = ( startColor + 1 ) % 3
//                    endColor = ( endColor + 1 ) % 3
//                    interpolatePercent = 0
//                }
//                currentColor=  backgroundColors.get(startColor).interpolate(
//                                         backgroundColors.get(endColor),
//                                         interpolatePercent)
//                interpolatePercent += 0.01f
//
//            }
//        }, 0, 1000L / 30L)
    }
}