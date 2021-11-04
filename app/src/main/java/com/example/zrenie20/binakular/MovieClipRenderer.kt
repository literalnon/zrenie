package com.example.zrenie20.binakular

import android.content.Context
import android.opengl.GLES20

import android.content.res.AssetFileDescriptor

import android.content.res.AssetManager

import android.media.MediaPlayer

import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener

import android.os.Looper

import com.example.zrenie20.common.rendering.ShaderUtil

import android.opengl.GLES11Ext
import android.opengl.Matrix
import android.os.Handler
import android.util.Log
import android.view.Surface

import javax.microedition.khronos.opengles.GL10

import com.google.ar.core.Pose
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.RuntimeException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer


class MovieClipRenderer : OnFrameAvailableListener {
    // Geometry data in GLES friendly data structure.
    private var mQuadVertices: FloatBuffer? = null
    private var mQuadTexCoord: FloatBuffer? = null

    // Shader program id and parameters.
    private var mQuadProgram = 0
    private var mQuadPositionParam = 0
    private var mQuadTexCoordParam = 0
    private var mModelViewProjectionUniform = 0
    private var mTextureId = -1

    // Matrix for the location and perspective of the quad.
    private val mModelMatrix = FloatArray(16)

    // Media player,  texture and other bookkeeping.
    private var player: MediaPlayer? = null
    private var videoTexture: SurfaceTexture? = null
    private var frameAvailable = false

    @get:Synchronized
    var isStarted = false
        private set
    private var done = false
    private var prepared = false

    // Lock used for waiting if the player was not yet created.
    private val lock = Object()

    /**
     * Update the model matrix based on the location and scale to draw the quad.
     */
    fun update(modelMatrix: FloatArray?, scaleFactor: Float) {
        val scaleMatrix = FloatArray(16)
        Matrix.setIdentityM(scaleMatrix, 0)
        scaleMatrix[0] = scaleFactor
        scaleMatrix[5] = scaleFactor
        scaleMatrix[10] = scaleFactor
        Matrix.multiplyMM(mModelMatrix, 0, modelMatrix, 0, scaleMatrix, 0)
    }

    /**
     * Initialize the GLES objects.
     * This is called from the GL render thread to make sure
     * it has access to the EGLContext.
     */
    fun createOnGlThread() {

        // 1 texture to hold the video frame.
        val textures = IntArray(1)
        GLES20.glGenTextures(1, textures, 0)
        mTextureId = textures[0]
        val mTextureTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
        GLES20.glBindTexture(mTextureTarget, mTextureId)
        GLES20.glTexParameteri(
            mTextureTarget, GLES20.GL_TEXTURE_WRAP_S,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            mTextureTarget, GLES20.GL_TEXTURE_WRAP_T,
            GLES20.GL_CLAMP_TO_EDGE
        )
        GLES20.glTexParameteri(
            mTextureTarget, GLES20.GL_TEXTURE_MIN_FILTER,
            GLES20.GL_NEAREST
        )
        GLES20.glTexParameteri(
            mTextureTarget, GLES20.GL_TEXTURE_MAG_FILTER,
            GLES20.GL_NEAREST
        )
        videoTexture = SurfaceTexture(mTextureId)
        videoTexture!!.setOnFrameAvailableListener(this)

        // Make a quad to hold the movie
        val bbVertices: ByteBuffer = ByteBuffer.allocateDirect(
            QUAD_COORDS.size * FLOAT_SIZE
        )
        bbVertices.order(ByteOrder.nativeOrder())
        mQuadVertices = bbVertices.asFloatBuffer()
        mQuadVertices?.put(QUAD_COORDS)
        mQuadVertices?.position(0)
        val numVertices = 4
        val bbTexCoords: ByteBuffer = ByteBuffer.allocateDirect(
            numVertices * TEXCOORDS_PER_VERTEX * FLOAT_SIZE
        )
        bbTexCoords.order(ByteOrder.nativeOrder())
        mQuadTexCoord = bbTexCoords.asFloatBuffer()
        mQuadTexCoord?.put(QUAD_TEXCOORDS)
        mQuadTexCoord?.position(0)
        val vertexShader = loadGLShader(TAG, GLES20.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fragmentShader = loadGLShader(
            TAG,
            GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER
        )
        mQuadProgram = GLES20.glCreateProgram()
        GLES20.glAttachShader(mQuadProgram, vertexShader)
        GLES20.glAttachShader(mQuadProgram, fragmentShader)
        GLES20.glLinkProgram(mQuadProgram)
        GLES20.glUseProgram(mQuadProgram)
        ShaderUtil.checkGLError(TAG, "Program creation")
        mQuadPositionParam = GLES20.glGetAttribLocation(mQuadProgram, "a_Position")
        mQuadTexCoordParam = GLES20.glGetAttribLocation(mQuadProgram, "a_TexCoord")
        mModelViewProjectionUniform = GLES20.glGetUniformLocation(
            mQuadProgram, "u_ModelViewProjection"
        )
        ShaderUtil.checkGLError(TAG, "Program parameters")
        Matrix.setIdentityM(mModelMatrix, 0)
        initializeMediaPlayer()
    }

    fun draw(pose: Pose, cameraView: FloatArray?, cameraPerspective: FloatArray?) {
        if (done || !prepared) {
            return
        }
        synchronized(this) {
            if (frameAvailable) {
                videoTexture!!.updateTexImage()
                frameAvailable = false
            }
        }
        val modelMatrix = FloatArray(16)
        pose.toMatrix(modelMatrix, 0)
        val modelView = FloatArray(16)
        val modelViewProjection = FloatArray(16)
        Matrix.multiplyMM(modelView, 0, cameraView, 0, mModelMatrix, 0)
        Matrix.multiplyMM(modelViewProjection, 0, cameraPerspective, 0, modelView, 0)
        ShaderUtil.checkGLError(TAG, "Before draw")
        GLES20.glEnable(GL10.GL_BLEND)
        GLES20.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextureId)
        GLES20.glUseProgram(mQuadProgram)

        // Set the vertex positions.
        GLES20.glVertexAttribPointer(
            mQuadPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
            false, 0, mQuadVertices
        )
        // Set the texture coordinates.
        GLES20.glVertexAttribPointer(
            mQuadTexCoordParam, TEXCOORDS_PER_VERTEX,
            GLES20.GL_FLOAT, false, 0, mQuadTexCoord
        )

        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(mQuadPositionParam)
        GLES20.glEnableVertexAttribArray(mQuadTexCoordParam)
        GLES20.glUniformMatrix4fv(
            mModelViewProjectionUniform, 1, false,
            modelViewProjection, 0
        )
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(mQuadPositionParam)
        GLES20.glDisableVertexAttribArray(mQuadTexCoordParam)
        ShaderUtil.checkGLError(TAG, "Draw")
    }

