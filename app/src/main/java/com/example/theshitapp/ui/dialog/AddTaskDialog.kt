package com.example.theshitapp.ui.dialog

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.theshitapp.TheShitApp
import com.example.theshitapp.databinding.DialogAddTaskBinding
import com.example.theshitapp.model.DayOfWeek
import com.example.theshitapp.model.Task
import com.example.theshitapp.model.TaskCategory
import com.example.theshitapp.model.TaskPriority
import com.example.theshitapp.repository.TaskRepositoryImpl
import com.example.theshitapp.util.AlarmScheduler
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTaskDialog : DialogFragment() {

    private var _binding: DialogAddTaskBinding? = null
    private val binding get() = _binding!!
    
    private val calendar = Calendar.getInstance()
    private var selectedDate: Date? = null
    private var selectedTimeInMillis: Long? = null
    private var categories: List<TaskCategory> = emptyList()
    private lateinit var alarmScheduler: AlarmScheduler
    private val subtasks = mutableListOf<String>()
    private var selectedColor = "#3B82F6" // Default blue color
    private val selectedHabitDays = mutableSetOf<DayOfWeek>()
    private val dayCheckboxMap = mutableMapOf<DayOfWeek, CheckBox>()
    
    // Repository reference
    private val repository: TaskRepositoryImpl by lazy { 
        TheShitApp.getInstance().repository
    }
    
    // Task added listener
    private var taskAddedListener: ((Task) -> Unit)? = null
    
    // Set preselected values
    private var preselectedDate: Date? = null
    private var preselectedHour: Int? = null
    private var preSelectedCategoryId: String? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        alarmScheduler = AlarmScheduler(requireContext())
        
        // Setup arguments if provided
        arguments?.let { args ->
            preselectedDate = args.getSerializable(ARG_DATE) as? Date
            preselectedHour = args.getInt(ARG_HOUR, -1).takeIf { it >= 0 }
            preSelectedCategoryId = args.getString(ARG_CATEGORY_ID)
        }
        
        // Apply preselected values
        preselectedDate?.let {
            calendar.time = it
            selectedDate = it
            if (preselectedHour != null) {
                calendar.set(Calendar.HOUR_OF_DAY, preselectedHour!!)
                calendar.set(Calendar.MINUTE, 0)
                selectedTimeInMillis = calendar.timeInMillis
            }
        }
        
        // Setup UI
        setupCategorySpinner()
        setupDateTimePicker()
        setupButtons()
        setupHabitUI()
        
        // Update UI with preselected values if any
        updateDateTimeDisplay()
    }
    
    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    fun setTaskAddedListener(listener: (Task) -> Unit) {
        taskAddedListener = listener
    }
    
    private fun setupCategorySpinner() {
        // Load categories asynchronously
        viewLifecycleOwner.lifecycleScope.launch {
            categories = repository.getTaskCategories().firstOrNull() ?: emptyList()
            
            val categoryNames = categories.map { it.name }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                categoryNames
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.categorySpinner.adapter = adapter
            
            // Set preselected category if provided
            if (preSelectedCategoryId != null) {
                val categoryIndex = categories.indexOfFirst { it.id == preSelectedCategoryId }
                if (categoryIndex != -1) {
                    binding.categorySpinner.setSelection(categoryIndex)
                }
            }
        }
    }
    
    private fun setupDateTimePicker() {
        binding.dueDateContainer.setOnClickListener {
            showDatePicker()
        }
        
        binding.dueTimeContainer.setOnClickListener {
            showTimePicker()
        }
    }
    
    private fun showDatePicker() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(Calendar.YEAR, selectedYear)
            calendar.set(Calendar.MONTH, selectedMonth)
            calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
            
            selectedDate = calendar.time
            updateDateTimeDisplay()
        }, year, month, day).show()
    }
    
    private fun showTimePicker() {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        
        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
            calendar.set(Calendar.MINUTE, selectedMinute)
            calendar.set(Calendar.SECOND, 0)
            
            selectedTimeInMillis = calendar.timeInMillis
            updateDateTimeDisplay()
        }, hour, minute, false).show()
    }
    
    private fun updateDateTimeDisplay() {
        if (selectedDate != null) {
            val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            binding.dueDateText.text = dateFormat.format(selectedDate!!)
        }
        
        if (selectedTimeInMillis != null) {
            val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            binding.dueTimeText.text = timeFormat.format(Date(selectedTimeInMillis!!))
        }
    }
    
    private fun setupButtons() {
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        
        binding.saveButton.setOnClickListener {
            saveTask()
        }
        
        binding.addSubtaskButton.setOnClickListener {
            addSubtask()
        }
        
        binding.selectColorButton.setOnClickListener {
            showColorPicker()
        }
        
        // Set up reminder switch listener
        binding.reminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.reminderOptionsContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }
    
    private fun setupHabitUI() {
        // Initialize day of week checkboxes
        dayCheckboxMap[DayOfWeek.MONDAY] = binding.mondayCheckbox
        dayCheckboxMap[DayOfWeek.TUESDAY] = binding.tuesdayCheckbox
        dayCheckboxMap[DayOfWeek.WEDNESDAY] = binding.wednesdayCheckbox
        dayCheckboxMap[DayOfWeek.THURSDAY] = binding.thursdayCheckbox
        dayCheckboxMap[DayOfWeek.FRIDAY] = binding.fridayCheckbox
        dayCheckboxMap[DayOfWeek.SATURDAY] = binding.saturdayCheckbox
        dayCheckboxMap[DayOfWeek.SUNDAY] = binding.sundayCheckbox
        
        // Set up checkbox listeners
        dayCheckboxMap.forEach { (day, checkbox) ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedHabitDays.add(day)
                } else {
                    selectedHabitDays.remove(day)
                }
            }
        }
        
        // Set up habit switch listener
        binding.habitSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.habitDaysContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
            binding.habitDurationContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
        }
    }
    
    private fun addSubtask() {
        val subtaskTitle = binding.subtaskInput.text.toString().trim()
        if (subtaskTitle.isNotEmpty()) {
            subtasks.add(subtaskTitle)
            addSubtaskToUI(subtaskTitle)
            binding.subtaskInput.text?.clear()
        }
    }
    
    private fun addSubtaskToUI(subtaskTitle: String) {
        val subtaskLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }
        
        val subtaskText = TextView(requireContext()).apply {
            text = subtaskTitle
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
            )
            setTextColor(requireContext().getColor(com.example.theshitapp.R.color.text_primary))
        }
        
        val removeButton = androidx.appcompat.widget.AppCompatImageButton(requireContext()).apply {
            setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            background = null
            setOnClickListener {
                binding.subtasksContainer.removeView(subtaskLayout)
                subtasks.remove(subtaskTitle)
            }
        }
        
        subtaskLayout.addView(subtaskText)
        subtaskLayout.addView(removeButton)
        
        binding.subtasksContainer.addView(subtaskLayout)
    }
    
    private fun showColorPicker() {
        ColorPickerDialog(requireContext(), selectedColor) { newColor ->
            selectedColor = newColor
            updateColorPreview()
        }.show()
    }
    
    private fun updateColorPreview() {
        // Get the color name based on the hex value
        val colorName = when (selectedColor) {
            "#3B82F6" -> "Blue"
            "#22C55E" -> "Green"
            "#EF4444" -> "Red"
            "#EAB308" -> "Yellow"
            "#8B5CF6" -> "Purple"
            "#EC4899" -> "Pink"
            "#F97316" -> "Orange"
            "#14B8A6" -> "Teal"
            else -> "Custom"
        }
        
        // Update the UI
        binding.selectedColorText.text = "Selected color: $colorName"
        binding.colorPreview.setBackgroundColor(android.graphics.Color.parseColor(selectedColor))
    }
    
    private fun saveTask() {
        val title = binding.taskTitleInput.text.toString().trim()
        val description = binding.taskDescriptionInput.text.toString().trim()
        
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a task title", Toast.LENGTH_SHORT).show()
            return
        }
        
        val categoryPosition = binding.categorySpinner.selectedItemPosition
        if (categoryPosition == -1 || categoryPosition >= categories.size) {
            Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (selectedDate == null) {
            Toast.makeText(requireContext(), "Please select a due date", Toast.LENGTH_SHORT).show()
            return
        }
        
        val taskCategory = categories[categoryPosition]
        val taskPriority = when (binding.priorityRadioGroup.checkedRadioButtonId) {
            binding.priorityHigh.id -> TaskPriority.HIGH
            binding.priorityMedium.id -> TaskPriority.MEDIUM
            else -> TaskPriority.LOW
        }
        
        val isReminderSet = binding.reminderSwitch.isChecked
        var reminderMinutes = 30
        var reminderCount = 1
        
        if (isReminderSet) {
            try {
                reminderMinutes = binding.reminderMinutesInput.text.toString().toIntOrNull() ?: 30
                reminderCount = binding.reminderCountInput.text.toString().toIntOrNull() ?: 1
                
                // Validate inputs
                if (reminderMinutes <= 0) {
                    Toast.makeText(requireContext(), "Minutes before must be greater than 0", Toast.LENGTH_SHORT).show()
                    return
                }
                
                if (reminderCount <= 0 || reminderCount > 5) {
                    Toast.makeText(requireContext(), "Number of reminders must be between 1 and 5", Toast.LENGTH_SHORT).show()
                    return
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Please enter valid reminder settings", Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        // Get habit settings if habit switch is on
        val isHabit = binding.habitSwitch.isChecked
        val habitDurationWeeks = if (isHabit) binding.habitDurationWeeksInput.text.toString().toIntOrNull() ?: 0 else 0
        val habitDurationMonths = if (isHabit) binding.habitDurationMonthsInput.text.toString().toIntOrNull() ?: 0 else 0
        
        // Validate habit settings
        if (isHabit) {
            if (selectedHabitDays.isEmpty()) {
                Toast.makeText(requireContext(), "Please select at least one day for the habit", Toast.LENGTH_SHORT).show()
                return
            }
            
            if (habitDurationWeeks == 0 && habitDurationMonths == 0) {
                Toast.makeText(requireContext(), "Please enter a duration for the habit", Toast.LENGTH_SHORT).show()
                return
            }
        }
        
        val durationMinutes = binding.taskDurationInput.text.toString().toIntOrNull() ?: 60
        
        // Use coroutine to add task with the new repository
        viewLifecycleOwner.lifecycleScope.launch {
            // Use milliseconds for the time
            val newTask = repository.addTask(
                title = title,
                description = description,
                categoryId = taskCategory.id,
                dueDate = selectedDate!!,
                dueTime = selectedTimeInMillis,
                assigneeIds = listOf("1"), // Current user for now
                priority = taskPriority,
                reminderSet = isReminderSet,
                durationMinutes = durationMinutes,
                taskColor = selectedColor,
                isHabit = isHabit,
                habitDays = selectedHabitDays,
                habitDurationWeeks = habitDurationWeeks,
                habitDurationMonths = habitDurationMonths
            )
            
            // If reminder is set, schedule notifications
            if (isReminderSet && selectedTimeInMillis != null) {
                scheduleReminders(newTask, reminderMinutes, reminderCount)
            }
            
            // Add subtasks
            subtasks.forEach { subtaskTitle ->
                repository.addSubtask(newTask.id, subtaskTitle)
            }
            
            // Notify listener
            taskAddedListener?.invoke(newTask)
            
            // Dismiss dialog
            dismiss()
        }
    }
    
    private fun scheduleReminders(task: Task, minutesBefore: Int, reminderCount: Int) {
        if (task.dueTime == null) return
        
        // Calculate total minutes to distribute reminders evenly
        val totalMinutes = minutesBefore
        
        // Schedule reminders at evenly distributed intervals
        for (i in 0 until reminderCount) {
            val minutesBeforeTask = if (reminderCount == 1) {
                totalMinutes
            } else {
                // Distribute reminders evenly
                // Example: if 30 mins and 3 reminders, we'll have reminders at 30, 20, and 10 mins before
                totalMinutes - (i * (totalMinutes / reminderCount))
            }
            
            // Schedule the reminder
            alarmScheduler.scheduleTaskReminder(
                task, 
                minutesBeforeTask.toLong(), 
                "Your task '${task.title}' will start in ${minutesBeforeTask} minutes"
            )
        }
    }
    
    companion object {
        private const val ARG_DATE = "arg_date"
        private const val ARG_HOUR = "arg_hour"
        private const val ARG_CATEGORY_ID = "arg_category_id"
        
        fun newInstance(
            preselectedDate: Date? = null,
            preselectedHour: Int? = null,
            preSelectedCategoryId: String? = null
        ): AddTaskDialog {
            return AddTaskDialog().apply {
                arguments = Bundle().apply {
                    preselectedDate?.let { putSerializable(ARG_DATE, it) }
                    preselectedHour?.let { putInt(ARG_HOUR, it) }
                    preSelectedCategoryId?.let { putString(ARG_CATEGORY_ID, it) }
                }
            }
        }
    }
} 