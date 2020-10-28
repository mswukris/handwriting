package com.example.kris.handwriting

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import com.example.kris.handwriting.paint.PaintType
import com.example.kris.handwriting.paint.PaintTypeChangedListener

class CustomGLSurface(context: Context): GLSurfaceView(context), PaintTypeChangedListener {

    private val renderer: CustomGLRenderer

    init {
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        renderer = CustomGLRenderer(this)
        setRenderer(renderer)

        // Render the view only when there is a change in the drawing data
        // Might need to change for the painting app
        renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
    }

    override fun onPause() {
        super.onPause()
        renderer.onPause()
    }

    override fun onResume() {
        super.onResume()
        renderer.onResume()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> renderer.processEventMove(event)
            MotionEvent.ACTION_DOWN -> renderer.processEventDown(event)
            MotionEvent.ACTION_UP -> renderer.processEventUp()
        }
        return true
    }

    override fun onTypeChanged(type: PaintType) {
        renderer.paintType = type
    }
}
