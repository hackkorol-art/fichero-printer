package com.example.ficheroandroid

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ficheroandroid.di.AppContainer
import com.example.ficheroandroid.domain.model.DesignObjectType
import com.example.ficheroandroid.feature.designer.DesignerViewModel
import com.example.ficheroandroid.feature.designer.DesignerViewModelFactory
import com.example.ficheroandroid.ui.screens.MainScreen
import com.example.ficheroandroid.ui.theme.FicheroTheme

class MainActivity : ComponentActivity() {
    private val viewModel: DesignerViewModel by viewModels {
        DesignerViewModelFactory(AppContainer(applicationContext))
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            // ViewModel re-checks permissions on connect.
        }

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                runCatching {
                    contentResolver.openInputStream(uri)?.use { input ->
                        val bytes = input.readBytes()
                        if (bytes.isNotEmpty()) {
                            val encoded = Base64.encodeToString(bytes, Base64.DEFAULT)
                            viewModel.addImageObject(encoded)
                        }
                    }
                }.onFailure {
                    // ignored: UI already shows generic errors through ViewModel operations
                }
            }
        }

    private var pendingTemplateExportJson: String? = null

    private val exportTemplateLauncher =
        registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            val json = pendingTemplateExportJson
            if (uri != null && !json.isNullOrBlank()) {
                runCatching {
                    contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray())
                    }
                }.onFailure {
                    Toast.makeText(this, "Template export failed", Toast.LENGTH_SHORT).show()
                }
            }
            pendingTemplateExportJson = null
        }

    private val importTemplateLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                runCatching {
                    val raw = contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } ?: ""
                    val ok = viewModel.importTemplateJson(raw)
                    Toast.makeText(this, if (ok) "Template imported" else "Invalid template JSON", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(this, "Template import failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            FicheroTheme {
                MainScreen(
                    state = uiState,
                    onConnect = {
                        if (!viewModel.hasBluetoothPermissions()) requestBluetoothPermissions()
                        else viewModel.connect()
                    },
                    onDisconnect = viewModel::disconnect,
                    onSelectPrinter = viewModel::selectPrinter,
                    onRefreshPrinters = viewModel::refreshPairedPrinters,
                    onRefreshDiagnostics = viewModel::refreshDiagnostics,
                    onReconnect = viewModel::reconnectPrinter,
                    onToggleHeartbeat = viewModel::toggleHeartbeat,
                    onToggleDiagnosticsExpanded = viewModel::toggleDiagnosticsExpanded,
                    onPrint = viewModel::printLabel,
                    onSetLabelWidth = viewModel::updateLabelWidth,
                    onSetLabelHeight = viewModel::updateLabelHeight,
                    onSetDensity = viewModel::updateDensity,
                    onSetThreshold = viewModel::updateThreshold,
                    onSetPostProcess = viewModel::updatePostProcess,
                    onToggleInvert = viewModel::toggleInvert,
                    onSetLabelType = viewModel::updateLabelType,
                    onSetOffsetX = viewModel::updateOffsetX,
                    onSetOffsetY = viewModel::updateOffsetY,
                    onToggleOffsetType = viewModel::toggleOffsetType,
                    onCancelPrint = viewModel::cancelPrint,
                    onSetCopies = viewModel::updateCopies,
                    onSetCsvText = viewModel::updateCsvText,
                    onToggleCsv = viewModel::toggleCsvEnabled,
                    onSaveTemplate = viewModel::saveCurrentTemplate,
                    onLoadTemplate = viewModel::loadLastTemplate,
                    onSelectTemplate = viewModel::selectTemplate,
                    onLoadSelectedTemplate = viewModel::loadSelectedTemplate,
                    onExportTemplateJson = {
                        val json = viewModel.exportSelectedTemplateJson()
                        if (!json.isNullOrBlank()) {
                            pendingTemplateExportJson = json
                            exportTemplateLauncher.launch("fichero-template.json")
                        } else {
                            Toast.makeText(this, "No template to export", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onImportTemplateJson = {
                        importTemplateLauncher.launch(arrayOf("application/json", "text/plain"))
                    },
                    onClearCanvas = viewModel::clearCanvas,
                    onUndo = viewModel::undo,
                    onRedo = viewModel::redo,
                    onAddText = { viewModel.addObject(DesignObjectType.TEXT) },
                    onAddQr = { viewModel.addObject(DesignObjectType.QRCODE) },
                    onAddBarcode = { viewModel.addObject(DesignObjectType.BARCODE) },
                    onAddRect = { viewModel.addObject(DesignObjectType.RECTANGLE) },
                    onAddLine = { viewModel.addObject(DesignObjectType.LINE) },
                    onAddCircle = { viewModel.addObject(DesignObjectType.CIRCLE) },
                    onAddImage = { imagePickerLauncher.launch("image/*") },
                    onCloneSelected = viewModel::cloneSelectedObject,
                    onCenterSelectedH = viewModel::centerSelectedHorizontally,
                    onCenterSelectedV = viewModel::centerSelectedVertically,
                    onBringFront = viewModel::bringSelectedToFront,
                    onSendBack = viewModel::sendSelectedToBack,
                    onSelectObject = viewModel::selectObject,
                    onRemoveSelected = viewModel::removeSelectedObject,
                    onUpdateObjectText = viewModel::updateSelectedObjectText,
                    onUpdateObjectX = viewModel::updateSelectedObjectX,
                    onUpdateObjectY = viewModel::updateSelectedObjectY,
                    onUpdateObjectWidth = viewModel::updateSelectedObjectWidth,
                    onUpdateObjectHeight = viewModel::updateSelectedObjectHeight,
                    onUpdateObjectFontSize = viewModel::updateSelectedObjectFontSize,
                    onMoveSelectedBy = viewModel::moveSelectedObjectBy,
                    onTransformSelected = viewModel::transformSelectedObject,
                )
            }
        }
    }

    private fun requestBluetoothPermissions() {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions += Manifest.permission.BLUETOOTH_SCAN
            permissions += Manifest.permission.BLUETOOTH_CONNECT
        } else {
            permissions += Manifest.permission.BLUETOOTH
            permissions += Manifest.permission.BLUETOOTH_ADMIN
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }
}
