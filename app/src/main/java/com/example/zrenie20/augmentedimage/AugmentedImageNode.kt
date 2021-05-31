/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.zrenie20.augmentedimage

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.widget.MediaController
import android.widget.VideoView
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.zrenie20.R
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.common.reflect.Reflection.getPackageName
import java.lang.Exception
import java.util.concurrent.CompletableFuture

/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
class AugmentedImageNode(
    val context: Context?,
    val augmentedImage: AugmentedImage
) : AnchorNode() {

    // The augmented image represented by this node.
    var image: AugmentedImage? = null
        private set

    fun destroy() {
        videoView?.stopPlayback()
    }

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
        val rend = Companion.mRenderable[index]

        if (rend != null && !rend.isDone) {
            CompletableFuture.allOf(Companion.mRenderable[index])
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
        cornerNode.renderable = Companion.mRenderable[index]?.getNow(null)
    }

    companion object {
        private const val TAG = "AugmentedImageNode"

        // Models of the 4 corners.  We use completable futures here to simplify
        // the error handling and asynchronous loading.  The loading is started with the
        // first construction of an instance, and then used when the image is set.
        private var mRenderable: HashMap<Int, CompletableFuture<Renderable>> = hashMapOf()
    }

    var videoView: VideoView? = null

    class RenderableConfig(
        val renderableType: Int,
        val resource: String,
        val videoAnchorNode: AnchorNode,
        val augmentedImage: AugmentedImage
    ) {
        var videoView: VideoView? = null
        private var mediaPlayer: MediaPlayer = MediaPlayer()
        private var externalTexture: ExternalTexture? = null
        private var modelRenderable: Renderable? = null
        //private lateinit var videoAnchorNode: AnchorNode

        fun getRenderable(context: Context?): CompletableFuture<Renderable> {
            return if (false && renderableType == 0) {
                externalTexture = ExternalTexture().also {
                    mediaPlayer.setSurface(it.surface)
                }

                /*ViewRenderable.builder()
                    .setView(context, R.layout.item_video_view)
                    .build()
                    .thenApply {
                        *//*videoView = it.view.findViewById<VideoView?>(R.id.video_view)

                        videoView?.setVideoPath(resource)

                        videoView?.start()*//*

                        it
                    }*/

                val item: CompletableFuture<Renderable> = ModelRenderable.builder()
                    .setSource(context, R.raw.augmented_video_model)
                    .build()
                    .thenApply { it }

                item.thenAccept { renderable ->
                    modelRenderable = renderable
                    renderable.isShadowCaster = false
                    renderable.isShadowReceiver = false
                    renderable.material.setExternalTexture("videoTexture", externalTexture)
                }

                item.exceptionally { throwable ->
                    Log.e(TAG, "Could not create ModelRenderable", throwable)
                    return@exceptionally null
                }

                playbackArVideo(context)
                item

            } else {
                ModelRenderable.builder()
                    .setSource(
                        context,
                        //Uri.parse(resource)
                        RenderableSource.builder().setSource(
                            context,
                            Uri.parse(resource),//"file:///android_asset/aImage/i6.glb"),
                            RenderableSource.SourceType.GLB
                        )
                            .setScale(0.05f) // Scale the original model to 50%.
                            .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                            .build()
                    )
                    //.setRegistryId(augmentedImage.name)
                    .build()
                    .thenApply {
                        it
                    }
            }

            /*videoAnchorNode = AnchorNode().apply {
                setParent(arSceneView.scene)
            }*/
        }

        private fun playbackArVideo(context: Context?) {
            Log.d(TAG, "playbackVideo = ${augmentedImage.name}")

            context?.assets?.openFd("augmented_video_model.sfb")
                ?.use { descriptor ->
                    mediaPlayer.reset()
                    mediaPlayer.setDataSource(descriptor)
                }
                ?.also {
                    mediaPlayer.isLooping = true
                    mediaPlayer.prepare()
                    mediaPlayer.start()
                }


            videoAnchorNode.anchor?.detach()
            videoAnchorNode.anchor = augmentedImage.createAnchor(augmentedImage.centerPose)
            videoAnchorNode.localScale = Vector3(
                augmentedImage.extentX, // width
                1.0f,
                augmentedImage.extentZ
            ) // height

            //activeAugmentedImage = augmentedImage

            externalTexture?.surfaceTexture?.setOnFrameAvailableListener {
                it.setOnFrameAvailableListener(null)
                videoAnchorNode.renderable = modelRenderable
            }
        }
    }

    val items = arrayListOf<RenderableConfig>(
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
    )

    init {
        // Upon construction, start loading the models for the corners of the frame.
        val index = augmentedImage.index ?: 0

        if (Companion.mRenderable[index] == null) {

            val rConfig = items.get(index)

            try {
                Companion.mRenderable.put(index, rConfig.getRenderable(context))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            videoView = rConfig.videoView

            /*ModelRenderable.builder()
                      .setSource(
                              context,
                              RenderableSource.builder().setSource(
                                      context,
                                      Uri.parse("file:///android_asset/aImage/i6.glb"),
                                      RenderableSource.SourceType.GLB
                              )
                                      .setScale(0.05f) // Scale the original model to 50%.
                                      .setRecenterMode(RenderableSource.RecenterMode.ROOT)
                                      .build()
                      )
                      //.setRegistryId(augmentedImage.name)
                      .build();*/
        }
    }
}