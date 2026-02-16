package com.jorge.mysound.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

// 🔥 ESTA FUNCIÓN ES ORO PURO. CÓPIALA Y GUÁRDALA.
fun uriToFile(context: Context, uri: Uri): File? {
    try {
        val contentResolver = context.contentResolver
        // 1. Creamos un archivo temporal en la caché de la app
        val tempFile = File.createTempFile("upload_image", ".jpg", context.cacheDir)

        // 2. Abrimos el grifo del sistema para leer la URI
        val inputStream = contentResolver.openInputStream(uri) ?: return null
        val outputStream = FileOutputStream(tempFile)

        // 3. Copiamos los datos byte a byte
        inputStream.copyTo(outputStream)

        // 4. Cerramos el grifo
        inputStream.close()
        outputStream.close()

        return tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}