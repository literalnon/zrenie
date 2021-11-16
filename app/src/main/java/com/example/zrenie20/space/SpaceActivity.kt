package com.example.zrenie20.space

import android.util.Log
import com.example.zrenie20.R
import com.example.zrenie20.SettingsActivity
import com.example.zrenie20.data.*
import com.example.zrenie20.myarsample.BaseArActivity
import com.example.zrenie20.network.DataItemsService
import com.example.zrenie20.network.createService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.realm.Realm
import io.realm.RealmList


open class SpaceActivity : BaseArActivity() {
    override val layoutId: Int = R.layout.activity_my_sample_2

    init {
        assetsArray = arrayListOf<DataItemObject>()
    }
}