package com.example.ficheroandroid.domain.usecase

import com.example.ficheroandroid.domain.model.DesignObject
import com.example.ficheroandroid.domain.model.OffsetType
import com.example.ficheroandroid.domain.model.PostProcessType
import com.example.ficheroandroid.domain.repository.PrinterRepository
import com.example.ficheroandroid.domain.service.LabelRenderer

class PrintDesignUseCase(
    private val printerRepository: PrinterRepository,
    private val labelRenderer: LabelRenderer,
) {
    data class Params(
        val labelWidthPx: Int,
        val labelHeightPx: Int,
        val objects: List<DesignObject>,
        val density: Int,
        val copies: Int,
        val labelType: Int,
        val csvEnabled: Boolean,
        val csvText: String,
        val postProcess: PostProcessType,
        val threshold: Int,
        val invert: Boolean,
        val offsetX: Int,
        val offsetY: Int,
        val offsetType: OffsetType,
        val onProgress: (Int) -> Unit = {},
        val shouldStop: () -> Boolean = { false },
    )

    operator fun invoke(params: Params) {
        val rows = if (params.csvEnabled) parseCsvRows(params.csvText) else listOf(emptyMap())
        val total = (rows.size * params.copies.coerceAtLeast(1)).coerceAtLeast(1)
        var done = 0
        for (rowVars in rows) {
            if (params.shouldStop()) return
            val encoded = labelRenderer.render(
                labelWidthPx = params.labelWidthPx,
                labelHeightPx = params.labelHeightPx,
                objects = params.objects,
                variables = rowVars,
                postProcess = params.postProcess,
                threshold = params.threshold,
                invert = params.invert,
                offsetX = params.offsetX,
                offsetY = params.offsetY,
                offsetType = params.offsetType,
            )
            repeat(params.copies.coerceAtLeast(1)) {
                if (params.shouldStop()) return
                printerRepository.printLabel(
                    encodedLabel = encoded,
                    density = params.density,
                    labelType = params.labelType,
                    copies = 1,
                )
                done++
                params.onProgress(((done * 100f) / total).toInt().coerceIn(0, 100))
            }
        }
    }

    private fun parseCsvRows(raw: String): List<Map<String, String>> {
        val lines = raw.lines().map { it.trim() }.filter { it.isNotEmpty() }
        if (lines.size < 2) return listOf(emptyMap())
        val headers = lines.first().split(",").map { it.trim() }
        val rows = mutableListOf<Map<String, String>>()
        for (line in lines.drop(1)) {
            val values = line.split(",").map { it.trim().replace("\\n", "\n") }
            val row = headers.mapIndexed { idx, h -> h to (values.getOrNull(idx) ?: "") }.toMap()
            val times = row["\$times"]?.toIntOrNull()?.coerceAtLeast(0) ?: 1
            repeat(times) {
                rows += row
            }
        }
        return if (rows.isEmpty()) listOf(emptyMap()) else rows
    }
}

