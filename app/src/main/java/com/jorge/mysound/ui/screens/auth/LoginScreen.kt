package com.jorge.mysound.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jorge.mysound.R
import com.jorge.mysound.ui.theme.MySoundTheme

@Composable
fun LoginScreen(
    // AHORA RECIBE EL BOOLEAN DEL CHECKBOX
    onLoginClick: (String, String, Boolean) -> Unit,
    onRegisterClick: () -> Unit,
    errorMessage: String? = null
) {

    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // ESTADO DEL CHECKBOX
    var rememberMe by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Icon(
            painter = painterResource(id = R.drawable.ic_mysound_logo),
            contentDescription = "MySound Logo",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            stringResource(R.string.app_name),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = user,
            onValueChange = { user = it },
            label = { Text(text = stringResource(id = R.string.email_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text(stringResource(R.string.pass_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if(passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible)
                    painterResource(id = R.drawable.ic_visibility_on)
                else
                    painterResource(id = R.drawable.ic_visibility_off)

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(painter = image, contentDescription = if (passwordVisible) "Hide" else "Show")
                }
            }
        )

        // --- AQUÍ VA EL CHECKBOX ---
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            RememberMeCheckbox(
                isChecked = rememberMe,
                onCheckedChange = { rememberMe = it }
            )
        }
        // ---------------------------

        Spacer(modifier = Modifier.height(24.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error, // Rojo de Material3
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp),
                fontWeight = FontWeight.Bold
            )
        }


        Button(
            // PASAMOS LOS 3 ARGUMENTOS AL CLICK
            onClick = { onLoginClick(user, pass, rememberMe) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.btn_login), color = Color.White)
        }

        TextButton(onClick = onRegisterClick) {
            Text(
                stringResource(R.string.register_prompt),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                stringResource(R.string.register_action),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// --- EL COMPONENTE VISUAL ---
@Composable
fun RememberMeCheckbox(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onCheckedChange(!isChecked) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = null, // null porque lo maneja el Row clickable
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Recordar sesión", // Puedes usar stringResource(R.string.remember_me)
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// --- PREVIEWS (Arregladas para que compilen con el nuevo parámetro) ---
@Preview(showBackground = true, name = "Modo Día")
@Composable
fun LoginScreenPreview() {
    MySoundTheme(darkTheme = false) {
        LoginScreen(onLoginClick = { _, _, _ -> }, onRegisterClick = {})
    }
}

@Preview(showBackground = true, name = "Modo Noche")
@Composable
fun LoginScreenDarkPreview() {
    MySoundTheme(darkTheme = true) {
        LoginScreen(onLoginClick = { _, _, _ -> }, onRegisterClick = {})
    }
}