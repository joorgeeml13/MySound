package com.jorge.mysound.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role.Companion.Checkbox
import androidx.compose.ui.unit.dp

@Composable
fun RememberMeCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onCheckedChange(!isChecked) } // Trucazo: Click en toda la fila
            .padding(vertical = 8.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null // Lo manejamos en el Row para mejor UX
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "Recuérdame, guapo", // O "Mantener sesión iniciada" si te pones serio
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}