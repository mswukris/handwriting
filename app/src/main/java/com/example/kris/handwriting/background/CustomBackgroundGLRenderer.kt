package com.example.kris.handwriting.background

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.MotionEvent
import com.example.kris.handwriting.*
import com.example.kris.handwriting.util.Vector
import com.example.kris.handwriting.mesh.LegacyMesh
import com.example.kris.handwriting.mesh.MousePoint
import com.example.kris.handwriting.util.ColorV4
import com.example.kris.handwriting.util.Smoother
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.hypot


class CustomBackgroundGLRenderer(// Misc
    var context: Context, surface: CustomGLSurface
) : GLSurfaceView.Renderer {
    // Our matrices
    private val mtrxProjection = FloatArray(16)
    private val mtrxView = FloatArray(16)
    private val mtrxProjectionAndView = FloatArray(16)
    private val colorIntensityArray = floatArrayOf(
        0.345098f, 0.172549f, 0.513725f,
        0.380915f, 0.161046f, 0.519216f,
        0.416732f, 0.149542f, 0.524706f,
        0.452549f, 0.138039f, 0.530196f,
        0.488366f, 0.126536f, 0.535686f,
        0.524183f, 0.115033f, 0.541176f,
        0.560000f, 0.103529f, 0.546667f,
        0.595817f, 0.092026f, 0.552157f,
        0.631634f, 0.080523f, 0.557647f,
        0.667451f, 0.069020f, 0.563137f,
        0.703268f, 0.057516f, 0.568627f,
        0.739085f, 0.046013f, 0.574118f,
        0.774902f, 0.034510f, 0.579608f,
        0.810719f, 0.023007f, 0.585098f,
        0.846536f, 0.011503f, 0.590588f,
        0.882353f, 0.000000f, 0.596078f
    )
    var legacyMeshes: ConcurrentLinkedQueue<LegacyMesh>? = null
    var mMousePoints: ConcurrentLinkedQueue<MousePoint>? = null
    var openGLObjects: ArrayList<IOpenGLObject>? = null
    private lateinit var mousePoints: FloatArray
    var texture: Bitmap? = null
    var smoother = Smoother

    // Our screen resolution
    var screenWidth = 1280f
    var screenHeight = 768f
    var lastTime: Long
    var mProgram = 0
    var surface: CustomGLSurface
    private var swipeMesh1: SwipeMesh? = null
    private var swipeMesh2: SwipeMesh? = null
    private var backgroundMesh: BackgroundMesh? = null
    private var prevPointer1Point: Vector? = null
    private var prevPointer2Point: Vector? = null
    private var screenhypotenuse = 0f
    private var surfaceLoaded = false
    fun onPause() {
        /* Do stuff to pause the renderer */
    }

    fun onResume() {
        /* Do stuff to resume the renderer */
        lastTime = System.currentTimeMillis()
    }

    override fun onDrawFrame(unused: GL10) {
        // Get the current time
        val now = System.currentTimeMillis()
        // We should make sure we are valid and sane
        if (lastTime > now) return
        // Get the amount of time the last frame took.
        val elapsed = now - lastTime
        // Update our example
        // Render our example
        render(mtrxProjectionAndView)
        // Save the current time to see how long it took <img src="http://androidblog.reindustries.com/wp-includes/images/smilies/icon_smile.gif" alt=":)" class="wp-smiley"> .
        lastTime = now
    }

    private fun render(m: FloatArray) {
        // clear Screen and Depth Buffer, we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        for (openGLObject in openGLObjects!!) {
            openGLObject.draw(m)
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        // We need to know the current width and height.
        Log.d("<^>", "Surface Changed start")
        surfaceLoaded = false
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()
        screenhypotenuse = Math.hypot(screenWidth.toDouble(), screenHeight.toDouble()).toFloat()
        swipeMesh1!!.screenHeight = height.toFloat()
        swipeMesh2!!.screenHeight = height.toFloat()
        backgroundMesh!!.setScreenHeight(screenHeight)
        backgroundMesh!!.setScreenWidth(screenWidth)

        // Redo the Viewport, making it fullscreen.
        GLES20.glViewport(0, 0, screenWidth.toInt(), screenHeight.toInt())

        // Clear our matrices
        for (i in 0..15) {
            mtrxProjection[i] = 0.0f
            mtrxView[i] = 0.0f
            mtrxProjectionAndView[i] = 0.0f
        }
        // Setup our screen width and height for normal sprite translation.
        Matrix.orthoM(mtrxProjection, 0, 0f, screenWidth, 0.0f, screenHeight, 0f, 50f)

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mtrxView, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f)

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mtrxProjectionAndView, 0, mtrxProjection, 0, mtrxView, 0)
        mousePoints = FloatArray(4)
        surfaceLoaded = true
        Log.d("<^>", "Surface Changed end")
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        Log.d("<^>", "Surface Create started")

        surfaceLoaded = false
        val id = context.resources.getIdentifier(
            "drawable/touch_gradient", null,
            context.packageName
        )

        // Temporary create a bitmap
        if (texture == null) {
            texture = BitmapFactory.decodeResource(context.resources, id)
        }

