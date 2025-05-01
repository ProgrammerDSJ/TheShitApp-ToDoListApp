package com.example.theshitapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.theshitapp.R
import com.example.theshitapp.model.CalendarDay
import java.util.Calendar
import java.util.Date

class CalendarDayAdapter(
    private var days: List<CalendarDay>,
    private val onDayClick: (Date) -> Unit
) : RecyclerView.Adapter<CalendarDayAdapter.CalendarDayViewHolder>() {

    private var selectedDayPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return CalendarDayViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarDayViewHolder, position: Int) {
        val calendarDay = days[position]
        holder.bind(calendarDay, position == selectedDayPosition)
    }

    override fun getItemCount(): Int = days.size

    fun updateDays(newDays: List<CalendarDay>) {
        days = newDays
        notifyDataSetChanged()
    }

    fun selectDay(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date
        
        // Find day position
        val newSelectedPosition = days.indexOfFirst { day ->
            if (day.date != null) {
                val dayCalendar = Calendar.getInstance()
                dayCalendar.time = day.date
                dayCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                dayCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                dayCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
            } else {
                false
            }
        }
        
        if (newSelectedPosition != -1) {
            val oldSelectedPosition = selectedDayPosition
            selectedDayPosition = newSelectedPosition
            
            if (oldSelectedPosition != -1) {
                notifyItemChanged(oldSelectedPosition)
            }
            notifyItemChanged(selectedDayPosition)
        }
    }

    inner class CalendarDayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayText: TextView = itemView.findViewById(R.id.dayText)
        private val dayIndicator: View = itemView.findViewById(R.id.dayIndicator)
        
        fun bind(day: CalendarDay, isSelected: Boolean) {
            if (day.date == null) {
                // Empty day
                dayText.text = ""
                dayIndicator.visibility = View.GONE
                itemView.setOnClickListener(null)
                dayText.background = null
            } else {
                // Set day number
                val calendar = Calendar.getInstance()
                calendar.time = day.date
                dayText.text = calendar.get(Calendar.DAY_OF_MONTH).toString()
                
                // Set click listener
                itemView.setOnClickListener {
                    onDayClick(day.date)
                }
                
                // Show indicator if there are tasks
                dayIndicator.visibility = if (day.hasTask) View.VISIBLE else View.GONE
                
                // Apply different styles based on day type and selection
                if (isSelected) {
                    // Selected day
                    dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))
                    dayText.setBackgroundResource(R.drawable.bg_calendar_day_selected)
                } else if (day.isToday) {
                    // Today
                    dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.blue))
                    dayText.setBackgroundResource(R.drawable.bg_calendar_day_today)
                } else if (day.isCurrentMonth) {
                    // Current month
                    dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_primary))
                    dayText.background = null
                } else {
                    // Other month
                    dayText.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                    dayText.background = null
                }
            }
        }
    }
} 