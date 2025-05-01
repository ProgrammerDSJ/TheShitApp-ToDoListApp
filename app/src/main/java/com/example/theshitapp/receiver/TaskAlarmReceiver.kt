package com.example.theshitapp.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.theshitapp.MainActivity
import com.example.theshitapp.R
import com.example.theshitapp.TheShitApp
import com.example.theshitapp.ui.TaskDetailActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TASK_TITLE = "task_title"
        const val EXTRA_NOTIFICATION_MESSAGE = "notification_message"
        private const val CHANNEL_ID = "task_reminder_channel"
        private const val NOTIFICATION_ID = 1000
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task reminder"
        val notificationMessage = intent.getStringExtra(EXTRA_NOTIFICATION_MESSAGE) 
            ?: "Reminder for task: $taskTitle"
        
        // Create notification channel for Android 8.0+
        createNotificationChannel(context)
        
        // Intent to open task detail directly when notification is clicked
        val contentIntent = Intent(context, TaskDetailActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(TaskDetailActivity.EXTRA_TASK_ID, taskId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Get alarm sound
        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        
        // Build the notification
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Task Reminder")
            .setContentText(notificationMessage)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
        
        // Show the notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Use a unique notification ID for each reminder
        val notificationId = System.currentTimeMillis().toInt() + taskId.hashCode()
        notificationManager.notify(notificationId, notificationBuilder.build())
        
        // Also add the notification to in-app notifications
        addInAppNotification(context, taskId, notificationMessage)
    }
    
    private fun addInAppNotification(context: Context, taskId: String, message: String) {
        // In a real app, this would add the notification to a database
        // For this mock implementation, we'll just add it to preferences
        val sharedPrefs = context.getSharedPreferences("app_notifications", Context.MODE_PRIVATE)
        val notificationId = System.currentTimeMillis().toString()
        
        sharedPrefs.edit().putString(notificationId, "$taskId|$message").apply()
        
        // Send a broadcast to update any open notification screens
        val updateIntent = Intent("com.example.theshitapp.NEW_NOTIFICATION")
        context.sendBroadcast(updateIntent)
    }
    
    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Reminders"
            val descriptionText = "Notifications for task reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000, 1000, 1000)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
} 