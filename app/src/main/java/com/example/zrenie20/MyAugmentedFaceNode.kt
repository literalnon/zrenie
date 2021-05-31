package com.example.zrenie20

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.google.ar.core.AugmentedFace
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import java.lang.Exception
import java.util.concurrent.CompletableFuture

class MyAugmentedFaceNode(
    val context: Context?,
    val face: AugmentedFace
) : AnchorNode() {

    // The augmented image represented by this node.
    var image: AugmentedImage? = null
        private set


    /**
     * Called when the AugmentedImage is detected and should be rendered. A Sceneform node tree is
     * created based on an Anchor created from the image. The corners are then positioned based on the
     * extents of the image. There is no need to worry about world coordinates since everything is
     * relative to the center of the image, which is the parent node of the corners.
     */
    fun setImage(image: AugmentedImage) {
        this.image = image
        val index = image.index
        // If any of the models are not loaded, then recurse when all are loaded.
        val rend = Companion.mRenderable

        if (rend != null && !rend.isDone) {
            CompletableFuture.allOf(Companion.mRenderable)
                .thenAccept { aVoid: Void? -> setImage(image) }
                .exceptionally { throwable: Throwable? ->
                    Log.e(TAG, "Exception loading", throwable)
                    null
                }
        }

        // Set the anchor based on the center of the image.
        anchor = image.createAnchor(image.centerPose)

        // Make the 4 corner nodes.
        val localPosition = Vector3()
        val cornerNode: Node

        // Upper left corner.
        //localPosition[0f, 0.0f] = 0f
        cornerNode = Node()
        cornerNode.setParent(this)
        cornerNode.localPosition = localPosition
        cornerNode.renderable = mRenderable?.getNow(null)
    }

    companion object {
        private const val TAG = "AugmentedImageNode"

        // Models of the 4 corners.  We use completable futures here to simplify
        // the error handling and asynchronous loading.  The loading is started with the
        // first construction of an instance, and then used when the image is set.
        private var mRenderable: CompletableFuture<Renderable>? = null
    }

    /*val items = arrayListOf<RenderableConfig>(
        RenderableConfig(
            renderableType = 0,
            resource = "android.resource://" + (context?.packageName ?: "") + "/" + R.raw.augmented_video_model,
            videoAnchorNode = this,
            augmentedImage = augmentedImage
        ),
        RenderableConfig(
            renderableType = 0,
            resource = "android.resource://" + (context?.packageName ?: "") + "/" + R.raw.i2,
            videoAnchorNode = this,
            augmentedImage = augmentedImage
        ),
        RenderableConfig(
            renderableType = 0,
            resource = "android.resource://" + (context?.packageName ?: "") + "/" + R.raw.i3,
            videoAnchorNode = this,
            augmentedImage = augmentedImage
        ),
        RenderableConfig(
            renderableType = 0,
            resource = "android.resource://" + (context?.packageName ?: "") + "/" + R.raw.i4,
            videoAnchorNode = this,
            augmentedImage = augmentedImage
        ),
        RenderableConfig(
            renderableType = 0,
            resource = "android.resource://" + (context?.packageName ?: "") + "/" + R.raw.i5,
            videoAnchorNode = this,
            augmentedImage = augmentedImage
        ),
        RenderableConfig(
            renderableType = 1,
            resource = "file:///android_asset/aImage/i6.glb",
            videoAnchorNode = this,
            augmentedImage = augmentedImage
        ),
        RenderableConfig(
            renderableType = 1,
            resource = "file:///android_asset/aImage/i7a.glb",
            videoAnchorNode = this,
            augmentedImage = augmentedImage
        )
    )*/

    init {
        // Upon construction, start loading the models for the corners of the frame.
        //val index = augmentedImage.index ?: 0

        if (mRenderable == null) {

            //val rConfig = items.get(index)

            /*try {
                Companion.mRenderable = //rConfig.getRenderable(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }*/
            //videoView = rConfig.videoView

            mRenderable = ModelRenderable.builder()
                .setSource(
                    context, R.raw.i1
                )
                //.setRegistryId(augmentedImage.name)
                .build()
                .thenApply { it }
            ;
        }
    }
}