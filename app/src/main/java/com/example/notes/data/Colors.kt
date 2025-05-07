package com.example.notes.data

import androidx.compose.ui.graphics.Color


object colors{
    var noteColors=listOf<Color>(
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
        Color(0xFF232427),
    )
    fun getColor(color: Int): Color {
        return when (color) {
            0 -> Color.Transparent
            1 -> Color(0xFF77172E)
            2 -> Color(0xFF692B17)
            3 -> Color(0xFF7C4A03)
            4 -> Color(0xFF264D3B)
            5 -> Color(0xFF0C625D)
            6 -> Color(0xFF256377)
            7 -> Color(0xFF284255)
            8 -> Color(0xFF472e5b)
            9 -> Color(0xFF6c394f)
            10 -> Color(0xFF4b443a)
            else -> Color(0xFF232427)
        }
    }
    fun setColor(color: Color): Int{
       return when (color) {
            Color.Transparent -> 0
            Color(0xFF77172E) -> 1
            Color(0xFF692B17) -> 2
            Color(0xFF7C4A03) -> 3
            Color(0xFF264D3B) -> 4
            Color(0xFF0C625D) -> 5
            Color(0xFF256377) -> 6
            Color(0xFF284255) -> 7
            Color(0xFF472e5b) -> 8
            Color(0xFF6c394f) -> 9
            Color(0xFF4b443a) -> 10
            else -> 11
        }
    }
}