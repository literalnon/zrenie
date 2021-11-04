package com.example.zrenie20.renderable

import android.content.Context
import android.util.Log
import com.example.zrenie20.R
import com.example.zrenie20.data.DataItemObject
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.NodeParent
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import java.io.File

class ArLoadingRenderObject(
    override val context: Context,
    override val dataItemObject: DataItemObject,
    override val renderableFile: File? = null
) : IArRenderObject {

    override var onTouchListener: Node.OnTouchListener? = null
        set(value) {
            field = value

            viewAnchorNode.setOnTouchListener { hitTestResult, motionEvent ->
                return@setOnTouchListener value?.onTouch(hitTestResult, motionEvent) ?: false
            }
        }
    /*
    companion objectArLoadingRenderObject {
        val TAG = "ArGlbRenderObject"
    }

    private var glbRenderable: Renderable? = null
    val cornerNode = AnchorNode()

    override fun pause() {
        Log.e(TAG, "pause")
        cornerNode.renderable = null
    }

    override fun resume() {
        Log.e(TAG, "resume")
        cornerNode.renderable = glbRenderable
    }

    override fun stop() {
        Log.e(TAG, "stop")
        cornerNode.renderable = null
    }

    override fun start(
        anchor: Anchor?,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        augmentedImage: AugmentedImage?
    ) {
        Log.e(TAG, "start")

        val builder = ViewRenderable.builder()
            .setView(context, R.layout.loading_layout)

        builder.build()
            .thenAccept { renderable ->
                Log.e(TAG, "thenAccept")
                glbRenderable = renderable

                val localPosition = Vector3()

                cornerNode.localPosition = localPosition
                cornerNode.renderable = glbRenderable

                onSuccess()
            }
            .exceptionally { throwable ->
                Log.e(TAG, "exceptionally : ${throwable.message}")
                throwable.printStackTrace()
                Log.e(ArVideoRenderObject.TAG, "Could not create ModelRenderable", throwable)
                onFailure()
                return@exceptionally null
            }
    }

    override fun getRenderable(): Renderable? {
        return glbRenderable
    }

    override fun setParent(parent: NodeParent) {
        Log.e(TAG, "setParent")
        cornerNode.setParent(parent)
    }

    override fun setWorldRotation(rotation: Quaternion) {
        //cornerNode?.worldRotation = rotation
    }*/


    companion object {
        val TAG = "DOWNLOAD_VIDEO_FILE1"
    }

    var viewRenderable: Renderable? = null
    private var anchorNode = AnchorNode()
    var viewAnchorNode: AnchorNode = anchorNode

    override fun setParent(parent: NodeParent) {
        viewAnchorNode.setParent(parent)
    }

    override fun pause() {
        viewAnchorNode.renderable = null
    }

    override fun resume() {
            viewAnchorNode = anchorNode
            viewAnchorNode.renderable = viewRenderable
    }

    override fun stop() {
            viewAnchorNode.anchor?.detach()
            viewAnchorNode.renderable = null
    }

    override fun start(
        anchor: Anchor?,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        augmentedImage: AugmentedImage?
    ) {
        val builder = ViewRenderable.builder()
            .setView(context, R.layout.loading_layout)

        builder.build()
            .thenAccept { renderable ->
                viewRenderable = renderable
                viewRenderable?.isShadowCaster = false
                viewRenderable?.isShadowReceiver = false

                viewAnchorNode = anchorNode
                //viewAnchorNode.anchor?.detach()
                //viewAnchorNode.anchor = anchor//augmentedImage.createAnchor(augmentedImage.centerPose)

                viewAnchorNode.worldScale = if (augmentedImage != null) {
                    Vector3(
                        0.2f, // width
                        0.2f,
                        0.2f
                    ) // height
                } else {
                    Vector3(
                        0.2f, // width
                        0.2f,
                        0.2f
                    )
                }

                viewAnchorNode.renderable = viewRenderable

                val rotation = Quaternion()
                rotation.x = 0f
                rotation.y = 0f
                rotation.z = 0f
                rotation.w = 1f

                viewAnchorNode.worldRotation = rotation


                //val anchorUp = anchorNode.up
                //viewAnchorNode.setLookDirection(Vector3.up(), anchorUp)

                onSuccess()
            }
            .exceptionally { throwable ->
                Log.e(TAG, "Could not create ModelRenderable", throwable)
                onFailure()
                return@exceptionally null
            }
    }



    override fun getRenderable(): Renderable? {
        return viewRenderable
    }

    override fun setWorldRotation(rotation: Quaternion) {
        viewAnchorNode?.worldRotation = rotation
        //viewAnchorNode?.localRotation = rotation
    }

}