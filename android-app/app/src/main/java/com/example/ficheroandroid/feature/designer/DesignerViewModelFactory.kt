package com.example.ficheroandroid.feature.designer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ficheroandroid.di.AppContainer

class DesignerViewModelFactory(
    private val appContainer: AppContainer,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DesignerViewModel(
            hasBluetoothPermissionsUseCase = appContainer.hasBluetoothPermissionsUseCase,
            connectPrinterUseCase = appContainer.connectPrinterUseCase,
            connectPrinterByNameUseCase = appContainer.connectPrinterByNameUseCase,
            listPairedPrintersUseCase = appContainer.listPairedPrintersUseCase,
            disconnectPrinterUseCase = appContainer.disconnectPrinterUseCase,
            ensurePrinterConnectedUseCase = appContainer.ensurePrinterConnectedUseCase,
            getPrinterDiagnosticsUseCase = appContainer.getPrinterDiagnosticsUseCase,
            printDesignUseCase = appContainer.printDesignUseCase,
            saveTemplateUseCase = appContainer.saveTemplateUseCase,
            loadLastTemplateUseCase = appContainer.loadLastTemplateUseCase,
            loadTemplatesUseCase = appContainer.loadTemplatesUseCase,
            exportTemplateJsonUseCase = appContainer.exportTemplateJsonUseCase,
            importTemplateJsonUseCase = appContainer.importTemplateJsonUseCase,
        ) as T
    }
}

