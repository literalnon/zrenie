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
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.example.zrenie20.AugmentedFaceNode
import com.example.zrenie20.MyAugmentedFaceNode
import com.example.zrenie20.R
import com.example.zrenie20.SettingsActivity
import com.example.zrenie20.myarsample.BaseArActivity
import com.example.zrenie20.myarsample.data.VrObject
import com.google.ar.core.AugmentedFace
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.rendering.Texture
import kotlinx.android.synthetic.main.augmented_faces_activity.*
import kotlinx.android.synthetic.main.layout_main_activities.*
import java.util.*
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
        assetsArray = arrayListOf(
            VrObject(
                id = 1,
                link = "file:///android_asset/face/f1.glb",
                name = "f1"
            ),
            VrObject(
                id = 2,
                link = "file:///android_asset/face/f2.glb",
                name = "f2"
            ),
            VrObject(
                id = 3,
                link = "file:///android_asset/face/f3.glb",
                name = "f3"
            ),
            VrObject(
                id = 4,
                link = "file:///android_asset/face/f4.glb",
                name = "f4"
            ),
            VrObject(
                id = 5,
                link = "file:///android_asset/face/f5a.glb",
                name = "f5a"
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*Texture.builder()
            .setSource(this, R.drawable.fox_face_mesh_texture)
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

        //sceneView?.cameraStreamRenderPriority = Renderable.RENDER_PRIORITY_FIRST
        val scene = sceneView?.scene
        scene?.addOnUpdateListener { frameTime: FrameTime? ->
            if (currentRenderable?.vrRenderable == null) {
                return@addOnUpdateListener
            }

            val faceList = sceneView?.session!!.getAllTrackables(
                AugmentedFace::class.java
            )

            for (face in faceList) {
                if (!faceNodeMap.containsKey(face)) {
                    val faceNode = AugmentedFaceNode(face)//MyAugmentedFaceNode(this, face)//face.createAnchor(face.getRegionPose(AugmentedFace.RegionType.NOSE_TIP))
                    faceNode.setParent(scene)
                    //faceNode = face.createAnchor(face.centerPose)
                    //faceNode.faceRegionsRenderable = currentRenderable?.vrRenderable
                    faceNode.renderable = currentRenderable?.vrRenderable

                    // Overlay the 3D assets on the face.
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
                    faceNode!!.setParent(null)
                    iter.remove()
                } else if (face.trackingState == TrackingState.TRACKING) {
                    val faceNode = entry.value
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
            } else {
                llFocus.visibility = View.VISIBLE
                llMainActivities.visibility = View.GONE
            }
        }

        ivSettings?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun selectedRenderable(vrObjectDataClass: VrObject): Boolean {
        val returnValue = super.selectedRenderable(vrObjectDataClass)
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