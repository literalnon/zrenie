package com.example.zrenie20

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.zrenie20.augmentedimage.AugmentedImageActivity
import com.example.zrenie20.myarsample.SpaceActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, AugmentedImageActivity::class.java))
            this.finish()
        }, 1000)
    }
}