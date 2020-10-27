package com.example.kris.handwriting

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var glSurfaceView: GLSurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        glSurfaceView = CustomGLSurface(this)
        setContentView(R.layout.activity_main)

        val layout = findViewById<View>(R.id.surfaceContainer) as RelativeLayout
        val glParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT)
        layout.addView(glSurfaceView, glParams)

        supportFragmentManager.beginTransaction().replace(R.id.paintToolsContainer, PaintToolFragment()).commit()

//        findViewById<View>(R.id.eraser).setOnClickListener {
//            (glSurfaceView as CustomGLSurface).paintType = PaintType.ERASER
//        }
//        findViewById<View>(R.id.pencil).setOnClickListener {
//            (glSurfaceView as CustomGLSurface).paintType = PaintType.PEN
//        }
//        findViewById<View>(R.id.brush).setOnClickListener {
//            (glSurfaceView as CustomGLSurface).paintType = PaintType.BRUSH
//        }
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView!!.onPause()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView!!.onResume()
    }
}

