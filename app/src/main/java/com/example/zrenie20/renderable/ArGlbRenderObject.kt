package com.example.zrenie20.renderable

import android.animation.Animator
import android.animation.ObjectAnimator
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
        var index = -1
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
            Log.e(TAG, "playAnimation ${renderable?.animationDataCount} : ${animator?.isRunning}")
            if ((renderable?.animationDataCount ?: 0) > 0 && animator?.isRunning != true) {
                var ind = 0
                val data: AnimationData? = renderable?.getAnimationData(ind)

                if (animator == null) {
                    animator = ModelAnimator(data, renderable)

                        /*?.apply {
                        addListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator?) {

                            }

                            override fun onAnimationEnd(animation: Animator?) {
                                ++ind
                                if (renderable?.animationDataCount > ind) {
                                    //animator?.setTarget(null)
                                    val anim = renderable?.getAnimationData(ind)
                                    animator = ModelAnimator(anim, renderable)
                                    animator?.start()
                                    Log.e(TAG, "playAnimation onAnimationEnd : ${ind} : ${anim.durationMs} : ${anim.name}")
                                }
                            }

                            override fun onAnimationCancel(animation: Animator?) {

                            }

                            override fun onAnimationRepeat(animation: Animator?) {

                            }
                        })
                    }*/

                    /*(1 until renderable?.animationDataCount).forEach {
                        val anim = renderable?.getAnimationData(it)
                        ModelAnimator(anim, renderable)?.start()
                        Log.e(TAG, "playAnimation ind : ${it} : ${anim.durationMs} : ${anim.name}")
                    }*/
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
        index++
        val builder = if (renderableFile.absolutePath.contains(".sfb")) {
            ModelRenderable.builder()
                .setSource(context, renderableFile.toUri())//Uri.parse("file:///android_asset/e${index%9 + 1}.sfb"))//
        } else {
            ModelRenderable.builder()
                .setSource(
                    context,
                    //Uri.parse(resource)
                    RenderableSource.builder()
                        .setSource(
                            context,
                            renderableFile.toUri(),//Uri.parse("file:///android_asset/aImage/i6.glb"),//renderableFile.toUri(),//Uri.parse("file:///android_asset/aImage/i6.glb"),//renderableFile.toUri(), //renderableFile.toUri(),//Uri.parse(renderableFile.),//"file:///android_asset/aImage/i6.glb"),
                            RenderableSource.SourceType.GLB
                        )
                        .setScale(0.05f) // Scale the original model to 50%.//dataItemObject.scale?.toFloatOrNull() ?:
                        .setRecenterMode(RenderableSource.RecenterMode.NONE)
                        .build()
                )
        }
        //.setRegistryId(augmentedImage.name)
        builder
            //.setScale(0.5f)
            .build()
            .thenAccept { renderable ->
                Log.e(TAG, "thenAccept")
                glbRenderable = renderable
                animator = null

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