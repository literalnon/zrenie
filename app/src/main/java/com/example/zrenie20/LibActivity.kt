package com.example.zrenie20

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.zrenie20.augmentedimage.AugmentedImageActivity
import com.example.zrenie20.augmentedimage.AugmentedImageFragment
import com.example.zrenie20.base.adapters.DelegationAdapter
import com.example.zrenie20.data.*
import com.example.zrenie20.myarsample.BaseArActivity
import com.example.zrenie20.network.DataItemsService
import com.example.zrenie20.network.createService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_lib.*
import kotlinx.android.synthetic.main.activity_lib.ivBack
import kotlinx.android.synthetic.main.activity_lib.rvAr
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.IOException
import java.util.ArrayList

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
                if (SettingsActivity.currentScreen == SCREENS.AUGMENTED_IMAGE) {
                    loadAugmentedImageData()
                } else {
                    onBackPressed()
                }
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

    fun getImageData(assetsArray: ArrayList<DataItemObject>) {
        AugmentedImageFragment.bitmaps.clear()

        Log.e(SettingsActivity.TAG, "getImageData assetsArray: ${assetsArray.size}")

        val nonEmptyAssetsArray = assetsArray.filter { itemObject ->
            itemObject.trigger?.filePath?.isNotEmpty() == true
        }

        AugmentedImageFragment.assetsArray.clear()
        AugmentedImageFragment.assetsArray.addAll(nonEmptyAssetsArray)

        nonEmptyAssetsArray.forEach { itemObject ->
            try {
                Log.e(SettingsActivity.TAG, "getImageData trigger: ${itemObject.trigger}")
                Log.e(SettingsActivity.TAG, "getImageData filePath: ${itemObject.trigger?.filePath}")

                Glide.with(this)
                    .asBitmap()
                    .load(itemObject.trigger?.filePath)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            Log.e(SettingsActivity.TAG, "getImageData onResourceReady IVideoArFragment.bitmaps : ${AugmentedImageFragment.bitmaps.size}")
                            AugmentedImageFragment.bitmaps[itemObject.trigger?.filePath!!] = resource
                            if (AugmentedImageFragment.bitmaps.size == nonEmptyAssetsArray.size) {
                                startActivity(
                                    Intent(this@LibActivity, AugmentedImageActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            Log.e(SettingsActivity.TAG, "getImageData onLoadCleared IVideoArFragment.bitmaps : ${AugmentedImageFragment.bitmaps.size}")
                        }
                    })
            } catch (e: IOException) {
                Log.e(SettingsActivity.TAG, "IO exception loading augmented image bitmap.", e)
            }
        }

        llProgress?.visibility = View.GONE
    }

    open fun loadAugmentedImageData() {
        llProgress?.visibility = View.VISIBLE
        var assetsArray = arrayListOf<DataItemObject>()

        val isNeedFilterTrigger = true

        val service = createService(DataItemsService::class.java)

        Log.e(SettingsActivity.TAG, "loadData")

        Realm.getDefaultInstance()
            .executeTransaction { realm ->
                val packages = realm.where(RealmDataPackageObject::class.java)
                    .findAll()
                    .map { it.toDataPackageObject() }
                    .sortedBy { it.order?.toLongOrNull() }

                val activePackage = if (BaseArActivity.checkedPackageId == null) {
                    val ap = packages.firstOrNull()
                    BaseArActivity.checkedPackageId = ap?.id
                    ap
                } else {
                    packages.firstOrNull {
                        it.id == BaseArActivity.checkedPackageId
                    }
                }

                Log.e(
                    SettingsActivity.TAG,
                    "loadData 1 activePackage?.dataItems?.isNotEmpty() : ${activePackage?.dataItems?.isNotEmpty()}"
                )


                val items = realm.where(RealmDataItemObject::class.java)
                    .equalTo("dataPackageId", activePackage?.id)

                var dataItems = items.findAll()
                    .map { it.toDataItemObject() }

                if (isNeedFilterTrigger) {
                    //items.equalTo("triggerId", SettingsActivity.currentScreen.type.id)
                    dataItems =
                        dataItems.filter { it.trigger?.typeId == SettingsActivity.currentScreen.type.id }
                }
                Log.e(
                    SettingsActivity.TAG,
                    "loadData 11 dataItems : ${dataItems.isNotEmpty()}, ${dataItems.count()}"
                )

                if (dataItems.isNotEmpty()) {
                    assetsArray = arrayListOf<DataItemObject>().apply {
                        addAll(dataItems)
                    }

                    getImageData(assetsArray)

                    return@executeTransaction
                }

                val observable =
                    /*Observable.fromIterable<DataPackageObject>(packages)
                    .flatMap { packageObject ->*/
                    service.getEntryTypes()//packageObject.id.toString()
                        //}
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ items ->
                            Log.e(
                                SettingsActivity.TAG,
                                "subscribe 2 checkedPackageId : ${BaseArActivity.checkedPackageId}, SettingsActivity.currentScreen.type.id : ${SettingsActivity.currentScreen.type.id}"
                            )
                            Log.e(SettingsActivity.TAG, "subscribe 3 items : ${items.count()}")
                            Log.e(
                                SettingsActivity.TAG,
                                "subscribe 3 1 items : ${items.map { it?.triggerId }}"
                            )
                            val currentPackageItems = items
                                .filter {
                                    it?.dataPackageId == BaseArActivity.checkedPackageId && if (isNeedFilterTrigger) {
                                        it?.trigger?.type?.id == SettingsActivity.currentScreen.type.id
                                    } else {
                                        true
                                    }
                                }

                            Log.e(
                                SettingsActivity.TAG,
                                "subscribe 3 currentPackageItems : ${currentPackageItems.count()}"
                            )

                            if (currentPackageItems.isNotEmpty()) {
                                assetsArray = arrayListOf<DataItemObject>().apply {
                                    addAll(currentPackageItems)
                                }
                            }

                            realm
                                .executeTransaction { realm ->

                                    realm.delete(RealmDataItemObject::class.java)

                                    items.map {
                                        realm.copyToRealm(it.toRealmDataItemObject())
                                    }
                                }

                            getImageData(assetsArray)

                        }, {
                            Log.e(SettingsActivity.TAG, "subscribe 4 error : ${it.message}")
                            llProgress?.visibility = View.GONE
                            Toast.makeText(this, "error load data from network : ${it.message}", Toast.LENGTH_LONG).show()
                            it.printStackTrace()
                        })
            }
    }
}