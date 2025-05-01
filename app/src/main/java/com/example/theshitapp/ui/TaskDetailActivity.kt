package com.example.theshitapp.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.theshitapp.TheShitApp
import com.example.theshitapp.databinding.ActivityTaskDetailBinding
import com.example.theshitapp.model.Task
import com.example.theshitapp.model.SubTask
import com.example.theshitapp.repository.TaskRepository
import android.widget.CheckBox
import android.widget.EditText
import kotlinx.coroutines.launch

class TaskDetailActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityTaskDetailBinding
    private var taskId: String? = null
    private var task: Task? = null
    
    // Get repository instance
    private val repository by lazy { (application as TheShitApp).repository }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaskDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        taskId = intent.getStringExtra(EXTRA_TASK_ID)
        if (taskId == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        loadTaskDetails()
        setupListeners()
    }
    
    private fun loadTaskDetails() {
        lifecycleScope.launch {
            try {
                task = repository.getTaskById(taskId!!)
                
                if (task == null) {
                    Toast.makeText(this@TaskDetailActivity, "Task not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }
                
                // Set task data to views
                with(binding) {
                    taskTitle.text = task!!.title
                    taskCategoryLabel.text = task!!.category.name
                    additionalDescriptionValue.text = task!!.description
                    
                    // Time remaining
                    timeLeftValue.text = TaskRepository.getRemainingTime(task!!.dueDate)
                    dateValue.text = TaskRepository.formatDate(task!!.dueDate)
                    
                    // Created by info
                    createdValue.text = "Created on ${TaskRepository.formatDate(task!!.createdDate)}, by ${task!!.createdBy.name}"
                    
                    // Load subtasks
                    subtasksContainer.removeAllViews()
                    if (task!!.subtasks.isEmpty()) {
                        // Add an "Add Subtask" button if there are no subtasks
                        val addSubtaskBtn = android.widget.Button(this@TaskDetailActivity).apply {
                            text = "Add Subtask"
                            setTextColor(resources.getColor(com.example.theshitapp.R.color.dark_blue, theme))
                            setOnClickListener {
                                showAddSubtaskDialog()
                            }
                        }
                        subtasksContainer.addView(addSubtaskBtn)
                    } else {
                        // Display existing subtasks
                        for (subtask in task!!.subtasks) {
                            addSubtaskToUI(subtask)
                        }
                        
                        // Add a button to add more subtasks
                        val addMoreBtn = android.widget.Button(this@TaskDetailActivity).apply {
                            text = "Add More Subtasks"
                            setTextColor(resources.getColor(com.example.theshitapp.R.color.dark_blue, theme))
                            setOnClickListener {
                                showAddSubtaskDialog()
                            }
                        }
                        subtasksContainer.addView(addMoreBtn)
                    }
                    
                    // Set background color based on category
                    when (task!!.category.name) {
                        "Sweet Home" -> root.setBackgroundColor(resources.getColor(com.example.theshitapp.R.color.light_yellow, theme))
                        "Work" -> root.setBackgroundColor(resources.getColor(com.example.theshitapp.R.color.light_blue, theme))
                        else -> root.setBackgroundColor(resources.getColor(com.example.theshitapp.R.color.light_gray, theme))
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@TaskDetailActivity, "Error loading task: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    
    private fun addSubtaskToUI(subtask: SubTask) {
        val checkBox = CheckBox(this).apply {
            text = subtask.title
            isChecked = subtask.isCompleted
            setTextColor(resources.getColor(com.example.theshitapp.R.color.dark_blue, theme))
            setOnCheckedChangeListener { _, _ ->
                lifecycleScope.launch {
                    repository.toggleSubtaskStatus(taskId!!, subtask.id)
                }
            }
        }
        binding.subtasksContainer.addView(checkBox)
    }
    
    private fun showAddSubtaskDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val editText = EditText(this)
        editText.hint = "Enter subtask title"
        
        dialogBuilder.setTitle("Add Subtask")
            .setView(editText)
            .setPositiveButton("Add") { _, _ ->
                val subtaskTitle = editText.text.toString().trim()
                if (subtaskTitle.isNotEmpty()) {
                    lifecycleScope.launch {
                        if (repository.addSubtask(taskId!!, subtaskTitle)) {
                            // Reload task details to refresh the UI
                            loadTaskDetails()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun setupListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }
        
        binding.menuButton.setOnClickListener {
            Toast.makeText(this, "Menu options", Toast.LENGTH_SHORT).show()
        }
        
        binding.markAsDoneButton.setOnClickListener {
            markTaskAsDone()
        }
    }
    
    private fun markTaskAsDone() {
        taskId?.let { id ->
            lifecycleScope.launch {
                val success = repository.completeTask(id)
                if (success) {
                    Toast.makeText(this@TaskDetailActivity, "Task marked as done", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this@TaskDetailActivity, "Failed to mark task as done", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        
        fun start(context: Context, taskId: String) {
            val intent = Intent(context, TaskDetailActivity::class.java).apply {
                putExtra(EXTRA_TASK_ID, taskId)
            }
            context.startActivity(intent)
        }
    }
} 