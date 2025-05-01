package com.example.theshitapp.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.theshitapp.TheShitApp
import com.example.theshitapp.databinding.DialogAddCategoryBinding
import com.example.theshitapp.model.TaskCategory
import com.example.theshitapp.repository.TaskRepositoryImpl
import kotlinx.coroutines.launch

class AddCategoryDialog : DialogFragment() {

    private var _binding: DialogAddCategoryBinding? = null
    private val binding get() = _binding!!
    
    // Repository reference
    private val repository: TaskRepositoryImpl by lazy {
        TheShitApp.getInstance().repository
    }
    
    // Category added listener
    private var categoryAddedListener: ((TaskCategory) -> Unit)? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtons()
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
    
    fun setCategoryAddedListener(listener: (TaskCategory) -> Unit) {
        categoryAddedListener = listener
    }
    
    private fun setupButtons() {
        binding.saveButton.setOnClickListener {
            val categoryName = binding.categoryNameInput.text.toString().trim()
            if (categoryName.isNotEmpty()) {
                lifecycleScope.launch {
                    try {
                        val category = repository.addCategory(categoryName)
                        if (category != null) {
                            categoryAddedListener?.invoke(category)
                            dismiss()
                        } else {
                            // Category name already exists
                            binding.categoryNameInput.error = "Category name already exists"
                        }
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error adding category: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                binding.categoryNameInput.error = "Please enter a category name"
            }
        }
        
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }
    
    companion object {
        fun newInstance(): AddCategoryDialog {
            return AddCategoryDialog()
        }
    }
} 