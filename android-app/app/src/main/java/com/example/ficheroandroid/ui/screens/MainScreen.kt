package com.example.ficheroandroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ficheroandroid.domain.model.PostProcessType
import com.example.ficheroandroid.feature.designer.DesignerUiState
import com.example.ficheroandroid.feature.designer.PrinterConnectionStatusUi
import com.example.ficheroandroid.ui.components.AppFooter
import com.example.ficheroandroid.ui.components.ConnectionPanel
import com.example.ficheroandroid.ui.components.DesignerCanvas
import com.example.ficheroandroid.ui.components.MainHeader
import com.example.ficheroandroid.ui.components.ObjectToolbar
import com.example.ficheroandroid.ui.components.PreviewDialog
import com.example.ficheroandroid.ui.components.TopToolbar
import com.example.ficheroandroid.ui.theme.FicheroColors

private const val DPMM = 8

@Composable
fun MainScreen(
    state: DesignerUiState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onSelectPrinter: (String) -> Unit,
    onRefreshPrinters: () -> Unit,
    onRefreshDiagnostics: () -> Unit,
    onReconnect: () -> Unit,
    onToggleHeartbeat: () -> Unit,
    onToggleDiagnosticsExpanded: () -> Unit,
    onPrint: () -> Unit,
    onSetLabelWidth: (Int) -> Unit,
    onSetLabelHeight: (Int) -> Unit,
    onSetDensity: (Int) -> Unit,
    onSetThreshold: (Int) -> Unit,
    onSetPostProcess: (PostProcessType) -> Unit,
    onToggleInvert: () -> Unit,
    onSetLabelType: (Int) -> Unit,
    onSetOffsetX: (Int) -> Unit,
    onSetOffsetY: (Int) -> Unit,
    onToggleOffsetType: () -> Unit,
    onCancelPrint: () -> Unit,
    onSetCopies: (Int) -> Unit,
    onSetCsvText: (String) -> Unit,
    onToggleCsv: () -> Unit,
    onSaveTemplate: () -> Unit,
    onLoadTemplate: () -> Unit,
    onSelectTemplate: (Int) -> Unit,
    onLoadSelectedTemplate: () -> Unit,
    onExportTemplateJson: () -> Unit,
    onImportTemplateJson: () -> Unit,
    onClearCanvas: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onAddText: () -> Unit,
    onAddQr: () -> Unit,
    onAddBarcode: () -> Unit,
    onAddRect: () -> Unit,
    onAddLine: () -> Unit,
    onAddCircle: () -> Unit,
    onAddImage: () -> Unit,
    onCloneSelected: () -> Unit,
    onCenterSelectedH: () -> Unit,
    onCenterSelectedV: () -> Unit,
    onBringFront: () -> Unit,
    onSendBack: () -> Unit,
    onSelectObject: (Long) -> Unit,
    onRemoveSelected: () -> Unit,
    onUpdateObjectText: (String) -> Unit,
    onUpdateObjectX: (Int) -> Unit,
    onUpdateObjectY: (Int) -> Unit,
    onUpdateObjectWidth: (Int) -> Unit,
    onUpdateObjectHeight: (Int) -> Unit,
    onUpdateObjectFontSize: (Float) -> Unit,
    onMoveSelectedBy: (Int, Int) -> Unit,
    onTransformSelected: (Int, Int, Int, Int) -> Unit,
) {
    var previewOpen by remember { mutableStateOf(false) }
    val selected = state.objects.firstOrNull { it.id == state.selectedObjectId }

    Scaffold(
        containerColor = FicheroColors.Surface0,
    ) { padding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FicheroColors.Surface0)
                .padding(padding)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    MainHeader()
                }
                Column(modifier = Modifier.weight(1f)) {
                    ConnectionPanel(
                        status = state.status,
                        deviceName = state.deviceName,
                        pairedPrinters = state.pairedPrinters,
                        selectedPrinterName = state.selectedPrinterName,
                        diagnostics = state.diagnostics,
                        diagnosticsExpanded = state.diagnosticsExpanded,
                        heartbeatEnabled = state.heartbeatEnabled,
                        heartbeatFails = state.heartbeatFails,
                        onSelectPrinter = onSelectPrinter,
                        onRefreshPrinters = onRefreshPrinters,
                        onRefreshDiagnostics = onRefreshDiagnostics,
                        onReconnect = onReconnect,
                        onToggleHeartbeat = onToggleHeartbeat,
                        onToggleDiagnosticsExpanded = onToggleDiagnosticsExpanded,
                        onConnect = onConnect,
                        onDisconnect = onDisconnect,
                    )
                }
            }

            DesignerCanvas(
                labelWidthPx = state.labelWidthPx,
                labelHeightPx = state.labelHeightPx,
                objects = state.objects,
                selectedObjectId = state.selectedObjectId,
                onSelectObject = onSelectObject,
                onMoveSelectedBy = onMoveSelectedBy,
                onTransformSelected = onTransformSelected,
            )

            TopToolbar(
                connected = state.status == PrinterConnectionStatusUi.CONNECTED,
                csvEnabled = state.csvEnabled,
                canUndo = state.canUndo,
                canRedo = state.canRedo,
                onSaveTemplate = onSaveTemplate,
                onLoadTemplate = onLoadTemplate,
                onClear = onClearCanvas,
                onUndo = onUndo,
                onRedo = onRedo,
                onToggleCsv = onToggleCsv,
                onAddText = onAddText,
                onAddQr = onAddQr,
                onPreview = { previewOpen = true },
                onPrint = {
                    previewOpen = true
                },
            )

            ObjectToolbar(
                selected = selected,
                onRemoveSelected = onRemoveSelected,
                onCloneSelected = onCloneSelected,
                onAddBarcode = onAddBarcode,
                onAddRect = onAddRect,
                onAddLine = onAddLine,
                onAddCircle = onAddCircle,
                onAddImage = onAddImage,
                onCenterH = onCenterSelectedH,
                onCenterV = onCenterSelectedV,
                onBringFront = onBringFront,
                onSendBack = onSendBack,
                onUpdateObjectText = onUpdateObjectText,
            )

            if (state.templates.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.selectedTemplateIndex.toString(),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        label = { Text("Template index") },
                    )
                    Button(onClick = { /* index managed via arrows below */ }, enabled = false) {
                        Text("Saved: ${state.templates.size}")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { if (state.selectedTemplateIndex > 0) onSelectTemplate(state.selectedTemplateIndex - 1) },
                        enabled = state.selectedTemplateIndex > 0,
                        modifier = Modifier.weight(1f),
                    ) { Text("Prev template") }
                    Button(
                        onClick = onLoadSelectedTemplate,
                        modifier = Modifier.weight(1f),
                    ) { Text("Load selected") }
                    Button(
                        onClick = {
                            if (state.selectedTemplateIndex < state.templates.lastIndex) {
                                onSelectTemplate(state.selectedTemplateIndex + 1)
                            }
                        },
                        enabled = state.selectedTemplateIndex < state.templates.lastIndex,
                        modifier = Modifier.weight(1f),
                    ) { Text("Next template") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onExportTemplateJson, modifier = Modifier.weight(1f)) { Text("Export JSON") }
                    Button(onClick = onImportTemplateJson, modifier = Modifier.weight(1f)) { Text("Import JSON") }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = (state.labelWidthPx / DPMM).toString(),
                    onValueChange = { it.toIntOrNull()?.let { mm -> onSetLabelWidth(mm * DPMM) } },
                    modifier = Modifier.weight(1f),
                    label = { Text("Width mm") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                OutlinedTextField(
                    value = (state.labelHeightPx / DPMM).toString(),
                    onValueChange = { it.toIntOrNull()?.let { mm -> onSetLabelHeight(mm * DPMM) } },
                    modifier = Modifier.weight(1f),
                    label = { Text("Height mm") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            if (selected != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selected.x.toString(),
                        onValueChange = { it.toIntOrNull()?.let(onUpdateObjectX) },
                        modifier = Modifier.weight(1f),
                        label = { Text("X") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = selected.y.toString(),
                        onValueChange = { it.toIntOrNull()?.let(onUpdateObjectY) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Y") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selected.width.toString(),
                        onValueChange = { it.toIntOrNull()?.let(onUpdateObjectWidth) },
                        modifier = Modifier.weight(1f),
                        label = { Text("W") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = selected.height.toString(),
                        onValueChange = { it.toIntOrNull()?.let(onUpdateObjectHeight) },
                        modifier = Modifier.weight(1f),
                        label = { Text("H") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                OutlinedTextField(
                    value = selected.fontSizePx.toInt().toString(),
                    onValueChange = { it.toFloatOrNull()?.let(onUpdateObjectFontSize) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Font size") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }

            if (state.csvEnabled) {
                OutlinedTextField(
                    value = state.csvText,
                    onValueChange = onSetCsvText,
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    label = { Text("CSV data") },
                )
            }

            state.errorMessage?.let {
                Text(it, color = FicheroColors.StatusDanger)
            }

            Spacer(Modifier.height(8.dp))
            AppFooter()
        }
    }

    if (previewOpen) {
        PreviewDialog(
            state = state,
            onClose = { previewOpen = false },
            onSetCopies = onSetCopies,
            onSetDensity = onSetDensity,
            onSetThreshold = onSetThreshold,
            onSetPostProcess = onSetPostProcess,
            onToggleInvert = onToggleInvert,
            onSetLabelType = onSetLabelType,
            onSetOffsetX = onSetOffsetX,
            onSetOffsetY = onSetOffsetY,
            onToggleOffsetType = onToggleOffsetType,
            onCancelPrint = onCancelPrint,
            onPrint = {
                onPrint()
            },
        )
    }
}
