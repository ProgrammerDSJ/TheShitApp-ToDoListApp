package com.example.theshitapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.theshitapp.adapter.TaskAdapter
import com.example.theshitapp.adapter.TaskCategoryAdapter
import com.example.theshitapp.databinding.FragmentTasksBinding
import com.example.theshitapp.model.Task
import com.example.theshitapp.model.TaskCategory
import com.example.theshitapp.repository.TaskRepository
import com.example.theshitapp.ui.dialog.AddTaskDialog
import com.example.theshitapp.TheShitApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {
    
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    
    private var selectedCategory: TaskCategory? = null
    private lateinit var categoryAdapter: TaskCategoryAdapter
    
    // Get repository instance
    private val repository by lazy { (requireActivity().application as TheShitApp).repository }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBackButton()
        setupCategoriesList()
    }
    
    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            backToCategories()
        }
        // Initially hide back button since we're showing categories
        binding.backButton.visibility = View.GONE
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh when returning from task detail
        if (selectedCategory != null) {
            showTasksForCategory(selectedCategory!!)
        } else {
            setupCategoriesList()
        }
    }
    
    private fun setupCategoriesList() {
        lifecycleScope.launch {
            try {
                val categories = repository.getTaskCategories().first()
                categoryAdapter = TaskCategoryAdapter(
                    categories,
                    onCategoryClick = { category ->
                        // Navigate to filtered tasks by category
                        showTasksForCategory(category)
                    },
                    onAddTaskClick = { category ->
                        showAddTaskDialog(category)
                    },
                    onDeleteCategory = { categoryId ->
                        deleteCategory(categoryId)
                    }
                )
                
                binding.taskCategoriesRecyclerView.apply {
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = categoryAdapter
                }
                binding.taskCategoriesRecyclerView.visibility = View.VISIBLE
                binding.categoryTitleText.text = "Categories"
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading categories: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showTasksForCategory(category: TaskCategory) {
        selectedCategory = category
        
        lifecycleScope.launch {
            try {
                val tasks = repository.getTasksForCategory(category.id).first()
                // Update UI elements for tasks view
                binding.backButton.visibility = View.VISIBLE
                binding.categoryTitleText.text = category.name
                
                // Set the adapter with tasks for this category
                val adapter = TaskAdapter(tasks) { task ->
                    markTaskAsDone(task)
                }
                binding.taskCategoriesRecyclerView.adapter = adapter
                
                // Make sure the RecyclerView is visible
                binding.taskCategoriesRecyclerView.visibility = View.VISIBLE
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading tasks: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun backToCategories() {
        selectedCategory = null
        binding.backButton.visibility = View.GONE
        binding.categoryTitleText.text = "Categories"
        setupCategoriesList()
    }
    
    private fun showTaskDetails(task: Task) {
        TaskDetailActivity.start(requireContext(), task.id)
    }
    
    private fun markTaskAsDone(task: Task) {
        lifecycleScope.launch {
            val success = repository.completeTask(task.id)
            if (success) {
                Toast.makeText(requireContext(), "Task marked as done", Toast.LENGTH_SHORT).show()
                // Refresh the list
                selectedCategory?.let { showTasksForCategory(it) }
            } else {
                Toast.makeText(requireContext(), "Failed to mark task as done", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showAddTaskDialog(category: TaskCategory) {
        val dialog = AddTaskDialog.newInstance(
            preSelectedCategoryId = category.id
        )
        dialog.setTaskAddedListener {
            // Refresh the tasks
            refreshCategories()
        }
        dialog.show(parentFragmentManager, "addTask")
    }
    
    private fun deleteCategory(categoryId: String) {
        lifecycleScope.launch {
            repository.deleteCategory(categoryId)
            // Refresh the categories list
            refreshCategories()
            Toast.makeText(requireContext(), "Category deleted", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun refreshCategories() {
        lifecycleScope.launch {
            try {
                val categories = repository.getTaskCategories().first()
                categoryAdapter = TaskCategoryAdapter(
                    categories,
                    onCategoryClick = { category ->
                        // Navigate to filtered tasks by category
                        showTasksForCategory(category)
                    },
                    onAddTaskClick = { category ->
                        showAddTaskDialog(category)
                    },
                    onDeleteCategory = { categoryId ->
                        deleteCategory(categoryId)
                    }
                )
                binding.taskCategoriesRecyclerView.adapter = categoryAdapter
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error loading categories: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    fun refresh() {
        refreshCategories()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    companion object {
        fun newInstance() = TasksFragment()
    }
} 