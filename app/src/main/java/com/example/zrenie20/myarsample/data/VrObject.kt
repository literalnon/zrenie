package com.example.zrenie20.myarsample.data

typealias VrObjectId = Int

data class VrObject(
    val id: VrObjectId,
    val link: String,
    val name: String
)