package com.jorge.mysound.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jorge.mysound.R
import com.jorge.mysound.ui.theme.MySoundTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String, String) -> Unit,
    onBackToLogin: () -> Unit
) {
    var user by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confPass by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var passVisible by remember { mutableStateOf(false) }

    // Estado para el Selector de Fecha
    val datePickerState = rememberDatePickerState()
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis?.let {
                        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        formatter.format(Date(it))
                    } ?: ""
                    birthDate = date
                    showDatePicker = false
                }) { Text("OK") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()

            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_mysound_logo),
            contentDescription = "MySound Logo",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.reg_title),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Campo Usuario
        OutlinedTextField(
            value = user,
            onValueChange = { user = it },
            label = { Text(stringResource(R.string.user_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo Correo
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo Fecha de Nacimiento (Solo lectura, abre el diálogo)
        OutlinedTextField(
            value = birthDate,
            onValueChange = { },
            label = { Text(stringResource(R.string.birth_date_label)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(painterResource(R.drawable.ic_calendar), contentDescription = null)
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Contraseñas (igual que antes...)
        OutlinedTextField(
            value = pass,
            onValueChange = { pass = it },
            label = { Text(stringResource(R.string.pass_label)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passVisible) painterResource(R.drawable.ic_visibility_on)
                else painterResource(R.drawable.ic_visibility_off)
                IconButton(onClick = { passVisible = !passVisible }) {
                    Icon(painter = image, contentDescription = null)
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confPass,
            onValueChange = { confPass = it },
            label = { Text(stringResource(R.string.conf_pass_label)) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Checkbox de Términos y Condiciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Text(
                text = stringResource(R.string.terms_check),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Registrarse con validación
        Button(
            onClick = {
                if (user.isNotEmpty() && email.isNotEmpty() && birthDate.isNotEmpty() &&
                    pass == confPass && termsAccepted) {
                    onRegisterClick(user, email, birthDate, pass)
                }
            },
            enabled = termsAccepted, // El botón se bloquea si no acepta términos
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(stringResource(R.string.btn_register_now), color = Color.White)
        }

        TextButton(onClick = onBackToLogin) {
            Text(stringResource(R.string.already_account), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview(showBackground = true, name = "Modo Día")
@Composable
fun RegisterScreenPreview() {
    MySoundTheme(darkTheme = false) {
        RegisterScreen(onRegisterClick = { _, _, _, _-> }, onBackToLogin = {})
    }
}

@Preview(showBackground = true, name = "Modo Noche")
@Composable
fun RegisterScreenDarkPreview() {
    MySoundTheme(darkTheme = true) {
        RegisterScreen(onRegisterClick = { _, _, _, _ -> }, onBackToLogin = {})
    }
}

@Preview(showBackground = true, locale = "es", name = "Preview Inglés")
@Composable
fun RegisterEnglishPreview() {
    MySoundTheme {
        RegisterScreen(onRegisterClick = { _, _, _, _ -> }, onBackToLogin = {})
    }
}

@Preview(showBackground = true, locale = "es", name = "Preview Inglés")
@Composable
fun RegisterEnglishPreviewBlack() {
    MySoundTheme (darkTheme = true){
        RegisterScreen(onRegisterClick = { _, _, _, _ -> }, onBackToLogin = {})
    }
}