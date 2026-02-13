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

@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit) {

    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

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
            label = { Text(text = stringResource(id = R.string.user_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true

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
                    painterResource(id = R.drawable.ic_visibility_on) // Necesitarás estos iconos
                else
                    painterResource(id = R.drawable.ic_visibility_off)

                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(painter = image, contentDescription = description)
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { onLoginClick(user, pass) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(stringResource(R.string.btn_login), color = Color.White)
        }

        TextButton(onClick = onRegisterClick) {
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

@Preview(showBackground = true, name = "Modo Día")
@Composable
fun LoginScreenPreview() {
    MySoundTheme(darkTheme = false) {
        LoginScreen(onLoginClick = { _, _ -> }, onRegisterClick = {})
    }
}

@Preview(showBackground = true, name = "Modo Noche")
@Composable
fun LoginScreenDarkPreview() {
    MySoundTheme(darkTheme = true) {
        LoginScreen(onLoginClick = { _, _ -> }, onRegisterClick = {})
    }
}

@Preview(showBackground = true, locale = "es", name = "Preview Inglés")
@Composable
fun LoginEnglishPreview() {
    MySoundTheme {
        LoginScreen(onLoginClick = { _, _ -> }, onRegisterClick = {})
    }
}

@Preview(showBackground = true, locale = "es", name = "Preview Inglés")
@Composable
fun LoginEnglishPreviewBlack() {
    MySoundTheme (darkTheme = true){
        LoginScreen(onLoginClick = { _, _ -> }, onRegisterClick = {})
    }
}