package com.example.ficheroandroid.domain.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.TextPaint
import com.example.ficheroandroid.domain.model.DesignObject
import com.example.ficheroandroid.domain.model.DesignObjectType
import com.example.ficheroandroid.domain.model.EncodedLabel
import com.example.ficheroandroid.domain.model.OffsetType
import com.example.ficheroandroid.domain.model.PostProcessType
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import android.util.Base64
import kotlin.math.abs
import kotlin.math.max

class LabelRenderer {
    fun render(
        labelWidthPx: Int,
        labelHeightPx: Int,
        objects: List<DesignObject>,
        variables: Map<String, String> = emptyMap(),
        postProcess: PostProcessType = PostProcessType.THRESHOLD,
        threshold: Int = 140,
        invert: Boolean = false,
        offsetX: Int = 0,
        offsetY: Int = 0,
        offsetType: OffsetType = OffsetType.INNER,
    ): EncodedLabel {
        val widthPx = (labelWidthPx.coerceAtLeast(8) / 8) * 8
        val heightPx = (labelHeightPx.coerceAtLeast(8) / 8) * 8
        val baseBitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(baseBitmap)
        canvas.drawColor(Color.WHITE)

        objects.forEach { obj ->
            when (obj.type) {
                DesignObjectType.TEXT -> drawText(canvas, obj, variables)
                DesignObjectType.QRCODE -> drawQr(baseBitmap, obj, variables)
                DesignObjectType.BARCODE -> drawBarcode(baseBitmap, obj, variables)
                DesignObjectType.RECTANGLE -> drawRectangle(canvas, obj)
                DesignObjectType.LINE -> drawLine(canvas, obj)
                DesignObjectType.CIRCLE -> drawCircle(canvas, obj)
                DesignObjectType.IMAGE -> drawImage(canvas, obj)
            }
        }

        val processed = applyPostProcess(baseBitmap, postProcess, threshold.coerceIn(1, 255), invert)
        val withOffset = applyOffset(processed, offsetX, offsetY, offsetType)
        val rotated = rotateCW90(withOffset)
        return bitmapToEncoded(rotated)
    }

