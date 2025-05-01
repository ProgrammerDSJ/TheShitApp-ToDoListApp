package com.example.theshitapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.theshitapp.databinding.ActivityMainBinding
import com.example.theshitapp.repository.TaskRepository
import com.example.theshitapp.ui.BoardsContainerFragment
import com.example.theshitapp.ui.NotificationsActivity
import com.example.theshitapp.ui.ScheduleContainerFragment
import com.example.theshitapp.ui.dialog.AddTaskDialog
import com.example.theshitapp.ui.dialog.AddCategoryDialog
import com.example.theshitapp.util.AlarmScheduler
import com.google.android.material.bottomsheet.BottomSheetDialog

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var alarmScheduler: AlarmScheduler
    
    private lateinit var scheduleContainerFragment: ScheduleContainerFragment
    private lateinit var boardsContainerFragment: BoardsContainerFragment
    
    // Get repository from Application class
    private val repository by lazy { (application as TheShitApp).repository }
    
    // Notification permission request
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, schedule alarms
            scheduleAllTaskAlarms()
        } else {
            // Permission denied, show a message
            Toast.makeText(
                this,
                "Notifications won't work without permission",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        alarmScheduler = AlarmScheduler(this)
        
        if (savedInstanceState == null) {
            scheduleContainerFragment = ScheduleContainerFragment.newInstance()
            boardsContainerFragment = BoardsContainerFragment.newInstance()
            
            // Add the Schedule container fragment by default
            supportFragmentManager.beginTransaction()
                .add(binding.mainFragmentContainer.id, boardsContainerFragment, "boards")
                .hide(boardsContainerFragment)
                .add(binding.mainFragmentContainer.id, scheduleContainerFragment, "schedule")
                .commit()
        } else {
            // Restore fragments from saved state
            scheduleContainerFragment = supportFragmentManager.findFragmentByTag("schedule") as ScheduleContainerFragment
            boardsContainerFragment = supportFragmentManager.findFragmentByTag("boards") as BoardsContainerFragment
        }
        
        setupBottomNavigation()
        setupListeners()
        
        // Check notification permission
        checkNotificationPermission()
    }
    
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted, schedule alarms
                    scheduleAllTaskAlarms()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Explain why we need the permission
                    Toast.makeText(
                        this,
                        "We need notification permission to remind you of your tasks",
                        Toast.LENGTH_LONG
                    ).show()
                    // Then request it
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request the permission directly
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 12 and below don't need runtime permission for notifications
            scheduleAllTaskAlarms()
        }
    }
    
    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.example.theshitapp.R.id.navigation_schedule -> {
                    switchToFragment(scheduleContainerFragment)
                    binding.addTaskFab.visibility = View.VISIBLE
                    true
                }
                com.example.theshitapp.R.id.navigation_boards -> {
                    switchToFragment(boardsContainerFragment)
                    binding.addTaskFab.visibility = View.GONE // Hide FAB on boards
                    true
                }
                else -> false
            }
        }
        
        // Set default selection
        binding.bottomNavigation.selectedItemId = com.example.theshitapp.R.id.navigation_schedule
    }
    
    private fun switchToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            hide(if (fragment is ScheduleContainerFragment) boardsContainerFragment else scheduleContainerFragment)
            show(fragment)
        }.commit()
    }
    
    private fun setupListeners() {
        // Remove the addIcon listener since we're using the FAB menu instead
        binding.addIcon.visibility = View.GONE
        
        binding.notificationIcon.setOnClickListener {
            try {
                // Create the intent for NotificationsActivity
                val intent = Intent(this, NotificationsActivity::class.java)
                
                // Start the activity in a safer way
                startActivity(intent)
                
                // Use a safer background thread method with comprehensive error handling
                Thread {
                    try {
                        // Add a small delay to ensure the activity has time to initialize
                        Thread.sleep(300)
                        ensureSampleNotifications()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this, "Error loading notifications: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }.start()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Could not open notifications: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
        
        binding.profileImage.setOnClickListener {
            Toast.makeText(this, "Open profile", Toast.LENGTH_SHORT).show()
        }
        
        binding.addTaskFab.setOnClickListener {
            showFabOptions()
        }
    }
    
    private fun showFabOptions() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_fab_menu, null)
        dialog.setContentView(view)
        
        // Set up click listeners for options
        view.findViewById<View>(R.id.addTaskOption).setOnClickListener {
            dialog.dismiss()
            showAddTaskDialog()
        }
        
        view.findViewById<View>(R.id.addCategoryOption).setOnClickListener {
            dialog.dismiss()
            showAddCategoryDialog()
        }
        
        dialog.show()
    }
    
    private fun showNotificationsScreen() {
        try {
            val intent = Intent(this, NotificationsActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            // Handle any possible exception during activity launch
            Toast.makeText(this, "Could not open notifications screen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun scheduleAllTaskAlarms() {
        val tasks = TaskRepository.getAllTasks().filter { it.reminderSet && it.dueTime != null }
        alarmScheduler.scheduleAllTasks(tasks)
    }
    
    private fun showAddTaskDialog() {
        val dialog = AddTaskDialog.newInstance()
        dialog.setTaskAddedListener { task ->
            Toast.makeText(this, "Task added: ${task.title}", Toast.LENGTH_SHORT).show()
            
            // Schedule alarm for the task if reminder is set
            if (task.reminderSet && task.dueTime != null) {
                alarmScheduler.scheduleAlarm(task)
            }
        }
        dialog.show(supportFragmentManager, "addTask")
    }
    
    private fun showAddCategoryDialog() {
        val dialog = AddCategoryDialog.newInstance()
        dialog.setCategoryAddedListener { category ->
            Toast.makeText(this, "Category added: ${category.name}", Toast.LENGTH_SHORT).show()
        }
        dialog.show(supportFragmentManager, "addCategory")
    }
    
    private fun refreshCurrentView() {
        val currentFragment = getCurrentVisibleFragment()
        if (currentFragment is ScheduleContainerFragment) {
            currentFragment.refreshCurrentTab()
        }
    }
    
    private fun getCurrentVisibleFragment(): Fragment? {
        val fragments = supportFragmentManager.fragments
        if (fragments.isNotEmpty()) {
            for (fragment in fragments) {
                if (fragment.isVisible) {
                    return fragment
                }
            }
        }
        return null
    }
    
    private fun ensureSampleNotifications() {
        try {
            val sharedPrefs = getSharedPreferences("app_notifications", Context.MODE_PRIVATE)
            
            // Only add sample notifications if there are none
            if (sharedPrefs.all.isEmpty()) {
                // Add some sample notifications
                val currentTime = System.currentTimeMillis()
                val tasks = TaskRepository.getAllTasks()
                
                // Ensure we have tasks to work with
                if (tasks.isNotEmpty()) {
                    // Only take up to 3 tasks
                    val tasksToUse = tasks.take(3)
                    
                    tasksToUse.forEachIndexed { index, task ->
                        val notificationId = (currentTime - (index * 60000)).toString()
                        val message = when (index) {
                            0 -> "Reminder: \"${task.title}\" is due soon!"
                            1 -> "Task \"${task.title}\" has been assigned to you"
                            else -> "Don't forget about \"${task.title}\""
                        }
                        
                        sharedPrefs.edit().putString(notificationId, "${task.id}|$message").apply()
                    }
                    
                    // Broadcast to update notification screens if open
                    val updateIntent = Intent("com.example.theshitapp.NEW_NOTIFICATION")
                    sendBroadcast(updateIntent)
                }
            }
        } catch (e: Exception) {
            // Handle exception gracefully
            Toast.makeText(this, "Could not load notifications: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}