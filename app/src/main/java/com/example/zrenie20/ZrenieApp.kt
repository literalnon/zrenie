package com.example.zrenie20

import android.app.Application
import android.net.ConnectivityManager
import io.realm.Realm


class ZrenieApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Realm.init(this)

    }

    init {
        instance = this
    }

    companion object {
        private var instance: ZrenieApp? = null

        fun get(): ZrenieApp? {
            return instance
        }

        fun isOnline(): Boolean {
            val cm = get()!!.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            return activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting
        }
    }



}