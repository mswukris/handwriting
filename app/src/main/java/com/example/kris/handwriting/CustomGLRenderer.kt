package com.example.kris.handwriting

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.Log
import android.view.MotionEvent
import com.example.kris.handwriting.mesh.MeshBase
import com.example.kris.handwriting.mesh.MeshPoint
import com.example.kris.handwriting.mesh.MousePoint
import com.example.kris.handwriting.util.ColorV4
import java.util.concurrent.ConcurrentLinkedQueue
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.collections.ArrayList
import kotlin.math.hypot


class CustomGLRenderer(private var surface: CustomGLSurface) : GLSurfaceView.Renderer {
    
    private val tag = CustomGLRenderer::class.java.simpleName
    
    // Our matrices
    private val mtrxProjection = FloatArray(16)
    private val mtrxView = FloatArray(16)
    private val mtrxProjectionAndView = FloatArray(16)

    var meshes: ArrayList<MeshBase>? = null

    var mMousePoints: ConcurrentLinkedQueue<MousePoint>? = null
    private lateinit var mousePoints: FloatArray

    var screenWidth = 1280f
    var screenHeight = 768f
    private var lastTime: Long

    private var prevPointerPoint: MeshPoint? = null
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

    private fun render(mtrxProjectionAndView: FloatArray) {
        // clear Screen and Depth Buffer, we have set the clear color as black.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        for (aMesh in meshes!!) {
            aMesh.draw(mtrxProjectionAndView)
        }
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        // We need to know the current width and height.
        Log.d(tag, "Surface Changed start")
        surfaceLoaded = false
        screenWidth = width.toFloat()
        screenHeight = height.toFloat()

        for (aMesh in meshes!!) {
            aMesh.screenHeight = height.toFloat()
        }

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
        Log.d(tag, "Surface Changed end")
    }

    override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {
        Log.d(tag, "Surface Create started")
        surfaceLoaded = false
        
        meshes = ArrayList()
        mMousePoints = ConcurrentLinkedQueue()

        // Set our shader program
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

        surfaceLoaded = true
        Log.d(tag, "Surface Created ended")
    }

    fun processEventDown(paintType: PaintType) {
        when(paintType) {
            PaintType.PEN -> meshes!!.add(Pen(screenHeight, surface))
            PaintType.BRUSH -> meshes!!.add(SwipeMesh(screenHeight, surface))
        }
    }

    fun processEventMove(event: MotionEvent) {
        if (!surfaceLoaded) return
        if (event.pointerCount <= 2) {
            if (event.findPointerIndex(0) != -1) {
                val pointerIndex = event.findPointerIndex(0)
                val meshPoint = MeshPoint(
                    event.getX(pointerIndex).toDouble(),
                    event.getY(pointerIndex).toDouble()
                )

                val lastMesh = meshes!!.last()
                if (lastMesh is Pen) {
                    lastMesh.addPoint(meshPoint.point, ColorV4(0f, 0f, 1f, 1f))
                } else if (lastMesh is SwipeMesh) {
                    if (prevPointerPoint == null) {
                        meshPoint.color = ColorV4(1f, 0f, 0f, 1f)
                    } else {
                        val screenHypotenuse = hypot(screenWidth.toDouble(), screenHeight.toDouble()).toFloat()
                        meshPoint.color = MeshPoint.getDistanceColor(prevPointerPoint!!, meshPoint, screenHypotenuse)
                    }
                    lastMesh.addPoint(meshPoint)
                    prevPointerPoint = meshPoint
                }
            }
        }
    }

    fun processEventUp() {}

    fun clear() {
        for (mesh in meshes!!) {
            mesh.clearAllPoint()
        }
        meshes!!.clear()
    }

    init {
        lastTime = System.currentTimeMillis() + 100
    }
}