package com.jorge.mysound.ui.screens.auth

import android.util.Log
import androidx.compose.foundation.background
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    // User, Email, BirthDate, Pass
    onRegisterClick: (String, String, String, String) -> Unit,
    onBackToLogin: () -> Unit,
    errorMessage: String? = null
) {
    var user by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var confPass by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var passVisible by remember { mutableStateOf(false) }

    // Estado para el Selector de Fecha
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // No permitimos fechas futuras
                return utcTimeMillis <= System.currentTimeMillis()
            }
            override fun isSelectableYear(year: Int): Boolean {
                return year <= 2026
            }
        }
    )
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val date = datePickerState.selectedDateMillis?.let {
                        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        formatter.format(Date(it))
                    } ?: ""
                    birthDate = date
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
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
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo Fecha de Nacimiento
        OutlinedTextField(
            value = birthDate,
            onValueChange = { },
            label = { Text(stringResource(R.string.birth_date_label)) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true, // IMPORTANTE: Solo lectura
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(painterResource(R.drawable.ic_calendar), contentDescription = null)
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Contraseñas
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

        // Checkbox de Términos (ESTO SÍ ES NECESARIO)
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

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error, // Rojo de Material3
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp),
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Botón Registrarse
        Button(
            onClick = {
                Log.d("DEBUG_AUTH", "Botón pulsado. User: $user, Email: $email, Date: $birthDate, PassMatch: ${pass == confPass}, Terms: $termsAccepted")
                if (user.isEmpty() || email.isEmpty() || birthDate.isEmpty()) {
                    Log.e("DEBUG_AUTH", "Error: Hay campos vacíos")
                    // Aquí deberías mostrar un Toast o un error en la UI
                } else if (pass != confPass) {
                    Log.e("DEBUG_AUTH", "Error: Las contraseñas no coinciden")
                } else if (!termsAccepted) {
                    Log.e("DEBUG_AUTH", "Error: Términos no aceptados")
                } else {
                    Log.d("DEBUG_AUTH", "¡Todo OK! Lanzando petición...")
                    onRegisterClick(email, pass, birthDate, user)
                }
            },
            enabled = termsAccepted,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(stringResource(R.string.btn_register_now), color = Color.White)
        }

        TextButton(onClick = onBackToLogin) {
            Text(
                stringResource(R.string.login_prompt),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                stringResource(R.string.login_action),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// --- PREVIEWS (Arregladas) ---
@Preview(showBackground = true, name = "Modo Día")
@Composable
fun RegisterScreenPreview() {
    MySoundTheme(darkTheme = false) {
        RegisterScreen(onRegisterClick = { _, _, _, _ -> }, onBackToLogin = {})
    }
}

@Preview(showBackground = true, name = "Modo Noche")
@Composable
fun RegisterScreenDarkPreview() {
    MySoundTheme(darkTheme = true) {
        RegisterScreen(onRegisterClick = { _, _, _, _ -> }, onBackToLogin = {})
    }
}