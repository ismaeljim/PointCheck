package com.pointcheck.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Gestor de seguridad encargado de manejar el almacenamiento encriptado.
 * Implementa las recomendaciones de ISO 27001 para la protección de PII 
 * (Personally Identifiable Information).
 */
class SecurityManager(context: Context) {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_user_data",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Guarda un valor de forma segura.
     */
    fun saveSecureString(key: String, value: String) {
        encryptedPrefs.edit { putString(key, value) }
    }

    /**
     * Recupera un valor encriptado.
     */
    fun getSecureString(key: String): String? {
        return encryptedPrefs.getString(key, null)
    }

    /**
     * Limpia los datos sensibles.
     */
    fun clearSecureData() {
        encryptedPrefs.edit { clear() }
    }

    companion object {
        const val KEY_AUTH_TOKEN = "auth_token"
        const val KEY_USER_RUT = "user_rut"
    }
}
