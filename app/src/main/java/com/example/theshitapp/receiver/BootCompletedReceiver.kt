package com.example.theshitapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.theshitapp.repository.TaskRepository
import com.example.theshitapp.util.AlarmScheduler

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule all active task alarms
            val tasks = TaskRepository.getAllTasks().filter { 
                !it.isCompleted && it.reminderSet && it.dueTime != null 
            }
            
            val alarmScheduler = AlarmScheduler(context)
            alarmScheduler.scheduleAllTasks(tasks)
        }
    }
} 