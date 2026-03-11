package com.example.ficheroandroid.feature.designer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ficheroandroid.domain.model.DesignObject
import com.example.ficheroandroid.domain.model.DesignObjectType
import com.example.ficheroandroid.domain.model.LabelContentType
import com.example.ficheroandroid.domain.model.LabelTemplate
import com.example.ficheroandroid.domain.model.OffsetType
import com.example.ficheroandroid.domain.model.PostProcessType
import com.example.ficheroandroid.domain.usecase.ConnectPrinterUseCase
import com.example.ficheroandroid.domain.usecase.ConnectPrinterByNameUseCase
import com.example.ficheroandroid.domain.usecase.DisconnectPrinterUseCase
import com.example.ficheroandroid.domain.usecase.EnsurePrinterConnectedUseCase
import com.example.ficheroandroid.domain.usecase.GetPrinterDiagnosticsUseCase
import com.example.ficheroandroid.domain.usecase.HasBluetoothPermissionsUseCase
import com.example.ficheroandroid.domain.usecase.ImportTemplateJsonUseCase
import com.example.ficheroandroid.domain.usecase.ListPairedPrintersUseCase
import com.example.ficheroandroid.domain.usecase.LoadLastTemplateUseCase
import com.example.ficheroandroid.domain.usecase.LoadTemplatesUseCase
import com.example.ficheroandroid.domain.usecase.ExportTemplateJsonUseCase
import com.example.ficheroandroid.domain.usecase.PrintDesignUseCase
import com.example.ficheroandroid.domain.service.LabelRenderer
import com.example.ficheroandroid.domain.usecase.SaveTemplateUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class DesignerViewModel(
    private val hasBluetoothPermissionsUseCase: HasBluetoothPermissionsUseCase,
    private val connectPrinterUseCase: ConnectPrinterUseCase,
    private val connectPrinterByNameUseCase: ConnectPrinterByNameUseCase,
    private val listPairedPrintersUseCase: ListPairedPrintersUseCase,
    private val disconnectPrinterUseCase: DisconnectPrinterUseCase,
    private val ensurePrinterConnectedUseCase: EnsurePrinterConnectedUseCase,
    private val getPrinterDiagnosticsUseCase: GetPrinterDiagnosticsUseCase,
    private val printDesignUseCase: PrintDesignUseCase,
    private val saveTemplateUseCase: SaveTemplateUseCase,
    private val loadLastTemplateUseCase: LoadLastTemplateUseCase,
    private val loadTemplatesUseCase: LoadTemplatesUseCase,
    private val exportTemplateJsonUseCase: ExportTemplateJsonUseCase,
    private val importTemplateJsonUseCase: ImportTemplateJsonUseCase,
) : ViewModel() {
    private data class EditorSnapshot(
        val labelWidthPx: Int,
        val labelHeightPx: Int,
        val contentType: LabelContentType,
        val objects: List<DesignObject>,
        val selectedObjectId: Long,
        val csvEnabled: Boolean,
        val csvText: String,
        val postProcess: PostProcessType,
        val threshold: Int,
        val invert: Boolean,
        val labelType: Int,
        val offsetX: Int,
        val offsetY: Int,
        val offsetType: OffsetType,
    )

    private val _uiState = MutableStateFlow(DesignerUiState())
    val uiState: StateFlow<DesignerUiState> = _uiState.asStateFlow()
    private val undoStack = ArrayDeque<EditorSnapshot>()
    private val redoStack = ArrayDeque<EditorSnapshot>()
    private val cancelPrintFlag = AtomicBoolean(false)
    private var heartbeatJob: Job? = null

    init {
        refreshTemplates()
        if (hasBluetoothPermissionsUseCase()) {
            refreshPairedPrinters()
        }
    }

    fun hasBluetoothPermissions(): Boolean = hasBluetoothPermissionsUseCase()

    fun connect() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.value = _uiState.value.copy(status = PrinterConnectionStatusUi.CONNECTING, errorMessage = null)
                val preferred = _uiState.value.selectedPrinterName
                val device = if (preferred.isNotBlank()) {
                    connectPrinterByNameUseCase(preferred)
                } else {
                    connectPrinterUseCase()
                }
                _uiState.value = _uiState.value.copy(
                    status = PrinterConnectionStatusUi.CONNECTED,
                    deviceName = device.name,
                    errorMessage = null,
                )
                refreshDiagnostics()
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(
                    status = PrinterConnectionStatusUi.DISCONNECTED,
                    errorMessage = t.message ?: "Connection failed",
                )
                refreshDiagnostics(t.message)
            }
        }
    }

    fun refreshPairedPrinters() {
        val devices = listPairedPrintersUseCase()
        val selected = _uiState.value.selectedPrinterName
        _uiState.value = _uiState.value.copy(
            pairedPrinters = devices,
            selectedPrinterName = if (selected.isNotBlank()) selected else devices.firstOrNull().orEmpty(),
        )
        refreshDiagnostics()
    }

    fun selectPrinter(name: String) {
        _uiState.value = _uiState.value.copy(selectedPrinterName = name)
        refreshDiagnostics()
    }

    fun disconnect() {
        viewModelScope.launch(Dispatchers.IO) {
            disconnectPrinterUseCase()
            stopHeartbeat()
            _uiState.value = _uiState.value.copy(status = PrinterConnectionStatusUi.DISCONNECTED)
            refreshDiagnostics()
        }
    }

    fun toggleDiagnosticsExpanded() {
        _uiState.value = _uiState.value.copy(diagnosticsExpanded = !_uiState.value.diagnosticsExpanded)
    }

    fun refreshDiagnostics(lastError: String? = null) {
        val s = _uiState.value
        _uiState.value = s.copy(
            diagnostics = getPrinterDiagnosticsUseCase(
                selectedPrinterName = s.selectedPrinterName.ifBlank { null },
                lastError = lastError ?: s.errorMessage,
            ),
        )
    }

    fun reconnectPrinter() {
        disconnect()
        connect()
    }

    fun toggleHeartbeat() {
        if (_uiState.value.heartbeatEnabled) {
            stopHeartbeat()
        } else {
            startHeartbeat()
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        _uiState.value = _uiState.value.copy(heartbeatEnabled = true)
        heartbeatJob = viewModelScope.launch(Dispatchers.IO) {
            var fails = _uiState.value.heartbeatFails
            while (_uiState.value.heartbeatEnabled) {
                val ok = ensurePrinterConnectedUseCase()
                if (!ok) {
                    fails++
                }
                _uiState.value = _uiState.value.copy(heartbeatFails = fails)
                refreshDiagnostics()
                delay(1500)
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        _uiState.value = _uiState.value.copy(heartbeatEnabled = false)
    }

    fun printLabel() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (!ensurePrinterConnectedUseCase()) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Printer is not connected")
                    return@launch
                }
                cancelPrintFlag.set(false)
                val s = _uiState.value
                _uiState.value = s.copy(isPrinting = true, printProgress = 0, errorMessage = null)
                printDesignUseCase(
                    PrintDesignUseCase.Params(
                        labelWidthPx = s.labelWidthPx,
                        labelHeightPx = s.labelHeightPx,
                        objects = s.objects,
                        density = s.density,
                        copies = s.copies,
                        labelType = s.labelType,
                        csvEnabled = s.csvEnabled,
                        csvText = s.csvText,
                        postProcess = s.postProcess,
                        threshold = s.threshold,
                        invert = s.invert,
                        offsetX = s.offsetX,
                        offsetY = s.offsetY,
                        offsetType = s.offsetType,
                        onProgress = { progress ->
                            _uiState.value = _uiState.value.copy(printProgress = progress)
                        },
                        shouldStop = {
                            cancelPrintFlag.get()
                        },
                    ),
                )
                _uiState.value = _uiState.value.copy(isPrinting = false, printProgress = 100)
            } catch (t: Throwable) {
                _uiState.value = _uiState.value.copy(isPrinting = false, errorMessage = t.message ?: "Print failed")
            }
        }
    }

    fun cancelPrint() {
        cancelPrintFlag.set(true)
        _uiState.value = _uiState.value.copy(isPrinting = false)
    }

    fun updateLabelWidth(newWidth: Int) {
        saveHistorySnapshot()
        _uiState.value = _uiState.value.copy(labelWidthPx = normalizeLabelWidth(newWidth))
        publishHistoryAvailability()
    }

    fun updateLabelHeight(newHeight: Int) {
        saveHistorySnapshot()
        val clamped = newHeight.coerceIn(DOTS_PER_MM, LabelRenderer.PRINTHEAD_PX)
        _uiState.value = _uiState.value.copy(labelHeightPx = (clamped / DOTS_PER_MM) * DOTS_PER_MM)
        publishHistoryAvailability()
    }

    fun updateCopies(newCopies: Int) {
        _uiState.value = _uiState.value.copy(copies = newCopies.coerceIn(1, 10))
    }

    fun updateDensity(newDensity: Int) {
        _uiState.value = _uiState.value.copy(density = newDensity.coerceIn(1, 20))
    }

    fun updateThreshold(newThreshold: Int) {
        _uiState.value = _uiState.value.copy(threshold = newThreshold.coerceIn(1, 255))
    }

    fun updatePostProcess(type: PostProcessType) {
        _uiState.value = _uiState.value.copy(postProcess = type)
    }

    fun toggleInvert() {
        _uiState.value = _uiState.value.copy(invert = !_uiState.value.invert)
    }

    fun updateLabelType(type: Int) {
        _uiState.value = _uiState.value.copy(labelType = type.coerceIn(1, 3))
    }

    fun updateOffsetX(value: Int) {
        _uiState.value = _uiState.value.copy(offsetX = value.coerceIn(-500, 500))
    }

    fun updateOffsetY(value: Int) {
        _uiState.value = _uiState.value.copy(offsetY = value.coerceIn(-500, 500))
    }

    fun toggleOffsetType() {
        _uiState.value = _uiState.value.copy(
            offsetType = if (_uiState.value.offsetType == OffsetType.INNER) OffsetType.OUTER else OffsetType.INNER,
        )
    }

    fun updateContentType(newType: LabelContentType) {
        val current = _uiState.value
        val selected = current.objects.firstOrNull { it.id == current.selectedObjectId } ?: return
        saveHistorySnapshot()
        val objType = when (newType) {
            LabelContentType.TEXT -> DesignObjectType.TEXT
            LabelContentType.QRCODE -> DesignObjectType.QRCODE
            LabelContentType.BARCODE -> DesignObjectType.BARCODE
        }
        val updated = selected.copy(type = objType)
        _uiState.value = current.copy(
            contentType = newType,
            objects = current.objects.map { if (it.id == selected.id) updated else it },
        )
        publishHistoryAvailability()
    }

    fun addObject(type: DesignObjectType) {
        saveHistorySnapshot()
        val current = _uiState.value
        val id = System.currentTimeMillis()
        val obj = DesignObject(
            id = id,
            type = type,
            text = when (type) {
                DesignObjectType.TEXT -> "Text"
                DesignObjectType.QRCODE -> "QR123"
                DesignObjectType.BARCODE -> "12345678"
                DesignObjectType.RECTANGLE -> ""
                DesignObjectType.LINE -> ""
                DesignObjectType.CIRCLE -> ""
                DesignObjectType.IMAGE -> ""
            },
            x = 8,
            y = 8 + (current.objects.size * 8),
            width = 70,
            height = 24,
        )
        _uiState.value = current.copy(objects = current.objects + obj, selectedObjectId = id)
        publishHistoryAvailability()
    }

    fun addImageObject(imageBase64: String) {
        saveHistorySnapshot()
        val current = _uiState.value
        val id = System.currentTimeMillis()
        val obj = DesignObject(
            id = id,
            type = DesignObjectType.IMAGE,
            text = "image",
            x = 8,
            y = 8 + (current.objects.size * 8),
            width = 96,
            height = 96,
            imageBase64 = imageBase64,
        )
        _uiState.value = current.copy(objects = current.objects + obj, selectedObjectId = id)
        publishHistoryAvailability()
    }

    fun selectObject(id: Long) {
        _uiState.value = _uiState.value.copy(selectedObjectId = id)
    }

    fun removeSelectedObject() {
        saveHistorySnapshot()
        val current = _uiState.value
        if (current.objects.isEmpty()) return
        val filtered = current.objects.filterNot { it.id == current.selectedObjectId }
        _uiState.value = current.copy(objects = filtered, selectedObjectId = filtered.firstOrNull()?.id ?: 0L)
        publishHistoryAvailability()
    }

    fun updateSelectedObjectText(text: String) = updateSelectedObject { it.copy(text = text) }
    fun updateSelectedObjectX(x: Int) = updateSelectedObject {
        it.copy(x = x.coerceIn(0, _uiState.value.labelWidthPx - 1))
    }

    fun updateSelectedObjectY(y: Int) = updateSelectedObject {
        it.copy(y = y.coerceIn(0, _uiState.value.labelHeightPx - 1))
    }

    fun updateSelectedObjectWidth(width: Int) = updateSelectedObject {
        it.copy(width = width.coerceIn(1, _uiState.value.labelWidthPx))
    }

    fun updateSelectedObjectHeight(height: Int) = updateSelectedObject {
        it.copy(height = height.coerceIn(1, _uiState.value.labelHeightPx))
    }
    fun updateSelectedObjectFontSize(size: Float) = updateSelectedObject { it.copy(fontSizePx = size.coerceIn(8f, 64f)) }
    fun moveSelectedObjectBy(deltaX: Int, deltaY: Int) = updateSelectedObject {
        it.copy(
            x = (it.x + deltaX).coerceIn(0, _uiState.value.labelWidthPx - 1),
            y = (it.y + deltaY).coerceIn(0, _uiState.value.labelHeightPx - 1),
        )
    }

    fun transformSelectedObject(dx: Int, dy: Int, dw: Int, dh: Int) = updateSelectedObject {
        val s = _uiState.value
        it.copy(
            x = (it.x + dx).coerceIn(0, s.labelWidthPx - 8),
            y = (it.y + dy).coerceIn(0, s.labelHeightPx - 8),
            width = (it.width + dw).coerceIn(8, s.labelWidthPx),
            height = (it.height + dh).coerceIn(8, s.labelHeightPx),
        )
    }

    fun toggleCsvEnabled() {
        saveHistorySnapshot()
        _uiState.value = _uiState.value.copy(csvEnabled = !_uiState.value.csvEnabled)
        publishHistoryAvailability()
    }

    fun updateCsvText(text: String) {
        saveHistorySnapshot()
        _uiState.value = _uiState.value.copy(csvText = text)
        publishHistoryAvailability()
    }

    fun saveCurrentTemplate() {
        val s = _uiState.value
        saveTemplateUseCase(
            LabelTemplate(
                title = "Template ${System.currentTimeMillis()}",
                text = s.labelText,
                fontSizePx = s.fontSizePx,
                labelWidthPx = s.labelWidthPx,
                labelHeightPx = s.labelHeightPx,
                density = s.density,
                copies = s.copies,
                contentType = s.contentType,
                objects = s.objects,
                csvEnabled = s.csvEnabled,
                csvText = s.csvText,
                postProcess = s.postProcess,
                threshold = s.threshold,
                invert = s.invert,
                labelType = s.labelType,
                offsetX = s.offsetX,
                offsetY = s.offsetY,
                offsetType = s.offsetType,
            ),
        )
        refreshTemplates()
    }

    fun loadLastTemplate() {
        val template = loadLastTemplateUseCase() ?: return
        applyTemplate(template)
        publishHistoryAvailability()
    }

    fun refreshTemplates() {
        val list = loadTemplatesUseCase()
        val current = _uiState.value
        val idx = current.selectedTemplateIndex.coerceIn(0, (list.size - 1).coerceAtLeast(0))
        _uiState.value = current.copy(templates = list, selectedTemplateIndex = idx)
    }

    fun selectTemplate(index: Int) {
        val templates = _uiState.value.templates
        if (templates.isEmpty()) return
        _uiState.value = _uiState.value.copy(selectedTemplateIndex = index.coerceIn(0, templates.lastIndex))
    }

    fun loadSelectedTemplate() {
        val templates = _uiState.value.templates
        if (templates.isEmpty()) return
        val template = templates[_uiState.value.selectedTemplateIndex.coerceIn(0, templates.lastIndex)]
        applyTemplate(template)
        publishHistoryAvailability()
    }

    fun exportSelectedTemplateJson(): String? {
        val templates = _uiState.value.templates
        if (templates.isEmpty()) return null
        val template = templates[_uiState.value.selectedTemplateIndex.coerceIn(0, templates.lastIndex)]
        return exportTemplateJsonUseCase(template)
    }

    fun importTemplateJson(raw: String): Boolean {
        val template = importTemplateJsonUseCase(raw) ?: return false
        saveTemplateUseCase(template.copy(title = "${template.title} (imported)"))
        refreshTemplates()
        return true
    }

    private fun applyTemplate(template: LabelTemplate) {
        saveHistorySnapshot()
        _uiState.value = _uiState.value.copy(
            labelText = template.text,
            fontSizePx = template.fontSizePx,
            labelWidthPx = normalizeLabelWidth(template.labelWidthPx),
            labelHeightPx = template.labelHeightPx,
            density = template.density,
            copies = template.copies,
            contentType = template.contentType,
            objects = template.objects,
            selectedObjectId = template.objects.firstOrNull()?.id ?: 0L,
            csvEnabled = template.csvEnabled,
            csvText = template.csvText,
            postProcess = template.postProcess,
            threshold = template.threshold,
            invert = template.invert,
            labelType = template.labelType,
            offsetX = template.offsetX,
            offsetY = template.offsetY,
            offsetType = template.offsetType,
        )
    }

    fun cloneSelectedObject() {
        val current = _uiState.value
        val selected = current.objects.firstOrNull { it.id == current.selectedObjectId } ?: return
        saveHistorySnapshot()
        val nextId = System.currentTimeMillis()
        val cloned = selected.copy(
            id = nextId,
            x = (selected.x + 8).coerceAtMost(current.labelWidthPx - 1),
            y = (selected.y + 8).coerceAtMost(current.labelHeightPx - 1),
        )
        _uiState.value = current.copy(objects = current.objects + cloned, selectedObjectId = nextId)
        publishHistoryAvailability()
    }

    fun clearCanvas() {
        val current = _uiState.value
        if (current.objects.isEmpty()) return
        saveHistorySnapshot()
        _uiState.value = current.copy(objects = emptyList(), selectedObjectId = 0L)
        publishHistoryAvailability()
    }

    fun centerSelectedHorizontally() {
        val current = _uiState.value
        val selected = current.objects.firstOrNull { it.id == current.selectedObjectId } ?: return
        val x = ((current.labelWidthPx - selected.width) / 2).coerceAtLeast(0)
        updateSelectedObject { it.copy(x = x) }
    }

    fun centerSelectedVertically() {
        val current = _uiState.value
        val selected = current.objects.firstOrNull { it.id == current.selectedObjectId } ?: return
        val y = ((current.labelHeightPx - selected.height) / 2).coerceAtLeast(0)
        updateSelectedObject { it.copy(y = y) }
    }

    fun bringSelectedToFront() {
        val current = _uiState.value
        val selected = current.objects.firstOrNull { it.id == current.selectedObjectId } ?: return
        saveHistorySnapshot()
        val reordered = current.objects.filterNot { it.id == selected.id } + selected
        _uiState.value = current.copy(objects = reordered)
        publishHistoryAvailability()
    }

    fun sendSelectedToBack() {
        val current = _uiState.value
        val selected = current.objects.firstOrNull { it.id == current.selectedObjectId } ?: return
        saveHistorySnapshot()
        val reordered = listOf(selected) + current.objects.filterNot { it.id == selected.id }
        _uiState.value = current.copy(objects = reordered)
        publishHistoryAvailability()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val current = _uiState.value
        redoStack.addLast(current.toSnapshot())
        val snapshot = undoStack.removeLast()
        _uiState.value = current.copy(
            labelWidthPx = snapshot.labelWidthPx,
            labelHeightPx = snapshot.labelHeightPx,
            contentType = snapshot.contentType,
            objects = snapshot.objects,
            selectedObjectId = snapshot.selectedObjectId,
            csvEnabled = snapshot.csvEnabled,
            csvText = snapshot.csvText,
            postProcess = snapshot.postProcess,
            threshold = snapshot.threshold,
            invert = snapshot.invert,
            labelType = snapshot.labelType,
            offsetX = snapshot.offsetX,
            offsetY = snapshot.offsetY,
            offsetType = snapshot.offsetType,
        )
        publishHistoryAvailability()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val current = _uiState.value
        undoStack.addLast(current.toSnapshot())
        val snapshot = redoStack.removeLast()
        _uiState.value = current.copy(
            labelWidthPx = snapshot.labelWidthPx,
            labelHeightPx = snapshot.labelHeightPx,
            contentType = snapshot.contentType,
            objects = snapshot.objects,
            selectedObjectId = snapshot.selectedObjectId,
            csvEnabled = snapshot.csvEnabled,
            csvText = snapshot.csvText,
            postProcess = snapshot.postProcess,
            threshold = snapshot.threshold,
            invert = snapshot.invert,
            labelType = snapshot.labelType,
            offsetX = snapshot.offsetX,
            offsetY = snapshot.offsetY,
            offsetType = snapshot.offsetType,
        )
        publishHistoryAvailability()
    }

    private fun updateSelectedObject(transform: (DesignObject) -> DesignObject) {
        val current = _uiState.value
        val selected = current.objects.firstOrNull { it.id == current.selectedObjectId } ?: return
        saveHistorySnapshot()
        val updated = transform(selected)
        _uiState.value = current.copy(objects = current.objects.map { if (it.id == selected.id) updated else it })
        publishHistoryAvailability()
    }

    private fun normalizeLabelWidth(width: Int): Int {
        val clamped = width.coerceIn(DOTS_PER_MM, 480)
        return (clamped / DOTS_PER_MM) * DOTS_PER_MM
    }

    companion object {
        const val DOTS_PER_MM = 8
    }

    private fun saveHistorySnapshot() {
        val current = _uiState.value
        if (undoStack.isNotEmpty() && undoStack.last() == current.toSnapshot()) {
            return
        }
        undoStack.addLast(current.toSnapshot())
        if (undoStack.size > 60) {
            undoStack.removeFirst()
        }
        redoStack.clear()
    }

    private fun publishHistoryAvailability() {
        val current = _uiState.value
        _uiState.value = current.copy(
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty(),
        )
    }

    private fun DesignerUiState.toSnapshot(): EditorSnapshot {
        return EditorSnapshot(
            labelWidthPx = labelWidthPx,
            labelHeightPx = labelHeightPx,
            contentType = contentType,
            objects = objects,
            selectedObjectId = selectedObjectId,
            csvEnabled = csvEnabled,
            csvText = csvText,
            postProcess = postProcess,
            threshold = threshold,
            invert = invert,
            labelType = labelType,
            offsetX = offsetX,
            offsetY = offsetY,
            offsetType = offsetType,
        )
    }

    override fun onCleared() {
        heartbeatJob?.cancel()
        super.onCleared()
    }
}

