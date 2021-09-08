package com.example.zrenie20

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.zrenie20.augmentedFace.augmentedfaces.AugmentedFacesActivity
import com.example.zrenie20.augmentedimage.AugmentedImageActivity
import com.example.zrenie20.augmentedimage.AugmentedImageFragment
import com.example.zrenie20.data.*
import com.example.zrenie20.location.LocationActivity
import com.example.zrenie20.myarsample.BaseArActivity
import com.example.zrenie20.network.DataItemsService
import com.example.zrenie20.network.createService
import com.example.zrenie20.space.SpaceActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_settings.*
import java.io.IOException
import java.util.ArrayList

enum class SCREENS(val type: ArTypes) {
    SPACE(type = ArTypes.ArOSpaceType()),
    AUGMENTED_FACES(type = ArTypes.ArFaceType()),
    AUGMENTED_IMAGE(type = ArTypes.ArImageType()),
    LOCATION(type = ArTypes.ArGeoType())
}

class SettingsActivity : AppCompatActivity() {

    companion object {
        var currentScreen: SCREENS = SCREENS.AUGMENTED_IMAGE
        val TAG = "SettingsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        llSpace?.setOnClickListener {
            currentScreen = SCREENS.SPACE
            startActivity(Intent(this, SpaceActivity::class.java))
        }

        llBodyPart?.setOnClickListener {
            currentScreen = SCREENS.AUGMENTED_FACES
            startActivity(Intent(this, AugmentedFacesActivity::class.java))
        }

        llAugmentedImage?.setOnClickListener {
            currentScreen = SCREENS.AUGMENTED_IMAGE
            loadAugmentedImageData()
            //startActivity(Intent(this, AugmentedImageActivity::class.java))
        }

        llLocation?.setOnClickListener {
            currentScreen = SCREENS.LOCATION
            startActivity(Intent(this, LocationActivity::class.java))
        }

        tvLibAr?.setOnClickListener {
            startActivity(Intent(this, SearchActivity::class.java))
        }

        ivSpaceSelected.visibility = View.GONE
        tvSpace.setTextColor(Color.WHITE)
        ivSpace.setColorFilter(Color.WHITE)

        ivLocationSelected.visibility = View.GONE
        tvLocation.setTextColor(Color.WHITE)
        ivLocation.setColorFilter(Color.WHITE)

        ivAugmentedImageSelected.visibility = View.GONE
        tvAugmentedImage.setTextColor(Color.WHITE)
        ivAugmentedImage.setColorFilter(Color.WHITE)

        ivBodyPartSelected.visibility = View.GONE
        tvBodyPart.setTextColor(Color.WHITE)
        ivBodyPart.setColorFilter(Color.WHITE)

        ivBack?.setOnClickListener {
            onBackPressed()
        }

        val selectedColor = ContextCompat.getColor(this, R.color.selectedColor)

        when (currentScreen) {
            SCREENS.SPACE -> {
                tvSpace.setTextColor(selectedColor)
                ivSpace.setColorFilter(selectedColor)
                ivSpaceSelected.visibility = View.VISIBLE
            }
            SCREENS.AUGMENTED_FACES -> {
                tvBodyPart.setTextColor(selectedColor)
                ivBodyPart.setColorFilter(selectedColor)
                ivBodyPartSelected.visibility = View.VISIBLE
            }
            SCREENS.AUGMENTED_IMAGE -> {
                tvAugmentedImage.setTextColor(selectedColor)
                ivAugmentedImage.setColorFilter(selectedColor)
                ivAugmentedImageSelected.visibility = View.VISIBLE
            }
            SCREENS.LOCATION -> {
                tvLocation.setTextColor(selectedColor)
                ivLocation.setColorFilter(selectedColor)
                ivLocationSelected.visibility = View.VISIBLE
            }
        }
    }

    fun getImageData(assetsArray: ArrayList<DataItemObject>) {
        AugmentedImageFragment.bitmaps.clear()

        Log.e(TAG, "getImageData assetsArray: ${assetsArray.size}")

        val nonEmptyAssetsArray = assetsArray.filter { itemObject ->
            itemObject.trigger?.filePath?.isNotEmpty() == true
        }

        AugmentedImageFragment.assetsArray.clear()
        AugmentedImageFragment.assetsArray.addAll(nonEmptyAssetsArray)

        nonEmptyAssetsArray.forEach { itemObject ->
            try {
                Log.e(TAG, "getImageData trigger: ${itemObject.trigger}")
                Log.e(TAG, "getImageData filePath: ${itemObject.trigger?.filePath}")

                Glide.with(this)
                    .asBitmap()
                    .load(itemObject.trigger?.filePath)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            Log.e(TAG, "getImageData onResourceReady IVideoArFragment.bitmaps : ${AugmentedImageFragment.bitmaps.size}")
                            AugmentedImageFragment.bitmaps[itemObject.trigger?.filePath!!] = resource
                            if (AugmentedImageFragment.bitmaps.size == nonEmptyAssetsArray.size) {
                                startActivity(Intent(this@SettingsActivity, AugmentedImageActivity::class.java))
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            Log.e(TAG, "getImageData onLoadCleared IVideoArFragment.bitmaps : ${AugmentedImageFragment.bitmaps.size}")
                        }
                    })
            } catch (e: IOException) {
                Log.e(TAG, "IO exception loading augmented image bitmap.", e)
            }
        }

        llProgress?.visibility = View.GONE
    }

    open fun loadAugmentedImageData() {
        llProgress?.visibility = View.VISIBLE
        var assetsArray = arrayListOf<DataItemObject>()

        val isNeedFilterTrigger = true

        val service = createService(DataItemsService::class.java)

        Log.e(TAG, "loadData")

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
                    TAG,
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
                    TAG,
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
                                TAG,
                                "subscribe 2 checkedPackageId : ${BaseArActivity.checkedPackageId}, SettingsActivity.currentScreen.type.id : ${SettingsActivity.currentScreen.type.id}"
                            )
                            Log.e(TAG, "subscribe 3 items : ${items.count()}")
                            Log.e(
                                TAG,
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
                                TAG,
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
                            Log.e(TAG, "subscribe 4 error : ${it.message}")
                            llProgress?.visibility = View.GONE
                            Toast.makeText(this, "error load data from network : ${it.message}", Toast.LENGTH_LONG).show()
                            it.printStackTrace()
                        })


                /*assetsArray = arrayListOf<DataItemObject>().apply {
                    addAll(firstPackage
                        ?.dataItems
                        ?.map {
                            it.toDataItemObject()
                        } ?: listOf()
                    )
                }*/

                //adapter.addAll(assetsArray)
            }
    }
}