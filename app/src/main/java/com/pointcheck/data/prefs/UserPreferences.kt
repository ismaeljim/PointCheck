package com.pointcheck.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPrefsKeys {
    val NAME = stringPreferencesKey("name")
    val EMAIL = stringPreferencesKey("email")
    val LOGGED = booleanPreferencesKey("logged")
    val AVATAR_URI = stringPreferencesKey("avatar_uri")
}

class UserPreferences(private val context: Context) {
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
    val name: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.NAME] }
    val email: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.EMAIL] }
    val avatar: Flow<String?> = context.dataStore.data.map { it[UserPrefsKeys.AVATAR_URI] }
    val isLogged: Flow<Boolean> = context.dataStore.data.map { it[UserPrefsKeys.LOGGED] ?: false }
    suspend fun clear() {
        context.dataStore.edit {
            it.remove(UserPrefsKeys.NAME)
            it.remove(UserPrefsKeys.EMAIL)
            it.remove(UserPrefsKeys.LOGGED)
            it.remove(UserPrefsKeys.AVATAR_URI)
        }
    }
}
