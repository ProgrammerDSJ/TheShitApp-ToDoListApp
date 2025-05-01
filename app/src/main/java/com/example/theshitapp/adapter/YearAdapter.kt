package com.example.theshitapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.theshitapp.R

class YearAdapter(
    private val years: List<Int>,
    private val selectedYear: Int,
    private val onYearSelected: (Int) -> Unit
) : RecyclerView.Adapter<YearAdapter.YearViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YearViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_year, parent, false)
        return YearViewHolder(view)
    }

    override fun onBindViewHolder(holder: YearViewHolder, position: Int) {
        val year = years[position]
        holder.bind(year)
    }

    override fun getItemCount(): Int = years.size

    inner class YearViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val yearText: TextView = itemView.findViewById(R.id.yearText)

        fun bind(year: Int) {
            yearText.text = year.toString()
            
            // Highlight the selected year
            if (year == selectedYear) {
                yearText.setTextColor(itemView.context.getColor(R.color.blue))
            } else {
                yearText.setTextColor(itemView.context.getColor(R.color.white))
            }

            itemView.setOnClickListener {
                onYearSelected(year)
            }
        }
    }
} 