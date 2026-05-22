package com.wayfarer.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.wayfarer.app.utils.SessionManager

class WayFarerApp : Application() {

    lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        // Firebase initializes automatically from google-services.json

        if (sessionManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
