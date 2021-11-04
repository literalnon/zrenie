package com.example.zrenie20.binakular.mvp

import com.example.zrenie20.network.DataItemsService
import com.example.zrenie20.network.createService

class SpaceScreenModel {
    val service = createService(DataItemsService::class.java)

    fun getDataPackage() {

    }
}