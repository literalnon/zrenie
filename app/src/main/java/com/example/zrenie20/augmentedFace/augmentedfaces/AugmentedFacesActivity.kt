/*
 * Copyright 2020 Google LLC
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
package com.example.zrenie20.augmentedFace.augmentedfaces

import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.OrientationEventListener
import android.view.View
import com.example.zrenie20.R
import com.example.zrenie20.SettingsActivity
import com.example.zrenie20.data.DataItemObject
import com.example.zrenie20.data.TypeItemObjectCodeNames
import com.example.zrenie20.myarsample.BaseArActivity
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import com.google.ar.sceneform.ux.AugmentedFaceNode
import kotlinx.android.synthetic.main.activity_location.*
import kotlinx.android.synthetic.main.activity_my_sample.*
import kotlinx.android.synthetic.main.augmented_faces_activity.*
import kotlinx.android.synthetic.main.augmented_faces_activity.ivChangeVisibility
import kotlinx.android.synthetic.main.augmented_faces_activity.llFocus
import kotlinx.android.synthetic.main.augmented_faces_activity.llMainActivities
import kotlinx.android.synthetic.main.layout_main_activities.*
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Consumer


/**
 * This is an example activity that uses the Sceneform UX package to make common Augmented Faces
 * tasks easier.
 */
class AugmentedFacesActivity : BaseArActivity() {
    private val faceNodeMap = HashMap<AugmentedFace, Node?>()

    override val layoutId: Int = R.layout.augmented_faces_activity
    var faceMeshTexture: Texture? = null
    private var faceRegionsRenderable: ModelRenderable? = null

