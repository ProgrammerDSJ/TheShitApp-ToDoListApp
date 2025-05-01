package com.example.theshitapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.theshitapp.R
import com.example.theshitapp.databinding.ActivityNotificationsBinding
import com.example.theshitapp.TheShitApp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var adapter: NotificationAdapter
    private val notifications = mutableListOf<Notification>()
    
    // Get repository instance
    private val repository by lazy { (application as TheShitApp).repository }
    
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
        try {
            val sharedPrefs = getSharedPreferences("app_notifications", Context.MODE_PRIVATE)
            val notifications = mutableListOf<Notification>()
            
            // Get all stored notifications
            sharedPrefs.all.forEach { (id, value) ->
                try {
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
                } catch (e: Exception) {
                    // If there's an error with a specific notification, remove it
                    sharedPrefs.edit().remove(id).apply()
                    e.printStackTrace()
                }
            }
            
            // Sort by timestamp (newest first)
            notifications.sortByDescending { it.timestamp }
            
            // Update the UI
            adapter.updateNotifications(notifications)
            
            // Show empty state if no notifications
            binding.emptyStateLayout.visibility = if (notifications.isEmpty()) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            // Handle any errors during the overall process
            e.printStackTrace()
            android.widget.Toast.makeText(
                this,
                "Error loading notifications: ${e.message}",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            
            // Show empty state
            binding.emptyStateLayout.visibility = View.VISIBLE
            adapter.updateNotifications(emptyList())
        }
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
    ) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
        
        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val messageTextView: TextView = itemView.findViewById(R.id.notificationMessage)
            val timeTextView: TextView = itemView.findViewById(R.id.notificationTime)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification, parent, false)
            return ViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val notification = notifications[position]
            
            holder.messageTextView.text = notification.message
            
            // Format the timestamp
            val date = Date(notification.timestamp)
            val dateFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            holder.timeTextView.text = dateFormat.format(date)
            
            // Handle click to view the task
            holder.itemView.setOnClickListener {
                try {
                    // Check if the task exists before opening detail screen using the persistent repository
                    lifecycleScope.launch {
                        val task = repository.getTaskById(notification.taskId)
                        if (task != null) {
                            val intent = Intent(this@NotificationsActivity, TaskDetailActivity::class.java)
                            intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, notification.taskId)
                            startActivity(intent)
                        } else {
                            // Show a message that the task no longer exists
                            android.widget.Toast.makeText(
                                this@NotificationsActivity,
                                "This task no longer exists",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            
                            // Remove this notification
                            val sharedPrefs = getSharedPreferences("app_notifications", Context.MODE_PRIVATE)
                            sharedPrefs.edit().remove(notification.id).apply()
                            
                            // Refresh the list
                            loadNotifications()
                        }
                    }
                } catch (e: Exception) {
                    // Handle any exceptions gracefully
                    android.widget.Toast.makeText(
                        this@NotificationsActivity,
                        "Error opening task: ${e.message}",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        
        override fun getItemCount(): Int = notifications.size
        
        fun updateNotifications(newNotifications: List<Notification>) {
            notifications = newNotifications
            notifyDataSetChanged()
        }
    }
} 