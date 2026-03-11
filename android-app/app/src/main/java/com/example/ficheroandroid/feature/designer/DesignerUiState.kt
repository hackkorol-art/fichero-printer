package com.example.ficheroandroid.feature.designer

import com.example.ficheroandroid.domain.model.DesignObject
import com.example.ficheroandroid.domain.model.DesignObjectType
import com.example.ficheroandroid.domain.model.LabelContentType
import com.example.ficheroandroid.domain.model.LabelTemplate
import com.example.ficheroandroid.domain.model.OffsetType
import com.example.ficheroandroid.domain.model.PostProcessType
import com.example.ficheroandroid.domain.model.PrinterDiagnostics

enum class PrinterConnectionStatusUi {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
}

data class DesignerUiState(
    val status: PrinterConnectionStatusUi = PrinterConnectionStatusUi.DISCONNECTED,
    val deviceName: String? = null,
    val pairedPrinters: List<String> = emptyList(),
    val selectedPrinterName: String = "",
    val diagnosticsExpanded: Boolean = false,
    val heartbeatEnabled: Boolean = false,
    val heartbeatFails: Int = 0,
    val diagnostics: PrinterDiagnostics? = null,
    val templates: List<LabelTemplate> = emptyList(),
    val selectedTemplateIndex: Int = 0,
    val errorMessage: String? = null,
    val labelText: String = "Hello Fichero",
    val fontSizePx: Float = 28f,
    val labelWidthPx: Int = 240,
    val labelHeightPx: Int = 96,
    val copies: Int = 1,
    val density: Int = 2,
    val contentType: LabelContentType = LabelContentType.TEXT,
    val objects: List<DesignObject> = listOf(
        DesignObject(
            id = 1L,
            type = DesignObjectType.TEXT,
            text = "Hello",
            x = 8,
            y = 8,
            width = 100,
            height = 28,
            fontSizePx = 24f,
        ),
    ),
    val selectedObjectId: Long = 1L,
    val csvEnabled: Boolean = false,
    val csvText: String = "name,code\nJohn,123456",
    val postProcess: PostProcessType = PostProcessType.THRESHOLD,
    val threshold: Int = 140,
    val invert: Boolean = false,
    val labelType: Int = 1,
    val offsetX: Int = 0,
    val offsetY: Int = 0,
    val offsetType: OffsetType = OffsetType.INNER,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isPrinting: Boolean = false,
    val printProgress: Int = 0,
)

