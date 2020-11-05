package com.example.kris.handwriting

import android.opengl.GLES20

object CustomShader {
    const val vs_mouseSwipe = "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "uniform float pointSize;" +
            "attribute vec4 a_color;" +
            "varying vec4 v_color;" +
            "void main() {" +
            "  gl_Position = uMVPMatrix * vPosition;" +
            "  gl_PointSize = pointSize;" +
            "  v_color = a_color;" +
            "}"
    const val fs_mouseSwipe = "precision mediump float;" +
            "varying vec4 v_color;" +
            "void main() {" +
            "  gl_FragColor = v_color;" +
            "}"

    const val vs_Texture = "attribute vec4 inVertex;" +
            "attribute vec2 inTextureCoordinate;" +
            "uniform mat4 MVP;" +
            "varying vec2 textureCoordinate;" +
            "void main() {" +
            "gl_Position = MVP * inVertex;" +
            "textureCoordinate = inTextureCoordinate;" +
            "}"
    const val fs_Texture = "precision mediump float;" +
            "varying vec2 textureCoordinate;" +
            "uniform sampler2D texture;" +
            "void main() {" +
            "gl_FragColor = texture2D(texture, textureCoordinate);" +
            "}"

    var sp_mouse_swipe = 0
    var sp_background = 0

    fun loadShader(type: Int, shaderCode: String?): Int {

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        val shader = GLES20.glCreateShader(type)

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        // return the shader
        return shader
    }
}