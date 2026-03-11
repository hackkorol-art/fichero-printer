package com.example.ficheroandroid.domain.service

import com.example.ficheroandroid.domain.model.DesignObject
import com.example.ficheroandroid.domain.model.DesignObjectType
import com.example.ficheroandroid.domain.model.OffsetType
import com.example.ficheroandroid.domain.model.PostProcessType
import org.junit.Test
import org.junit.Assert.*

class LabelRendererTest {

    @Test
    fun `render produces correct raster dimensions for 30x15mm label`() {
        val renderer = LabelRenderer()
        val objects = listOf(
            DesignObject(
                id = 1L,
                type = DesignObjectType.TEXT,
                text = "Test",
                x = 8,
                y = 8,
                width = 100,
                height = 28,
                fontSizePx = 24f,
            )
        )

        val result = renderer.render(
            labelWidthPx = 240,
            labelHeightPx = 120,
            objects = objects,
            variables = emptyMap(),
            postProcess = PostProcessType.THRESHOLD,
            threshold = 140,
            invert = false,
            offsetX = 0,
            offsetY = 0,
            offsetType = OffsetType.INNER,
        )

        assertEquals("bytesPerRow must be 12", 12, result.bytesPerRow)
        assertEquals("rows must match height after 90deg rotation (width)", 240, result.rows)
        assertEquals("rowsData length must be rows * bytesPerRow", 240 * 12, result.rowsData.size)
    }

    @Test
    fun `render produces correct raster dimensions for 40x12mm label`() {
        val renderer = LabelRenderer()
        val objects = listOf(
            DesignObject(
                id = 1L,
                type = DesignObjectType.TEXT,
                text = "Test",
                x = 8,
                y = 8,
                width = 100,
                height = 28,
                fontSizePx = 24f,
            )
        )

        val result = renderer.render(
            labelWidthPx = 320,
            labelHeightPx = 96,
            objects = objects,
            variables = emptyMap(),
            postProcess = PostProcessType.THRESHOLD,
            threshold = 140,
            invert = false,
            offsetX = 0,
            offsetY = 0,
            offsetType = OffsetType.INNER,
        )

        assertEquals("bytesPerRow must be 12", 12, result.bytesPerRow)
        assertEquals("rows must match height after 90deg rotation (width)", 320, result.rows)
        assertEquals("rowsData length must be rows * bytesPerRow", 320 * 12, result.rowsData.size)
    }

    @Test
    fun `render produces correct raster dimensions for 29x12mm label`() {
        val renderer = LabelRenderer()
        val objects = listOf(
            DesignObject(
                id = 1L,
                type = DesignObjectType.TEXT,
                text = "Test",
                x = 8,
                y = 8,
                width = 100,
                height = 28,
                fontSizePx = 24f,
            )
        )

        val result = renderer.render(
            labelWidthPx = 232,
            labelHeightPx = 96,
            objects = objects,
            variables = emptyMap(),
            postProcess = PostProcessType.THRESHOLD,
            threshold = 140,
            invert = false,
            offsetX = 0,
            offsetY = 0,
            offsetType = OffsetType.INNER,
        )

        assertEquals("bytesPerRow must be 12", 12, result.bytesPerRow)
        assertEquals("rows must match height after 90deg rotation (width)", 232, result.rows)
        assertEquals("rowsData length must be rows * bytesPerRow", 232 * 12, result.rowsData.size)
    }
}
