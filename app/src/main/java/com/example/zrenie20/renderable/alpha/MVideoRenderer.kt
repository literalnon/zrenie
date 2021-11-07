package com.example.zrenie20.renderable.alpha

import android.graphics.Color
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.Handler
import android.util.Log
import android.view.Surface
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.util.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MVideoRenderer(
    val mSurfaceTexture: SurfaceTexture,
    val mSurface: Surface,
    val onSurfacePrepareListener: OnSurfacePrepareListener
) : MGLTextureView.Renderer,
    OnFrameAvailableListener {
    private val triangleVerticesData = floatArrayOf( // X, Y, Z, U, V
        -1.0f, -1.0f, 0f, 0f, 0f,
        1.0f, -1.0f, 0f, 1f, 0f,
        -1.0f, 1.0f, 0f, 0f, 1f,
        1.0f, 1.0f, 0f, 1f, 1f
    )
    private val triangleVertices: FloatBuffer
    private val vertexShader = """uniform mat4 uMVPMatrix;
uniform mat4 uSTMatrix;
attribute vec4 aPosition;
attribute vec4 aTextureCoord;
varying vec2 vTextureCoord;
void main() {
  gl_Position = uMVPMatrix * aPosition;
  vTextureCoord = (uSTMatrix * aTextureCoord).xy;
}
"""
    private val alphaShader = """#extension GL_OES_EGL_image_external : require
precision mediump float;
varying vec2 vTextureCoord;
uniform samplerExternalOES sTexture;
varying mediump float text_alpha_out;
void main() {
  vec4 color = texture2D(sTexture, vTextureCoord);
  float red = %f;
  float green = %f;
  float blue = %f;
  float accuracy = %f;
  if (abs(color.r - red) <= accuracy && abs(color.g - green) <= accuracy && abs(color.b - blue) <= accuracy) {
      gl_FragColor = vec4(color.r, color.g, color.b, 0.0);
  } else {
      gl_FragColor = vec4(color.r, color.g, color.b, 1.0);
  }
}
"""
    private var accuracy = 0.0
    private var shader = alphaShader
    private val mVPMatrix = FloatArray(16)
    private val sTMatrix = FloatArray(16)
    private var program = 0
    private var textureID = 0
    private var uMVPMatrixHandle = 0
    private var uSTMatrixHandle = 0
    private var aPositionHandle = 0
    private var aTextureHandle = 0
    //private var mSurfaceTexture: SurfaceTexture? = null
    private var updateSurface = false
    //private var onSurfacePrepareListener: OnSurfacePrepareListener? = null
    private var isCustom = false
    private var redParam = 0.0f
    private var greenParam = 1.0f
    private var blueParam = 0.0f

    override fun onDrawFrame(gl: GL10?) {
        Log.e("AlphaVideo", "MVideoRenderer onDrawFrame")
        synchronized(this) {
            if (updateSurface) {
                mSurfaceTexture!!.updateTexImage()
                mSurfaceTexture!!.getTransformMatrix(sTMatrix)
                updateSurface = false
            }
        }
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT or GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glUseProgram(program)
        checkGlError("glUseProgram")
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(MVideoRenderer.Companion.GL_TEXTURE_EXTERNAL_OES, textureID)
        triangleVertices.position(MVideoRenderer.Companion.TRIANGLE_VERTICES_DATA_POS_OFFSET)
        GLES20.glVertexAttribPointer(
            aPositionHandle, 3, GLES20.GL_FLOAT, false,
            MVideoRenderer.Companion.TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices
        )
        checkGlError("glVertexAttribPointer maPosition")
        GLES20.glEnableVertexAttribArray(aPositionHandle)
        checkGlError("glEnableVertexAttribArray aPositionHandle")
        triangleVertices.position(MVideoRenderer.Companion.TRIANGLE_VERTICES_DATA_UV_OFFSET)
        GLES20.glVertexAttribPointer(
            aTextureHandle, 3, GLES20.GL_FLOAT, false,
            MVideoRenderer.Companion.TRIANGLE_VERTICES_DATA_STRIDE_BYTES, triangleVertices
        )
        checkGlError("glVertexAttribPointer aTextureHandle")
        GLES20.glEnableVertexAttribArray(aTextureHandle)
        checkGlError("glEnableVertexAttribArray aTextureHandle")
        Matrix.setIdentityM(mVPMatrix, 0)
        GLES20.glUniformMatrix4fv(uMVPMatrixHandle, 1, false, mVPMatrix, 0)
        GLES20.glUniformMatrix4fv(uSTMatrixHandle, 1, false, sTMatrix, 0)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        checkGlError("glDrawArrays")
        /*GLES20.glFinish()*/
    }

    override fun onSurfaceDestroyed(gl: GL10?) {}

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.e("AlphaVideo", "MVideoRenderer onSurfaceCreated")

        program = createProgram(vertexShader, resolveShader())
        if (program == 0) {
            return
        }
        aPositionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        checkGlError("glGetAttribLocation aPosition")
        if (aPositionHandle == -1) {
            throw RuntimeException("Could not get attrib location for aPosition")
        }
        aTextureHandle = GLES20.glGetAttribLocation(program, "aTextureCoord")
        checkGlError("glGetAttribLocation aTextureCoord")
        if (aTextureHandle == -1) {
            throw RuntimeException("Could not get attrib location for aTextureCoord")
        }
        uMVPMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        checkGlError("glGetUniformLocation uMVPMatrix")
        if (uMVPMatrixHandle == -1) {
            throw RuntimeException("Could not get attrib location for uMVPMatrix")
        }
        uSTMatrixHandle = GLES20.glGetUniformLocation(program, "uSTMatrix")
        checkGlError("glGetUniformLocation uSTMatrix")
        if (uSTMatrixHandle == -1) {
            throw RuntimeException("Could not get attrib location for uSTMatrix")
        }
        prepareSurface()
    }

    private fun prepareSurface() {
        Log.e("AlphaVideo", "MVideoRenderer prepareSurface")
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        textureID = textures[0]
        GLES20.glBindTexture(MVideoRenderer.Companion.GL_TEXTURE_EXTERNAL_OES, textureID)
        checkGlError("glBindTexture textureID")
        GLES20.glTexParameterf(
            MVideoRenderer.Companion.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST.toFloat()
        )
        GLES20.glTexParameterf(
            MVideoRenderer.Companion.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_LINEAR.toFloat()
        )
        //mSurfaceTexture = SurfaceTexture(textureID)
        mSurfaceTexture!!.setOnFrameAvailableListener(this)
        //val surface = Surface(mSurfaceTexture)
        Log.e("AlphaVideo", "MVideoRenderer surfacePrepared : ${mSurface != null}, ${mSurfaceTexture != null}")
        onSurfacePrepareListener!!.surfacePrepared(mSurface)
        synchronized(this) { updateSurface = false }
    }

    @Synchronized
    override fun onFrameAvailable(surface: SurfaceTexture) {
        updateSurface = true
    }

    private fun loadShader(shaderType: Int, source: String): Int {
        var shader = GLES20.glCreateShader(shaderType)
        if (shader != 0) {
            GLES20.glShaderSource(shader, source)
            GLES20.glCompileShader(shader)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0)
            if (compiled[0] == 0) {
                Log.e(
                    MVideoRenderer.Companion.TAG,
                    "Could not compile shader $shaderType:"
                )
                Log.e(MVideoRenderer.Companion.TAG, GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                shader = 0
            }
        }
        return shader
    }

    private fun createProgram(vertexSource: String, fragmentSource: String): Int {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource)
        if (vertexShader == 0) {
            return 0
        }
        val pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource)
        if (pixelShader == 0) {
            return 0
        }
        var program = GLES20.glCreateProgram()
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader)
            checkGlError("glAttachShader")
            GLES20.glAttachShader(program, pixelShader)
            checkGlError("glAttachShader")
            GLES20.glLinkProgram(program)
            val linkStatus = IntArray(1)
            GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] != GLES20.GL_TRUE) {
                Log.e(MVideoRenderer.Companion.TAG, "Could not link program: ")
                Log.e(MVideoRenderer.Companion.TAG, GLES20.glGetProgramInfoLog(program))
                GLES20.glDeleteProgram(program)
                program = 0
            }
        }
        return program
    }

    fun setAlphaColor(color: Int) {
        redParam = Color.red(color).toFloat() / MVideoRenderer.Companion.COLOR_MAX_VALUE
        //greenParam = Color.green(color).toFloat() / MVideoRenderer.Companion.COLOR_MAX_VALUE
        blueParam = Color.blue(color).toFloat() / MVideoRenderer.Companion.COLOR_MAX_VALUE
    }

    fun setCustomShader(customShader: String) {
        isCustom = true
        shader = customShader
    }

    fun setAccuracy(accuracy: Double) {
        var accuracy = accuracy
        if (accuracy > 1.0) {
            accuracy = 1.0
        } else if (accuracy < 0.0) {
            accuracy = 0.0
        }
        this.accuracy = accuracy
    }

    fun getAccuracy(): Double {
        return accuracy
    }

    private fun resolveShader(): String {
        return if (isCustom) shader else String.format(
            Locale.ENGLISH, alphaShader,
            redParam, greenParam, blueParam, 1 - accuracy
        )
    }

    private fun checkGlError(op: String) {
        var error: Int
        if (GLES20.glGetError().also { error = it } != GLES20.GL_NO_ERROR) {
            Log.e(
                MVideoRenderer.Companion.TAG,
                "$op: glError $error"
            )
            throw RuntimeException("$op: glError $error")
        }
    }

    interface OnSurfacePrepareListener {
        fun surfacePrepared(surface: Surface?)
    }

    companion object {
        private const val TAG = "VideoRender"
        private const val COLOR_MAX_VALUE = 255
        private const val FLOAT_SIZE_BYTES = 4
        private val TRIANGLE_VERTICES_DATA_STRIDE_BYTES: Int =
            5 * MVideoRenderer.Companion.FLOAT_SIZE_BYTES
        private const val TRIANGLE_VERTICES_DATA_POS_OFFSET = 0
        private const val TRIANGLE_VERTICES_DATA_UV_OFFSET = 3
        private const val GL_TEXTURE_EXTERNAL_OES = 0x8D65
    }

    init {
        triangleVertices = ByteBuffer.allocateDirect(
            triangleVerticesData.size * MVideoRenderer.Companion.FLOAT_SIZE_BYTES
        )
            .order(ByteOrder.nativeOrder()).asFloatBuffer()
        triangleVertices.put(triangleVerticesData).position(0)
        Matrix.setIdentityM(sTMatrix, 0)

        onSurfaceCreated(null, null)
    }
}