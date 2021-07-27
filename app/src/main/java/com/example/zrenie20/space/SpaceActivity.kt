package com.example.zrenie20.space

import com.example.zrenie20.R
import com.example.zrenie20.data.RealmDataPackageObject
import com.example.zrenie20.myarsample.BaseArActivity
import com.example.zrenie20.myarsample.data.DataItemObject
import com.example.zrenie20.myarsample.data.toDataItemObject
import io.realm.Realm


open class SpaceActivity : BaseArActivity() {
    override val layoutId: Int = R.layout.activity_my_sample

    init {
        assetsArray = arrayListOf(
            DataItemObject(
                id = 1,
                filePath = "file:///android_asset/space/s1.glb",
                name = "s1"
            ),
            DataItemObject(
                id = 2,
                filePath = "file:///android_asset/space/s2.glb",
                name = "s2"
            ),
            DataItemObject(
                id = 3,
                filePath = "file:///android_asset/space/s3.glb",
                name = "s3"
            ),
            DataItemObject(
                id = 4,
                filePath = "file:///android_asset/space/s4.glb",
                name = "s4"
            ),
            DataItemObject(
                id = 5,
                filePath = "file:///android_asset/space/s5.glb",
                name = "s5"
            ),
            DataItemObject(
                id = 6,
                filePath = "file:///android_asset/space/s6.glb",
                name = "s6"
            ),
            DataItemObject(
                id = 7,
                filePath = "file:///android_asset/space/s7.glb",
                name = "s7"
            ),
            DataItemObject(
                id = 8,
                filePath = "file:///android_asset/space/s8.glb",
                name = "s8"
            ),
            DataItemObject(
                id = 9,
                filePath = "file:///android_asset/space/s9.glb",
                name = "s9"
            ),
            DataItemObject(
                id = 10,
                filePath = "file:///android_asset/space/s10.glb",
                name = "s10"
            ),
            DataItemObject(
                id = 11,
                filePath = "file:///android_asset/space/s11a.glb",
                name = "s11a"
            ),
            DataItemObject(
                id = 12,
                filePath = "file:///android_asset/space/s12a.glb",
                name = "s12a"
            ),
            DataItemObject(
                id = 13,
                filePath = "file:///android_asset/space/s13a.glb",
                name = "s13a"
            ),
            DataItemObject(
                id = 14,
                filePath = "file:///android_asset/space/s14a.glb",
                name = "s14a"
            ),
            DataItemObject(
                id = 15,
                filePath = "file:///android_asset/space/s15a.glb",
                name = "s15a"
            )
        )
    }

    override fun loadData() {
        Realm.getDefaultInstance()
            .executeTransaction { realm ->
                val firstPackage: RealmDataPackageObject? = realm.where(RealmDataPackageObject::class.java)
                    .findFirst()

                assetsArray = arrayListOf<DataItemObject>().apply {
                    addAll(firstPackage
                        ?.dataItems
                        ?.map {
                            it.toDataItemObject()
                        } ?: listOf()
                    )
                }

                adapter.addAll(assetsArray)
            }
    }
}