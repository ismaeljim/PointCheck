package com.pointcheck.core.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPrefsKeys {
    val USER_ID = stringPreferencesKey("user_id")
    val NAME = stringPreferencesKey("name")
    val EMAIL = stringPreferencesKey("email")
    val ROLE = stringPreferencesKey("role")
    val PHONE = stringPreferencesKey("phone")
    val RUT = stringPreferencesKey("rut")
    val LOGGED = booleanPreferencesKey("logged")
    val AVATAR_URI = stringPreferencesKey("avatar_uri")
    val PROFESSIONAL_PROFILE_ID = stringPreferencesKey("professional_profile_id")
    val SUBSCRIPTION_STATUS = stringPreferencesKey("subscription_status")
}

class UserPreferences(private val context: Context) {

    /**
     * Guarda la sesión completa del usuario obtenida del backend.
     */
    suspend fun saveSession(
        userId: String,
        name: String,
        email: String,
        role: String,
        phone: String,
        rut: String
    ) {
        context.dataStore.edit { p ->
            p[UserPrefsKeys.USER_ID] = userId
            p[UserPrefsKeys.NAME] = name
            p[UserPrefsKeys.EMAIL] = email
            p[UserPrefsKeys.ROLE] = role
            p[UserPrefsKeys.PHONE] = phone
            p[UserPrefsKeys.RUT] = rut
            p[UserPrefsKeys.LOGGED] = true
        }
    }

    /**
     * Método legacy para compatibilidad. Se recomienda usar saveSession.
     */
    suspend fun saveUser(name: String, email: String) {
        context.dataStore.edit { p ->
            p[UserPrefsKeys.NAME] = name
            p[UserPrefsKeys.EMAIL] = email
            p[UserPrefsKeys.LOGGED] = true
        }
    }

    suspend fun saveName(name: String) {
        context.dataStore.edit { p ->
            p[UserPrefsKeys.NAME] = name
            p[UserPrefsKeys.LOGGED] = true
        }
    }

    suspend fun saveEmail(email: String) {
        context.dataStore.edit { p ->
            p[UserPrefsKeys.EMAIL] = email
            p[UserPrefsKeys.LOGGED] = true
        }
    }

    suspend fun setAvatar(uri: String) {
        context.dataStore.edit { it[UserPrefsKeys.AVATAR_URI] = uri }
    }

    suspend fun saveProfessionalProfileId(id: String) {
        context.dataStore.edit { it[UserPrefsKeys.PROFESSIONAL_PROFILE_ID] = id }
    }

    suspend fun saveSubscriptionStatus(status: String) {
        context.dataStore.edit { it[UserPrefsKeys.SUBSCRIPTION_STATUS] = status }
    }

    // --- Flows para observar datos de sesión ---

    val userId: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.USER_ID] }
    val name: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.NAME] }
    val email: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.EMAIL] }
    val role: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.ROLE] }
    val phone: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.PHONE] }
    val rut: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.RUT] }
    val avatar: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.AVATAR_URI] }
    val professionalProfileId: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.PROFESSIONAL_PROFILE_ID] }
    val subscriptionStatus: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.SUBSCRIPTION_STATUS] }
    val isLogged: Flow<Boolean> = context.dataStore.data.map { it[UserPrefsKeys.LOGGED] ?: false }

    /**
     * Limpia toda la sesión y preferencias del usuario.
     */
    suspend fun clear() {
        context.dataStore.edit {
            it.clear()
        }
    }
}
