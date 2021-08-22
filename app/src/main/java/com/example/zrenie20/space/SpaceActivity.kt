package com.example.zrenie20.space

import android.util.Log
import com.example.zrenie20.R
import com.example.zrenie20.SettingsActivity
import com.example.zrenie20.data.*
import com.example.zrenie20.myarsample.BaseArActivity
import com.example.zrenie20.network.DataItemsService
import com.example.zrenie20.network.createService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmList


open class SpaceActivity : BaseArActivity() {
    override val layoutId: Int = R.layout.activity_my_sample

    init {
        /*assetsArray = arrayListOf(
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
        )*/

        assetsArray = arrayListOf<DataItemObject>()
        /*DataItemObject(
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
    )*/
    }

    /*override fun loadData() {
        val service = createService(DataItemsService::class.java)

        Log.e("FileDownloadManager", "loadData")

        Realm.getDefaultInstance()
            .executeTransaction { realm ->
                val packages = realm.where(RealmDataPackageObject::class.java)
                    .findAll()
                    .map { it.toDataPackageObject() }
                    .sortedBy { it.order?.toLongOrNull() }

                val activePackage = if (checkedPackageId == null) {
                    val ap = packages.firstOrNull()
                    checkedPackageId = ap?.id
                    ap
                } else {
                    packages.firstOrNull {
                        it.id == checkedPackageId
                    }
                }

                Log.e(
                    "FileDownloadManager",
                    "loadData 1 activePackage?.dataItems?.isNotEmpty() : ${activePackage?.dataItems?.isNotEmpty()}"
                )

                val dataItems = realm.where(RealmDataItemObject::class.java)
                    .equalTo("dataPackageId", activePackage?.id)
                    .equalTo("triggerId", SettingsActivity.currentScreen.type.id)
                    .findAll()

                Log.e(
                    "FileDownloadManager",
                    "loadData 11 dataItems : ${dataItems.isNotEmpty()}, ${dataItems.count()}"
                )

                if (dataItems.isNotEmpty()) {
                    assetsArray = arrayListOf<DataItemObject>().apply {
                        addAll(dataItems.map { it.toDataItemObject() })
                    }

                    adapter.replaceAll(assetsArray)

                    return@executeTransaction
                }

                val observable =
                    *//*Observable.fromIterable<DataPackageObject>(packages)
                    .flatMap { packageObject ->*//*
                    service.getEntryTypes()//packageObject.id.toString()
                        //}
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ items ->
                            Log.e(
                                "FileDownloadManager",
                                "subscribe 2 checkedPackageId : ${checkedPackageId}, SettingsActivity.currentScreen.type.id : ${SettingsActivity.currentScreen.type.id}"
                            )
                            Log.e("FileDownloadManager", "subscribe 3 items : ${items.count()}")
                            Log.e("FileDownloadManager", "subscribe 3 1 items : ${items.map { it?.triggerId  }}")
                            val currentPackageItems = items
                                .filter {
                                    it?.dataPackageId == checkedPackageId &&
                                            it?.triggerId == SettingsActivity.currentScreen.type.id
                                }

                            Log.e("FileDownloadManager", "subscribe 3 currentPackageItems : ${currentPackageItems.count()}")

                            if (currentPackageItems.isNotEmpty()) {
                                assetsArray = arrayListOf<DataItemObject>().apply {
                                    addAll(currentPackageItems)
                                }
                                adapter.replaceAll(assetsArray)
                            }

                            realm
                                .executeTransaction { realm ->

                                    realm.delete(RealmDataItemObject::class.java)

                                    items.map {
                                        realm.copyToRealm(it.toRealmDataItemObject())
                                    }
                                }
                        }, {
                            Log.e("FileDownloadManager", "subscribe 4 error : ${it.message}")

                            it.printStackTrace()
                        })


                *//*assetsArray = arrayListOf<DataItemObject>().apply {
                    addAll(firstPackage
                        ?.dataItems
                        ?.map {
                            it.toDataItemObject()
                        } ?: listOf()
                    )
                }*//*

                //adapter.addAll(assetsArray)
            }
    }*/
}