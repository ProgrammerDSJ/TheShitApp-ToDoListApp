package com.example.theshitapp.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.theshitapp.R
import com.example.theshitapp.adapter.CalendarDayAdapter
import com.example.theshitapp.adapter.TaskAdapter
import com.example.theshitapp.databinding.FragmentDailyTasksBinding
import com.example.theshitapp.model.CalendarDay
import com.example.theshitapp.model.Task
import com.example.theshitapp.repository.TaskRepository
import com.example.theshitapp.ui.dialog.AddTaskDialog
import com.example.theshitapp.ui.dialog.YearPickerDialog
import com.example.theshitapp.TheShitApp
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

class DailyTasksFragment : Fragment() {
    
    private var _binding: FragmentDailyTasksBinding? = null
    private val binding get() = _binding!!
    
    private var selectedDate: Date = Calendar.getInstance().time
    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private lateinit var calendarDayAdapter: CalendarDayAdapter
    
    // Get repository instance
    private val repository by lazy { (requireActivity().application as TheShitApp).repository }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDailyTasksBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupCustomCalendarHeader()
        setupCalendar()
        setupAddTaskButton()
        loadTasksForSelectedDate()
    }
    
    private fun setupCustomCalendarHeader() {
        // Get references to our custom header views
        val headerTextView = view?.findViewById<TextView>(R.id.calendarHeaderText)
        val prevButton = view?.findViewById<ImageButton>(R.id.prevMonthButton)
        val nextButton = view?.findViewById<ImageButton>(R.id.nextMonthButton)
        
        // Set initial header text
        headerTextView?.text = dateFormat.format(selectedDate)
        
        // Set click listeners for navigation buttons
        prevButton?.setOnClickListener {
            navigateToPreviousMonth()
        }
        
        nextButton?.setOnClickListener {
            navigateToNextMonth()
        }
        
        // Set click listener on the month/year text to show year picker
        headerTextView?.setOnClickListener {
            showYearPicker()
        }
    }
    
    private fun navigateToPreviousMonth() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        calendar.add(Calendar.MONTH, -1)
        updateCalendarMonth(calendar.time)
    }
    
    private fun navigateToNextMonth() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        calendar.add(Calendar.MONTH, 1)
        updateCalendarMonth(calendar.time)
    }
    
    private fun updateCalendarMonth(date: Date) {
        selectedDate = date
        
        // Update the header text
        view?.findViewById<TextView>(R.id.calendarHeaderText)?.text = dateFormat.format(date)
        
        // Generate calendar days for the month and update the adapter
        generateCalendarDays()
        
        // Update tasks for the selected date
        loadTasksForSelectedDate()
    }
    
    private fun setupCalendar() {
        // Initialize RecyclerView
        binding.calendarDaysRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            
            // Initialize adapter
            calendarDayAdapter = CalendarDayAdapter(
                emptyList(),
                onDayClick = { date ->
                    selectedDate = date
                    calendarDayAdapter.selectDay(date)
                    loadTasksForSelectedDate()
                }
            )
            adapter = calendarDayAdapter
        }
        
        // Generate and display calendar days
        generateCalendarDays()
    }
    
    private suspend fun hasTasksOnDate(date: Date): Boolean {
        return try {
            val tasks = repository.getTasksForDate(date)
            tasks.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }
    
    private fun generateCalendarDays() {
        lifecycleScope.launch {
            val calendarDays = mutableListOf<CalendarDay>()
            
            // Get the calendar instance for the selected month
            val calendar = Calendar.getInstance()
            calendar.time = selectedDate
            
            // Move to the first day of the month
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            
            // Determine the day of week for the first day (0 = Sunday, 1 = Monday, etc.)
            val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            
            // Add days from the previous month to fill the first row
            if (firstDayOfWeek > Calendar.SUNDAY) {
                val prevCalendar = Calendar.getInstance()
                prevCalendar.time = calendar.time
                prevCalendar.add(Calendar.MONTH, -1)
                
                // Get the last day of the previous month
                val daysInPrevMonth = prevCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                
                // Add the required days from the previous month
                val daysToAdd = firstDayOfWeek - Calendar.SUNDAY
                for (i in daysToAdd downTo 1) {
                    prevCalendar.set(Calendar.DAY_OF_MONTH, daysInPrevMonth - i + 1)
                    
                    calendarDays.add(
                        CalendarDay(
                            date = prevCalendar.time,
                            isCurrentMonth = false,
                            isToday = isSameDay(prevCalendar.time, Calendar.getInstance().time),
                            hasTask = hasTasksOnDate(prevCalendar.time)
                        )
                    )
                }
            }
            
            // Add days of the current month
            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val today = Calendar.getInstance().time
            
            for (day in 1..daysInMonth) {
                calendar.set(Calendar.DAY_OF_MONTH, day)
                
                calendarDays.add(
                    CalendarDay(
                        date = calendar.time,
                        isCurrentMonth = true,
                        isToday = isSameDay(calendar.time, today),
                        hasTask = hasTasksOnDate(calendar.time)
                    )
                )
            }
            
            // Add days from the next month to complete the last row
            val totalDaysAdded = calendarDays.size
            val remainingCells = 42 - totalDaysAdded // 6 rows of 7 days
            
            if (remainingCells > 0) {
                calendar.add(Calendar.MONTH, 1)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                
                for (day in 1..remainingCells) {
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    
                    calendarDays.add(
                        CalendarDay(
                            date = calendar.time,
                            isCurrentMonth = false,
                            isToday = isSameDay(calendar.time, today),
                            hasTask = hasTasksOnDate(calendar.time)
                        )
                    )
                }
            }
            
            // Update the adapter
            calendarDayAdapter.updateDays(calendarDays)
            
            // Select the current day
            calendarDayAdapter.selectDay(selectedDate)
        }
    }
    
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
               cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }
    
    private fun showYearPicker() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        val currentYear = calendar.get(Calendar.YEAR)
        
        YearPickerDialog(
            requireContext(),
            currentYear,
            onYearSelected = { selectedYear ->
                navigateToYear(selectedYear)
            }
        ).show()
    }
    
    private fun navigateToYear(year: Int) {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate
        
        // If the current date is already in the selected year, do nothing
        if (calendar.get(Calendar.YEAR) == year) {
            return
        }
        
        // Otherwise, set the year but keep the month and day
        calendar.set(Calendar.YEAR, year)
        selectedDate = calendar.time
        
        // Update the calendar view and header
        updateCalendarMonth(selectedDate)
    }
    
    private fun setupAddTaskButton() {
        binding.addTaskButton.setOnClickListener {
            showAddTaskDialog()
        }
    }
    
    private fun showAddTaskDialog() {
        val dialog = AddTaskDialog.newInstance(
            preselectedDate = selectedDate
        )
        dialog.setTaskAddedListener {
            loadTasksForSelectedDate()
            generateCalendarDays() // Refresh the calendar to show new task indicators
        }
        dialog.show(parentFragmentManager, "addTask")
    }
    
    private fun loadTasksForSelectedDate() {
        // Get tasks for the selected date using the repository
        lifecycleScope.launch {
            val tasks = repository.getTasksForDate(selectedDate)
            
            // Update the task list
            updateTaskList(tasks)
            
            // Update the date text
            val detailDateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            binding.dateText.text = detailDateFormat.format(selectedDate)
            
            // Update task count with separate counts for tasks and habits
            val tasksCount = tasks.count { !it.isHabit }
            val habitsCount = tasks.count { it.isHabit }
            
            val taskText = if (tasksCount == 1) "task" else "tasks"
            val habitText = if (habitsCount == 1) "habit" else "habits"
            
            if (habitsCount > 0) {
                binding.taskCountText.text = "$tasksCount $taskText & $habitsCount $habitText"
            } else {
                binding.taskCountText.text = "$tasksCount $taskText"
            }
        }
    }
    
    private fun updateTaskList(tasks: List<Task>) {
        if (tasks.isEmpty()) {
            binding.noTasksText.visibility = View.VISIBLE
            binding.tasksRecyclerView.visibility = View.GONE
        } else {
            binding.noTasksText.visibility = View.GONE
            binding.tasksRecyclerView.visibility = View.VISIBLE
            
            val adapter = DailyTaskListAdapter(tasks) { task ->
                markTaskAsDone(task)
            }
            
            binding.tasksRecyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                this.adapter = adapter
            }
        }
    }
    
    private fun markTaskAsDone(task: Task) {
        lifecycleScope.launch {
            val success = repository.completeTask(task.id)
            if (success) {
                loadTasksForSelectedDate()
                generateCalendarDays() // Refresh the calendar to show updated task indicators
            }
        }
    }
    
    fun refreshTasks() {
        loadTasksForSelectedDate()
        generateCalendarDays() // Refresh the calendar indicators
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = DailyTasksFragment()
    }
    
    inner class DailyTaskListAdapter(
        private val tasks: List<Task>,
        private val onTaskCompleted: (Task) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<DailyTaskListAdapter.ViewHolder>() {
        
        inner class ViewHolder(val binding: com.example.theshitapp.databinding.ItemDailyTaskBinding) : 
            androidx.recyclerview.widget.RecyclerView.ViewHolder(binding.root)
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = com.example.theshitapp.databinding.ItemDailyTaskBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return ViewHolder(binding)
        }
        
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val task = tasks[position]
            
            // Set the task title
            holder.binding.taskTitle.text = task.title
            
            // Set the task details
            val dueTime = if (task.dueTime != null) {
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                timeFormat.format(Date(task.dueTime))
            } else {
                "No time set"
            }
            
            holder.binding.taskDetails.text = "Time: $dueTime | Duration: ${task.durationMinutes} mins | Category: ${task.category.name}"
            
            // Set the priority
            val priorityText = when (task.priority) {
                com.example.theshitapp.model.TaskPriority.HIGH -> "High"
                com.example.theshitapp.model.TaskPriority.MEDIUM -> "Medium"
                else -> "Low"
            }
            holder.binding.taskPriority.text = "Priority: $priorityText"
            
            // Set the color of the card based on priority
            val color = when {
                task.isHabit -> android.graphics.Color.parseColor("#3B82F6") // Blue for Habits
                task.priority == com.example.theshitapp.model.TaskPriority.HIGH -> android.graphics.Color.parseColor("#EF4444") // Red for High Priority
                task.priority == com.example.theshitapp.model.TaskPriority.MEDIUM -> android.graphics.Color.parseColor("#EAB308") // Yellow for Medium Priority
                else -> android.graphics.Color.parseColor("#22C55E") // Green for Low Priority
            }
            holder.binding.priorityIndicator.setBackgroundColor(color)
            
            // Setup checkbox
            holder.binding.taskCheckbox.isChecked = task.isCompleted
            holder.binding.taskCheckbox.setOnClickListener {
                if (!task.isCompleted) {
                    onTaskCompleted(task)
                }
            }
            
            // Set click listener for the entire card
            holder.binding.root.setOnClickListener {
                val intent = Intent(requireContext(), TaskDetailActivity::class.java)
                intent.putExtra(TaskDetailActivity.EXTRA_TASK_ID, task.id)
                startActivity(intent)
            }
        }
        
        override fun getItemCount(): Int = tasks.size
    }
} 