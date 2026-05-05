package com.wayfarer.app.utils

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.wayfarer.app.data.models.User

class SessionManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        "wayfarer_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER = "user_json"
    }

    fun saveToken(token: String) = prefs.edit { putString(KEY_TOKEN, token) }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUser(user: User) = prefs.edit { putString(KEY_USER, Gson().toJson(user)) }

    fun getUser(): User? {
        val json = prefs.getString(KEY_USER, null) ?: return null
        return try { Gson().fromJson(json, User::class.java) } catch (_: Exception) { null }
    }

    fun isLoggedIn(): Boolean = !getToken().isNullOrEmpty()

    fun clearSession() = prefs.edit { clear() }
}
