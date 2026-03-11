package com.example.ficheroandroid.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CancelPresentation
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TableView
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TopToolbar(
    connected: Boolean,
    csvEnabled: Boolean,
    canUndo: Boolean,
    canRedo: Boolean,
    onSaveTemplate: () -> Unit,
    onLoadTemplate: () -> Unit,
    onClear: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onToggleCsv: () -> Unit,
    onAddText: () -> Unit,
    onAddQr: () -> Unit,
    onPreview: () -> Unit,
    onPrint: () -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(onClick = onClear) {
            Icon(Icons.Default.CancelPresentation, contentDescription = null)
        }
        OutlinedButton(onClick = onSaveTemplate) {
            Icon(Icons.Default.Save, contentDescription = null)
        }
        OutlinedButton(onClick = onLoadTemplate) {
            Icon(Icons.Default.FolderOpen, contentDescription = null)
        }
        OutlinedButton(onClick = onUndo, enabled = canUndo) {
            Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = null)
        }
        OutlinedButton(onClick = onRedo, enabled = canRedo) {
            Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = null)
        }
        AssistChip(
            onClick = onToggleCsv,
            label = { Text(if (csvEnabled) "CSV ON" else "CSV OFF") },
            leadingIcon = { Icon(Icons.Default.TableView, contentDescription = null) },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = if (csvEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                labelColor = if (csvEnabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            ),
        )
        OutlinedButton(onClick = onAddText) {
            Icon(Icons.Default.TextFields, contentDescription = null)
        }
        OutlinedButton(onClick = onAddQr) {
            Icon(Icons.Default.QrCode2, contentDescription = null)
        }
        Button(onClick = onPreview) {
            Icon(Icons.Default.Visibility, contentDescription = null)
            Text("Preview", modifier = Modifier.padding(start = 4.dp))
        }
        Button(onClick = onPrint, enabled = connected) {
            Icon(Icons.Default.Print, contentDescription = null)
            Text("Print", modifier = Modifier.padding(start = 4.dp))
        }
    }
}
