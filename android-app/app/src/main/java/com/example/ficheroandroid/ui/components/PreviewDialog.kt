package com.example.ficheroandroid.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ficheroandroid.domain.model.OffsetType
import com.example.ficheroandroid.domain.model.PostProcessType
import com.example.ficheroandroid.feature.designer.DesignerUiState
import com.example.ficheroandroid.feature.designer.PrinterConnectionStatusUi

@Composable
fun PreviewDialog(
    state: DesignerUiState,
    onClose: () -> Unit,
    onSetCopies: (Int) -> Unit,
    onSetDensity: (Int) -> Unit,
    onSetThreshold: (Int) -> Unit,
    onSetPostProcess: (PostProcessType) -> Unit,
    onToggleInvert: () -> Unit,
    onSetLabelType: (Int) -> Unit,
    onSetOffsetX: (Int) -> Unit,
    onSetOffsetY: (Int) -> Unit,
    onToggleOffsetType: () -> Unit,
    onCancelPrint: () -> Unit,
    onPrint: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onClose,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        title = { Text("Print Preview", color = MaterialTheme.colorScheme.onSurface) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DesignerCanvas(
                    labelWidthPx = state.labelWidthPx,
                    labelHeightPx = state.labelHeightPx,
                    objects = state.objects,
                    selectedObjectId = state.selectedObjectId,
                    onSelectObject = {},
                    onMoveSelectedBy = { _, _ -> },
                    onTransformSelected = { _, _, _, _ -> },
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Copies", color = MaterialTheme.colorScheme.onSurface)
                    Text(state.copies.toString(), color = MaterialTheme.colorScheme.onSurface)
                }
                Slider(value = state.copies.toFloat(), onValueChange = { onSetCopies(it.toInt()) }, valueRange = 1f..10f, steps = 8)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Density", color = MaterialTheme.colorScheme.onSurface)
                    Text(state.density.toString(), color = MaterialTheme.colorScheme.onSurface)
                }
                Slider(value = state.density.toFloat(), onValueChange = { onSetDensity(it.toInt()) }, valueRange = 1f..20f, steps = 18)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Threshold", color = MaterialTheme.colorScheme.onSurface)
                    Text(state.threshold.toString(), color = MaterialTheme.colorScheme.onSurface)
                }
                Slider(value = state.threshold.toFloat(), onValueChange = { onSetThreshold(it.toInt()) }, valueRange = 1f..255f, steps = 253)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { onSetPostProcess(PostProcessType.THRESHOLD) }) { Text("Threshold") }
                    OutlinedButton(onClick = { onSetPostProcess(PostProcessType.DITHER) }) { Text("Dither") }
                    OutlinedButton(onClick = { onSetPostProcess(PostProcessType.BAYER) }) { Text("Bayer") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onToggleInvert) {
                        Icon(Icons.Default.InvertColors, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(if (state.invert) "Invert ON" else "Invert OFF")
                    }
                    OutlinedButton(onClick = { onSetLabelType(1) }) { Text("LabelType 1") }
                    OutlinedButton(onClick = { onSetLabelType(2) }) { Text("LabelType 2") }
                    OutlinedButton(onClick = { onSetLabelType(3) }) { Text("LabelType 3") }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = state.offsetX.toString(),
                        onValueChange = { it.toIntOrNull()?.let(onSetOffsetX) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Offset X") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = state.offsetY.toString(),
                        onValueChange = { it.toIntOrNull()?.let(onSetOffsetY) },
                        modifier = Modifier.weight(1f),
                        label = { Text("Offset Y") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                OutlinedButton(onClick = onToggleOffsetType, modifier = Modifier.fillMaxWidth()) {
                    Text(if (state.offsetType == OffsetType.INNER) "Offset: INNER" else "Offset: OUTER")
                }
                if (state.isPrinting) {
                    Text("Printing... ${state.printProgress}%")
                    LinearProgressIndicator(progress = { state.printProgress / 100f }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(onClick = onPrint, enabled = state.status == PrinterConnectionStatusUi.CONNECTED && !state.isPrinting) {
                Icon(Icons.Default.Print, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Print")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (state.isPrinting) {
                    OutlinedButton(onClick = onCancelPrint) { Text("Cancel") }
                }
                OutlinedButton(onClick = onClose) { Text("Close") }
            }
        },
    )
}
