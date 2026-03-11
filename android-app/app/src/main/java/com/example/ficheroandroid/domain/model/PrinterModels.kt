package com.example.ficheroandroid.domain.model

data class PrinterDevice(
    val name: String,
)

data class EncodedLabel(
    val bytesPerRow: Int,
    val rows: Int,
    val rowsData: ByteArray,
)

data class PrinterDiagnostics(
    val bluetoothEnabled: Boolean,
    val pairedPrintersCount: Int,
    val socketConnected: Boolean,
    val selectedPrinterName: String? = null,
    val lastError: String? = null,
)