    private fun initializeMediaPlayer() {
        if (handler == null) handler = Handler(Looper.getMainLooper())
        handler?.post(Runnable {
            synchronized(lock) {
                player = MediaPlayer()
                lock.notify()
            }
        })
    }

    override fun onFrameAvailable(surfaceTexture: SurfaceTexture) {
        synchronized(this) { frameAvailable = true }
    }

    @Throws(FileNotFoundException::class)
    fun play(filename: String?, context: Context): Boolean {
        // Wait for the player to be created.
        if (player == null) {
            synchronized(lock) {
                while (player == null) {
                    try {
                        lock.wait()
                    } catch (e: InterruptedException) {
                        return false
                    }
                }
            }
        }
        player!!.reset()
        done = false
        player!!.setOnPreparedListener { mp ->
            prepared = true
            mp.start()
        }
        player!!.setOnErrorListener { mp, what, extra ->
            done = true
            Log.e("VideoPlayer", String.format("Error occured: %d, %d\n", what, extra))
            false
        }
        player!!.setOnCompletionListener { done = true }
        player!!.setOnInfoListener { mediaPlayer, i, i1 -> false }
        try {
            val assets: AssetManager = context.getAssets()
            val descriptor = assets.openFd(filename!!)
            player!!.setDataSource(
                descriptor.fileDescriptor,
                descriptor.startOffset,
                descriptor.length
            )
            player!!.setSurface(Surface(videoTexture))
            player!!.prepareAsync()
            synchronized(this) { isStarted = true }
        } catch (e: IOException) {
            Log.e(TAG, "Exception preparing movie", e)
            return false
        }
        return true
    }

    companion object {
        private val TAG = MovieClipRenderer::class.java.simpleName

        // Quad geometry
        private const val COORDS_PER_VERTEX = 3
        private const val TEXCOORDS_PER_VERTEX = 2
        private const val FLOAT_SIZE = 4
        private val QUAD_COORDS = floatArrayOf(
            -1.0f, -1.0f, 0.0f,
            -1.0f, +1.0f, 0.0f,
            +1.0f, -1.0f, 0.0f,
            +1.0f, +1.0f, 0.0f
        )
        private val QUAD_TEXCOORDS = floatArrayOf(
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
        )

        // Shader for a flat quad.
        private const val VERTEX_SHADER = "uniform mat4 u_ModelViewProjection;\n\n" +
                "attribute vec4 a_Position;\n" +
                "attribute vec2 a_TexCoord;\n\n" +
                "varying vec2 v_TexCoord;\n\n" +
                "void main() {\n" +
                "   gl_Position = u_ModelViewProjection * vec4(a_Position.xyz, 1.0);\n" +
                "   v_TexCoord = a_TexCoord;\n" +
                "}"

        // The fragment shader samples the video texture, blending to
        //  transparent for the green screen
        //  color.  The color was determined by sampling a screenshot
        //  of the video in an image editor.
        private const val FRAGMENT_SHADER = "#extension GL_OES_EGL_image_external : require\n" +
                "\n" +
                "precision mediump float;\n" +
                "varying vec2 v_TexCoord;\n" +
                "uniform samplerExternalOES sTexture;\n" +
                "\n" +
                "void main() {\n" +
                "    //TODO make this a uniform variable - " +
                " but this is the color of the background. 17ad2b\n" +
                "  vec3 keying_color = vec3(23.0f/255.0f, 173.0f/255.0f, 43.0f/255.0f);\n" +
                "  float thresh = 0.4f; // 0 - 1.732\n" +
                "  float slope = 0.2;\n" +
                "  vec3 input_color = texture2D(sTexture, v_TexCoord).rgb;\n" +
                "  float d = abs(length(abs(keying_color.rgb - input_color.rgb)));\n" +
                "  float edge0 = thresh * (1.0f - slope);\n" +
                "  float alpha = smoothstep(edge0,thresh,d);\n" +
                "  gl_FragColor = vec4(input_color, alpha);\n" +
                "}"
        private var handler: Handler? = null
        fun loadGLShader(tag: String?, type: Int, code: String?): Int {
            var shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, code)
            GLES20.glCompileShader(shader)

            // Get the compilation status.
            val compileStatus = IntArray(1)
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                Log.e(tag, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader))
                GLES20.glDeleteShader(shader)
                shader = 0
            }
            if (shader == 0) {
                throw RuntimeException("Error creating shader.")
            }
            return shader
        }
    }
}