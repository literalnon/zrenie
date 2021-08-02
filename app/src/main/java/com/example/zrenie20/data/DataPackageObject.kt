package com.example.zrenie20.data

import com.example.zrenie20.myarsample.data.DataItemObject
import com.example.zrenie20.myarsample.data.toRealmDataItemObject
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
    var isHidden: Boolean,
    var createdAt: String?,
    var updatedAt: String?,
    var dataItems: List<DataItemObject> = HashMap
)

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
        updatedAt = updatedAt,
        dataItems = RealmList<RealmDataItemObject>().apply {
            addAll(dataItems.map { it.toRealmDataItemObject() })
        }
    )
}

/*
data class DataItemObject(
    val id: DataItemId?,
    val typeId: TypeId?,
    val name: String?,
    val description: String?,
    val thumbnailPath: String?,
    val scale: String?,
    val filePath: String?,
    val dataPackageId: DataPackageId?,
    val triggerId: TriggerId?,
    val platform: String?,
    val isHidden: String?,
    val createdAt: String?,
    val updatedAt: String?
)
*/

open class RealmDataItemObject(
    var id: DataItemId? = null,
    var typeId: TypeId? = null,
    var name: String? = null,
    var description: String? = null,
    var thumbnailPath: String? = null,
    var scale: String? = null,
    var filePath: String? = null,
    var dataPackageId: DataPackageId? = null,
    var triggerId: TriggerId? = null,
    var platform: String? = null,
    var isHidden: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null
) : RealmObject()
