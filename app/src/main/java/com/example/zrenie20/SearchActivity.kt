package com.example.zrenie20

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.zrenie20.SettingsActivity.Companion.checkLocationSettings
import com.example.zrenie20.SettingsActivity.Companion.loadAugmentedImageData
import com.example.zrenie20.augmentedFace.augmentedfaces.AugmentedFacesActivity
import com.example.zrenie20.base.adapters.DelegationAdapter
import com.example.zrenie20.cloudAnchor2.MainActivity
import com.example.zrenie20.data.*
import com.example.zrenie20.location.LocationActivity
import com.example.zrenie20.myarsample.BaseArActivity
import com.example.zrenie20.space.FileDownloadManager
import com.example.zrenie20.space.SpaceActivity
import com.tsuryo.swipeablerv.SwipeLeftRightCallback
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.activity_search.ivBack
import kotlinx.android.synthetic.main.activity_search.rvAr
import java.io.File

class SearchActivity : AppCompatActivity() {
    val adapter = DelegationAdapter<Any>()
    open var assetsArray = arrayListOf<Any>()
    val fileDownloadManager = FileDownloadManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val allFiles = fileDownloadManager.getAllFiles(this)

        ivBack?.setOnClickListener {
            onBackPressed()
        }

        rvAr.adapter = adapter
        val layoutManager = LinearLayoutManager(this)
        rvAr.layoutManager = layoutManager

        rvAr.setListener(object : SwipeLeftRightCallback.Listener {
            override fun onSwipedLeft(position: Int) {
                val item = adapter.items.getOrNull(position)

                if (item is DataItemObject && item.filePath != null) {
                    fileDownloadManager.removeFile(
                        filePath = item.filePath
                    )

                    Realm.getDefaultInstance()
                        .executeTransaction { realm ->
                            realm.where(RealmDataItemObject::class.java)
                                .findAll()
                                .filter { it.id == item.id }
                                .forEach {
                                    it.deleteFromRealm()
                                }

                        }
                }

                adapter.remove(position)
                adapter.notifyDataSetChanged()
            }

            override fun onSwipedRight(position: Int) {
                val item = adapter.items.getOrNull(position)

                if (item is DataItemObject && item.filePath != null) {
                    fileDownloadManager.removeFile(
                        filePath = item.filePath
                    )

                    Realm.getDefaultInstance()
                        .executeTransaction { realm ->
                            realm.where(RealmDataItemObject::class.java)
                                .findAll()
                                .filter { it.id == item.id }
                                .forEach {
                                    it.deleteFromRealm()
                                }

                        }
                }

                adapter.remove(position)
                adapter.notifyDataSetChanged()
            }
        });

        rvAr.setRightText("Delete")

        adapter?.manager?.addDelegate(
            LibAdapter(onSelectedItem = {

            })
        )

        adapter?.manager?.addDelegate(
            LibDataItemObjectAdapter(
                onSelectedItem = {
                    BaseArActivity.checkedPackageId = it.dataPackageId

                    when (it.trigger?.type?.codeName) {
                        TypeItemObjectCodeNames.GEO.codeName -> {
                            if (checkLocationSettings(this)) {
                                SettingsActivity.currentScreen = SCREENS.LOCATION
                                startActivity(Intent(this, LocationActivity::class.java))
                            }
                        }
                        TypeItemObjectCodeNames.BODYPARTS.codeName -> {
                            SettingsActivity.currentScreen = SCREENS.AUGMENTED_FACES
                            startActivity(Intent(this, AugmentedFacesActivity::class.java))
                        }
                        TypeItemObjectCodeNames.SPACE.codeName -> {
                            SettingsActivity.currentScreen = SCREENS.SPACE
                            startActivity(Intent(this, SpaceActivity::class.java))
                        }
                        TypeItemObjectCodeNames.IMAGE.codeName -> {
                            SettingsActivity.currentScreen = SCREENS.AUGMENTED_IMAGE
                            loadAugmentedImageData(this, null)
                        }
                        TypeItemObjectCodeNames.SHARE.codeName -> {
                            SettingsActivity.currentScreen = SCREENS.SHARED
                            startActivity(Intent(this, MainActivity::class.java))
                        }
                    }
                },
                onSelectedIcon = {
                    if (it.trigger?.type?.codeName == ArTypes.ArGeoType().codeName) {
                        val gmmIntentUri: Uri =
                            Uri.parse("google.navigation:q=${it.trigger.latitude},${it.trigger.longitude}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps")
                        startActivity(mapIntent)
                    }
                }
            )
        )

        val mSettings = getSharedPreferences(SettingsActivity.APP_PREFERENCES, Context.MODE_PRIVATE)
        val lastModified = mSettings.getLong(SettingsActivity.APP_PREFERENCES_CHECKED, 0L)

        //if (lastModified > 0) {
        Realm.getDefaultInstance()
            .executeTransaction { realm ->
                val objects = realm.where(RealmDataItemObject::class.java)
                    .findAll()
                    .map { it.toDataItemObject() }
                    .filter { dataItemObj ->
                        val file = allFiles?.firstOrNull {
                            it.contains(
                                dataItemObj
                                    .filePath
                                    ?.split("/")
                                    ?.lastOrNull() ?: " "
                            )
                        }
                        file != null && (lastModified == 0L || File(file)?.lastModified() < lastModified)
                    }

                assetsArray.clear()

                assetsArray.addAll(objects)

                adapter.replaceAll(assetsArray)
            }
        //}

        //mAlphaMovie2?.setVideoFromAssets("ball.mp4")
        //mAlphaMovie2?.

        val fileDownloadManager = FileDownloadManager()

        Log.e("MainActivity", "0 : ${lastModified}, ${lastModified > 0}")

        tvRemoveArMb.text =
            if (assetsArray.isNotEmpty()) {
                fileDownloadManager
                    .getAllSize(this)
                    .toString()
            } else {
              "0"
            } + " MB"

        tvRemoveCache?.setOnClickListener {
            fileDownloadManager?.removeAllFiles(this)

            tvRemoveArMb.text = ""

            onBackPressed()
        }
    }
}