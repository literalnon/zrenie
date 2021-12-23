package com.example.zrenie20.renderable

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.zrenie20.R
import com.example.zrenie20.data.DataItemObject
import com.example.zrenie20.myarsample.BaseArActivity
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.NodeParent
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Color
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.*
import java.io.File

class ArVideoRenderObject(
    override val context: Context,
    override val dataItemObject: DataItemObject,
    override val renderableFile: File,
    val arFragment: ArFragment? = null
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

            videoTransformableNode?.setOnTouchListener { hitTestResult, motionEvent ->
                return@setOnTouchListener value?.onTouch(hitTestResult, motionEvent) ?: false
            }
        }

    private var mediaPlayer: MediaPlayer = MediaPlayer()
    private var externalTexture: ExternalTexture = ExternalTexture().also {
        mediaPlayer.setSurface(it.surface)
        mediaPlayer.isLooping = true
        //mediaPlayer.setVideoScalingMode(MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING)
    }
    var videoRenderable: ModelRenderable? = null
    private val anchorNode = AnchorNode()
    var videoAnchorNode: AnchorNode = anchorNode

    /*.apply {
    setParent(mScene)
}*/
    var videoTransformableNode: TransformableNode? = null

    override fun setParent(parent: NodeParent) {
        videoAnchorNode.setParent(parent)
    }

    override fun pause() {
        if (mediaPlayer.isPlaying) {
            //videoAnchorNode?.parent?.removeChild(videoAnchorNode)
            videoTransformableNode?.renderable = null
            videoAnchorNode.renderable = null
            mediaPlayer.pause()
        }
    }

    override fun resume() {
        if (!mediaPlayer.isPlaying) {
            videoAnchorNode = anchorNode
            mediaPlayer.start()
            videoAnchorNode.renderable = videoRenderable
            videoTransformableNode?.renderable = videoRenderable
        }
    }

    override fun stop() {
        if (mediaPlayer.isPlaying) {
            videoAnchorNode.anchor?.detach()
            videoAnchorNode.renderable = null
            videoTransformableNode?.renderable = null
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
            .setSource(context,
            if (dataItemObject.alphaChannel == true) {
                R.raw.chroma_key_video
            } else {
                R.raw.augmented_video_model
            })//R.raw.chroma_key_video
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

                Log.e(TAG, "renderableFile.absolutePath : ${renderableFile.absolutePath}")
                //mediaPlayer.reset()
                mediaPlayer.setDataSource(renderableFile.absolutePath)

                mediaPlayer.isLooping = true
                mediaPlayer.prepare()
                mediaPlayer.start()

                videoAnchorNode = anchorNode
                videoAnchorNode.anchor?.detach()
                videoAnchorNode.anchor = null

                Log.e(
                    "ArVideoRenderObject",
                    "1 videoAnchorNode.worldPosition : ${videoAnchorNode.worldPosition}"
                )
                Log.e(
                    "ArVideoRenderObject",
                    "1 videoAnchorNode.localPosition : ${videoAnchorNode.localPosition}"
                )
                val x =
                    videoAnchorNode.localPosition.x + 0.1f//(dataItemObject.offsetX ?: 100).toFloat()
                val y =
                    videoAnchorNode.localPosition.y + 0.1f//(dataItemObject.offsetY ?: 100).toFloat()
                val z = videoAnchorNode.localPosition.z + (dataItemObject.offsetZ ?: 10).toFloat()

                videoAnchorNode.localPosition = Vector3(x, y, z)

                Log.e(
                    "ArVideoRenderObject",
                    "2 videoAnchorNode.worldPosition : ${videoAnchorNode.worldPosition}"
                )
                Log.e(
                    "ArVideoRenderObject",
                    "2 videoAnchorNode.localPosition : ${videoAnchorNode.localPosition}"
                )

                videoAnchorNode.anchor =
                    anchor//augmentedImage.createAnchor(augmentedImage.centerPose)

                Log.e(
                    "ArVideoRenderObject",
                    "3 videoAnchorNode.worldPosition : ${videoAnchorNode.worldPosition}"
                )
                Log.e(
                    "ArVideoRenderObject",
                    "3 videoAnchorNode.localPosition : ${videoAnchorNode.localPosition}"
                )

                val scale = dataItemObject.scale?.toFloatOrNull() ?: 4f

                videoAnchorNode.localScale = if (augmentedImage != null) {
                    Vector3(
                        augmentedImage?.extentX, // width
                        1.0f,
                        augmentedImage?.extentZ * mediaPlayer.videoHeight / mediaPlayer.videoWidth
                    ) // height
                } else {
                    Vector3(
                        scale,//dataItemObject.scale?.toFloatOrNull() ?: 4f, // width
                        scale,//dataItemObject.scale?.toFloatOrNull() ?: 4f,
                        scale//dataItemObject.scale?.toFloatOrNull() ?: 4f
                    )
                }

                /*val x = 1000f//(dataItemObject.offsetX ?: 100).toFloat()
                val y = 1000f//(dataItemObject.offsetY ?: 100).toFloat()
                val z = (dataItemObject.offsetZ ?: 100).toFloat()

                videoAnchorNode.worldPosition = Vector3(x, y, z)*/

                externalTexture.surfaceTexture.setOnFrameAvailableListener {
                    Log.e(TAG, "externalTexture.surfaceTexture")
                    it.setOnFrameAvailableListener(null)
                    //videoAnchorNode.renderable = videoRenderable

                    if (augmentedImage != null) {
                        videoTransformableNode = TransformableNode(arFragment?.transformationSystem).apply {
                            transformationSystem?.selectionVisualizer = CustomVisualizer()
                        }
                        videoTransformableNode?.setParent(videoAnchorNode)
                        videoTransformableNode?.renderable = videoRenderable
                        videoTransformableNode?.select()

                        Log.e(
                            "ArVideoRenderObject",
                            "1 videoTransformableNode.worldPosition : ${videoTransformableNode?.worldPosition}"
                        )
                        Log.e(
                            "ArVideoRenderObject",
                            "1 videoTransformableNode?.localPosition : ${videoTransformableNode?.localPosition}"
                        )

                        val mx = (videoTransformableNode?.worldPosition?.x
                            ?: 0f) + (dataItemObject.offsetX ?: 0).toFloat() / 1000f
                        val my = (videoTransformableNode?.worldPosition?.y
                            ?: 0f) + (dataItemObject.offsetY ?: 0).toFloat() / 1000f
                        val mz = (videoTransformableNode?.worldPosition?.z
                            ?: 0f) + (dataItemObject.offsetZ ?: 0).toFloat() / 1000f

                        videoTransformableNode?.worldPosition = Vector3(mx, my, mz)

                        Log.e(
                            "ArVideoRenderObject",
                            "2 videoTransformableNode?.worldPosition : ${videoTransformableNode?.worldPosition}"
                        )
                        Log.e(
                            "ArVideoRenderObject",
                            "2 videoTransformableNode?.localPosition : ${videoTransformableNode?.localPosition}"
                        )
                    } else {
                        videoAnchorNode.renderable = videoRenderable
                    }
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
        videoTransformableNode?.worldRotation = rotation

        //videoAnchorNode?.localRotation = rotation
    }
}

class CustomVisualizer: SelectionVisualizer {
    override fun applySelectionVisual(node: BaseTransformableNode?) {

    }

    override fun removeSelectionVisual(node: BaseTransformableNode?) {

    }
}