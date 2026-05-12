package com.wayfarer.app

import android.app.Application
import com.wayfarer.app.data.api.RetrofitClient
import com.wayfarer.app.utils.SessionManager

class WayFarerApp : Application() {

    lateinit var sessionManager: SessionManager

    override fun onCreate() {
        super.onCreate()
        sessionManager = SessionManager(this)
        RetrofitClient.init(sessionManager)

        if (sessionManager.isDarkMode()) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
