package com.example.zrenie20

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zrenie20.base.adapters.DelegationAdapter
import com.example.zrenie20.data.DataItemObject
import com.example.zrenie20.data.DataPackageObject
import com.example.zrenie20.data.RealmDataPackageObject
import com.example.zrenie20.data.toDataPackageObject
import com.example.zrenie20.myarsample.BaseArActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_lib.*
import kotlinx.android.synthetic.main.activity_lib.rvAr

class LibActivity : AppCompatActivity() {

    val adapter = DelegationAdapter<Any>()
    open var assetsArray = arrayListOf<DataPackageObject>()
        /*
        DataItemObject(
            id = 0,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/1.glb",
            name = "s1"
        ),
        DataItemObject(
            id = 1,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/2.glb",
            name = "s2"
        ),
        DataItemObject(
            id = 2,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/3.glb",
            name = "s3"
        ),
        DataItemObject(
            id = 3,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/4.glb",
            name = "s4"
        ),
        DataItemObject(
            id = 4,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/5.glb",
            name = "s5"
        ),
        DataItemObject(
            id = 5,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/6.glb",
            name = "s6"
        ),
        DataItemObject(
            id = 6,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/Eagle%20fbx%20to%20glb.glb",
            name = "s7"
        ),
        DataItemObject(
            id = 7,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/8.glb",
            name = "s8"
        ),
        DataItemObject(
            id = 8,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/eagle.glb",
            name = "s9"
        ),
        DataItemObject(
            id = 9,
            filePath = "https://drive.google.com/u/0/uc?id=1GVtgZU03z9ZtDWHh7WWjw9P0Q7RErUql&export=download",
            name = "s10"
        ),
        DataItemObject(
            id = 11,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/11.glb",
            name = "s11"
        ),
        DataItemObject(
            id = 12,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/12.glb",
            name = "s12"
        ),
        DataItemObject(
            id = 13,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/13.glb",
            name = "s13"
        ),
        DataItemObject(
            id = 14,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/14.glb",
            name = "s14"
        ),
        DataItemObject(
            id = 15,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/15.glb",
            name = "s15"
        ),
        DataItemObject(
            id = 16,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/16.glb",
            name = "s16"
        ),
        DataItemObject(
            id = 17,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/17.glb",
            name = "s17"
        ),
        DataItemObject(
            id = 18,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/18.glb",
            name = "s18"
        ),
        DataItemObject(
            id = 19,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/19.glb",
            name = "s19"
        ),
        DataItemObject(
            id = 21,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/21.glb",
            name = "s20"
        ),
        DataItemObject(
            id = 22,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/22.glb",
            name = "s21"
        ),
        DataItemObject(
            id = 23,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/23.glb",
            name = "s22"
        ),
        DataItemObject(
            id = 24,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/24.glb",
            name = "s23"
        ),
        DataItemObject(
            id = 25,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/25.glb",
            name = "s24"
        ),
        DataItemObject(
            id = 26,
            filePath = "https://github.com/literalnon/AR/raw/master/app/src/main/models/26.glb",
            name = "s25"
        )
    )
    */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lib)

        rvAr.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        rvAr.layoutManager = layoutManager

        adapter?.manager?.addDelegate(
            LibAdapter(onSelectedItem = {
                BaseArActivity.checkedPackageId = it.id
                onBackPressed()
            })
        )

        Realm.getDefaultInstance()
            .executeTransaction { realm ->
                val packages = realm.where(RealmDataPackageObject::class.java)
                    .findAll()
                    .map { it.toDataPackageObject() }
                    .sortedBy { it.order?.toLongOrNull() }

                assetsArray = arrayListOf<DataPackageObject>().apply {
                    addAll(packages)
                }

                adapter.addAll(assetsArray)
            }

        ivBack?.setOnClickListener {
            onBackPressed()
        }
    }
}