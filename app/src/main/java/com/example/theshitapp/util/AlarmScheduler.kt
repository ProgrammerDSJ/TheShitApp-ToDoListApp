package com.example.theshitapp.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.theshitapp.model.Task
import com.example.theshitapp.receiver.TaskAlarmReceiver
import java.util.Calendar
import java.util.Date

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedules an alarm for a task at its due time
     */
    fun scheduleAlarm(task: Task) {
        if (task.dueTime == null || !task.reminderSet) {
            return
        }

        val calendar = Calendar.getInstance()
        calendar.time = task.dueDate
        
        // Set the time from dueTime (which is in milliseconds since epoch)
        val timeCalendar = Calendar.getInstance()
        timeCalendar.timeInMillis = task.dueTime
        
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
        calendar.set(Calendar.SECOND, 0)
        
        scheduleAlarmAt(task, calendar.timeInMillis, "Task is starting now: ${task.title}")
    }
    
    /**
     * Schedule a reminder some minutes before the task's due time
     */
    fun scheduleTaskReminder(task: Task, minutesBefore: Long, message: String) {
        if (task.dueTime == null) {
            return
        }
        
        val calendar = Calendar.getInstance()
        calendar.time = task.dueDate
        
        // Set the time from dueTime
        val timeCalendar = Calendar.getInstance()
        timeCalendar.timeInMillis = task.dueTime
        
        calendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
        calendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
        calendar.set(Calendar.SECOND, 0)
        
        // Subtract minutesBefore from the time
        calendar.add(Calendar.MINUTE, -minutesBefore.toInt())
        
        // Generate a unique request code for each reminder
        val requestCode = "${task.id}_${minutesBefore}".hashCode()
        
        scheduleAlarmAt(task, calendar.timeInMillis, message, requestCode)
    }
    
    /**
     * Schedule an alarm at a specific time
     */
    private fun scheduleAlarmAt(task: Task, timeInMillis: Long, message: String, requestCode: Int = task.id.hashCode()) {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskAlarmReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(TaskAlarmReceiver.EXTRA_NOTIFICATION_MESSAGE, message)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        }
    }
    
    fun cancelAlarm(task: Task) {
        val intent = Intent(context, TaskAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.cancel(pendingIntent)
    }
    
    fun scheduleAllTasks(tasks: List<Task>) {
        tasks.forEach { task ->
            if (task.reminderSet && task.dueTime != null) {
                scheduleAlarm(task)
            }
        }
    }
} 