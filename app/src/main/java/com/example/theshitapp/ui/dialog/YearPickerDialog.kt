package com.example.theshitapp.ui.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.theshitapp.adapter.YearAdapter
import com.example.theshitapp.databinding.DialogYearPickerBinding
import java.util.Calendar

class YearPickerDialog(
    context: Context,
    private val currentYear: Int,
    private val onYearSelected: (Int) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogYearPickerBinding
    private val years = generateYearsList(currentYear)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogYearPickerBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        val yearAdapter = YearAdapter(
            years = years,
            selectedYear = currentYear,
            onYearSelected = { year ->
                onYearSelected(year)
                dismiss()
            }
        )

        binding.yearPickerRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = yearAdapter
            
            // Scroll to position near the current year
            val currentYearPosition = years.indexOf(currentYear)
            if (currentYearPosition != -1) {
                post {
                    scrollToPosition(currentYearPosition)
                }
            }
        }
    }

    private fun generateYearsList(currentYear: Int): List<Int> {
        // Generate a range of years from 100 years ago to 30 years in the future
        val startYear = currentYear - 75
        val endYear = currentYear + 25
        return (startYear..endYear).toList()
    }
} 