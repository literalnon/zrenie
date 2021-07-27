package com.example.zrenie20.network

import com.example.zrenie20.data.DataPackageObject
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

interface DataPackageService{

    @GET("data-package")
    fun getEntryTypes(): Observable<List<DataPackageObject>>

}

interface DataItemsService{

    @GET("data-package")
    fun getEntryTypes(): Observable<List<DataPackageObject>>

}