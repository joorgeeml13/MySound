package com.jorge.mysound.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.jorge.mysound.R

/**
 * RememberMeCheckbox: Componente de control para la persistencia de credenciales.
 * Optimiza la experiencia de usuario (UX) al permitir la interacción en toda el área del contenedor
 * y cumple con los estándares de accesibilidad mediante el uso de roles semánticos.
 */
@Composable
fun RememberMeCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    /**
     * Implementación de la fila como un elemento conmutable (toggleable).
     * Esto mejora la usabilidad al ampliar el target de pulsación (Touch Target)
     * a toda la línea de texto, no solo al recuadro del Checkbox.
     */
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .toggleable(
                value = isChecked,
                role = Role.Checkbox,
                onValueChange = { onCheckedChange(it) }
            )
            .padding(vertical = 8.dp)
    ) {
        // El checkbox visual es puramente representativo, el estado lo gestiona el Row
        Checkbox(
            checked = isChecked,
            onCheckedChange = null // Delegamos la lógica al modificador toggleable del Row
        )

        Spacer(modifier = Modifier.width(8.dp))



        // Etiqueta de texto vinculada al estado de la sesión
        Text(
            text = stringResource(R.string.auth_remember_me),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}