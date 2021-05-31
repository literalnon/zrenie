package com.example.zrenie20.myarsample

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zrenie20.R
import com.example.zrenie20.SettingsActivity
import com.example.zrenie20.base.adapters.DelegationAdapter
import com.example.zrenie20.myarsample.data.VrObject
import com.example.zrenie20.myarsample.data.VrObjectId
import com.example.zrenie20.myarsample.data.VrRenderableObject
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_my_sample.*
import kotlinx.android.synthetic.main.layout_main_activities.*


open class SpaceActivity : BaseArActivity() {
    override val layoutId: Int = R.layout.activity_my_sample

    init {
        assetsArray = arrayListOf(
            VrObject(
                id = 1,
                link = "file:///android_asset/space/s1.glb",
                name = "s1"
            ),
            VrObject(
                id = 2,
                link = "file:///android_asset/space/s2.glb",
                name = "s2"
            ),
            VrObject(
                id = 3,
                link = "file:///android_asset/space/s3.glb",
                name = "s3"
            ),
            VrObject(
                id = 4,
                link = "file:///android_asset/space/s4.glb",
                name = "s4"
            ),
            VrObject(
                id = 5,
                link = "file:///android_asset/space/s5.glb",
                name = "s5"
            ),
            VrObject(
                id = 6,
                link = "file:///android_asset/space/s6.glb",
                name = "s6"
            ),
            VrObject(
                id = 7,
                link = "file:///android_asset/space/s7.glb",
                name = "s7"
            ),
            VrObject(
                id = 8,
                link = "file:///android_asset/space/s8.glb",
                name = "s8"
            ),
            VrObject(
                id = 9,
                link = "file:///android_asset/space/s9.glb",
                name = "s9"
            ),
            VrObject(
                id = 10,
                link = "file:///android_asset/space/s10.glb",
                name = "s10"
            ),
            VrObject(
                id = 11,
                link = "file:///android_asset/space/s11a.glb",
                name = "s11a"
            ),
            VrObject(
                id = 12,
                link = "file:///android_asset/space/s12a.glb",
                name = "s12a"
            ),
            VrObject(
                id = 13,
                link = "file:///android_asset/space/s13a.glb",
                name = "s13a"
            ),
            VrObject(
                id = 14,
                link = "file:///android_asset/space/s14a.glb",
                name = "s14a"
            ),
            VrObject(
                id = 15,
                link = "file:///android_asset/space/s15a.glb",
                name = "s15a"
            )
        )
    }
}