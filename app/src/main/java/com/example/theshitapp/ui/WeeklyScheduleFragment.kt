package com.example.theshitapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.theshitapp.databinding.FragmentWeeklyScheduleBinding
import com.example.theshitapp.repository.TaskRepository
import com.example.theshitapp.ui.dialog.AddTaskDialog
import com.example.theshitapp.TheShitApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeeklyScheduleFragment : Fragment() {

    private var _binding: FragmentWeeklyScheduleBinding? = null
    private val binding get() = _binding!!
    
    private val dateFormatter = SimpleDateFormat("MMM d - MMM d, yyyy", Locale.getDefault())
    private var currentWeekStart: Date = Calendar.getInstance().time
    
    // Get repository instance
    private val repository by lazy { (requireActivity().application as TheShitApp).repository }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeeklyScheduleBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize with current week
        initWeekView()
        
        // Setup navigation buttons
        binding.prevWeekButton.setOnClickListener {
            navigateToPreviousWeek()
        }
        
        binding.nextWeekButton.setOnClickListener {
            navigateToNextWeek()
        }
        
        // Setup add task button
        binding.addTaskFab.setOnClickListener {
            val today = Calendar.getInstance().time
            showAddTaskDialog(today)
        }
        
        // Setup click listeners for time slots and tasks
        binding.weeklyScheduleView.onTimeSlotClickListener = { date, hour ->
            showAddTaskDialog(date, hour)
        }
        
        binding.weeklyScheduleView.onTaskClickListener = { task ->
            // Show task details
            activity?.let { 
                com.example.theshitapp.ui.TaskDetailActivity.start(it, task.id)
            }
        }
    }
    
    private fun initWeekView() {
        // Get current week's Sunday
        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.SUNDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        
        currentWeekStart = calendar.time
        updateWeekView()
    }
    
    private fun updateWeekView() {
        // Update week date range text
        val calendar = Calendar.getInstance()
        calendar.time = currentWeekStart
        
        // End date is 6 days after start (Saturday)
        val endCalendar = Calendar.getInstance()
        endCalendar.time = currentWeekStart
        endCalendar.add(Calendar.DAY_OF_YEAR, 6)
        
        val dateRangeText = "${formatDateShort(currentWeekStart)} - ${formatDateShort(endCalendar.time)}"
        binding.currentWeekText.text = dateRangeText
        
        // Update schedule view with tasks for this week
        binding.weeklyScheduleView.setWeekStartDate(currentWeekStart)
        
        // Get tasks for the week
        lifecycleScope.launch {
            try {
                val tasks = repository.getAllTasks().first()
                binding.weeklyScheduleView.setTasks(tasks)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading tasks: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun navigateToPreviousWeek() {
        val calendar = Calendar.getInstance()
        calendar.time = currentWeekStart
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        currentWeekStart = calendar.time
        
        updateWeekView()
    }
    
    private fun navigateToNextWeek() {
        val calendar = Calendar.getInstance()
        calendar.time = currentWeekStart
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        currentWeekStart = calendar.time
        
        updateWeekView()
    }
    
    private fun formatDateShort(date: Date): String {
        val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
        return formatter.format(date)
    }
    
    private fun showAddTaskDialog(date: Date, hour: Int? = null) {
        if (isAdded) {
            val dialog = AddTaskDialog.newInstance(
                preselectedDate = date,
                preselectedHour = hour,
            )
            dialog.setTaskAddedListener {
                // Refresh the view
                updateWeekView()
            }
            dialog.show(parentFragmentManager, "addTask")
        }
    }
    
    fun refreshTasks() {
        updateWeekView()
    }
    
    override fun onResume() {
        super.onResume()
        updateWeekView()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = WeeklyScheduleFragment()
    }
} 