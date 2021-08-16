package com.example.zrenie20

import android.app.Application
import android.os.Environment
import android.util.Log
import io.realm.Realm
import java.io.File


class ZrenieApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)

    }
}