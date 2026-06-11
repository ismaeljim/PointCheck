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
    val ADDRESS = stringPreferencesKey("address")
    val LOGGED = booleanPreferencesKey("logged")
    val AVATAR_URI = stringPreferencesKey("avatar_uri")
    val PROFESSIONAL_PROFILE_ID = stringPreferencesKey("professional_profile_id")
    val SUBSCRIPTION_STATUS = stringPreferencesKey("subscription_status")
    val SPECIALTY = stringPreferencesKey("specialty")
}

/**
 * Gestión de preferencias de usuario mediante DataStore.
 * Almacena de forma persistente y segura (en el contexto de la App) los datos de sesión,
 * roles y el estado de la suscripción.
 * 
 * AUDITORÍA:
 * - Se utiliza DataStore en lugar de SharedPreferences para mayor robustez y soporte de Flows.
 * - Centraliza todas las claves de preferencia en el objeto UserPrefsKeys.
 */
class UserPreferences(private val context: Context) {

    /**
     * Guarda la sesión completa del usuario obtenida del backend tras un login o registro exitoso.
     *
     * @param userId Identificador único del usuario (UUID).
     * @param name Nombre completo del usuario.
     * @param email Correo electrónico.
     * @param role Rol asignado (ej: "CLIENT", "SPECIALIST").
     * @param phone Teléfono de contacto.
     * @param rut RUT del usuario.
     */
    suspend fun saveSession(
        userId: String,
        name: String,
        email: String,
        role: String,
        phone: String,
        rut: String,
        address: String? = null
    ) {
        context.dataStore.edit { p ->
            p[UserPrefsKeys.USER_ID] = userId
            p[UserPrefsKeys.NAME] = name
            p[UserPrefsKeys.EMAIL] = email
            p[UserPrefsKeys.ROLE] = role
            p[UserPrefsKeys.PHONE] = phone
            p[UserPrefsKeys.RUT] = rut
            p[UserPrefsKeys.ADDRESS] = address ?: ""
            p[UserPrefsKeys.LOGGED] = true
        }
    }

    /**
     * Guarda la URI del avatar seleccionado por el usuario.
     *
     * @param uri Cadena que representa la URI del archivo de imagen.
     */
    suspend fun setAvatar(uri: String) {
        context.dataStore.edit { it[UserPrefsKeys.AVATAR_URI] = uri }
    }

    /**
     * Almacena el ID del perfil profesional una vez que el usuario se registra como tal.
     *
     * @param id Identificador único del perfil profesional (UUID).
     */
    suspend fun saveProfessionalProfileId(id: String) {
        context.dataStore.edit { it[UserPrefsKeys.PROFESSIONAL_PROFILE_ID] = id }
    }

    /**
     * Actualiza el estado de la suscripción del usuario en las preferencias locales.
     *
     * @param status Nuevo estado (ej: "ACTIVE", "CANCELLED").
     */
    suspend fun saveSubscriptionStatus(status: String) {
        context.dataStore.edit { it[UserPrefsKeys.SUBSCRIPTION_STATUS] = status }
    }

    /**
     * Guarda la especialidad del profesional para personalización rápida.
     */
    suspend fun saveSpecialty(specialty: String) {
        context.dataStore.edit { it[UserPrefsKeys.SPECIALTY] = specialty }
    }

    // --- Flows para observar datos de sesión ---

    val userId: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.USER_ID] }
    val name: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.NAME] }
    val email: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.EMAIL] }
    val role: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.ROLE] }
    val phone: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.PHONE] }
    val rut: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.RUT] }
    val address: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.ADDRESS] }
    val avatar: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.AVATAR_URI] }
    val professionalProfileId: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.PROFESSIONAL_PROFILE_ID] }
    val subscriptionStatus: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.SUBSCRIPTION_STATUS] }
    val specialty: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.SPECIALTY] }
    val isLogged: Flow<Boolean> = context.dataStore.data.map { it[UserPrefsKeys.LOGGED] ?: false }

    /**
     * Limpia toda la sesión y preferencias del usuario (utilizado en Logout).
     */
    suspend fun clear() {
        context.dataStore.edit {
            it.clear()
        }
    }
}
