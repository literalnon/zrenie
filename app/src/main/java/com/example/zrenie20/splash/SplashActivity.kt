package com.example.zrenie20.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.example.zrenie20.R
import com.example.zrenie20.augmentedimage.AugmentedImageActivity
import com.example.zrenie20.data.toRealmDataPackageObject
import com.example.zrenie20.location.LocationActivity
import com.example.zrenie20.network.DataPackageService
import com.example.zrenie20.network.createService
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

        subscription.add(service.getEntryTypes()
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .map { list ->
                list.forEach { packageObject ->
                    Realm.getDefaultInstance()
                        .executeTransaction { realm ->
                            realm.copyToRealm(packageObject.toRealmDataPackageObject())
                        }
                }
            }
            .subscribe({
                //startActivity(Intent(this, AugmentedImageActivity::class.java))
                startActivity(Intent(this, LocationActivity::class.java))
                this.finish()
            }, {
                it.printStackTrace()
            })
        )

        /*Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, AugmentedImageActivity::class.java))
            this.finish()
        }, 1000)*/
    }
}