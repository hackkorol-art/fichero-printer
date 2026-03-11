package com.example.ficheroandroid.domain.model

enum class DesignObjectType {
    TEXT,
    QRCODE,
    BARCODE,
    RECTANGLE,
    LINE,
    CIRCLE,
    IMAGE,
}

data class DesignObject(
    val id: Long,
    val type: DesignObjectType,
    val text: String = "Text",
    val x: Int = 8,
    val y: Int = 8,
    val width: Int = 80,
    val height: Int = 28,
    val fontSizePx: Float = 18f,
    val strokeWidth: Int = 2,
    val imageBase64: String? = null,
)

