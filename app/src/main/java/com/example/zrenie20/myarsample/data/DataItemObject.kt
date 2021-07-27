package com.example.zrenie20.myarsample.data

import com.example.zrenie20.data.*

data class DataItemObject(
    val id: DataItemId,
    val name: String? = null,
    val typeId: TypeId? = null,
    val description: String? = null,
    val thumbnailPath: String? = null,
    val scale: String? = null,
    val filePath: String,
    val dataPackageId: DataPackageId? = null,
    val triggerId: TriggerId? = null,
    val platform: String? = null,
    val isHidden: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

fun RealmDataItemObject.toDataItemObject(): DataItemObject {
    return DataItemObject(
        id = id!!,
        typeId = typeId,
        name = name,
        description = description,
        thumbnailPath = thumbnailPath,
        scale = scale,
        filePath = filePath!!,
        dataPackageId = dataPackageId,
        triggerId = triggerId,
        platform = platform,
        isHidden = isHidden,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun DataItemObject.toRealmDataItemObject(): RealmDataItemObject {
    return RealmDataItemObject(
        id = id,
        typeId = typeId,
        name = name,
        description = description,
        thumbnailPath = thumbnailPath,
        scale = scale,
        filePath = filePath,
        dataPackageId = dataPackageId,
        triggerId = triggerId,
        platform = platform,
        isHidden = isHidden,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
