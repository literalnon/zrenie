package com.example.zrenie20.data

import io.realm.RealmObject

open class RealmTypeItemObject(
    var id: String? = null,
    var name: String? = null,
    var codeName: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null
) : RealmObject()

fun RealmTypeItemObject.toTypeItemObject(): TypeItemObject {
    return TypeItemObject(
        id = id,
        name = name,
        codeName = codeName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

open class RealmTriggerItemObject(
    var id: String? = null,
    var typeId: String? = null,
    var name: String? = null,
    var description: String? = null,
    var thumbnailPath: String? = null,
    var filePath: String? = null,
    var longitude: String? = null,
    var latitude: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var type: RealmTypeItemObject? = null
) : RealmObject()

fun RealmTriggerItemObject.toTriggerItemObject(): TriggerItemObject {
    return TriggerItemObject(
        id = id,
        typeId = typeId,
        name = name,
        description = description,
        thumbnailPath = thumbnailPath,
        filePath = filePath,
        longitude = longitude,
        latitude = latitude,
        createdAt = createdAt,
        updatedAt = updatedAt,
        type = type?.toTypeItemObject()
    )
}

open class RealmDataItemObject(
    var id: DataItemId? = null,
    var typeId: String? = null,
    var name: String? = null,
    var description: String? = null,
    var thumbnailPath: String? = null,
    var scale: String? = null,
    var filePath: String? = null,
    var dataPackageId: DataPackageId? = null,
    var triggerId: String? = null,
    var platform: String? = null,
    var isHidden: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var type: RealmTypeItemObject? = null,
    var trigger: RealmTriggerItemObject? = null
) : RealmObject()

fun RealmDataItemObject.toDataItemObject(): DataItemObject {
    return DataItemObject(
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
        updatedAt = updatedAt,
        type = type?.toTypeItemObject(),
        trigger = trigger?.toTriggerItemObject(),
    )
}
