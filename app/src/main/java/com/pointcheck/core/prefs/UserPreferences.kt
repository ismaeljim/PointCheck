package com.pointcheck.core.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.pointcheck.core.security.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPrefsKeys {
    val TOKEN = stringPreferencesKey("auth_token")
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

class UserPreferences(private val context: Context) {
    private val securityManager = SecurityManager(context)

    suspend fun saveSession(
        token: String? = null,
        userId: String,
        name: String,
        email: String,
        role: String,
        phone: String,
        rut: String,
        address: String? = null
    ) {
        context.dataStore.edit { p ->
            token?.let { securityManager.saveSecureString(SecurityManager.KEY_AUTH_TOKEN, it) }
            p[UserPrefsKeys.USER_ID] = userId
            p[UserPrefsKeys.NAME] = name
            p[UserPrefsKeys.EMAIL] = email
            p[UserPrefsKeys.ROLE] = role
            p[UserPrefsKeys.PHONE] = phone
            securityManager.saveSecureString(SecurityManager.KEY_USER_RUT, rut)
            p[UserPrefsKeys.ADDRESS] = address ?: ""
            p[UserPrefsKeys.LOGGED] = true
        }
    }

    val token: Flow<String?> = flow {
        // Emitimos el valor actual del SecurityManager
        emit(securityManager.getSecureString(SecurityManager.KEY_AUTH_TOKEN))
    }

    // Agregamos un flow que observe cambios en el DataStore para el rol de forma más directa
    val userRole: Flow<String> = context.dataStore.data.map { it[UserPrefsKeys.ROLE] ?: "CLIENT" }
    val userId: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.USER_ID] }
    val name: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.NAME] }
    val email: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.EMAIL] }
    val role: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.ROLE] }
    val phone: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.PHONE] }
    val rut: Flow<String?> = flow {
        emit(securityManager.getSecureString(SecurityManager.KEY_USER_RUT))
    }
    val address: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.ADDRESS] }
    val isLogged: Flow<Boolean> = context.dataStore.data.map { it[UserPrefsKeys.LOGGED] ?: false }
    val professionalProfileId: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.PROFESSIONAL_PROFILE_ID] }
    val specialty: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.SPECIALTY] }

    suspend fun setAvatar(uri: String) {
        context.dataStore.edit { it[UserPrefsKeys.AVATAR_URI] = uri }
    }

    suspend fun saveProfessionalProfileId(id: String) {
        context.dataStore.edit { it[UserPrefsKeys.PROFESSIONAL_PROFILE_ID] = id }
    }

    suspend fun saveSpecialty(specialty: String) {
        context.dataStore.edit { it[UserPrefsKeys.SPECIALTY] = specialty }
    }

    suspend fun clear() {
        securityManager.clearSecureData()
        context.dataStore.edit { it.clear() }
    }
}
