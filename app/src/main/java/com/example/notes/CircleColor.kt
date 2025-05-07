package com.example.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun CircleColor(onClick: () -> Unit={},color: Color=Color.Transparent,currentColor: Color=Color.Transparent){
    Box(
        modifier = Modifier.padding(10.dp)
            .size(45.dp)
            .background(color, RoundedCornerShape(100))
            .clip(RoundedCornerShape(percent = 50))
            .clickable(onClick = onClick)
            .border(
                width = if (currentColor == color) 2.dp else 1.dp,
                color = if (currentColor == color) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(100)
            )
        ,
        contentAlignment = Alignment.Center
    ) {
        if (currentColor == color) {
            Icon(
                painter = painterResource(R.drawable.check),
                contentDescription = "Check",
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        } else if (color == Color.Transparent) {
            Icon(
                painter = painterResource(R.drawable.color_reset),
                contentDescription = "Color Reset",
                modifier = Modifier.size(30.dp),
            )
        }
    }
}