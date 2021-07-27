package com.example.zrenie20.splash.mvp

import com.example.zrenie20.network.DataPackageService
import com.example.zrenie20.network.createService

class SplashScreenModel {
    val service = createService(DataPackageService::class.java)

    fun getDataPackage() {

    }
}