package com.pointcheck.core.prefs

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.pointcheck.core.security.SecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

/**
 * Arquitectura de Persistencia Blindada (Sprint 4).
 * 
 * Implementa una Capa de Caché en RAM (Hot State) para mitigar condiciones de carrera
 * causadas por la latencia de desencriptación de SharedPreferences y DataStore.
 */
class UserPreferences(private val context: Context) {
    private val securityManager = SecurityManager(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    // --- RAM CACHE (Hot State) ---
    private var _cachedToken: String? = null
    private var _cachedUserId: String? = null
    private var _cachedRole: String? = null
    private var _cachedIsLogged: Boolean? = null
    private var _cachedName: String? = null
    private var _cachedEmail: String? = null
    private var _cachedPhone: String? = null
    private var _cachedAddress: String? = null
    
    private val _isInitialized = MutableStateFlow(false)
    /** Flow que indica si la caché de RAM ha sido sincronizada con el disco. */
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    init {
        // Precarga asíncrona pero prioritaria de la sesión
        scope.launch {
            warmUp()
        }
    }

    /**
     * Sincroniza los datos físicos del disco a la memoria RAM.
     * Debe llamarse al inicio de la aplicación para evitar "saltos" en la navegación.
     */
    suspend fun warmUp() = withContext(Dispatchers.IO) {
        if (_isInitialized.value) return@withContext
        
        _cachedToken = securityManager.getSecureString(SecurityManager.KEY_AUTH_TOKEN)
        val prefs = context.dataStore.data.first()
        _cachedUserId = prefs[UserPrefsKeys.USER_ID]
        _cachedRole = prefs[UserPrefsKeys.ROLE] ?: "CLIENT"
        _cachedIsLogged = prefs[UserPrefsKeys.LOGGED] ?: false
        _cachedName = prefs[UserPrefsKeys.NAME]
        _cachedEmail = prefs[UserPrefsKeys.EMAIL]
        _cachedPhone = prefs[UserPrefsKeys.PHONE]
        _cachedAddress = prefs[UserPrefsKeys.ADDRESS]
        
        _isInitialized.value = true
    }

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
        // Actualizamos caché RAM de inmediato (Optimistic UI)
        _cachedToken = token ?: _cachedToken
        _cachedUserId = userId
        _cachedRole = role
        _cachedIsLogged = true
        _cachedName = name
        _cachedEmail = email
        _cachedPhone = phone
        _cachedAddress = address

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

    /** Retorna el token desde RAM si está disponible, sino lo busca en disco. */
    fun getSyncToken(): String? {
        return _cachedToken ?: securityManager.getSecureString(SecurityManager.KEY_AUTH_TOKEN).also {
            _cachedToken = it
        }
    }

    val token: Flow<String?> = flow {
        emit(getSyncToken())
    }

    val isLogged: Flow<Boolean> = context.dataStore.data
        .map { it[UserPrefsKeys.LOGGED] ?: false }
        .onEach { _cachedIsLogged = it }
        .distinctUntilChanged()

    // Acceso directo a RAM para evitar parpadeos en AppNavigation e inicialización atómica de ViewModels
    val isLoggedCached: Boolean get() = _cachedIsLogged ?: false
    val cachedRole: String get() = _cachedRole ?: "" // Cambiado a vacío para detectar carga pendiente
    val cachedUserId: String? get() = _cachedUserId
    val cachedName: String? get() = _cachedName
    val cachedEmail: String? get() = _cachedEmail
    val cachedPhone: String? get() = _cachedPhone
    val cachedAddress: String? get() = _cachedAddress

    val userRole: Flow<String> = context.dataStore.data.map { it[UserPrefsKeys.ROLE] ?: "CLIENT" }.onEach { _cachedRole = it }
    val userId: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.USER_ID] }.onEach { _cachedUserId = it }
    
    val name: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.NAME] }
    val email: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.EMAIL] }
    val role: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.ROLE] }
    val phone: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.PHONE] }
    val rut: Flow<String?> = flow {
        emit(securityManager.getSecureString(SecurityManager.KEY_USER_RUT))
    }
    val address: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.ADDRESS] }
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

    suspend fun clearSession() {
        // Limpieza atómica en RAM y Disco
        _cachedToken = null
        _cachedUserId = null
        _cachedRole = null
        _cachedIsLogged = false

        securityManager.clearSecureData()
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun clear() {
        clearSession()
    }
}
