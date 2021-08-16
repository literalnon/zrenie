package com.example.zrenie20.data

sealed class ArTypes {
    abstract val id: String
    abstract val name: String
    abstract val codeName: String

    class ArImageType : ArTypes() {
        override val id: String = "1"
        override val name: String = "Изображение"
        override val codeName: String = "image"
    }

    class ArObjectType(): ArTypes() {
        override val id: String = "2"
        override val name: String = "Объект"
        override val codeName: String = "object"
    }

    class ArFaceType(): ArTypes() {
        override val id: String = "3"
        override val name: String = "Части тела"
        override val codeName: String = "bodyParts"
    }

    class ArGeoType(): ArTypes() {
        override val id: String = "4"
        override val name: String = "Гео"
        override val codeName: String = "geo"
    }

    class ArOSpaceType(): ArTypes() {
        override val id: String = "6"
        override val name: String = "Пространство"
        override val codeName: String = "space"
    }

}