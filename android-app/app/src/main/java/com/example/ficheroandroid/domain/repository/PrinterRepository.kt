package com.example.ficheroandroid.domain.repository

import com.example.ficheroandroid.domain.model.EncodedLabel
import com.example.ficheroandroid.domain.model.PrinterDiagnostics
import com.example.ficheroandroid.domain.model.PrinterDevice

interface PrinterRepository {
    fun hasPermissions(): Boolean
    fun listPairedPrinters(): List<String>
    fun connect(): PrinterDevice
    fun connectByName(name: String): PrinterDevice
    fun disconnect()
    fun isConnected(): Boolean
    fun diagnostics(selectedPrinterName: String?, lastError: String? = null): PrinterDiagnostics
    fun printLabel(encodedLabel: EncodedLabel, density: Int, labelType: Int, copies: Int)
}

