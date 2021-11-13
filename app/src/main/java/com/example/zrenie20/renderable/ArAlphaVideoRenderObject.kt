package com.example.zrenie20.renderable

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.zrenie20.R
import com.example.zrenie20.data.DataItemObject
import com.example.zrenie20.renderable.alpha.MAlphaMovieView
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.NodeParent
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import java.io.File

class ArAlphaVideoRenderObject(
    override val context: Context,
    override val dataItemObject: DataItemObject,
    override val renderableFile: File?
) : IArRenderObject {
    companion object {
        val TAG = "DOWNLOAD_VIDEO_FILE"

        private val CHROMA_KEY_COLOR = Color(0.1843f, 1.0f, 0.098f)
    }

    override var onTouchListener: Node.OnTouchListener? = null
        set(value) {
            field = value

            videoAnchorNode.setOnTouchListener { hitTestResult, motionEvent ->
                return@setOnTouchListener value?.onTouch(hitTestResult, motionEvent) ?: false
            }
        }

    //private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var player: MAlphaMovieView? = null

    private var externalTexture: ExternalTexture = ExternalTexture().also { nnExternalTexture ->
        //mediaPlayer.setSurface(it.surface)
        //mediaPlayer.isLooping = true
        //mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
    }

    var videoRenderable: ModelRenderable? = null
    private val anchorNode = AnchorNode()
    var videoAnchorNode: AnchorNode = anchorNode
    /*.apply {
    setParent(mScene)
}*/

    override fun setParent(parent: NodeParent) {
        videoAnchorNode.setParent(parent)
    }

    override fun pause() {
        if (player?.mediaPlayer?.isPlaying == true) {
            videoAnchorNode.renderable = null
            player?.mediaPlayer?.pause()
        }
    }

    override fun resume() {
        if (!(player?.mediaPlayer?.isPlaying == true)) {
            videoAnchorNode = anchorNode
            player?.mediaPlayer?.start()
            videoAnchorNode.renderable = videoRenderable
        }
    }

    override fun stop() {
        if ((player?.mediaPlayer?.isPlaying == true)) {
            videoAnchorNode.anchor?.detach()
            videoAnchorNode.renderable = null
            player?.mediaPlayer?.reset()
        }
    }

    override fun start(
        anchor: Anchor?,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        augmentedImage: AugmentedImage?
    ) {

        ModelRenderable.builder()
            .setSource(context, R.raw.chroma_key_video)
            .build()
            .thenAccept { renderable ->

                videoRenderable = renderable
                videoRenderable?.isShadowCaster = false
                videoRenderable?.isShadowReceiver = false
                videoRenderable?.material?.setExternalTexture("videoTexture", externalTexture)

                renderable.material.setFloat4(
                    "keyColor",
                    CHROMA_KEY_COLOR
                )

                videoAnchorNode = anchorNode
                videoAnchorNode.anchor?.detach()
                videoAnchorNode.anchor =
                    anchor//augmentedImage.createAnchor(augmentedImage.centerPose)

                val scale = dataItemObject.scale?.toFloatOrNull() ?: 4f

                videoAnchorNode.localScale = if (augmentedImage != null) {
                    Vector3(
                        augmentedImage?.extentX, // width
                        1.0f,
                        augmentedImage?.extentZ * ((player?.mediaPlayer?.videoHeight ?: 0) / (player?.mediaPlayer?.videoWidth ?: 0))
                    ) // height
                } else {
                    Vector3(
                        scale,//dataItemObject.scale?.toFloatOrNull() ?: 4f, // width
                        scale,//dataItemObject.scale?.toFloatOrNull() ?: 4f,
                        scale//dataItemObject.scale?.toFloatOrNull() ?: 4f
                    )
                }

                externalTexture.surfaceTexture.setOnFrameAvailableListener {
                    Log.e(TAG, "externalTexture.surfaceTexture")
                    it.setOnFrameAvailableListener(null)
                    videoAnchorNode.renderable = videoRenderable
                }

                onSuccess()
            }
            .exceptionally { throwable ->
                Log.e(TAG, "Could not create ModelRenderable", throwable)
                onFailure()
                return@exceptionally null
            }
    }

    override fun getRenderable(): Renderable? {
        return videoRenderable
    }

    override fun setWorldRotation(rotation: Quaternion) {
        videoAnchorNode?.worldRotation = rotation
        //videoAnchorNode?.localRotation = rotation
    }
}