//        initTextures();
        legacyMeshes = ConcurrentLinkedQueue()
        backgroundMesh = texture?.let { BackgroundMesh(screenHeight, screenWidth, it) }

        swipeMesh1 = SwipeMesh(screenHeight, surface)
        swipeMesh2 = SwipeMesh(screenHeight, surface)
        mMousePoints = ConcurrentLinkedQueue()
        GLES20.glClearColor(1f, 1f, 1f, 1f)
        CustomShader.sp_mouse_swipe = GLES20.glCreateProgram()
        val mouseMeshVertexShader = CustomShader.loadShader(
            GLES20.GL_VERTEX_SHADER,
            CustomShader.vs_mouseSwipe
        )
        val mouseMeshFragmentShader = CustomShader.loadShader(
            GLES20.GL_FRAGMENT_SHADER,
            CustomShader.fs_mouseSwipe
        )
        GLES20.glAttachShader(CustomShader.sp_mouse_swipe, mouseMeshVertexShader)
        GLES20.glAttachShader(CustomShader.sp_mouse_swipe, mouseMeshFragmentShader)
        GLES20.glLinkProgram(CustomShader.sp_mouse_swipe)
        CustomShader.sp_background = GLES20.glCreateProgram()
        val backgroundVertexShader = CustomShader.loadShader(
            GLES20.GL_VERTEX_SHADER,
            CustomShader.vs_Texture
        )
        val backgroundFragmentShader = CustomShader.loadShader(
            GLES20.GL_FRAGMENT_SHADER,
            CustomShader.fs_Texture
        )
        GLES20.glAttachShader(CustomShader.sp_background, backgroundVertexShader)
        GLES20.glAttachShader(CustomShader.sp_background, backgroundFragmentShader)
        GLES20.glLinkProgram(CustomShader.sp_background)

        // Set our shader program
        openGLObjects = ArrayList()
        openGLObjects!!.add(backgroundMesh!!)
        openGLObjects!!.add(swipeMesh1!!)
        openGLObjects!!.add(swipeMesh2!!)
        surfaceLoaded = true
        Log.d("<^>", "Surface Created ended")
    }

    fun processTouchEvent(event: MotionEvent) {
        if (!surfaceLoaded) return
        var pointerIndex: Int
        if (event.pointerCount <= 2) {
            if (event.findPointerIndex(0) != -1) {
                pointerIndex = event.findPointerIndex(0)
                val point = Vector(
                    event.getX(pointerIndex).toDouble(),
                    event.getY(pointerIndex).toDouble()
                )
                if (prevPointer1Point == null) {
                    swipeMesh1!!.addPoint(point, ColorV4(1f, 0f, 0f, 1f))
                } else {
                    swipeMesh1!!.addPoint(
                        point,
                        getDistanceColor(prevPointer1Point!!, point)
                    )
                }
                prevPointer1Point = point
            }
            if (event.findPointerIndex(1) != -1) {
                pointerIndex = event.findPointerIndex(1)
                val point = Vector(
                    event.getX(pointerIndex).toDouble(),
                    event.getY(pointerIndex).toDouble()
                )
                if (prevPointer2Point == null) {
                    swipeMesh2!!.addPoint(point, ColorV4(1f, 0f, 0f, 1f))
                } else {
                    swipeMesh2!!.addPoint(
                        point,
                        getDistanceColor(prevPointer2Point!!, point)
                    )
                }
                prevPointer2Point = point
            }
        }
    }

    private fun getDistanceColor(p1: Vector, p2: Vector): ColorV4 {
        val width = abs(p1.x - p2.x)
        val height = abs(p1.y - p2.y)
        val distance = hypot(width, height)
        val relativeDistance = screenhypotenuse / 10.toDouble()
        val index: Int
        index = if (distance > relativeDistance) {
            colorIntensityArray.size - 3
        } else {
            floor(
                distance * (colorIntensityArray.size / 3) / relativeDistance
            ).toInt() * 3
        }
        return ColorV4(colorIntensityArray[index], colorIntensityArray[index + 1], colorIntensityArray[index + 2], 1f)
    }

    /**
     * Tension: 1 is high, 0 normal, -1 is low
     * Bias: 0 is even,
     * positive is towards first segment,
     * negative towards the other
     */
    fun hermiteInterpolate(
        pointA: Float, pointB: Float,
        pointC: Float, pointD: Float,
        mu: Float
    ): Float {
        val mu3: Float
        val a0: Float
        val a1: Float
        val a2: Float
        val a3: Float

        val mu2: Float = mu * mu
        mu3 = mu2 * mu

        var m0: Float = (pointB - pointA) * 1 / 2
        m0 += (pointC - pointB) * 1 / 2

        var m1: Float = (pointC - pointB) * 1 / 2
        m1 += (pointD - pointC) * 1 / 2

        a0 = 2 * mu3 - 3 * mu2 + 1
        a1 = mu3 - 2 * mu2 + mu
        a2 = mu3 - mu2
        a3 = -2 * mu3 + 3 * mu2
        return a0 * pointB + a1 * m0 + a2 * m1 + a3 * pointC
    }

    companion object {
        fun InterpolateHermite4pt3oX(x0: Float, x1: Float, x2: Float, x3: Float, t: Float): Float {
            Log.d("<^>", t.toString())
            val c1 = .5f * (x2 - x0)
            val c2 = x0 - 2.5f * x1 + 2 * x2 - .5f * x3
            val c3 = .5f * (x3 - x0) + 1.5f * (x1 - x2)
            return ((c3 * t + c2) * t + c1) * t + x1
        }
    }

    init {
        lastTime = System.currentTimeMillis() + 100
        this.surface = surface
    }
}