    init {
        assetsArray = arrayListOf()
        /*DataItemObject(
            id = 1,
            filePath = "file:///android_asset/face/f1.glb",
            name = "f1"
        ),
        DataItemObject(
            id = 2,
            filePath = "file:///android_asset/face/f2.glb",
            name = "f2"
        ),
        DataItemObject(
            id = 3,
            filePath = "file:///android_asset/face/f3.glb",
            name = "f3"
        ),
        DataItemObject(
            id = 4,
            filePath = "file:///android_asset/face/f4.glb",
            name = "f4"
        ),
        DataItemObject(
            id = 5,
            filePath = "file:///android_asset/face/f5a.glb",
            name = "f5a"
        )
    )*/
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*ModelRenderable.builder()
            .setSource(this, R.raw.canonical_face_mesh)
            .build()
            .thenAccept(
                Consumer { modelRenderable: ModelRenderable ->
                    faceRegionsRenderable = modelRenderable
                    modelRenderable.isShadowCaster = false
                    modelRenderable.isShadowReceiver = false
                })

        Texture.builder()
            .setSource(this, R.raw.canonical_face_mesh)
            .build()
            .thenAccept(Consumer { texture: Texture ->
                faceMeshTexture = texture
            })*/

        /*Texture.builder()
            .setSource(this, R.drawable.face4)
            //.setSource(this, R.raw.canonical_face_mesh)
            .build()
            .thenAccept(Consumer { texture: Texture ->
                faceMeshTexture = texture
            })*/

        /*ModelRenderable.builder()
            .setSource(this, R.raw.fox_face)
            .build()
            .thenAccept(
                Consumer { modelRenderable: ModelRenderable ->
                    faceRegionsRenderable = modelRenderable
                    modelRenderable.isShadowCaster = false
                    modelRenderable.isShadowReceiver = false
                })*/

        /*ModelRenderable.builder()
            .setSource(
                this,
                //Uri.parse(resource)
                RenderableSource.builder().setSource(
                    this,
                    Uri.parse("file:///android_asset/face/f1.glb"),
                    RenderableSource.SourceType.GLB
                )
                    .setRecenterMode(RenderableSource.RecenterMode.NONE)
                    .build()
            )
            //.setRegistryId(augmentedImage.name)
            .build()
            .thenApply {
                faceRegionsRenderable = it
                it
            }*/

        sceneView?.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST

        val scene = sceneView?.scene
        scene?.addOnUpdateListener { frameTime: FrameTime? ->

            /*if (currentRenderable?.getRenderable() == null) {
                return@addOnUpdateListener
            }*/

            val faceList = sceneView?.session!!.getAllTrackables(
                AugmentedFace::class.java
            )

            for (face in faceList) {

                if (!faceNodeMap.containsKey(face)) {
                    var faceNode: Node =
                        if (currentRenderable?.dataItemObject?.type?.codeName == TypeItemObjectCodeNames.VIDEO.codeName ||
                            currentRenderable?.dataItemObject?.type?.codeName == TypeItemObjectCodeNames.OBJECT.codeName
                        ) {
                            MyAugmentedFaceNode(face)
                        } else {
                            AugmentedFaceNode(face)//MyAugmentedFaceNode(this, face)//face.createAnchor(face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP))
                        }

                    faceNode.setParent(scene)

                    //val faceNode = MyAugmentedFaceNode(face)//MyAugmentedFaceNode(this, face)//face.createAnchor(face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP))

                    //faceNode.worldRotation = rotation
                    //faceNode = face.createAnchor(face.centerPose)
                    //faceNode.faceRegionsRenderable = faceRegionsRenderable//currentRenderable?.vrRenderable
                    //faceRegionsRenderable = currentRenderable?.vrRenderable
                    if (currentRenderable?.getRenderable() == null) {

                        currentRenderable?.start(
                            anchor = null,//faceNode.augmentedFace?.createAnchor(face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP)),
                            onSuccess = {
                                //faceNode.renderable = currentRenderable?.getRenderable()
                                //faceNode.faceRegionsRenderable = currentRenderable?.getRenderable()

                                setRenderableFace(faceNode)
                                //faceNode.iArRenderObject = currentRenderable
                            },
                            onFailure = {

                            }
                        )
                    } else {
                        //faceNode.renderable = currentRenderable?.getRenderable()
                        //faceNode.faceRegionsRenderable = currentRenderable?.getRenderable() as ModelRenderable
                        setRenderableFace(faceNode)
                        //faceNode.iArRenderObject = currentRenderable
                    }
                    // Overlay the 3D assets on the face.
                    //faceNode.faceRegionsRenderable = faceRegionsRenderable
                    //faceNode.faceRegionsRenderable = faceRegionsRenderable
                    // Overlay a texture on the face.

                    //faceNode.faceMeshTexture = faceMeshTexture

                    //faceNode.faceMeshTexture = faceMeshTexture
                    faceNodeMap[face] = faceNode
                }
            }

            // Remove any AugmentedFaceNodes associated with an AugmentedFace that stopped tracking.
            val iter: MutableIterator<Map.Entry<AugmentedFace, Node?>> =
                faceNodeMap.entries.iterator()
            while (iter.hasNext()) {
                val entry = iter.next()
                val face = entry.key
                Log.e("FACE_NODE_BUG", "track state : ${face.trackingState}")

                if (face.trackingState == TrackingState.STOPPED || face.trackingState == TrackingState.PAUSED) {
                    val faceNode = entry.value
                    if (faceNode is MyAugmentedFaceNode) {
                        faceNode.pause()
                    }

                    faceNode!!.setParent(null)
                    iter.remove()
                } else if (face.trackingState == TrackingState.TRACKING) {
                    val faceNode = entry.value

                    if (faceNode is MyAugmentedFaceNode) {
                        faceNode.resume()
                    }
                    //faceNode?.anchor = face.createAnchor(face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP))
                }

            }
        }


        /*fakeView?.setOnTouchListener { v, event ->
            Log.e("setOnTapArPlaneListener", "setOnTouchListener fakeView event: ${event.action}")

            if (event.action == MotionEvent.ACTION_DOWN && llFocus.visibility == View.VISIBLE) {
                llFocus.visibility = View.GONE
                llMainActivities.visibility = View.VISIBLE
                return@setOnTouchListener true
            }

            return@setOnTouchListener false
        }*/

        ivChangeVisibility?.setOnClickListener {
            if (llFocus.visibility == View.VISIBLE) {
                llFocus.visibility = View.GONE
                llMainActivities.visibility = View.VISIBLE

                arFragment?.arSceneView?.planeRenderer?.isEnabled = true
            } else {
                llFocus.visibility = View.VISIBLE
                llMainActivities.visibility = View.GONE

                arFragment?.arSceneView?.planeRenderer?.isEnabled = false
            }
        }

        ivSettings?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    fun setRenderableFace(faceNode: Node): Node {
        if (faceNode is MyAugmentedFaceNode) {
            val modelRenderable = currentRenderable?.getRenderable()

            if (modelRenderable is ModelRenderable) {
                faceNode.faceRegionsRenderable = modelRenderable
            } else {
                faceNode.renderable = modelRenderable
            }

            faceNode.iArRenderObject = currentRenderable

            return faceNode
        }

        faceNode as AugmentedFaceNode

        if (currentRenderable?.renderableFile?.name?.contains("png") == true ||
            currentRenderable?.renderableFile?.name?.contains("jpeg") == true
        ) {
            Texture.builder()
                .setSource { currentRenderable?.renderableFile?.inputStream() }
                .build()
                .thenAccept(Consumer { texture: Texture ->
                    faceMeshTexture = texture
                    faceNode.faceMeshTexture = faceMeshTexture
                })
        } else {
            val modelRenderable = currentRenderable?.getRenderable()
            modelRenderable?.isShadowCaster = false
            modelRenderable?.isShadowReceiver = false

            if (modelRenderable is ModelRenderable) {
                faceNode.faceRegionsRenderable = modelRenderable
            } else {
                faceNode.renderable = modelRenderable
            }
        }

        return faceNode
    }
    override fun selectedRenderable(dataItemObjectDataClass: DataItemObject): Boolean {
        val returnValue = super.selectedRenderable(dataItemObjectDataClass)
        val iter: MutableIterator<Map.Entry<AugmentedFace, Node?>> =
            faceNodeMap.entries.iterator()
        while (iter.hasNext()) {
            val entry = iter.next()
            val faceNode = entry.value
            faceNode!!.setParent(null)
            iter.remove()
        }

        return returnValue
    }
}