package com.jorge.mysound.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityOptionsCompat
import com.jorge.mysound.R
import com.jorge.mysound.ui.screens.RegisterScreen
import com.jorge.mysound.ui.theme.MySoundTheme

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MySoundTheme {
                RegisterScreen(
                    onRegisterClick = { user, email, birthDate, pass ->
                        // Lógica de registro
                    },
                    onBackToLogin = {
                        val intent = Intent(this, LoginActivity::class.java)

                        val options = ActivityOptionsCompat.makeCustomAnimation(
                            this,
                            R.anim.slide_in_right,
                            R.anim.fade_out
                        )

                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}