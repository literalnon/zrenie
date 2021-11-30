package com.example.zrenie20.splash

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.zrenie20.InstructionActivity
import com.example.zrenie20.R
import com.example.zrenie20.SCREENS
import com.example.zrenie20.SettingsActivity
import com.example.zrenie20.augmentedFace.augmentedfaces.AugmentedFacesActivity
import com.example.zrenie20.augmentedimage.AugmentedImageActivity
import com.example.zrenie20.cloudAnchor2.MainActivity
import com.example.zrenie20.data.DataPackageId
import com.example.zrenie20.data.RealmDataItemObject
import com.example.zrenie20.data.RealmDataPackageObject
import com.example.zrenie20.data.toRealmDataPackageObject
import com.example.zrenie20.location.LocationActivity
import com.example.zrenie20.myarsample.BaseArActivity
import com.example.zrenie20.network.DataPackageService
import com.example.zrenie20.network.createService
import com.example.zrenie20.space.SpaceActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.realm.Realm

class SplashActivity : AppCompatActivity() {

    val service = createService(DataPackageService::class.java)
    private val subscription = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()

        val mSettings = getSharedPreferences(SettingsActivity.APP_INSTRUCTION, Context.MODE_PRIVATE)
        val isShow = mSettings.getBoolean(SettingsActivity.APP_INSTRUCTION, false)

        if (!isShow) {
            mSettings.edit().putBoolean(SettingsActivity.APP_INSTRUCTION, true).apply()

            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.instruction_dialog_title)
                .setMessage(R.string.instruction_dialog)
                .setPositiveButton(R.string.instruction_dialog_positive_button) { dialog, id ->
                    dialog.cancel()
                    startActivity(Intent(this, InstructionActivity::class.java))
                }
                .setNegativeButton(R.string.cancel) { dialog, id ->
                    dialog.cancel()
                    loadData()
                }
            builder.create()
            builder.show()
        } else {
            loadData()
        }
    }

    fun loadData() {
        subscription.add(service.getEntryTypes()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .map { list ->
                Realm.getDefaultInstance()
                    .executeTransaction { realm ->
                        realm.delete(RealmDataPackageObject::class.java)
                        realm.delete(RealmDataItemObject::class.java)

                        list.forEach { packageObject ->
                            realm.copyToRealm(packageObject.toRealmDataPackageObject())
                        }
                    }
            }
            .subscribe({
                //startActivity(Intent(this, AugmentedImageActivity::class.java))
                showDeepLinkOffer(intent)
                //startActivity(Intent(this, SpaceActivity::class.java))

                this.finish()
            }, {
                Log.e("SPLASH", "error : ${it.message}")
                //startActivity(Intent(this, SpaceActivity::class.java))
                startActivity(Intent(this, AugmentedImageActivity::class.java))
                it.printStackTrace()
            })
        )
    }
    private fun showDeepLinkOffer(intent: Intent) {

        val appLinkAction: String? = intent?.action
        val appLinkData: Uri? = intent?.data

        val packageId: DataPackageId? = appLinkData?.getQueryParameter("package")?.toLongOrNull()
        val mode = appLinkData?.host

        Log.e("DeepLink", "intent?.data : ${intent?.dataString}")
        Log.e("DeepLink", "packageId : ${packageId}, mode : ${mode}")

        BaseArActivity.checkedPackageId = packageId ?: BaseArActivity.checkedPackageId

        when (mode) {
            "image" -> {
                SettingsActivity.currentScreen = SCREENS.AUGMENTED_IMAGE
                SettingsActivity.loadAugmentedImageData(this, null)
            }
            "geo" -> {
                if (SettingsActivity.checkLocationSettings(this)) {
                    SettingsActivity.currentScreen = SCREENS.LOCATION
                    startActivity(Intent(this, LocationActivity::class.java))
                }
            }
            "bodyParts" -> {
                SettingsActivity.currentScreen = SCREENS.AUGMENTED_FACES
                startActivity(Intent(this, AugmentedFacesActivity::class.java))
            }
            "space" -> {
                SettingsActivity.currentScreen = SCREENS.SPACE
                startActivity(Intent(this, SpaceActivity::class.java))
            }
            "shared" -> {
                SettingsActivity.currentScreen = SCREENS.SHARED
                startActivity(Intent(this, MainActivity::class.java))
            }
            else -> {
                //val address = Uri.parse("zrenie2://space?package=13")
                //val openLinkIntent = Intent(Intent.ACTION_VIEW, address)
                //startActivity(openLinkIntent);

                SettingsActivity.currentScreen = SCREENS.AUGMENTED_IMAGE
                startActivity(Intent(this, AugmentedImageActivity::class.java))
            }
        }
    }
}