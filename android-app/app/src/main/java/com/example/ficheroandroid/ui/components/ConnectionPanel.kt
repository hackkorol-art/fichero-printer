package com.example.ficheroandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ficheroandroid.domain.model.PrinterDiagnostics
import com.example.ficheroandroid.feature.designer.PrinterConnectionStatusUi
import com.example.ficheroandroid.ui.theme.FicheroColors

@Composable
fun ConnectionPanel(
    status: PrinterConnectionStatusUi,
    deviceName: String?,
    pairedPrinters: List<String>,
    selectedPrinterName: String,
    diagnostics: PrinterDiagnostics?,
    diagnosticsExpanded: Boolean,
    heartbeatEnabled: Boolean,
    heartbeatFails: Int,
    onSelectPrinter: (String) -> Unit,
    onRefreshPrinters: () -> Unit,
    onRefreshDiagnostics: () -> Unit,
    onReconnect: () -> Unit,
    onToggleHeartbeat: () -> Unit,
    onToggleDiagnosticsExpanded: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    var dropdownExpanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FicheroColors.BorderStandard, RoundedCornerShape(10.dp))
            .background(FicheroColors.Surface2, RoundedCornerShape(10.dp))
            .padding(10.dp),
    ) {
        when (status) {
            PrinterConnectionStatusUi.DISCONNECTED -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = if (selectedPrinterName.isBlank()) "Select paired printer" else selectedPrinterName,
                            onValueChange = {},
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            label = { Text("Printer") },
                        )
                        IconButton(onClick = { dropdownExpanded = true }) {
                            Icon(Icons.Default.Link, contentDescription = "Select printer")
                        }
                        IconButton(onClick = onRefreshPrinters) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh paired devices")
                        }
                        DropdownMenu(expanded = dropdownExpanded, onDismissRequest = { dropdownExpanded = false }) {
                            pairedPrinters.forEach { name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        onSelectPrinter(name)
                                        dropdownExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Button(onClick = onConnect, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Link, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Connect printer")
                    }
                }
            }

            PrinterConnectionStatusUi.CONNECTING -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.width(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Connecting...")
                }
            }

            PrinterConnectionStatusUi.CONNECTED -> {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = deviceName ?: "Connected",
                            color = FicheroColors.StatusOk,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Row {
                            IconButton(onClick = onToggleDiagnosticsExpanded) {
                                Icon(Icons.Default.Settings, contentDescription = "Diagnostics")
                            }
                            OutlinedButton(onClick = onDisconnect) {
                                Icon(Icons.Default.LinkOff, contentDescription = null, tint = Color.Unspecified)
                                Spacer(Modifier.width(6.dp))
                                Text("Disconnect")
                            }
                        }
                    }
                    if (diagnosticsExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(FicheroColors.Surface1, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("Diagnostics", color = MaterialTheme.colorScheme.onSurface)
                            Text("Bluetooth: ${if (diagnostics?.bluetoothEnabled == true) "ON" else "OFF"}")
                            Text("Paired supported: ${diagnostics?.pairedPrintersCount ?: 0}")
                            Text("Socket: ${if (diagnostics?.socketConnected == true) "connected" else "disconnected"}")
                            Text("Heartbeat: ${if (heartbeatEnabled) "ON" else "OFF"}; fails=$heartbeatFails")
                            diagnostics?.lastError?.let { Text("Last error: $it", color = MaterialTheme.colorScheme.error) }
                            Spacer(Modifier.height(2.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = onRefreshDiagnostics) { Text("Refresh info") }
                                OutlinedButton(onClick = onReconnect) { Text("Reconnect") }
                                OutlinedButton(onClick = onToggleHeartbeat) {
                                    Text(if (heartbeatEnabled) "Heartbeat off" else "Heartbeat on")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
