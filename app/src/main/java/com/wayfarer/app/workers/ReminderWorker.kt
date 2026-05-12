package com.wayfarer.app.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.wayfarer.app.utils.NotificationHelper

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Reminder"
        val message = inputData.getString("message") ?: "Your tour is coming up!"

        val notificationHelper = NotificationHelper(applicationContext)
        notificationHelper.showBookingNotification(title, message)

        return Result.success()
    }
}
