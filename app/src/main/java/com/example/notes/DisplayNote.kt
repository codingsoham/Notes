package com.example.notes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.notes.components.HighlightedText
import com.example.notes.data.Note
import com.example.notes.data.colors

@Composable
fun DisplayNote(
    note: Note,
    searchTerms: List<String> = emptyList(),
    onClickNote: (Note) -> Unit = {},
    onSelected: (Note) -> Unit = {},
    onDeselected: (Note) -> Unit = {},
    isSelected: Boolean=false,

    
    isAnySelected: Boolean=false
) {
    val currentColor=colors.getColor(note.color)
    Card(
        modifier = Modifier.heightIn(min=55.dp)
            .padding(top = 5.dp, start = 5.dp)
            .combinedClickable(
                onClick = {
                    if(isAnySelected) {
                        if(isSelected) {
                            onDeselected(note)
                        }else {
                            onSelected(note)
                        }
                    }else
                    onClickNote(note)
              },
                onLongClick = {
                    if(isSelected) {
                        onDeselected(note)
                    }else {
                        onSelected(note)
                    }
                }
            ),
        border =
            if(isSelected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
            else BorderStroke(1.dp, if(currentColor==Color.Transparent) MaterialTheme.colorScheme.outlineVariant else currentColor),
        colors = CardDefaults.cardColors(
            containerColor = if(currentColor!=Color.Transparent) currentColor else MaterialTheme.colorScheme.surface.copy(alpha = 0f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if (searchTerms.isEmpty()) {
                if (note.title.isNotEmpty()){
                    Text(
                        text = note.title,
                        style = typography.titleMedium,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
                if(note.content.isNotEmpty()) {
                    Text(
                        text = note.content,
                        style = typography.bodyMedium,
                        maxLines = 10,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            } else {
                if (note.title.isNotEmpty()) {
                    HighlightedText(
                        text = note.title,
                        searchTerms = searchTerms,
                        style = typography.titleMedium,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }

                if(note.content.isNotEmpty()) {
                    HighlightedText(
                        text = note.content,
                        searchTerms = searchTerms,
                        style = typography.bodyMedium,
                        maxLines = 10,
                    )
                }
            }
        }
    }
}