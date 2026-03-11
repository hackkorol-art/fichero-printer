package com.example.ficheroandroid.domain.usecase

import com.example.ficheroandroid.domain.model.PrinterDevice
import com.example.ficheroandroid.domain.model.PrinterDiagnostics
import com.example.ficheroandroid.domain.repository.PrinterRepository

class HasBluetoothPermissionsUseCase(
    private val printerRepository: PrinterRepository,
) {
    operator fun invoke(): Boolean = printerRepository.hasPermissions()
}

class ConnectPrinterUseCase(
    private val printerRepository: PrinterRepository,
) {
    operator fun invoke(): PrinterDevice = printerRepository.connect()
}

class ConnectPrinterByNameUseCase(
    private val printerRepository: PrinterRepository,
) {
    operator fun invoke(name: String): PrinterDevice = printerRepository.connectByName(name)
}

class ListPairedPrintersUseCase(
    private val printerRepository: PrinterRepository,
) {
    operator fun invoke(): List<String> = printerRepository.listPairedPrinters()
}

class DisconnectPrinterUseCase(
    private val printerRepository: PrinterRepository,
) {
    operator fun invoke() = printerRepository.disconnect()
}

class EnsurePrinterConnectedUseCase(
    private val printerRepository: PrinterRepository,
) {
    operator fun invoke(): Boolean = printerRepository.isConnected()
}

class GetPrinterDiagnosticsUseCase(
    private val printerRepository: PrinterRepository,
) {
    operator fun invoke(selectedPrinterName: String?, lastError: String? = null): PrinterDiagnostics {
        return printerRepository.diagnostics(selectedPrinterName, lastError)
    }
}

