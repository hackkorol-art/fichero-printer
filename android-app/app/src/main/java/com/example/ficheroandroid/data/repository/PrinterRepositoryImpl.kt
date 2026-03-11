package com.example.ficheroandroid.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.ficheroandroid.data.printer.AndroidFicheroDataSource
import com.example.ficheroandroid.domain.model.EncodedLabel
import com.example.ficheroandroid.domain.model.PrinterDiagnostics
import com.example.ficheroandroid.domain.model.PrinterDevice
import com.example.ficheroandroid.domain.repository.PrinterRepository

class PrinterRepositoryImpl(
    private val appContext: Context,
    private val dataSource: AndroidFicheroDataSource,
) : PrinterRepository {
    private val printerPrefixes = listOf("FICHERO", "D11s_")

    override fun hasPermissions(): Boolean {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            listOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
        }
        return permissions.all {
            ContextCompat.checkSelfPermission(appContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun listPairedPrinters(): List<String> = dataSource.listPairedByPrefix(printerPrefixes)

    override fun connect(): PrinterDevice {
        val name = dataSource.connectFirstPairedByPrefix(printerPrefixes)
        return PrinterDevice(name)
    }

    override fun connectByName(name: String): PrinterDevice {
        val connectedName = dataSource.connectByName(name, printerPrefixes)
        return PrinterDevice(connectedName)
    }

    override fun disconnect() {
        dataSource.disconnect()
    }

    override fun isConnected(): Boolean = dataSource.isConnected()

    override fun diagnostics(selectedPrinterName: String?, lastError: String?): PrinterDiagnostics {
        return PrinterDiagnostics(
            bluetoothEnabled = dataSource.isBluetoothEnabled(),
            pairedPrintersCount = dataSource.pairedSupportedCount(),
            socketConnected = dataSource.isConnected(),
            selectedPrinterName = selectedPrinterName,
            lastError = lastError,
        )
    }

    override fun printLabel(encodedLabel: EncodedLabel, density: Int, labelType: Int, copies: Int) {
        repeat(copies.coerceAtLeast(1)) {
            dataSource.printRasterLabel(
                bytesPerRow = encodedLabel.bytesPerRow,
                rows = encodedLabel.rows,
                rowsData = encodedLabel.rowsData,
                density = density.coerceIn(1, 20),
                labelType = labelType,
            )
        }
    }
}

