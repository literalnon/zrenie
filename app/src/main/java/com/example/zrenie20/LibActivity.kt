package com.example.zrenie20

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.zrenie20.base.adapters.DelegationAdapter
import com.example.zrenie20.myarsample.data.VrObject
import kotlinx.android.synthetic.main.activity_lib.*

class LibActivity : AppCompatActivity() {

    val adapter = DelegationAdapter<Any>()
    open val assetsArray = arrayListOf(
        VrObject(
            id = 0,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/1.glb",
            name = "s1"
        ),
        VrObject(
            id = 1,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/2.glb",
            name = "s2"
        ),
        VrObject(
            id = 2,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/3.glb",
            name = "s3"
        ),
        VrObject(
            id = 3,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/4.glb",
            name = "s4"
        ),
        VrObject(
            id = 4,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/5.glb",
            name = "s5"
        ),
        VrObject(
            id = 5,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/6.glb",
            name = "s6"
        ),
        VrObject(
            id = 6,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/Eagle%20fbx%20to%20glb.glb",
            name = "s7"
        ),
        VrObject(
            id = 7,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/8.glb",
            name = "s8"
        ),
        VrObject(
            id = 8,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/eagle.glb",
            name = "s9"
        ),
        VrObject(
            id = 9,
            link = "https://drive.google.com/u/0/uc?id=1GVtgZU03z9ZtDWHh7WWjw9P0Q7RErUql&export=download",
            name = "s10"
        ),
        VrObject(
            id = 11,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/11.glb",
            name = "s11"
        ),
        VrObject(
            id = 12,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/12.glb",
            name = "s12"
        ),
        VrObject(
            id = 13,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/13.glb",
            name = "s13"
        ),
        VrObject(
            id = 14,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/14.glb",
            name = "s14"
        ),
        VrObject(
            id = 15,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/15.glb",
            name = "s15"
        ),
        VrObject(
            id = 16,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/16.glb",
            name = "s16"
        ),
        VrObject(
            id = 17,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/17.glb",
            name = "s17"
        ),
        VrObject(
            id = 18,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/18.glb",
            name = "s18"
        ),
        VrObject(
            id = 19,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/19.glb",
            name = "s19"
        ),
        VrObject(
            id = 21,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/21.glb",
            name = "s20"
        ),
        VrObject(
            id = 22,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/22.glb",
            name = "s21"
        ),
        VrObject(
            id = 23,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/23.glb",
            name = "s22"
        ),
        VrObject(
            id = 24,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/24.glb",
            name = "s23"
        ),
        VrObject(
            id = 25,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/25.glb",
            name = "s24"
        ),
        VrObject(
            id = 26,
            link = "https://github.com/literalnon/AR/raw/master/app/src/main/models/26.glb",
            name = "s25"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lib)

        rvAr.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        rvAr.layoutManager = layoutManager

        adapter?.manager?.addDelegate(
            LibAdapter()
        )

        adapter.addAll(assetsArray)
    }
}