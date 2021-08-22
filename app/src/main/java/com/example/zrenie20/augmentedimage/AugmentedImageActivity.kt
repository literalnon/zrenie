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

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commitNow
import androidx.fragment.app.transaction
import com.example.zrenie20.R
import com.example.zrenie20.SettingsActivity
import com.google.ar.core.AugmentedImage
import com.google.ar.core.TrackingState
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.ux.ArFragment
import kotlinx.android.synthetic.main.activity_my_sample.*
import kotlinx.android.synthetic.main.layout_main_activities.*
import java.util.*

class AugmentedImageActivity : AppCompatActivity() {
    private var arFragment: ArFragment? = null
    private val augmentedImageMap: MutableMap<AugmentedImage, AugmentedImageNode?> = HashMap()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.augmented_image_activity)

        //arFragment = supportFragmentManager.findFragmentById(R.id.mArFragment) as ArFragment?
        ArVideoFragment()?.let { mArFragment ->
            arFragment = mArFragment
            supportFragmentManager.let {
                it.beginTransaction()
                    .replace(R.id.fragmentContainer, mArFragment)
                    .commit()

                /*arFragment!!.arSceneView?.scene?.addOnUpdateListener { frameTime: FrameTime ->
                    onUpdateFrame(
                        frameTime
                    )
                }*/
            }
        }

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

    /**
     * Registered with the Sceneform Scene object, this method is called at the start of each frame.
     *
     * @param frameTime - time since last frame.
     */
    /*private fun onUpdateFrame(frameTime: FrameTime) {
        val frame = arFragment!!.arSceneView.arFrame ?: return

        // If there is no frame, just return.
        val updatedAugmentedImages = frame.getUpdatedTrackables(
            AugmentedImage::class.java
        )

        for (augmentedImage in updatedAugmentedImages) {
            when (augmentedImage.trackingState) {
                TrackingState.PAUSED -> {
                    // When an image is in PAUSED state, but the camera is not PAUSED, it has been detected,
                    // but not yet tracked.
                    val text = "Detected Image " + augmentedImage.index
                    augmentedImageMap.forEach { (t, u) ->
                        u?.destroy()
                        arFragment!!.arSceneView.scene.removeChild(u)
                    }

                    Log.e("AugmentedImageFragment", "PAUSED : $text")
                }
                TrackingState.TRACKING -> {
                    // Have to switch to UI Thread to update View.
                    Log.e("AugmentedImageFragment", "TRACKING name: " + augmentedImage.name)

                    // Create a new anchor for newly found images.
                    if (!augmentedImageMap.containsKey(augmentedImage)) {
                        Log.e("AugmentedImageFragment", "name: " + augmentedImage.name)
                        val node = AugmentedImageNode(this, augmentedImage, renderableFile)
                        node.setImage(augmentedImage)

                        augmentedImageMap[augmentedImage] = node
                        arFragment!!.arSceneView.scene.addChild(node)
                    }
                }
                TrackingState.STOPPED -> {
                    Log.e("AugmentedImageFragment", "STOPPED")
                    augmentedImageMap.remove(augmentedImage)
                }
            }
        }
    }*/
}