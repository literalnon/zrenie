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
import androidx.core.net.toUri
import com.example.zrenie20.R
import com.example.zrenie20.data.DataItemObject
import com.example.zrenie20.renderable.ArRenderObjectFactory
import com.example.zrenie20.renderable.IArRenderObject
import com.google.ar.core.Anchor
import com.google.ar.core.AugmentedImage
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.ArSceneView
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ExternalTexture
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.ViewRenderable
import com.google.common.reflect.Reflection.getPackageName
import java.io.File
import java.lang.Exception
import java.util.concurrent.CompletableFuture

/**
 * Node for rendering an augmented image. The image is framed by placing the virtual picture frame
 * at the corners of the augmented image trackable.
 */
class AugmentedImageNode(
    val context: Context,
    val augmentedImage: AugmentedImage,
    val renderableFile: File,
    val dataItemObject: DataItemObject,
    val mScene: Scene,
) : AnchorNode() {

    // The augmented image represented by this node.
    var image: AugmentedImage? = null
        private set

    //val cornerNode = Node()

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
        val rend = Companion.arRenderObjectMap[index]

        // Set the anchor based on the center of the image.
        anchor = image.createAnchor(image.centerPose)
        rend?.start(
            anchor = augmentedImage.createAnchor(augmentedImage.centerPose),
            onSuccess = {},
            onFailure = {},
            augmentedImage = image
        )
        rend?.setParent(this)

        // Make the 4 corner nodes.
        //val localPosition = Vector3()

        // Upper left corner.
        //localPosition[0f, 0.0f] = 0f

        //cornerNode.localPosition = localPosition
        //cornerNode.renderable = Companion.mRenderable[index]?.getNow(null)
    }

    fun resumeImage() {
        Companion.arRenderObjectMap[image?.index]?.resume()
        //cornerNode.renderable = Companion.arRenderObjectMap[image?.index]?.getRenderable()
    }

    fun pauseImage() {
        Companion.arRenderObjectMap[image?.index]?.pause()
        //cornerNode.renderable = null
    }

    fun stop() {
        Companion.arRenderObjectMap[image?.index]?.stop()
    }

    companion object {
        private const val TAG = "AugmentedImageNode"

        // Models of the 4 corners.  We use completable futures here to simplify
        // the error handling and asynchronous loading.  The loading is started with the
        // first construction of an instance, and then used when the image is set.
        //private var mRenderable: HashMap<Int, CompletableFuture<Renderable>> = hashMapOf()
        private var arRenderObjectMap: HashMap<Int, IArRenderObject> = hashMapOf()
    }


    /*class RenderableConfig(
        val renderableType: Int,
        val resource: String,
        val videoAnchorNode: AnchorNode,
        val augmentedImage: AugmentedImage
    ) {

        fun getRenderable(context: Context?): CompletableFuture<Renderable> {
            return ModelRenderable.builder()
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
    }*/

    init {
        // Upon construction, start loading the models for the corners of the frame.
        val index = augmentedImage.index ?: 0

        if (Companion.arRenderObjectMap[index] == null) {

            /*val rConfig =
                RenderableConfig(
                renderableType = 1,
                resource = renderableFile.toUri().toString(),
                videoAnchorNode = this,
                augmentedImage = augmentedImage
            )*/

            val arRenderObject = ArRenderObjectFactory(
                context = context,
                dataItemObject = dataItemObject,
                mScene = mScene,
                renderableFile = renderableFile
            ).createRenderable()

            try {
                //Companion.mRenderable.put(index, arRenderObject)
                Companion.arRenderObjectMap.put(index, arRenderObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}