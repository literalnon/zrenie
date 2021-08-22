package com.example.zrenie20.data

import io.realm.RealmList
import io.realm.RealmObject

typealias DataPackageId = Long
typealias DataItemId = Long
typealias TriggerId = Long
typealias TypeId = Long

data class DataPackageObject(
    var id: DataPackageId,
    var name: String,
    var description: String?,
    var thumbnailPath: String?,
    var order: String?,
    var isHidden: Boolean?,
    var createdAt: String?,
    var updatedAt: String?,
    var dataItems: List<DataItemObject>? = null
)

fun RealmDataPackageObject.toDataPackageObject(): DataPackageObject {
    return DataPackageObject(
        id = id!!,
        name = name!!,
        description = description,
        thumbnailPath = thumbnailPath,
        order = order,
        isHidden = isHidden,
        createdAt = createdAt,
        updatedAt = updatedAt,
        dataItems = dataItems?.map { it.toDataItemObject() }
    )
}

open class RealmDataPackageObject(
    var id: DataPackageId? = null,
    var name: String? = null,
    var description: String? = null,
    var thumbnailPath: String? = null,
    var order: String? = null,
    var isHidden: Boolean? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var dataItems: RealmList<RealmDataItemObject>? = null
) : RealmObject()

fun DataPackageObject.toRealmDataPackageObject(): RealmDataPackageObject {
    return RealmDataPackageObject(
        id = id,
        name = name,
        description = description,
        thumbnailPath = thumbnailPath,
        order = order,
        isHidden = isHidden,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
