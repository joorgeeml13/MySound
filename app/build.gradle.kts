plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.jorge.mysound"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jorge.mysound"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        // Subimos a Java 17, que para SDK 36 es el estándar actual
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    // --- EL FIX PARA EL ERROR DE META-INF ---
    packaging {
        resources {
            // 1. Los sospechosos habituales (Netty, Google, Apache)
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/ASL2.0"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"

            // 2. EL FIX PARA MOZILLA (El que te está saltando ahora)
            excludes += "mozilla/public-suffix-list.txt"

            // 3. Reglas de "Supervivencia"
            // Si hay duplicados en estos sitios, quédate con el primero y no molestes
            pickFirsts += "META-INF/INDEX.LIST"
            pickFirsts += "META-INF/io.netty.versions.properties"
            pickFirsts += "mozilla/public-suffix-list.txt"

            // 4. Limpieza de firmas de Kotlin
            excludes += "META-INF/*.kotlin_module"
        }
    }
}

dependencies {
    // Base de Android y Compose
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.animation)

    // Navegación (He quitado la runtime vieja y dejado la de Compose)
    implementation("androidx.navigation:navigation-compose:2.9.7")

    // Firebase (Ojo con estas, son las que daban el error de META-INF)
    implementation(libs.firebase.appdistribution.gradle)
    implementation(libs.firebase.crashlytics.buildtools)

    // Media3 (Versión unificada)
    val media3_version = "1.2.1"
    implementation("androidx.media3:media3-exoplayer:$media3_version")
    implementation("androidx.media3:media3-session:$media3_version")
    implementation("androidx.media3:media3-ui:$media3_version")

    // Palette para extraer colores
    implementation("androidx.palette:palette-ktx:1.0.0")

    // Network (Retrofit + OkHttp)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Coil (Limpiado: solo una versión, la 2.6.0 que es más moderna)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // ViewModel y Seguridad
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}