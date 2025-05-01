package com.example.theshitapp.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.graphics.withSave
import com.example.theshitapp.R
import com.example.theshitapp.model.Task
import com.example.theshitapp.model.TaskPriority
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeeklyScheduleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Dimensions
    private var hourHeight = context.resources.getDimension(R.dimen.schedule_hour_height)
    private var dayWidth = context.resources.getDimension(R.dimen.schedule_day_width)
    private var hourLabelWidth = context.resources.getDimension(R.dimen.schedule_time_column_width)
    private var dayHeaderHeight = 60f
    private val dayGap = context.resources.getDimension(R.dimen.margin_small)
    private val taskCornerRadius = context.resources.getDimension(R.dimen.card_corner_radius)
    private val timeColumnWidth = context.resources.getDimension(R.dimen.schedule_time_column_width)
    
    // Scaling
    private var scaleFactor = 1.0f
    private val minScaleFactor = 0.5f
    private val maxScaleFactor = 3.0f
    
    // Colors
    private val hourLineColor = Color.parseColor("#CCCCCC")  // Light gray line color
    private val dayHeaderBgColor = Color.parseColor("#F8F9FA")  // Light header background
    private val dayHeaderTextColor = Color.parseColor("#333333")  // Dark header text
    private val timeTextColor = Color.parseColor("#333333")  // Dark color for time labels
    private val backgroundColor = Color.parseColor("#FFFFFF")  // Light background for the schedule
    private val priorityColors = mapOf(
        TaskPriority.LOW to Color.parseColor("#4CAF50"),
        TaskPriority.MEDIUM to Color.parseColor("#2196F3"),
        TaskPriority.HIGH to Color.parseColor("#F44336")
    )
    
    // Paints
    private val hourLinePaint = Paint().apply {
        color = hourLineColor
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }
    
    private val dayHeaderBgPaint = Paint().apply {
        color = dayHeaderBgColor
        style = Paint.Style.FILL
    }
    
    private val dayHeaderTextPaint = Paint().apply {
        color = dayHeaderTextColor
        textSize = 16f
        textAlign = Paint.Align.CENTER
    }
    
    private val timeTextPaint = Paint().apply {
        color = timeTextColor
        textSize = 14f
        textAlign = Paint.Align.RIGHT
    }
    
    private val taskPaint = Paint().apply {
        style = Paint.Style.FILL
    }
    
    private val taskTextPaint = Paint().apply {
        color = Color.WHITE
        textSize = 12f
        textAlign = Paint.Align.LEFT
    }
    
    private val completedTaskCheckmarkPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    
    // Data
    private var startDate: Date = Date()
    private var tasks: List<Task> = emptyList()
    private var tasksByDayAndHour: Map<Int, Map<Int, List<Task>>> = emptyMap()
    
    // Gesture detection
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            // First, check if a task was tapped
            val task = findTaskAtPosition(e.x, e.y)
            if (task != null) {
                onTaskClickListener?.invoke(task)
                return true
            }
            
            // Otherwise, handle time slot tap
            val adjustedX = e.x - timeColumnWidth
            if (adjustedX < 0) return false // Clicked in time column
            
            val dayIndex = ((adjustedX) / (dayWidth + dayGap)).toInt()
            val hour = ((e.y - getHeaderHeight()) / (hourHeight * scaleFactor)).toInt()
            
            if (dayIndex in 0..6 && hour in 0..23) {
                val calendar = Calendar.getInstance()
                calendar.time = startDate
                calendar.add(Calendar.DAY_OF_YEAR, dayIndex)
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                
                onTimeSlotClickListener?.invoke(calendar.time, hour)
                return true
            }
            return false
        }
    })
    
    // Scale gesture detector for zooming
    private val scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(minScaleFactor, maxScaleFactor)
            invalidate()
            requestLayout()
            return true
        }
    })
    
    var onTimeSlotClickListener: ((Date, Int) -> Unit)? = null
    var onTaskClickListener: ((Task) -> Unit)? = null
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = (timeColumnWidth + 7 * (dayWidth + dayGap)).toInt()
        // Ensure we have full 24 hour height
        val height = (getHeaderHeight() + 24 * hourHeight * scaleFactor).toInt()
        setMeasuredDimension(width, height)
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Clear canvas with background color
        canvas.drawColor(backgroundColor)
        
        // Draw day headers
        drawDayHeaders(canvas)
        
        // Draw time labels with proper scaling
        drawTimeLabels(canvas)
        
        // Draw hour lines
        drawHourLines(canvas)
        
        // Draw tasks
        drawTasks(canvas)
    }
    
    private fun drawTimeLabels(canvas: Canvas) {
        timeTextPaint.textSize = 14f * scaleFactor
        timeTextPaint.color = Color.parseColor("#333333")
        
        // Ensure we draw all 24 hours
        for (hour in 0 until 24) {
            val y = getHeaderHeight() + hour * hourHeight * scaleFactor
            val timeText = if (hour == 0) {
                "12 AM"
            } else if (hour < 12) {
                "$hour AM"
            } else if (hour == 12) {
                "12 PM"
            } else {
                "${hour - 12} PM"
            }
            
            canvas.drawText(timeText, hourLabelWidth - 4, y + 16f * scaleFactor, timeTextPaint)
        }
    }
    
    private fun drawDayHeaders(canvas: Canvas) {
        val headerHeight = getHeaderHeight()
        val dateFormat = SimpleDateFormat("EEE\nMMM d", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        
        for (day in 0 until 7) {
            val left = timeColumnWidth + day * (dayWidth + dayGap)
            val right = left + dayWidth
            
            // Draw header background
            canvas.drawRect(left, 0f, right, headerHeight, dayHeaderBgPaint)
            
            // Draw day text
            val dayText = dateFormat.format(calendar.time)
            canvas.drawText(
                dayText.split("\n")[0], 
                left + dayWidth / 2, 
                headerHeight / 3, 
                dayHeaderTextPaint
            )
            canvas.drawText(
                dayText.split("\n")[1], 
                left + dayWidth / 2, 
                headerHeight * 2 / 3, 
                dayHeaderTextPaint
            )
            
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }
    
    private fun drawHourLines(canvas: Canvas) {
        val headerHeight = getHeaderHeight()
        val contentWidth = timeColumnWidth + 7 * (dayWidth + dayGap)
        
        // Draw horizontal hour lines for all 24 hours
        for (hour in 0..24) {
            val y = headerHeight + hour * hourHeight * scaleFactor
            canvas.drawLine(0f, y, contentWidth, y, hourLinePaint)
        }
        
        // Draw vertical day separators
        for (day in 0..7) {
            val x = timeColumnWidth + day * (dayWidth + dayGap)
            canvas.drawLine(x, headerHeight, x, headerHeight + 24 * hourHeight * scaleFactor, hourLinePaint)
        }
    }
    
    private fun drawTasks(canvas: Canvas) {
        val headerHeight = getHeaderHeight()
        
        for (day in 0 until 7) {
            tasksByDayAndHour[day]?.forEach { (hour, tasksInHour) ->
                val top = headerHeight + hour * hourHeight * scaleFactor
                val left = timeColumnWidth + day * (dayWidth + dayGap)
                
                tasksInHour.forEachIndexed { _, task ->
                    // Calculate task height based on duration (minutes)
                    val durationHours = task.durationMinutes / 60.0f
                    val taskHeight = hourHeight * scaleFactor * durationHours
                    val taskTop = top
                    val taskBottom = taskTop + taskHeight
                    
                    // Draw task rectangle
                    val rect = RectF(left + 2, taskTop + 2, left + dayWidth - 2, taskBottom - 2)
                    
                    // Use the task's custom color if available, otherwise fall back to priority colors
                    try {
                        taskPaint.color = Color.parseColor(task.taskColor)
                    } catch (e: Exception) {
                        taskPaint.color = priorityColors[task.priority] ?: priorityColors[TaskPriority.MEDIUM]!!
                    }
                    
                    canvas.drawRoundRect(rect, taskCornerRadius, taskCornerRadius, taskPaint)
                    
                    // Center the task title both horizontally and vertically
                    taskTextPaint.textAlign = Paint.Align.CENTER
                    val titleX = rect.left + (rect.width() / 2)
                    val titleY = rect.top + (rect.height() / 2) + 6 // Small offset to visually center text
                    
                    canvas.drawText(
                        task.title,
                        titleX,
                        titleY,
                        taskTextPaint
                    )
                    
                    // If task is completed, add just a checkmark without the overlay
                    if (task.isCompleted) {
                        // Draw a checkmark in the corner
                        val checkSize = rect.width() / 4
                        val checkLeft = rect.right - checkSize - 4
                        val checkTop = rect.top + 4
                        
                        // Simple checkmark
                        canvas.drawLine(
                            checkLeft, 
                            checkTop + checkSize/2, 
                            checkLeft + checkSize/3, 
                            checkTop + checkSize, 
                            completedTaskCheckmarkPaint
                        )
                        canvas.drawLine(
                            checkLeft + checkSize/3, 
                            checkTop + checkSize, 
                            checkLeft + checkSize, 
                            checkTop, 
                            completedTaskCheckmarkPaint
                        )
                    }
                }
            }
        }
    }
    
    private fun findTaskAtPosition(x: Float, y: Float): Task? {
        val headerHeight = getHeaderHeight()
        
        // If clicked above header, no task there
        if (y < headerHeight) return null
        
        // Calculate day index and hour from coordinates
        val adjustedX = x - timeColumnWidth
        if (adjustedX < 0) return null // Clicked in time column
        
        val dayIndex = (adjustedX / (dayWidth + dayGap)).toInt()
        val hour = ((y - headerHeight) / (hourHeight * scaleFactor)).toInt()
        
        if (dayIndex !in 0..6 || hour !in 0..23) return null
        
        // Check if there's a task at this position
        val tasksInHour = tasksByDayAndHour[dayIndex]?.get(hour) ?: return null
        
        // For multiple tasks in same hour, we'd need more complex logic
        // Simplified for now - returns the first task
        return if (tasksInHour.isNotEmpty()) tasksInHour.first() else null
    }
    
    private fun getHeaderHeight() = 50f
    
    fun setWeekStartDate(date: Date) {
        val calendar = Calendar.getInstance()
        calendar.time = date
        
        // Move to the beginning of the week (Sunday)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        startDate = calendar.time
        organizeTasks()
        invalidate()
    }
    
    fun setTasks(tasks: List<Task>) {
        this.tasks = tasks
        organizeTasks()
        invalidate()
    }
    
    private fun organizeTasks() {
        val result = mutableMapOf<Int, MutableMap<Int, MutableList<Task>>>()
        
        // Initialize all days and hours
        for (day in 0 until 7) {
            result[day] = mutableMapOf()
            for (hour in 0 until 24) {
                result[day]!![hour] = mutableListOf()
            }
        }
        
        // Process each task
        val calendar = Calendar.getInstance()
        val endCalendar = Calendar.getInstance()
        endCalendar.time = startDate
        endCalendar.add(Calendar.DAY_OF_YEAR, 7)
        
        for (task in tasks) {
            calendar.time = task.dueDate
            
            // Skip if outside our week range
            if (calendar.time.before(startDate) || calendar.time.after(endCalendar.time)) {
                continue
            }
            
            // Calculate day index (0-6) from start date
            val dayDiff = dayDifference(startDate, calendar.time)
            if (dayDiff < 0 || dayDiff > 6) continue
            
            // Get hour of day
            val hour = if (task.dueTime != null) {
                val timeCalendar = Calendar.getInstance()
                timeCalendar.timeInMillis = task.dueTime
                timeCalendar.get(Calendar.HOUR_OF_DAY)
            } else {
                calendar.get(Calendar.HOUR_OF_DAY)
            }
            
            // Add to appropriate day and hour
            result[dayDiff]!![hour]!!.add(task)
        }
        
        tasksByDayAndHour = result
    }
    
    private fun dayDifference(date1: Date, date2: Date): Int {
        val cal1 = Calendar.getInstance()
        cal1.time = date1
        cal1.set(Calendar.HOUR_OF_DAY, 0)
        cal1.set(Calendar.MINUTE, 0)
        cal1.set(Calendar.SECOND, 0)
        cal1.set(Calendar.MILLISECOND, 0)
        
        val cal2 = Calendar.getInstance()
        cal2.time = date2
        cal2.set(Calendar.HOUR_OF_DAY, 0)
        cal2.set(Calendar.MINUTE, 0)
        cal2.set(Calendar.SECOND, 0)
        cal2.set(Calendar.MILLISECOND, 0)
        
        val diff = cal2.timeInMillis - cal1.timeInMillis
        return (diff / (24 * 60 * 60 * 1000)).toInt()
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Handle scaling
        scaleGestureDetector.onTouchEvent(event)
        
        // Handle taps
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }
} 