package com.example.zrenie20

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.example.zrenie20.space.FileDownloadManager
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_lib.*
import kotlinx.android.synthetic.main.activity_lib.ivBack
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.IOException
import java.util.*

class LibActivity : AppCompatActivity() {

    val adapter = DelegationAdapter<Any>()
    open var assetsArray = arrayListOf<Any>()
    val fileDownloadManager = FileDownloadManager()

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

        adapter?.manager?.addDelegate(
            LibDataItemObjectAdapter(onSelectedItem = {
                //BaseArActivity.checkedPackageId = it.id
                /*if (SettingsActivity.currentScreen == SCREENS.AUGMENTED_IMAGE) {
                    loadAugmentedImageData()
                } else {
                    onBackPressed()
                }*/
                if (it.trigger?.type?.codeName == ArTypes.ArGeoType().codeName) {
                    val gmmIntentUri: Uri = Uri.parse("google.navigation:q=${it.trigger.latitude},${it.trigger.longitude}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    startActivity(mapIntent)
                }
            })
        )

        val allFiles = fileDownloadManager.getAllFiles(this)

        imageView4?.setOnClickListener {
            Realm.getDefaultInstance()
                .executeTransaction { realm ->
                    val objects = realm.where(RealmDataPackageObject::class.java)
                        .findAll()
                        .map { it.toDataPackageObject() }
                        .filter { packageItem ->
                            realm.where(RealmDataItemObject::class.java)
                                .findAll()
                                .filter { it.dataPackageId == packageItem.id }
                                .find { it.trigger?.type?.codeName == ArTypes.ArImageType().codeName } != null
                        }
                        /*.filter { dataItemObj ->
                            allFiles?.filter {
                                it.contains(dataItemObj.filePath?.split("/")?.lastOrNull() ?: " ")
                            }?.isNotEmpty() == true
                        }*/

                    assetsArray.clear()

                    assetsArray.addAll(objects)

                    adapter.replaceAll(assetsArray)
                }
        }

        imageView3?.setOnClickListener {
            Realm.getDefaultInstance()
                .executeTransaction { realm ->
                    val objects = realm.where(RealmDataItemObject::class.java)
                        .findAll()
                        .map { it.toDataItemObject() }
                        .filter {
                            it.trigger?.type?.codeName == ArTypes.ArGeoType().codeName
                        }
                        /*.filter { dataItemObj ->
                            allFiles?.filter {
                                it.contains(dataItemObj.filePath?.split("/")?.lastOrNull() ?: " ")
                            }?.isNotEmpty() == true
                        }*/

                    assetsArray.clear()

                    assetsArray.addAll(objects)

                    adapter.replaceAll(assetsArray)
                }
        }

        imageView2?.setOnClickListener {
            Realm.getDefaultInstance()
                .executeTransaction { realm ->
                    val objects = realm.where(RealmDataPackageObject::class.java)
                        .findAll()
                        .map { it.toDataPackageObject() }
                        .filter { packageItem ->
                            realm.where(RealmDataItemObject::class.java)
                                .findAll()
                                .filter { it.dataPackageId == packageItem.id }
                                .find { it.trigger?.type?.codeName == ArTypes.ArFaceType().codeName } != null
                        }

                    assetsArray.clear()

                    assetsArray.addAll(objects)

                    adapter.replaceAll(assetsArray)
                }
        }

        imageView1?.setOnClickListener {
            Realm.getDefaultInstance()
                .executeTransaction { realm ->
                    val objects = realm.where(RealmDataPackageObject::class.java)
                        .findAll()
                        .map { it.toDataPackageObject() }
                        .filter { packageItem ->
                            realm.where(RealmDataItemObject::class.java)
                                .findAll()
                                .filter { it.dataPackageId == packageItem.id }
                                .find { it.trigger?.type?.codeName == ArTypes.ArOSpaceType().codeName } != null
                        }

                    assetsArray.clear()

                    assetsArray.addAll(objects)

                    adapter.replaceAll(assetsArray)

                    /*ArImageType()
                    ArObjectType()
                    ArFaceType()
                    ArGeoType()
                    ArOSpaceType()*/

                }
        }

        Realm.getDefaultInstance()
            .executeTransaction { realm ->
                val packages = realm.where(RealmDataPackageObject::class.java)
                    .findAll()
                    .map { it.toDataPackageObject() }
                    .sortedBy { it.order?.toLongOrNull() }

                assetsArray = arrayListOf<Any>().apply {
                    addAll(packages)
                }

                adapter.replaceAll(assetsArray)
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
                Log.e(
                    SettingsActivity.TAG,
                    "getImageData filePath: ${itemObject.trigger?.filePath}"
                )

                Glide.with(this)
                    .asBitmap()
                    .load(itemObject.trigger?.filePath)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            Log.e(
                                SettingsActivity.TAG,
                                "getImageData onResourceReady IVideoArFragment.bitmaps : ${AugmentedImageFragment.bitmaps.size}"
                            )
                            AugmentedImageFragment.bitmaps[itemObject.trigger?.filePath!!] =
                                resource
                            if (AugmentedImageFragment.bitmaps.size == nonEmptyAssetsArray.size) {
                                startActivity(
                                    Intent(this@LibActivity, AugmentedImageActivity::class.java)
                                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            Log.e(
                                SettingsActivity.TAG,
                                "getImageData onLoadCleared IVideoArFragment.bitmaps : ${AugmentedImageFragment.bitmaps.size}"
                            )
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
                            Toast.makeText(
                                this,
                                "error load data from network : ${it.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            it.printStackTrace()
                        })
            }
    }
}