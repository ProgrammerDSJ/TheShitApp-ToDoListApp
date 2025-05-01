package com.example.theshitapp.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.theshitapp.R
import com.example.theshitapp.databinding.ItemTaskCategoryBinding
import com.example.theshitapp.model.TaskCategory
import com.example.theshitapp.repository.TaskRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskCategoryAdapter(
    private val categories: List<TaskCategory>,
    private val onCategoryClick: (TaskCategory) -> Unit,
    private val onAddTaskClick: (TaskCategory) -> Unit,
    private val onDeleteCategory: (String) -> Unit
) : RecyclerView.Adapter<TaskCategoryAdapter.TaskCategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskCategoryViewHolder {
        val binding = ItemTaskCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskCategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskCategoryViewHolder, position: Int) {
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int = categories.size

    inner class TaskCategoryViewHolder(private val binding: ItemTaskCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCategoryClick(categories[position])
                }
            }

            binding.addTaskButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onAddTaskClick(categories[position])
                }
            }
            
            binding.menuButton.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    showPopupMenu(it, categories[position])
                }
            }
        }
        
        private fun showPopupMenu(view: View, category: TaskCategory) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.category_menu, popupMenu.menu)
            
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_info -> {
                        showCategoryInfo(category)
                        true
                    }
                    R.id.action_delete -> {
                        confirmDeleteCategory(category)
                        true
                    }
                    else -> false
                }
            }
            
            popupMenu.show()
        }
        
        private fun showCategoryInfo(category: TaskCategory) {
            val context = binding.root.context
            val tasksCount = TaskRepository.getRegularTasksCountForCategory(category.id)
            val createdDate = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * (1..30).random())))
            
            AlertDialog.Builder(context)
                .setTitle("Category Info")
                .setMessage("""
                    Category ID: ${category.id}
                    Category Name: ${category.name}
                    Date Created: $createdDate
                    Active Tasks: $tasksCount
                """.trimIndent())
                .setPositiveButton("OK", null)
                .show()
        }
        
        private fun confirmDeleteCategory(category: TaskCategory) {
            val context = binding.root.context
            AlertDialog.Builder(context)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete this category? All tasks in this category will also be deleted.")
                .setPositiveButton("Delete") { _, _ ->
                    onDeleteCategory(category.id)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        fun bind(category: TaskCategory) {
            binding.categoryName.text = category.name
            
            // Use the activeTasksCount directly from the category model
            val tasksCount = category.activeTasksCount
            
            // Update the active tasks text
            val taskText = if (tasksCount == 1) "Task" else "Tasks"
            binding.activeTasks.text = "$tasksCount Active $taskText"
            
            // In a real app, you might want to add profile pictures for each member
            // For simplicity, we'll use the placeholder for everyone
        }
    }
} 