package com.example.ficheroandroid.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BorderAll
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LayersClear
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ficheroandroid.domain.model.DesignObject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ObjectToolbar(
    selected: DesignObject?,
    onRemoveSelected: () -> Unit,
    onCloneSelected: () -> Unit,
    onAddBarcode: () -> Unit,
    onAddRect: () -> Unit,
    onAddLine: () -> Unit,
    onAddCircle: () -> Unit,
    onAddImage: () -> Unit,
    onCenterH: () -> Unit,
    onCenterV: () -> Unit,
    onBringFront: () -> Unit,
    onSendBack: () -> Unit,
    onUpdateObjectText: (String) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IconButton(onClick = onAddBarcode) {
            Icon(Icons.Default.Add, contentDescription = "Add barcode")
        }
        IconButton(onClick = onAddRect) {
            Icon(Icons.Default.BorderAll, contentDescription = "Add rectangle")
        }
        IconButton(onClick = onAddLine) {
            Icon(Icons.Default.HorizontalRule, contentDescription = "Add line")
        }
        IconButton(onClick = onAddCircle) {
            Icon(Icons.Default.RadioButtonUnchecked, contentDescription = "Add circle")
        }
        IconButton(onClick = onAddImage) {
            Icon(Icons.Default.Image, contentDescription = "Add image")
        }
        if (selected != null) {
            IconButton(onClick = onCloneSelected) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Clone")
            }
            IconButton(onClick = onRemoveSelected) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
            IconButton(onClick = onCenterH) {
                Icon(Icons.Default.UnfoldMore, contentDescription = "Center horizontally")
            }
            IconButton(onClick = onCenterV) {
                Icon(Icons.Default.UnfoldLess, contentDescription = "Center vertically")
            }
            IconButton(onClick = onBringFront) {
                Icon(Icons.Default.Layers, contentDescription = "Bring front")
            }
            IconButton(onClick = onSendBack) {
                Icon(Icons.Default.LayersClear, contentDescription = "Send back")
            }
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = selected.text,
                onValueChange = onUpdateObjectText,
                singleLine = true,
                label = { Text("Selected object value") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            )
        }
    }
}
