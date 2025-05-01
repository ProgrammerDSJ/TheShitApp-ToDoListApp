package com.example.theshitapp.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.theshitapp.databinding.FragmentCompletedTasksBinding
import com.example.theshitapp.model.Task
import com.example.theshitapp.repository.TaskRepository
import java.util.Date

class CompletedTasksFragment : Fragment() {
    
    private var _binding: FragmentCompletedTasksBinding? = null
    private val binding get() = _binding!!
    
    // Constants for view types
    private val TYPE_HEADER = 0
    private val TYPE_TASK = 1
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedTasksBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCompletedTasks()
    }
    
    override fun onResume() {
        super.onResume()
        refreshCompletedTasks()
    }
    
    private fun setupCompletedTasks() {
        val completedTasksGrouped = TaskRepository.getCompletedTasksByDate()
        
        if (completedTasksGrouped.isEmpty()) {
            binding.emptyCompletedTasksView.visibility = View.VISIBLE
            binding.completedTasksRecyclerView.visibility = View.GONE
        } else {
            binding.emptyCompletedTasksView.visibility = View.GONE
            binding.completedTasksRecyclerView.visibility = View.VISIBLE
            
            val adapter = CompletedTasksAdapter(completedTasksGrouped)
            binding.completedTasksRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                this.adapter = adapter
            }
        }
    }
    
    fun refreshCompletedTasks() {
        setupCompletedTasks()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    // ViewHolder for date headers with scores
    inner class DateHeaderViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val dateText = view.findViewById<android.widget.TextView>(com.example.theshitapp.R.id.dateHeaderText)
        private val scoreText = view.findViewById<android.widget.TextView>(com.example.theshitapp.R.id.scoreText)
        
        fun bind(date: Date, scoreData: Pair<Int, Int>) {
            // Format the date
            dateText.text = TaskRepository.formatDateWithDayName(date)
            
            // Set score text and color
            val completed = scoreData.first
            val total = scoreData.second
            scoreText.text = "Score: $completed/$total"
            
            // Color code based on completion
            if (completed == total && total > 0) {
                // Perfect score - green
                dateText.setTextColor(Color.parseColor("#4CAF50")) // Green
                scoreText.setTextColor(Color.parseColor("#4CAF50")) // Green
            } else {
                // Incomplete - red
                dateText.setTextColor(Color.parseColor("#F44336")) // Red
                scoreText.setTextColor(Color.parseColor("#F44336")) // Red
            }
        }
    }
    
    // ViewHolder for completed task items
    inner class CompletedTaskViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val titleText = view.findViewById<android.widget.TextView>(com.example.theshitapp.R.id.taskTitleText)
        private val categoryText = view.findViewById<android.widget.TextView>(com.example.theshitapp.R.id.taskCategoryText)
        private val completionTimeText = view.findViewById<android.widget.TextView>(com.example.theshitapp.R.id.taskCompletionTimeText)
        
        fun bind(task: Task) {
            titleText.text = task.title
            categoryText.text = "Category: ${task.category.name}"
            
            // For completed time, we'd ideally store the completion timestamp
            // As a simplification, we'll use the due time if available, or just show "Completed"
            val completionTime = if (task.dueTime != null) {
                "Completed: ${TaskRepository.formatTime(task.dueTime)}"
            } else {
                "Completed"
            }
            completionTimeText.text = completionTime
        }
    }
    
    // Adapter implementation supporting both date headers and task items
    inner class CompletedTasksAdapter(private val groupedTasks: Map<Date, List<Task>>) : 
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        
        // Flatten the grouped data for the adapter
        private val items = mutableListOf<Any>()
        
        init {
            // Sort dates in descending order (newest first)
            val sortedDates = groupedTasks.keys.sortedByDescending { it }
            
            // Create flattened list with headers and tasks
            sortedDates.forEach { date ->
                // Add the date as a header
                items.add(date)
                
                // Add all tasks for this date
                val tasks = groupedTasks[date] ?: emptyList()
                items.addAll(tasks)
            }
        }
        
        override fun getItemViewType(position: Int): Int {
            return when (items[position]) {
                is Date -> TYPE_HEADER
                is Task -> TYPE_TASK
                else -> throw IllegalArgumentException("Unknown view type")
            }
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                TYPE_HEADER -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(com.example.theshitapp.R.layout.item_completed_date_header, parent, false)
                    DateHeaderViewHolder(view)
                }
                TYPE_TASK -> {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(com.example.theshitapp.R.layout.item_completed_task, parent, false)
                    CompletedTaskViewHolder(view)
                }
                else -> throw IllegalArgumentException("Unknown view type: $viewType")
            }
        }
        
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            when (holder) {
                is DateHeaderViewHolder -> {
                    val date = items[position] as Date
                    val scoreData = TaskRepository.getCompletionScoreForDate(date)
                    holder.bind(date, scoreData)
                }
                is CompletedTaskViewHolder -> {
                    val task = items[position] as Task
                    holder.bind(task)
                }
            }
        }
        
        override fun getItemCount(): Int = items.size
    }
    
    companion object {
        fun newInstance() = CompletedTasksFragment()
    }
} 