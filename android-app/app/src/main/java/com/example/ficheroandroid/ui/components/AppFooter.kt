package com.example.ficheroandroid.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ficheroandroid.ui.theme.FicheroColors

@Composable
fun AppFooter() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text("English", color = FicheroColors.InkSecondary, style = MaterialTheme.typography.labelMedium)
        Text("Built for Android", color = FicheroColors.InkSecondary, style = MaterialTheme.typography.labelMedium)
        Text("github.com/mohamedha/fichero-printer", color = FicheroColors.InkSecondary, style = MaterialTheme.typography.labelMedium)
    }
}
