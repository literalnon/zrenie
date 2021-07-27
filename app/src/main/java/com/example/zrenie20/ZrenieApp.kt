package com.example.zrenie20

import android.app.Application
import io.realm.Realm
import io.realm.RealmConfiguration

class ZrenieApp: Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)

    }
}