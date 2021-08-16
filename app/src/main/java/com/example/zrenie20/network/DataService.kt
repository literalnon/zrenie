package com.example.zrenie20.network

import com.example.zrenie20.data.DataItemObject
import com.example.zrenie20.data.DataPackageObject
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface DataPackageService{

    @GET("data-package")
    fun getEntryTypes(
        @Query("platform") platform: String = "android"
    ): Observable<List<DataPackageObject>>

}

interface DataItemsService{

    @GET("data-item")
    fun getEntryTypes(
        @Query ("platform") platform: String = "android"
    ): Observable<List<DataItemObject>>

}