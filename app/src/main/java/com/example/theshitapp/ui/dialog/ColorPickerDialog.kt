package com.example.theshitapp.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.example.theshitapp.R

class ColorPickerDialog(
    context: Context,
    private val initialColor: String,
    private val onColorSelected: (String) -> Unit
) : Dialog(context) {

    private val colorOptions = mapOf(
        "Blue" to "#3B82F6",
        "Green" to "#22C55E",
        "Red" to "#EF4444",
        "Yellow" to "#EAB308",
        "Purple" to "#8B5CF6", 
        "Pink" to "#EC4899",
        "Orange" to "#F97316",
        "Teal" to "#14B8A6"
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_color_picker)
        setTitle("Select Task Color")
        
        val container = findViewById<LinearLayout>(R.id.colorOptionsContainer)
        
        // Create color options
        colorOptions.forEach { (name, hex) ->
            val colorView = createColorView(name, hex)
            container.addView(colorView)
        }
    }
    
    private fun createColorView(colorName: String, hexColor: String): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_color_option, null)
        val colorPreview = view.findViewById<View>(R.id.colorPreview)
        val colorNameText = view.findViewById<android.widget.TextView>(R.id.colorName)
        
        colorPreview.setBackgroundColor(Color.parseColor(hexColor))
        colorNameText.text = colorName
        
        // Set click listener
        view.setOnClickListener {
            onColorSelected(hexColor)
            dismiss()
        }
        
        // Highlight if it's the currently selected color
        if (hexColor == initialColor) {
            view.setBackgroundColor(ContextCompat.getColor(context, R.color.light_gray))
        }
        
        return view
    }
} 