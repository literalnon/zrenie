package com.example.zrenie20.renderable

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import com.example.zrenie20.R
import com.example.zrenie20.data.DataItemObject
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.NodeParent
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.animation.ModelAnimator
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import java.io.File
import java.io.InputStream
import java.util.concurrent.Callable
import android.content.Intent




class ArImageRenderObject(
    override val context: Context,
    override val dataItemObject: DataItemObject,
    //override val mScene: Scene,
    override val renderableFile: File
) : IArRenderObject {

    override var onTouchListener: Node.OnTouchListener? = null
        set(value) {
            field = value

            viewAnchorNode.setOnTouchListener { hitTestResult, motionEvent ->
                return@setOnTouchListener value?.onTouch(hitTestResult, motionEvent) ?: false
            }
        }

    /*companion object {
        val TAG = "ArImageRenderObject"
    }

    private var imageRenderable: Renderable? = null

    override fun pause() {
        Log.e(TAG, "pause")
        //cornerNode.renderable = null
    }

    override fun resume() {
        Log.e(TAG, "resume")
        //cornerNode.renderable = imageRenderable
    }

    override fun stop() {
        Log.e(TAG, "stop")
        //cornerNode.renderable = null
    }

    override fun start(
        anchor: Anchor?,
        onSuccess: () -> Unit,
        onFailure: () -> Unit,
        augmentedImage: AugmentedImage?
    ) {
        Log.e(TAG, "start")

        val builder = ViewRenderable.builder()
            .setView(context, R.layout.example_layout)

        builder.build()
            .thenAccept { renderable ->
                Log.e(TAG, "thenAccept")
                val ivMarker = renderable.view?.findViewById<ImageView>(R.id.ivMarker)
                ivMarker?.setImageURI(renderableFile.toUri())

                imageRenderable = renderable

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
        return imageRenderable
    }

    override fun setParent(parent: NodeParent) {
        Log.e(TAG, "setParent")

    }

    override fun setWorldRotation(rotation: Quaternion) {

    }*/

    companion object {
        val TAG = "DOWNLOAD_VIDEO_FILE2"
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
            .setView(context, R.layout.example_layout)

        builder.build()
            .thenAccept { renderable ->
                val ivMarker = renderable.view?.findViewById<ImageView>(R.id.ivMarker)
                ivMarker?.setImageURI(renderableFile.toUri())

                ivMarker?.setOnClickListener {
                    val address = Uri.parse(dataItemObject.actionUrl)
                    val openLinkIntent = Intent(Intent.ACTION_VIEW, address)

                    if (openLinkIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(openLinkIntent)
                    } else {
                        Log.d("Intent", "Не получается обработать намерение!");
                    }
                }

                viewAnchorNode = anchorNode

                val localPosition = Vector3()
                viewAnchorNode.localPosition = localPosition

                viewRenderable = renderable
                viewRenderable?.isShadowCaster = false
                viewRenderable?.isShadowReceiver = false

                val scale = dataItemObject.scale?.toFloatOrNull() ?: 0.2f

                viewAnchorNode.worldScale = Vector3(
                        scale, // width
                        scale,
                        scale
                    ) // height

                viewAnchorNode.renderable = viewRenderable

                val rotation = Quaternion()
                rotation.x = 0f
                rotation.y = 0f
                rotation.z = 0f
                rotation.w = 1f

                viewAnchorNode.worldRotation = rotation
                //viewAnchorNode.worldPosition = position
                //viewAnchorNode.localPosition = Vector3(-1000f,-1000f,-100f)
                Log.e("FACE_BUG", "viewAnchorNode : ${viewAnchorNode.worldPosition}, ${viewAnchorNode.localPosition}")
                //val anchorUp = anchorNode.up
                //viewAnchorNode.setLookDirection(Vector3.up(), anchorUp)

                //viewAnchorNode.anchor?.detach()
                //viewAnchorNode.anchor = anchor//augmentedImage.createAnchor(augmentedImage.centerPose)

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

    override fun setWorldPosition(position: Vector3) {
        viewAnchorNode?.worldPosition = position
        //viewAnchorNode?.localRotation = rotation
    }
}