package com.example.ficheroandroid.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import com.example.ficheroandroid.domain.model.DesignObject
import com.example.ficheroandroid.domain.model.DesignObjectType
import com.example.ficheroandroid.ui.theme.FicheroColors
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import kotlin.math.min

private fun renderQrBitmap(text: String, sizePx: Int): Bitmap? = runCatching {
    val content = text.ifBlank { " " }
    val s = sizePx.coerceAtLeast(16)
    val matrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, s, s)
    val bmp = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
    for (y in 0 until matrix.height) {
        for (x in 0 until matrix.width) {
            bmp.setPixel(x, y, if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    bmp
}.getOrNull()

private fun renderBarcodeBitmap(text: String, w: Int, h: Int): Bitmap? = runCatching {
    val content = text.filter { it.code in 32..126 }.ifBlank { "12345678" }.take(48)
    val matrix = MultiFormatWriter().encode(content, BarcodeFormat.CODE_128, w.coerceAtLeast(24), h.coerceAtLeast(16))
    val bmp = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
    for (y in 0 until matrix.height) {
        for (x in 0 until matrix.width) {
            bmp.setPixel(x, y, if (matrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    bmp
}.getOrNull()

private enum class DragMode { NONE, MOVE, RESIZE_TL, RESIZE_TR, RESIZE_BL, RESIZE_BR }

private const val HANDLE_RADIUS = 14f

@Composable
fun DesignerCanvas(
    labelWidthPx: Int,
    labelHeightPx: Int,
    objects: List<DesignObject>,
    selectedObjectId: Long,
    onSelectObject: (Long) -> Unit,
    onMoveSelectedBy: (Int, Int) -> Unit,
    onTransformSelected: (Int, Int, Int, Int) -> Unit,
) {
    val curObjects by rememberUpdatedState(objects)
    val curSelectedId by rememberUpdatedState(selectedObjectId)
    val curLabelW by rememberUpdatedState(labelWidthPx)
    val curLabelH by rememberUpdatedState(labelHeightPx)
    val curOnSelect by rememberUpdatedState(onSelectObject)
    val curOnMove by rememberUpdatedState(onMoveSelectedBy)
    val curOnTransform by rememberUpdatedState(onTransformSelected)

    val ratio = labelWidthPx.toFloat() / labelHeightPx.toFloat()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, FicheroColors.BorderStandard)
            .background(FicheroColors.Surface1)
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        val maxW = maxWidth
        val maxH = maxW / ratio
        val canvasWidth = if (maxH > 220.dp) 220.dp * ratio else maxW
        val canvasHeight = if (maxH > 220.dp) 220.dp else maxH

        Canvas(
            modifier = Modifier
                .width(canvasWidth)
                .height(canvasHeight)
                .border(1.dp, FicheroColors.MarkFeed)
                .background(Color.White)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()

                        val scaleX = size.width.toFloat() / curLabelW.toFloat()
                        val scaleY = size.height.toFloat() / curLabelH.toFloat()

                        val sel = curObjects.firstOrNull { it.id == curSelectedId }
                        var dragMode = DragMode.NONE

                        if (sel != null) {
                            val sx = sel.x * scaleX
                            val sy = sel.y * scaleY
                            val sw = sel.width * scaleX
                            val sh = sel.height * scaleY
                            val hr = HANDLE_RADIUS
                            val px = down.position.x
                            val py = down.position.y

                            dragMode = when {
                                (px - sx) * (px - sx) + (py - sy) * (py - sy) < hr * hr * 4 -> DragMode.RESIZE_TL
                                (px - (sx + sw)) * (px - (sx + sw)) + (py - sy) * (py - sy) < hr * hr * 4 -> DragMode.RESIZE_TR
                                (px - sx) * (px - sx) + (py - (sy + sh)) * (py - (sy + sh)) < hr * hr * 4 -> DragMode.RESIZE_BL
                                (px - (sx + sw)) * (px - (sx + sw)) + (py - (sy + sh)) * (py - (sy + sh)) < hr * hr * 4 -> DragMode.RESIZE_BR
                                px in sx..(sx + sw) && py in sy..(sy + sh) -> DragMode.MOVE
                                else -> DragMode.NONE
                            }
                        }

                        if (dragMode == DragMode.NONE) {
                            val hit = curObjects.lastOrNull { obj ->
                                val ox = obj.x * scaleX; val oy = obj.y * scaleY
                                val ow = obj.width * scaleX; val oh = obj.height * scaleY
                                down.position.x in ox..(ox + ow) && down.position.y in oy..(oy + oh)
                            }
                            if (hit != null) {
                                curOnSelect(hit.id)
                                dragMode = DragMode.MOVE
                            }
                        }

                        var remX = 0f; var remY = 0f

                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed && dragMode != DragMode.NONE) {
                                    val delta = change.positionChange()
                                    change.consume()
                                    remX += delta.x / scaleX
                                    remY += delta.y / scaleY
                                    val dx = remX.toInt()
                                    val dy = remY.toInt()
                                    if (dx != 0 || dy != 0) {
                                        when (dragMode) {
                                            DragMode.MOVE -> curOnMove(dx, dy)
                                            DragMode.RESIZE_BR -> curOnTransform(0, 0, dx, dy)
                                            DragMode.RESIZE_BL -> curOnTransform(dx, 0, -dx, dy)
                                            DragMode.RESIZE_TR -> curOnTransform(0, dy, dx, -dy)
                                            DragMode.RESIZE_TL -> curOnTransform(dx, dy, -dx, -dy)
                                            else -> {}
                                        }
                                        remX -= dx; remY -= dy
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })
                    }
                },
        ) {
            val scaleX = size.width / labelWidthPx.toFloat()
            val scaleY = size.height / labelHeightPx.toFloat()
            val fontScale = min(scaleX, scaleY)

            objects.forEach { obj ->
                val x = obj.x * scaleX
                val y = obj.y * scaleY
                val w = obj.width * scaleX
                val h = obj.height * scaleY

                when (obj.type) {
                    DesignObjectType.TEXT -> {
                        drawContext.canvas.nativeCanvas.drawText(
                            obj.text, x, y + (obj.fontSizePx * fontScale),
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                textSize = obj.fontSizePx * fontScale
                                isAntiAlias = true
                            },
                        )
                    }

                    DesignObjectType.QRCODE -> {
                        val qrSize = minOf(obj.width, obj.height)
                        val bmp = renderQrBitmap(obj.text, qrSize)
                        if (bmp != null) {
                            drawContext.canvas.nativeCanvas.drawBitmap(
                                bmp, null,
                                android.graphics.Rect(x.toInt(), y.toInt(), (x + w).toInt(), (y + h).toInt()),
                                null,
                            )
                            bmp.recycle()
                        } else {
                            drawRect(Color.Black, Offset(x, y), Size(w, h),
                                style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))))
                        }
                    }

                    DesignObjectType.BARCODE -> {
                        val bmp = renderBarcodeBitmap(obj.text, obj.width, obj.height)
                        if (bmp != null) {
                            drawContext.canvas.nativeCanvas.drawBitmap(
                                bmp, null,
                                android.graphics.Rect(x.toInt(), y.toInt(), (x + w).toInt(), (y + h).toInt()),
                                null,
                            )
                            bmp.recycle()
                        } else {
                            drawRect(Color.Black, Offset(x, y), Size(w, h),
                                style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))))
                        }
                    }

                    DesignObjectType.RECTANGLE -> {
                        drawRect(Color.Black, Offset(x, y), Size(w, h),
                            style = Stroke(width = (obj.strokeWidth * fontScale).coerceAtLeast(1f)))
                    }

                    DesignObjectType.LINE -> {
                        drawLine(Color.Black, Offset(x, y), Offset(x + w, y + h),
                            strokeWidth = (obj.strokeWidth * fontScale).coerceAtLeast(1f))
                    }

                    DesignObjectType.CIRCLE -> {
                        drawCircle(Color.Black, minOf(w, h) / 2f,
                            center = Offset(x + w / 2f, y + h / 2f),
                            style = Stroke(width = (obj.strokeWidth * fontScale).coerceAtLeast(1f)))
                    }

                    DesignObjectType.IMAGE -> {
                        val encoded = obj.imageBase64
                        if (!encoded.isNullOrBlank()) {
                            val bytes = runCatching { Base64.decode(encoded, Base64.DEFAULT) }.getOrNull()
                            val bmp = bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                            if (bmp != null) {
                                drawContext.canvas.nativeCanvas.drawBitmap(
                                    bmp, null,
                                    android.graphics.Rect(x.toInt(), y.toInt(), (x + w).toInt(), (y + h).toInt()),
                                    null,
                                )
                                bmp.recycle()
                            } else {
                                drawRect(Color.Black, Offset(x, y), Size(w, h),
                                    style = Stroke(1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f))))
                            }
                        }
                    }
                }

                if (obj.id == selectedObjectId) {
                    drawRect(FicheroColors.Fichero, Offset(x, y), Size(w, h), style = Stroke(width = 2f))

                    val hr = HANDLE_RADIUS
                    val handleColor = FicheroColors.Fichero
                    val corners = listOf(
                        Offset(x, y), Offset(x + w, y),
                        Offset(x, y + h), Offset(x + w, y + h),
                    )
                    corners.forEach { c ->
                        drawRect(Color.White, Offset(c.x - hr / 2, c.y - hr / 2), Size(hr, hr))
                        drawRect(handleColor, Offset(c.x - hr / 2, c.y - hr / 2), Size(hr, hr),
                            style = Stroke(width = 2f))
                    }
                }
            }
        }
    }
}
