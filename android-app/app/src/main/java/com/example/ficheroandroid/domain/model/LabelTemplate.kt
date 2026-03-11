package com.example.ficheroandroid.domain.model

enum class LabelContentType {
    TEXT,
    QRCODE,
    BARCODE,
}

data class LabelTemplate(
    val title: String,
    val text: String,
    val fontSizePx: Float,
    val labelWidthPx: Int,
    val labelHeightPx: Int,
    val density: Int,
    val copies: Int,
    val contentType: LabelContentType,
    val objects: List<DesignObject> = emptyList(),
    val csvEnabled: Boolean = false,
    val csvText: String = "",
    val postProcess: PostProcessType = PostProcessType.THRESHOLD,
    val threshold: Int = 140,
    val invert: Boolean = false,
    val labelType: Int = 1,
    val offsetX: Int = 0,
    val offsetY: Int = 0,
    val offsetType: OffsetType = OffsetType.INNER,
)

