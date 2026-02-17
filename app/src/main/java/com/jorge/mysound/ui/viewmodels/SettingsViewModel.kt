package com.jorge.mysound.ui.viewmodels

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import com.jorge.mysound.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SettingsViewModel: Gestiona las preferencias globales de la aplicación.
 * Controla el estado del tema visual (Modo Oscuro/Claro) y la localización (Idioma).
 * Utiliza StateFlow para una propagación de estados reactiva y eficiente hacia la UI.
 */
class SettingsViewModel(private val repository: MusicRepository) : ViewModel() {

    private val _currentLanguage = MutableStateFlow("es")
    val currentLanguage = _currentLanguage.asStateFlow()

    init {
        // 🔥 GIGA-CHAD MOVE: Comprobar qué idioma tiene el sistema guardado al iniciar
        val currentAppLocales = AppCompatDelegate.getApplicationLocales()
        val firstLocale = currentAppLocales.get(0)

        // Si hay un idioma guardado, actualizamos el estado visual
        if (firstLocale != null) {
            _currentLanguage.value = firstLocale.language
        }
    }

    // Estado de persistencia del tema visual. Por defecto se establece en modo oscuro.
    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode = _isDarkMode.asStateFlow()

    /**
     * Estado del idioma actual configurado en la aplicación.
     * Soporta códigos estándar ISO (ej: "es" para español, "en" para inglés).
     */

    /**
     * Alterna entre el modo oscuro y el modo claro.
     * Este cambio se refleja automáticamente en el componente MySoundTheme de la MainActivity.
     */
    fun toggleTheme() {
        _isDarkMode.value = !_isDarkMode.value
    }

    /**
     * Actualiza el idioma de la aplicación de forma dinámica.
     * Utiliza [AppCompatDelegate] para notificar al sistema operativo el cambio de locales,
     * lo que permite cargar los recursos (strings.xml) correspondientes sin reiniciar la actividad.
     * * @param lang Código del idioma seleccionado (ej: "es", "en").
     */
    fun setLanguage(lang: String) {
        if (_currentLanguage.value == lang) return // Evitamos procesamiento innecesario

        _currentLanguage.value = lang

        // Configuración de la lista de locales para la compatibilidad con el sistema
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(lang)

        /**
         * AppCompatDelegate gestiona la persistencia del idioma seleccionado
         * a nivel de configuración de la aplicación.
         */
        AppCompatDelegate.setApplicationLocales(appLocale)
    }
}