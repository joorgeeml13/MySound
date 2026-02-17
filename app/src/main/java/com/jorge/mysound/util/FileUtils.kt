package com.jorge.mysound.util

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * FileUtils: Utilidades para la manipulación de archivos y flujos de datos.
 * Proporciona métodos para convertir URIs de contenido en archivos temporales
 * compatibles con peticiones Multipart/form-data.
 */

/**
 * Convierte una [Uri] de contenido (proporcionada por el sistema) en un [File] físico.
 * Este proceso es necesario para poder enviar imágenes o archivos al servidor a través de Retrofit.
 * * @param context El contexto de la aplicación para acceder al ContentResolver.
 * @param uri La URI del archivo seleccionado (ej. desde la galería).
 * @return Un objeto [File] temporal que contiene la copia del archivo, o null si ocurre un error.
 */
fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val contentResolver = context.contentResolver

        // Creamos un archivo temporal en el directorio de caché para no persistir basura innecesaria
        val tempFile = File.createTempFile("upload_tmp_", ".jpg", context.cacheDir)

        // Abrimos el flujo de entrada desde la URI y el de salida hacia el archivo temporal
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(tempFile)

        if (inputStream == null) return null

        /**
         * Utilizamos la función .use de Kotlin, que garantiza el cierre de los streams
         * incluso si ocurre una excepción durante la copia, evitando fugas de memoria.
         */
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }

        tempFile
    } catch (e: Exception) {
        Log.e("FileUtils", "Error al convertir URI a File: ${e.message}")
        null
    }
}