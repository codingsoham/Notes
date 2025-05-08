package com.example.notes.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.isSystemInDarkTheme

object colors {

    private val lightColors = listOf(
        Color.Transparent,
        Color(0xFFFFCDD2), // soft red
        Color(0xFFFFAB91), // peach
        Color(0xFFFFFFB3), // light yellow
        Color(0xFFB3E5FC), // sky blue
        Color(0xFFDCEDF9), // pale blue
        Color(0xFFB2DFDB), // mint green
        Color(0xFFDCEDC8), // soft green
        Color(0xFFD1C4E9), // soft purple
        Color(0xFFF8E1E1), // light pink
        Color(0xFFEDE7D1), // beige
        Color(0xFFF5F5F5)  // off-white
    )

    private val darkColors = listOf(
        Color.Transparent,
        Color(0xFF77172E),
        Color(0xFF692B17),
        Color(0xFF7C4A03),
        Color(0xFF264D3B),
        Color(0xFF0C625D),
        Color(0xFF256377),
        Color(0xFF284255),
        Color(0xFF472e5b),
        Color(0xFF6c394f),
        Color(0xFF4b443a),
        Color(0xFF232427)
    )

    fun noteColors(darkTheme: Boolean): List<Color> {
        return if (darkTheme) darkColors else lightColors
    }

    fun getColor(index: Int, darkTheme: Boolean): Color {
        return if (darkTheme) darkColors.getOrElse(index) { darkColors.last() }
        else lightColors.getOrElse(index) { lightColors.last() }
    }

    fun setColor(color: Color, darkTheme: Boolean): Int {
        val palette = if (darkTheme) darkColors else lightColors
        return palette.indexOfFirst { it == color }.takeIf { it != -1 } ?: 0
    }
}
