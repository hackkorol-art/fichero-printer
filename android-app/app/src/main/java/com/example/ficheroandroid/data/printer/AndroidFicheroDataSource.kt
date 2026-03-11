package com.example.ficheroandroid.data.printer

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class AndroidFicheroDataSource(
    private val appContext: Context,
) {
    private var socket: BluetoothSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    private val supportedPrefixes = listOf("FICHERO", "D11s_")

    private fun adapterOrNull() =
        (appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    fun listPairedByPrefix(prefixes: List<String>): List<String> {
        val adapter = adapterOrNull() ?: return emptyList()
        if (!adapter.isEnabled) return emptyList()
        return try {
            adapter.bondedDevices
                .mapNotNull { it.name }
                .filter { name -> prefixes.any { name.startsWith(it) } }
                .sorted()
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    fun connectFirstPairedByPrefix(prefixes: List<String>): String {
        val adapter = adapterOrNull() ?: error("Bluetooth adapter is unavailable")
        if (!adapter.isEnabled) error("Bluetooth is disabled")

        val bonded = try {
            adapter.bondedDevices
        } catch (_: SecurityException) {
            error("Bluetooth permission not granted")
        }
        val device = bonded.firstOrNull { d ->
            val name = d.name ?: ""
            prefixes.any { name.startsWith(it) }
        } ?: error("Pair your FICHERO/D11s printer in Android settings first")

        connect(device)
        return device.name ?: "Fichero"
    }

    fun connectByName(name: String, prefixes: List<String>): String {
        val adapter = adapterOrNull() ?: error("Bluetooth adapter is unavailable")
        if (!adapter.isEnabled) error("Bluetooth is disabled")
        val bonded = try {
            adapter.bondedDevices
        } catch (_: SecurityException) {
            error("Bluetooth permission not granted")
        }
        val target = bonded.firstOrNull { it.name == name }
            ?: error("Device '$name' is not paired")
        val valid = prefixes.any { (target.name ?: "").startsWith(it) }
        if (!valid) error("Device '$name' is not a supported Fichero/D11s printer")
        connect(target)
        return target.name ?: "Fichero"
    }

    fun isBluetoothEnabled(): Boolean = try {
        adapterOrNull()?.isEnabled == true
    } catch (_: SecurityException) {
        false
    }

    fun pairedSupportedCount(): Int = listPairedByPrefix(supportedPrefixes).size

    fun isConnected(): Boolean = socket?.isConnected == true

    fun disconnect() {
        try {
            input?.close()
        } catch (_: Throwable) {
        }
        try {
            output?.close()
        } catch (_: Throwable) {
        }
        try {
            socket?.close()
        } catch (_: Throwable) {
        }
        socket = null
        input = null
        output = null
    }

    private fun connect(device: BluetoothDevice) {
        disconnect()
        val sock = try {
            device.createRfcommSocketToServiceRecord(sppUuid)
        } catch (e: SecurityException) {
            error("Bluetooth permission not granted")
        }
        sock.connect()
        socket = sock
        input = sock.inputStream
        output = sock.outputStream
    }

    fun printRasterLabel(bytesPerRow: Int, rows: Int, rowsData: ByteArray, density: Int, labelType: Int) {
        send(byteArrayOf(0x10, 0xFF.toByte(), 0x10, 0x00, density.toByte()), waitForResponse = true)
        Thread.sleep(100)

        val paperByte = when (labelType) {
            2 -> 0x01
            3 -> 0x02
            else -> 0x00
        }.toByte()
        send(byteArrayOf(0x10, 0xFF.toByte(), 0x84.toByte(), paperByte), waitForResponse = true)
        Thread.sleep(50)

        send(ByteArray(12) { 0x00 })
        Thread.sleep(50)
        send(byteArrayOf(0x10, 0xFF.toByte(), 0xFE.toByte(), 0x01))
        Thread.sleep(50)

        val yL: Byte = (rows and 0xFF).toByte()
        val yH: Byte = ((rows shr 8) and 0xFF).toByte()
        val header = byteArrayOf(0x1D, 0x76, 0x30, 0x00, bytesPerRow.toByte(), 0x00, yL, yH)

        val payload = ByteArray(header.size + rowsData.size)
        System.arraycopy(header, 0, payload, 0, header.size)
        System.arraycopy(rowsData, 0, payload, header.size, rowsData.size)

        sendChunked(payload, chunkSize = 16_384, chunkDelayMs = 0)
        Thread.sleep(500)

        send(byteArrayOf(0x1D, 0x0C))
        Thread.sleep(300)
        send(byteArrayOf(0x10, 0xFF.toByte(), 0xFE.toByte(), 0x45), waitForResponse = true, timeoutMs = 60_000)
    }

    private fun send(data: ByteArray, waitForResponse: Boolean = false, timeoutMs: Long = 2_000): ByteArray {
        val out = output ?: throw IllegalStateException("Not connected")
        val inStream = input
        out.write(data)
        out.flush()
        if (!waitForResponse || inStream == null) return ByteArray(0)

        val start = System.currentTimeMillis()
        while (inStream.available() == 0 && System.currentTimeMillis() - start < timeoutMs) {
            Thread.sleep(10)
        }
        if (inStream.available() == 0) return ByteArray(0)

        val buffer = ByteArray(inStream.available())
        val read = inStream.read(buffer)
        return buffer.copyOf(read.coerceAtLeast(0))
    }

    private fun sendChunked(data: ByteArray, chunkSize: Int, chunkDelayMs: Long) {
        val out = output ?: throw IllegalStateException("Not connected")
        var offset = 0
        while (offset < data.size) {
            val end = (offset + chunkSize).coerceAtMost(data.size)
            out.write(data, offset, end - offset)
            out.flush()
            offset = end
            if (chunkDelayMs > 0) Thread.sleep(chunkDelayMs)
        }
    }
}

