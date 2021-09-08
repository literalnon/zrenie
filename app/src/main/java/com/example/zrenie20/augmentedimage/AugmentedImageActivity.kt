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
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.zrenie20.LibActivity
import com.example.zrenie20.R
import com.example.zrenie20.SettingsActivity
import com.google.ar.core.AugmentedImage
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

        AugmentedImageFragment()?.let { mArFragment ->
            arFragment = mArFragment
            supportFragmentManager.let {
                it.beginTransaction()
                    .replace(R.id.fragmentContainer, mArFragment)
                    .commit()
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

        ivStack?.setOnClickListener {
            startActivity(Intent(this, LibActivity::class.java))
        }
    }
}