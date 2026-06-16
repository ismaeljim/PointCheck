package com.pointcheck.core.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Utilidad para la gestión de archivos físicos y compartición mediante FileProvider.
 */
object FileUtil {

    /**
     * Guarda un contenido de texto (ej. CSV) en un archivo temporal en la caché y devuelve su URI.
     * 
     * @param context Contexto de la aplicación.
     * @param fileName Nombre del archivo (ej. "reporte.csv").
     * @param content Contenido de texto a guardar.
     * @return Uri seguro generado por FileProvider para compartir externamente.
     */
    fun saveTextToCache(context: Context, fileName: String, content: String): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "reports")
            cachePath.mkdirs()
            val file = File(cachePath, fileName)
            val stream = FileOutputStream(file)
            stream.write(content.toByteArray())
            stream.close()

            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
