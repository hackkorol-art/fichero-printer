package com.example.ficheroandroid.di

import android.content.Context
import com.example.ficheroandroid.data.printer.AndroidFicheroDataSource
import com.example.ficheroandroid.data.repository.PrinterRepositoryImpl
import com.example.ficheroandroid.data.repository.TemplateRepositoryImpl
import com.example.ficheroandroid.data.templates.LocalTemplateDataSource
import com.example.ficheroandroid.domain.service.LabelRenderer
import com.example.ficheroandroid.domain.usecase.ConnectPrinterUseCase
import com.example.ficheroandroid.domain.usecase.ConnectPrinterByNameUseCase
import com.example.ficheroandroid.domain.usecase.DisconnectPrinterUseCase
import com.example.ficheroandroid.domain.usecase.ExportTemplateJsonUseCase
import com.example.ficheroandroid.domain.usecase.EnsurePrinterConnectedUseCase
import com.example.ficheroandroid.domain.usecase.GetPrinterDiagnosticsUseCase
import com.example.ficheroandroid.domain.usecase.HasBluetoothPermissionsUseCase
import com.example.ficheroandroid.domain.usecase.ImportTemplateJsonUseCase
import com.example.ficheroandroid.domain.usecase.ListPairedPrintersUseCase
import com.example.ficheroandroid.domain.usecase.LoadLastTemplateUseCase
import com.example.ficheroandroid.domain.usecase.LoadTemplatesUseCase
import com.example.ficheroandroid.domain.usecase.PrintDesignUseCase
import com.example.ficheroandroid.domain.usecase.SaveTemplateUseCase

class AppContainer(context: Context) {
    private val printerDataSource = AndroidFicheroDataSource(context)
    private val templateDataSource = LocalTemplateDataSource(context)

    private val printerRepository = PrinterRepositoryImpl(context, printerDataSource)
    private val templateRepository = TemplateRepositoryImpl(templateDataSource)

    val hasBluetoothPermissionsUseCase = HasBluetoothPermissionsUseCase(printerRepository)
    val connectPrinterUseCase = ConnectPrinterUseCase(printerRepository)
    val connectPrinterByNameUseCase = ConnectPrinterByNameUseCase(printerRepository)
    val listPairedPrintersUseCase = ListPairedPrintersUseCase(printerRepository)
    val disconnectPrinterUseCase = DisconnectPrinterUseCase(printerRepository)
    val ensurePrinterConnectedUseCase = EnsurePrinterConnectedUseCase(printerRepository)
    val getPrinterDiagnosticsUseCase = GetPrinterDiagnosticsUseCase(printerRepository)
    val printDesignUseCase = PrintDesignUseCase(printerRepository, LabelRenderer())
    val saveTemplateUseCase = SaveTemplateUseCase(templateRepository)
    val loadLastTemplateUseCase = LoadLastTemplateUseCase(templateRepository)
    val loadTemplatesUseCase = LoadTemplatesUseCase(templateRepository)
    val exportTemplateJsonUseCase = ExportTemplateJsonUseCase(templateRepository)
    val importTemplateJsonUseCase = ImportTemplateJsonUseCase(templateRepository)
}

