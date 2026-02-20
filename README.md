# 🎵 MySound - App Moderna de Streaming de Música

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=google-play&logoColor=white)
![Media3](https://img.shields.io/badge/Media3-ExoPlayer-red?style=for-the-badge)

**MySound** es una aplicación nativa de streaming de música para Android totalmente funcional, construida con **Kotlin** y **Jetpack Compose**. Implementa los estándares de arquitectura más modernos de Android para proporcionar una experiencia de audio fluida y resiliente, replicando características core de gigantes de la industria como Spotify.

Este proyecto demuestra el dominio de conceptos avanzados como servicios en primer plano (foreground services) para la reproducción multimedia, autenticación segura con JWT, tematización dinámica y soporte multilingüe en tiempo real.

---

## 🚀 Características Principales

### 🎧 Experiencia Core
* **Media3 & ExoPlayer:** Reproducción de audio robusta mediante un *Foreground Service* asociado a una `MediaSession`, garantizando que la música siga sonando aunque la app pase a segundo plano o se cierre la UI.
* **MiniPlayer Global:** Un reproductor flotante persistente, accesible desde cualquier pantalla de la aplicación sin interrumpir la navegación.
* **Reproductor a Pantalla Completa:** Interfaz interactiva y fluida con barra de progreso (seek bar), controles de reproducción y animaciones de transiciones de carátulas.

### 🔐 Seguridad y Usuario
* **Autenticación JWT:** Flujo de Login y Registro seguro con persistencia de tokens encriptados.
* **Gestión de Perfil:** Estadísticas de usuario, algoritmo de emparejamiento "Musical Soulmate" y subida de avatares mediante peticiones *Multipart*.
* **Networking Seguro:** Uso de interceptores en `Retrofit` para la inyección automática del token de sesión y manejo centralizado de códigos de error HTTP.

### 📂 Gestión de Contenido
* **Biblioteca:** Creación de playlists personalizadas, vista de detalles y gestión reactiva de colecciones.
* **Búsqueda:** Motor de búsqueda de canciones en tiempo real con funcionalidad de "Añadir a Playlist" directa desde los resultados.
* **UI Dinámica:** Carga de imágenes optimizada a través de `Coil`, implementando caché en memoria/disco y *placeholders* inteligentes.

### ⚙️ Ajustes y Accesibilidad
* **Internacionalización (i18n):** Soporte completo para **Inglés** y **Español**, con cambio de idioma *on-the-fly* (sin necesidad de reiniciar la app).
* **Motor de Temas:** Cambio dinámico entre **Modo Claro/Oscuro** cumpliendo estrictamente con las guías de Material Design 3.

---

## 🛠 Stack Tecnológico

* **Lenguaje:** [Kotlin](https://kotlinlang.org/)
* **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
* **Arquitectura:** UDF (Unidirectional Data Flow) + MVVM + Repository Pattern
* **Asincronía:** Coroutines & StateFlow / SharedFlow
* **Red:** [Retrofit 2](https://square.github.io/retrofit/) + OkHttp 3 + Gson
* **Imágenes:** [Coil](https://coil-kt.github.io/coil/)
* **Audio:** [AndroidX Media3](https://developer.android.com/media/media3) (ExoPlayer)
* **Navegación:** Jetpack Compose Navigation
* **Ciclo de vida:** ViewModel & Lifecycle-aware components

---

## 🏗️ Resumen de la Arquitectura

**MySound** sigue las directrices oficiales de Google para una arquitectura de aplicaciones moderna, escalable y testable. El proyecto prioriza la separación de responsabilidades (SoC) y un flujo de datos reactivo:

1. **Capa de UI (Compose):** Completamente declarativa. Funciona de forma unidireccional (UDF). Los `ViewModels` exponen el estado inmutable mediante `StateFlow` y la UI se limita a renderizar y emitir eventos de usuario.
2. **Capa de Datos:** Implementa el patrón Repositorio como única fuente de verdad (SSOT). Separa la procedencia de los datos, gestionando llamadas remotas (Retrofit) y delegando la persistencia y caché.
3. **Gestión de Media:** El reproductor está desacoplado de la UI. La comunicación se realiza mediante un controlador multimedia que se conecta al servicio en primer plano, evitando fugas de memoria (memory leaks) y crashes al rotar la pantalla o cambiar de app.
