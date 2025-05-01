package com.example.theshitapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.theshitapp.databinding.ActivityNotificationsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var adapter: NotificationAdapter
    
    private val notificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.example.theshitapp.NEW_NOTIFICATION") {
                loadNotifications()
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupRecyclerView()
        setupListeners()
        
        // Register for new notification broadcasts
        registerReceiver(
            notificationReceiver, 
            IntentFilter("com.example.theshitapp.NEW_NOTIFICATION")
        )
        
        loadNotifications()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(notificationReceiver)
    }
    
    private fun setupRecyclerView() {
        adapter = NotificationAdapter(emptyList())
        binding.notificationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
            adapter = this@NotificationsActivity.adapter
        }
    }
    
    private fun setupListeners() {
        binding.clearAllButton.setOnClickListener {
            clearAllNotifications()
        }
        
        binding.backButton.setOnClickListener {
            finish()
        }
    }
    
    private fun loadNotifications() {
        val sharedPrefs = getSharedPreferences("app_notifications", Context.MODE_PRIVATE)
        val notifications = mutableListOf<Notification>()
        
        // Get all stored notifications
        sharedPrefs.all.forEach { (id, value) ->
            if (value is String) {
                val parts = value.split("|")
                if (parts.size >= 2) {
                    val taskId = parts[0]
                    val message = parts[1]
                    val timestamp = try {
                        id.toLong()
                    } catch (e: Exception) {
                        System.currentTimeMillis()
                    }
                    
                    notifications.add(
                        Notification(
                            id = id,
                            taskId = taskId,
                            message = message,
                            timestamp = timestamp
                        )
                    )
                }
            }
        }
        
        // Sort by timestamp (newest first)
        notifications.sortByDescending { it.timestamp }
        
        // Update the UI
        adapter.updateNotifications(notifications)
        
        // Show empty state if no notifications
        binding.emptyStateLayout.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE
    }
    
    private fun clearAllNotifications() {
        val sharedPrefs = getSharedPreferences("app_notifications", Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
        loadNotifications()
    }
    
    data class Notification(
        val id: String,
        val taskId: String,
        val message: String,
        val timestamp: Long
    )
    
    inner class NotificationAdapter(
        private var notifications: List<Notification>
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
        
        inner class ViewHolder(val binding: com.example.theshitapp.databinding.ItemNotificationBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)
        
        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): ViewHolder {
            val binding = com.example.theshitapp.databinding.ItemNotificationBinding.inflate(
                android.view.LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val notification = notifications[position]
            
            holder.binding.notificationMessage.text = notification.message
            
            // Format the timestamp
            val date = Date(notification.timestamp)
            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            holder.binding.notificationTime.text = dateFormat.format(date)
            
            // Handle click to view the task
            holder.binding.root.setOnClickListener {
                val intent = Intent(this@NotificationsActivity, TaskDetailActivity::class.java)
                intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, notification.taskId)
                startActivity(intent)
            }
        }
        
        override fun getItemCount(): Int = notifications.size
        
        fun updateNotifications(newNotifications: List<Notification>) {
            notifications = newNotifications
            notifyDataSetChanged()
        }
    }
} 