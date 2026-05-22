package com.wayfarer.app.utils

import android.content.Context
import com.google.firebase.auth.FirebaseAuth

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("wayfarer_prefs", Context.MODE_PRIVATE)

    fun isLoggedIn(): Boolean = FirebaseAuth.getInstance().currentUser != null

    fun saveDarkMode(isDark: Boolean) = prefs.edit().putBoolean("dark_mode", isDark).apply()

    fun isDarkMode(): Boolean = prefs.getBoolean("dark_mode", false)
}
