package com.jorge.mysound.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityOptionsCompat
import com.jorge.mysound.MainActivity
import com.jorge.mysound.R
import com.jorge.mysound.ui.screens.LoginScreen
import com.jorge.mysound.ui.theme.MySoundTheme

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MySoundTheme{
                LoginScreen(
                    onLoginClick = { user, pass ->
                        // Lógica de inicio de sesión
                        val intent = Intent(this, MainActivity::class.java)

                        val options = ActivityOptionsCompat.makeCustomAnimation(
                            this,
                            R.anim.slide_in_right,
                            R.anim.fade_out
                        )

                        startActivity(intent)
                        finish()
                    },
                    onRegisterClick = {
                        // Lógica para navegar a la pantalla de registro
                        val intent = Intent(this, RegisterActivity::class.java)

                        val options = ActivityOptionsCompat.makeCustomAnimation(
                            this,
                            R.anim.slide_in_right,
                            R.anim.fade_out
                        )

                        startActivity(intent)
                    }
                )
            }
        }
    }
}