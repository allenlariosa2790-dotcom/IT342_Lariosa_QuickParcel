package com.quickparcel.app.shared.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "quickparcel_app")

private object Keys {
    val TOKEN = stringPreferencesKey("token")
    val USER_DATA = stringPreferencesKey("user_data")
}

class TokenManager(private val context: Context) {

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.TOKEN] = token
        }
    }

    suspend fun saveUserData(userData: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_DATA] = userData
        }
    }

    fun getTokenFlow(): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.TOKEN]
        }
    }

    // Make this suspend instead of blocking
    suspend fun getToken(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.TOKEN]
        }.first()
    }

    suspend fun getUserData(): String? {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.USER_DATA]
        }.first()
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}