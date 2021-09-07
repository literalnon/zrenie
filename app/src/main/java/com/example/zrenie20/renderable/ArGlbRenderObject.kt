package com.example.zrenie20.renderable

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
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

class ArGlbRenderObject(
    override val context: Context,
    override val dataItemObject: DataItemObject,
    //override val mScene: Scene,
    override val renderableFile: File
) : IArRenderObject {
    companion object {
        val TAG = "ArGlbRenderObject"
    }

    private var glbRenderable: ModelRenderable? = null
    val cornerNode = AnchorNode()

    override fun pause() {
        Log.e(TAG, "pause")
        cornerNode.renderable = null
    }

    override fun resume() {
        Log.e(TAG, "resume")
        cornerNode.renderable = glbRenderable
        playAnimation()
    }

    private var animator: ModelAnimator? = null

    private fun playAnimation() {
        glbRenderable?.let { renderable ->
            if ((renderable?.animationDataCount ?: 0) > 0 && animator?.isRunning != true) {
                val data: AnimationData? = renderable?.getAnimationData(0)
                if (animator == null) {
                    animator = ModelAnimator(data, renderable)
                }

                animator?.start()
            }
        }
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

        val builder = if (renderableFile.absolutePath.contains(".sfb")) {
            ModelRenderable.builder()
                .setSource(context, renderableFile.toUri())
        } else {
            ModelRenderable.builder()
                .setSource(
                    context,
                    //Uri.parse(resource)
                    RenderableSource.builder()
                        .setSource(
                            context,
                            renderableFile.toUri(),//Uri.parse("file:///android_asset/aImage/i6.glb"),//renderableFile.toUri(), //renderableFile.toUri(),//Uri.parse(renderableFile.),//"file:///android_asset/aImage/i6.glb"),
                            RenderableSource.SourceType.GLB
                        )
                        .setScale(0.5f) // Scale the original model to 50%.//dataItemObject.scale?.toFloatOrNull() ?:
                        .setRecenterMode(RenderableSource.RecenterMode.NONE)
                        .build()
                )
        }
        //.setRegistryId(augmentedImage.name)
        builder.build()
            .thenAccept { renderable ->
                Log.e(TAG, "thenAccept")
                glbRenderable = renderable

                val localPosition = Vector3()

                cornerNode.localPosition = localPosition
                cornerNode.renderable = glbRenderable

                playAnimation()
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
        cornerNode?.worldRotation = rotation
    }
}