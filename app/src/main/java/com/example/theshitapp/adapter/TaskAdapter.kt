package com.example.theshitapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.theshitapp.R
import com.example.theshitapp.databinding.ItemTaskBinding
import com.example.theshitapp.model.Task
import com.example.theshitapp.model.TaskPriority
import com.example.theshitapp.repository.TaskRepository
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.Calendar

class TaskAdapter(
    private val tasks: List<Task>,
    private val onTaskClick: (Task) -> Unit,
    private val onMarkAsDoneClick: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    // Alternate constructor for just onMarkAsDoneClick
    constructor(
        tasks: List<Task>,
        onMarkAsDoneClick: (Task) -> Unit
    ) : this(
        tasks = tasks,
        onTaskClick = { /* Default no-op implementation */ },
        onMarkAsDoneClick = onMarkAsDoneClick
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTaskClick(tasks[position])
                }
            }

            binding.markAsDoneButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onMarkAsDoneClick(tasks[position])
                }
            }
        }

        fun bind(task: Task) {
            binding.taskTitle.text = task.title
            binding.taskCategoryLabel.text = task.category.name
            binding.additionalDescriptionValue.text = task.description
            
            // Time remaining
            binding.timeLeftValue.text = calculateRemainingTime(task.dueDate)
            binding.dateValue.text = formatDate(task.dueDate)
            
            // Created by info
            binding.createdValue.text = "Created on ${formatDate(task.createdDate)}, by ${task.createdBy.name}"
            
            // Set the appropriate background based on task type and priority
            val backgroundDrawableId = when {
                task.isHabit -> R.drawable.task_card_blue_background
                task.priority == TaskPriority.HIGH -> R.drawable.task_card_red_background
                task.priority == TaskPriority.LOW -> R.drawable.task_card_green_background
                else -> R.drawable.task_card_yellow_background // MEDIUM priority uses yellow
            }
            
            binding.root.findViewById<ViewGroup>(R.id.taskCardContainer).background = 
                ContextCompat.getDrawable(binding.root.context, backgroundDrawableId)
        }
        
        private fun formatDate(date: Date): String {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            return dateFormat.format(date)
        }
        
        private fun calculateRemainingTime(dueDate: Date): String {
            val currentTime = Calendar.getInstance().time
            val diff = dueDate.time - currentTime.time
            
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            
            return when {
                days > 0 -> "${days}d ${hours % 24}h"
                hours > 0 -> "${hours}h ${minutes % 60}m"
                else -> "${minutes}m"
            }
        }
    }
} 