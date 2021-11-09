package com.example.zrenie20.renderable.alpha2

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.content.res.TypedArray
import android.media.MediaDataSource
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaPlayer.*
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.Surface
import android.view.View.MeasureSpec
import java.io.FileDescriptor
import java.io.IOException
import java.util.HashMap

@SuppressLint("ViewConstructor")
class AlphaMovieView(context: Context?, attrs: AttributeSet) :
    GLTextureView(context, attrs) {
    private var videoAspectRatio: Float = AlphaMovieView.Companion.VIEW_ASPECT_RATIO
    var renderer: VideoRenderer? = null
    var mediaPlayer: MediaPlayer? = null
        private set
    private var onVideoStartedListener: AlphaMovieView.OnVideoStartedListener? = null
    private var onVideoEndedListener: AlphaMovieView.OnVideoEndedListener? = null
    private var isSurfaceCreated = false
    private var isDataSourceSet = false
    var state = AlphaMovieView.PlayerState.NOT_PREPARED
        private set

    private fun init(attrs: AttributeSet) {
        setEGLContextClientVersion(AlphaMovieView.Companion.GL_CONTEXT_VERSION)
        setEGLConfigChooser(8, 8, 8, 8, 16, 0)
        initMediaPlayer()
        renderer = VideoRenderer()
        obtainRendererOptions(attrs)
        addOnSurfacePrepareListener()
        setRenderer(renderer)
        bringToFront()
        preserveEGLContextOnPause = true
        isOpaque = false
    }

    private fun initMediaPlayer() {
        mediaPlayer = MediaPlayer()
        setScreenOnWhilePlaying(true)
        setLooping(true)
        mediaPlayer!!.setOnCompletionListener {
            state = AlphaMovieView.PlayerState.PAUSED
            if (onVideoEndedListener != null) {
                onVideoEndedListener!!.onVideoEnded()
            }
        }
    }

    private fun obtainRendererOptions(attrs: AttributeSet?) {
        if (attrs != null) {
            val arr = context.obtainStyledAttributes(attrs, R.styleable.AlphaMovieView)
            val alphaColor = arr.getColor(
                R.styleable.AlphaMovieView_alphaColor,
                AlphaMovieView.Companion.NOT_DEFINED_COLOR
            )
            if (alphaColor != AlphaMovieView.Companion.NOT_DEFINED_COLOR) {
                renderer!!.setAlphaColor(alphaColor)
            }
            val shader = arr.getString(R.styleable.AlphaMovieView_shader)
            if (shader != null) {
                renderer!!.setCustomShader(shader)
            }
            val accuracy = arr.getFloat(
                R.styleable.AlphaMovieView_accuracy,
                AlphaMovieView.Companion.NOT_DEFINED.toFloat()
            )
            if (accuracy != AlphaMovieView.Companion.NOT_DEFINED.toFloat()) {
                renderer!!.setAccuracy(accuracy.toDouble())
            }
            arr.recycle()
        }
    }

    private fun addOnSurfacePrepareListener() {
        if (renderer != null) {
            renderer!!.setOnSurfacePrepareListener(object : VideoRenderer.OnSurfacePrepareListener {
                override fun surfacePrepared(surface: Surface) {
                    isSurfaceCreated = true
                    mediaPlayer!!.setSurface(surface)
                    surface.release()
                    if (isDataSourceSet) {
                        prepareAndStartMediaPlayer()
                    }
                }
            })
        }
    }

    private fun prepareAndStartMediaPlayer() {
        prepareAsync { start() }
    }

    private fun calculateVideoAspectRatio(videoWidth: Int, videoHeight: Int) {
        if (videoWidth > 0 && videoHeight > 0) {
            videoAspectRatio = videoWidth.toFloat() / videoHeight
        }
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var widthSize = MeasureSpec.getSize(widthMeasureSpec)
        var heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val currentAspectRatio = widthSize.toDouble() / heightSize
        if (currentAspectRatio > videoAspectRatio) {
            widthSize = (heightSize * videoAspectRatio).toInt()
        } else {
            heightSize = (widthSize / videoAspectRatio) as Int
        }
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(widthSize, widthMode),
            MeasureSpec.makeMeasureSpec(heightSize, heightMode)
        )
    }

    private fun onDataSourceSet(retriever: MediaMetadataRetriever) {
        val videoWidth =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!
                .toInt()
        val videoHeight =
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!
                .toInt()
        calculateVideoAspectRatio(videoWidth, videoHeight)
        isDataSourceSet = true
        if (isSurfaceCreated) {
            prepareAndStartMediaPlayer()
        }
    }

    fun setVideoFromAssets(assetsFileName: String?) {
        reset()
        try {
            val assetFileDescriptor = context.assets.openFd(assetsFileName!!)
            mediaPlayer!!.setDataSource(
                assetFileDescriptor.fileDescriptor,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.length
            )
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(
                assetFileDescriptor.fileDescriptor,
                assetFileDescriptor.startOffset,
                assetFileDescriptor.length
            )
            onDataSourceSet(retriever)
        } catch (e: IOException) {
            Log.e(AlphaMovieView.Companion.TAG, e.message, e)
        }
    }

    fun setVideoByUrl(url: String?) {
        reset()
        try {
            mediaPlayer!!.setDataSource(url)
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(url, HashMap())
            onDataSourceSet(retriever)
        } catch (e: IOException) {
            Log.e(AlphaMovieView.Companion.TAG, e.message, e)
        }
    }

    fun setVideoFromFile(fileDescriptor: FileDescriptor?) {
        reset()
        try {
            mediaPlayer!!.setDataSource(fileDescriptor)
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(fileDescriptor)
            onDataSourceSet(retriever)
        } catch (e: IOException) {
            Log.e(AlphaMovieView.Companion.TAG, e.message, e)
        }
    }

    fun setVideoFromFile(fileDescriptor: FileDescriptor?, startOffset: Int, endOffset: Int) {
        reset()
        try {
            mediaPlayer!!.setDataSource(fileDescriptor, startOffset.toLong(), endOffset.toLong())
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(fileDescriptor, startOffset.toLong(), endOffset.toLong())
            onDataSourceSet(retriever)
        } catch (e: IOException) {
            Log.e(AlphaMovieView.Companion.TAG, e.message, e)
        }
    }

    @TargetApi(23)
    fun setVideoFromMediaDataSource(mediaDataSource: MediaDataSource?) {
        reset()
        mediaPlayer!!.setDataSource(mediaDataSource)
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(mediaDataSource)
        onDataSourceSet(retriever)
    }

    fun setVideoFromUri(context: Context?, uri: Uri?) {
        reset()
        try {
            mediaPlayer!!.setDataSource(context!!, uri!!)
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            onDataSourceSet(retriever)
        } catch (e: IOException) {
            Log.e(AlphaMovieView.Companion.TAG, e.message, e)
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
        pause()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        release()
    }

    private fun prepareAsync(onPreparedListener: OnPreparedListener) {
        if (mediaPlayer != null && state == AlphaMovieView.PlayerState.NOT_PREPARED
            || state == AlphaMovieView.PlayerState.STOPPED
        ) {
            mediaPlayer!!.setOnPreparedListener { mp ->
                state = AlphaMovieView.PlayerState.PREPARED
                onPreparedListener.onPrepared(mp)
            }
            mediaPlayer!!.prepareAsync()
        }
    }

    fun start() {
        if (mediaPlayer != null) {
            when (state) {
                AlphaMovieView.PlayerState.PREPARED -> {
                    mediaPlayer!!.start()
                    state = AlphaMovieView.PlayerState.STARTED
                    if (onVideoStartedListener != null) {
                        onVideoStartedListener!!.onVideoStarted()
                    }
                }
                AlphaMovieView.PlayerState.PAUSED -> {
                    mediaPlayer!!.start()
                    state = AlphaMovieView.PlayerState.STARTED
                }
                AlphaMovieView.PlayerState.STOPPED -> prepareAsync {
                    mediaPlayer!!.start()
                    state = AlphaMovieView.PlayerState.STARTED
                    if (onVideoStartedListener != null) {
                        onVideoStartedListener!!.onVideoStarted()
                    }
                }
            }
        }
    }

    fun pause() {
        if (mediaPlayer != null && state == AlphaMovieView.PlayerState.STARTED) {
            mediaPlayer!!.pause()
            state = AlphaMovieView.PlayerState.PAUSED
        }
    }

    fun stop() {
        if (mediaPlayer != null && (state == AlphaMovieView.PlayerState.STARTED || state == AlphaMovieView.PlayerState.PAUSED)) {
            mediaPlayer!!.stop()
            state = AlphaMovieView.PlayerState.STOPPED
        }
    }

    fun reset() {
        if (mediaPlayer != null && (state == AlphaMovieView.PlayerState.STARTED || state == AlphaMovieView.PlayerState.PAUSED || state == AlphaMovieView.PlayerState.STOPPED)) {
            mediaPlayer!!.reset()
            state = AlphaMovieView.PlayerState.NOT_PREPARED
        }
    }

    fun release() {
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            state = AlphaMovieView.PlayerState.RELEASE
        }
    }

    val isPlaying: Boolean
        get() = state == AlphaMovieView.PlayerState.STARTED
    val isPaused: Boolean
        get() = state == AlphaMovieView.PlayerState.PAUSED
    val isStopped: Boolean
        get() = state == AlphaMovieView.PlayerState.STOPPED
    val isReleased: Boolean
        get() = state == AlphaMovieView.PlayerState.RELEASE

    fun seekTo(msec: Int) {
        mediaPlayer!!.seekTo(msec)
    }

    fun setLooping(looping: Boolean) {
        mediaPlayer!!.isLooping = looping
    }

    val currentPosition: Int
        get() = mediaPlayer!!.currentPosition

    fun setScreenOnWhilePlaying(screenOn: Boolean) {
        mediaPlayer!!.setScreenOnWhilePlaying(screenOn)
    }

    fun setOnErrorListener(onErrorListener: OnErrorListener?) {
        mediaPlayer!!.setOnErrorListener(onErrorListener)
    }

    fun setOnVideoStartedListener(onVideoStartedListener: AlphaMovieView.OnVideoStartedListener?) {
        this.onVideoStartedListener = onVideoStartedListener
    }

    fun setOnVideoEndedListener(onVideoEndedListener: AlphaMovieView.OnVideoEndedListener?) {
        this.onVideoEndedListener = onVideoEndedListener
    }

    fun setOnSeekCompleteListener(onSeekCompleteListener: OnSeekCompleteListener?) {
        mediaPlayer!!.setOnSeekCompleteListener(onSeekCompleteListener)
    }

    interface OnVideoStartedListener {
        fun onVideoStarted()
    }

    interface OnVideoEndedListener {
        fun onVideoEnded()
    }

    private enum class PlayerState {
        NOT_PREPARED, PREPARED, STARTED, PAUSED, STOPPED, RELEASE
    }

    companion object {
        private const val GL_CONTEXT_VERSION = 2
        private const val NOT_DEFINED = -1
        private const val NOT_DEFINED_COLOR = 0
        private const val TAG = "VideoSurfaceView"
        private const val VIEW_ASPECT_RATIO = 4f / 3f
    }

    init {
        if (!isInEditMode) {
            init(attrs)
        }
    }
}