    private fun drawText(canvas: Canvas, obj: DesignObject, vars: Map<String, String>) {
        val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = obj.fontSizePx
            textAlign = Paint.Align.LEFT
        }
        val text = resolveVars(obj.text, vars)
        val yBase = obj.y + obj.fontSizePx
        canvas.drawText(text, obj.x.toFloat(), yBase, paint)
    }

    private fun drawQr(bitmap: Bitmap, obj: DesignObject, vars: Map<String, String>) {
        val size = minOf(obj.width, obj.height).coerceAtLeast(16)
        val text = resolveVars(obj.text, vars).ifBlank { " " }
        val matrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        drawMatrix(bitmap, matrix, obj.x, obj.y)
    }

    private fun drawBarcode(bitmap: Bitmap, obj: DesignObject, vars: Map<String, String>) {
        val w = obj.width.coerceAtLeast(24)
        val h = obj.height.coerceAtLeast(24)
        val text = resolveVars(obj.text, vars).filter { it.code in 32..126 }.ifBlank { "12345678" }
        val matrix = MultiFormatWriter().encode(
            text.take(48),
            BarcodeFormat.CODE_128,
            w,
            (h * 0.7f).toInt().coerceAtLeast(12),
        )
        drawMatrix(bitmap, matrix, obj.x, obj.y)
    }

    private fun drawRectangle(canvas: Canvas, obj: DesignObject) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = obj.strokeWidth.toFloat()
        }
        val r = Rect(obj.x, obj.y, obj.x + obj.width, obj.y + obj.height)
        canvas.drawRect(r, p)
    }

    private fun drawLine(canvas: Canvas, obj: DesignObject) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = obj.strokeWidth.toFloat()
        }
        canvas.drawLine(
            obj.x.toFloat(),
            obj.y.toFloat(),
            (obj.x + obj.width).toFloat(),
            (obj.y + obj.height).toFloat(),
            p,
        )
    }

    private fun drawCircle(canvas: Canvas, obj: DesignObject) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = obj.strokeWidth.toFloat()
        }
        val radius = minOf(obj.width, obj.height) / 2f
        canvas.drawCircle(obj.x + radius, obj.y + radius, radius, p)
    }

    private fun drawImage(canvas: Canvas, obj: DesignObject) {
        val encoded = obj.imageBase64 ?: return
        val decoded = runCatching { Base64.decode(encoded, Base64.DEFAULT) }.getOrNull() ?: return
        val bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.size) ?: return
        val dst = Rect(obj.x, obj.y, obj.x + obj.width, obj.y + obj.height)
        canvas.drawBitmap(bmp, null, dst, null)
        bmp.recycle()
    }

    private fun resolveVars(input: String, vars: Map<String, String>): String {
        var out = input
        vars.forEach { (k, v) -> out = out.replace("{$k}", v) }
        return out
    }

    private fun applyPostProcess(
        source: Bitmap,
        postProcess: PostProcessType,
        threshold: Int,
        invert: Boolean,
    ): Bitmap {
        val out = source.copy(Bitmap.Config.ARGB_8888, true)
        val w = out.width
        val h = out.height
        val pixels = IntArray(w * h)
        out.getPixels(pixels, 0, w, 0, 0, w, h)

        when (postProcess) {
            PostProcessType.THRESHOLD -> {
                for (i in pixels.indices) {
                    val l = luminance(pixels[i])
                    val isBlack = l < threshold
                    pixels[i] = toMono(isBlack xor invert)
                }
            }

            PostProcessType.BAYER -> {
                val m = arrayOf(
                    intArrayOf(0, 8, 2, 10),
                    intArrayOf(12, 4, 14, 6),
                    intArrayOf(3, 11, 1, 9),
                    intArrayOf(15, 7, 13, 5),
                )
                for (y in 0 until h) {
                    for (x in 0 until w) {
                        val idx = y * w + x
                        val t = (m[y % 4][x % 4] * 16) + (threshold - 128)
                        val isBlack = luminance(pixels[idx]) < t.coerceIn(1, 255)
                        pixels[idx] = toMono(isBlack xor invert)
                    }
                }
            }

            PostProcessType.DITHER -> {
                val gray = IntArray(pixels.size) { luminance(pixels[it]).toInt() }
                fun diffuse(x: Int, y: Int, err: Int, factor: Int) {
                    if (x !in 0 until w || y !in 0 until h) return
                    val idx = y * w + x
                    gray[idx] = (gray[idx] + (err * factor) / 8).coerceIn(0, 255)
                }
                for (y in 0 until h) {
                    for (x in 0 until w) {
                        val idx = y * w + x
                        val old = gray[idx]
                        val black = old < threshold
                        val newValue = if (black) 0 else 255
                        gray[idx] = newValue
                        val err = old - newValue
                        diffuse(x + 1, y, err, 1)
                        diffuse(x + 2, y, err, 1)
                        diffuse(x - 1, y + 1, err, 1)
                        diffuse(x, y + 1, err, 1)
                        diffuse(x + 1, y + 1, err, 1)
                        diffuse(x, y + 2, err, 1)
                    }
                }
                for (i in pixels.indices) {
                    val isBlack = gray[i] < 128
                    pixels[i] = toMono(isBlack xor invert)
                }
            }
        }

        out.setPixels(pixels, 0, w, 0, 0, w, h)
        source.recycle()
        return out
    }

    private fun applyOffset(source: Bitmap, offsetX: Int, offsetY: Int, offsetType: OffsetType): Bitmap {
        if (offsetX == 0 && offsetY == 0) return source

        return if (offsetType == OffsetType.INNER) {
            val out = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(source, offsetX.toFloat(), offsetY.toFloat(), null)
            source.recycle()
            out
        } else {
            val outW = max(8, source.width + abs(offsetX))
            val outH = max(8, source.height + abs(offsetY))
            val out = Bitmap.createBitmap((outW / 8) * 8, outH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(out)
            canvas.drawColor(Color.WHITE)
            val x = max(0, offsetX)
            val y = max(0, offsetY)
            canvas.drawBitmap(source, x.toFloat(), y.toFloat(), null)
            source.recycle()
            out
        }
    }

    private fun luminance(pixel: Int): Double {
        return 0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel)
    }

    private fun toMono(black: Boolean): Int = if (black) Color.BLACK else Color.WHITE

    private fun rotateCW90(source: Bitmap): Bitmap {
        val w = source.height
        val h = source.width
        val rotated = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(rotated)
        canvas.translate(w.toFloat(), 0f)
        canvas.rotate(90f)
        canvas.drawBitmap(source, 0f, 0f, null)
        source.recycle()
        return rotated
    }

    private fun drawMatrix(bitmap: Bitmap, matrix: BitMatrix, xOffset: Int, yOffset: Int) {
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                val px = x + xOffset
                val py = y + yOffset
                if (px !in 0 until bitmap.width || py !in 0 until bitmap.height) continue
                bitmap.setPixel(px, py, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }
    }

    private fun bitmapToEncoded(bitmap: Bitmap): EncodedLabel {
        val heightPx = bitmap.height
        val rowsData = ByteArray(heightPx * BYTES_PER_ROW)

        for (yPx in 0 until heightPx) {
            for (xByte in 0 until BYTES_PER_ROW) {
                var b = 0
                for (bit in 0 until 8) {
                    val xPx = xByte * 8 + bit
                    if (xPx < bitmap.width) {
                        val pixel = bitmap.getPixel(xPx, yPx)
                        val luminance = (0.299 * Color.red(pixel) + 0.587 * Color.green(pixel) + 0.114 * Color.blue(pixel))
                        if (luminance < 128) b = b or (1 shl (7 - bit))
                    }
                }
                rowsData[yPx * BYTES_PER_ROW + xByte] = b.toByte()
            }
        }
        bitmap.recycle()
        return EncodedLabel(bytesPerRow = BYTES_PER_ROW, rows = heightPx, rowsData = rowsData)
    }

    companion object {
        const val BYTES_PER_ROW = 12
        const val PRINTHEAD_PX = 96
    }
}

