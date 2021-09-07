package com.example.zrenie20.renderable

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.zrenie20.R
import com.example.zrenie20.data.DataItemObject
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.NodeParent
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import java.io.File

class ArVideoRenderObject(
    override val context: Context,
    override val dataItemObject: DataItemObject,
    override val renderableFile: File
) : IArRenderObject {
    companion object {
        val TAG = "DOWNLOAD_VIDEO_FILE"
    }

    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var externalTexture: ExternalTexture = ExternalTexture().also {
        mediaPlayer.setSurface(it.surface)
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
        if (mediaPlayer.isPlaying) {
            videoAnchorNode.renderable = null
            mediaPlayer.pause()
        }
    }

    override fun resume() {
        if (!mediaPlayer.isPlaying) {
            videoAnchorNode = anchorNode
            mediaPlayer.start()
            videoAnchorNode.renderable = videoRenderable
        }
    }

    override fun stop() {
        if (mediaPlayer.isPlaying) {
            videoAnchorNode.anchor?.detach()
            videoAnchorNode.renderable = null
            mediaPlayer.reset()
        }
    }

    override fun start(
        anchor: Anchor?,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        augmentedImage: AugmentedImage?
    ) {

        ModelRenderable.builder()
            .setSource(context, R.raw.augmented_video_model)
            .build()
            .thenAccept { renderable ->
                videoRenderable = renderable
                videoRenderable?.isShadowCaster = false
                videoRenderable?.isShadowReceiver = false
                videoRenderable?.material?.setExternalTexture("videoTexture", externalTexture)

                Log.e(TAG, "renderableFile.absolutePath : ${renderableFile.absolutePath}")
                //mediaPlayer.reset()
                mediaPlayer.setDataSource(renderableFile.absolutePath)

                mediaPlayer.isLooping = true
                mediaPlayer.prepare()
                mediaPlayer.start()

                videoAnchorNode = anchorNode
                videoAnchorNode.anchor?.detach()
                videoAnchorNode.anchor = anchor//augmentedImage.createAnchor(augmentedImage.centerPose)

                videoAnchorNode.localScale = if (augmentedImage != null) {
                    Vector3(
                        augmentedImage?.extentX, // width
                        1.0f,
                        augmentedImage?.extentZ * mediaPlayer.videoHeight / mediaPlayer.videoWidth
                    ) // height
                } else {
                    Vector3(
                        7f, // width
                        7f,
                        7f * mediaPlayer.videoHeight / mediaPlayer.videoWidth
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
        //videoAnchorNode?.worldRotation = rotation
        //videoAnchorNode?.localRotation = rotation
    }
}