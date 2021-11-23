package com.example.zrenie20.data

data class TypeItemObject(
    val id: String?,
    val name: String?,
    val codeName: String?,
    val createdAt: String?,
    val updatedAt: String?
)

enum class TypeItemObjectCodeNames(val codeName: String) {
    OBJECT("object"),
    BODYPARTS("bodyParts"),
    GEO("geo"),
    SPACE("space"),
    VIDEO("video"),
    IMAGE("image"),
    LOADING("loading")
}

fun TypeItemObject.toRealmTypeItemObject(): RealmTypeItemObject {
    return RealmTypeItemObject(
        id = id,
        name = name,
        codeName = codeName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

data class TriggerItemObject(
    val id: String?,
    val typeId: String?,
    val name: String?,
    val description: String?,
    val thumbnailPath: String?,
    val filePath: String?,
    var longitude: String? = null,
    var latitude: String? = null,
    val createdAt: String?,
    val updatedAt: String?,
    val type: TypeItemObject?
)

fun TriggerItemObject.toRealmTriggerItemObject(): RealmTriggerItemObject {
    return RealmTriggerItemObject(
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
        type = type?.toRealmTypeItemObject()
    )
}

open class DataItemObject(
    val id: DataItemId?,
    val typeId: String? = null,
    val name: String? = null,
    val description: String? = null,
    val thumbnailPath: String? = null,
    val scale: String? = null,
    val filePath: String? = null,
    val dataPackageId: DataPackageId? = null,
    val triggerId: String? = null,
    val platform: String? = null,
    val isHidden: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val type: TypeItemObject? = null,
    val trigger: TriggerItemObject? = null,
    val actionUrl: String? = null,
    val offsetX: Int? = null,
    val offsetY: Int? = null,
    val offsetZ: Int? = null
)

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
        updatedAt = updatedAt,
        type = type?.toRealmTypeItemObject(),
        trigger = trigger?.toRealmTriggerItemObject(),
        actionUrl = actionUrl,
        offsetX = offsetX,
        offsetY = offsetY,
        offsetZ = offsetZ
    )
}


