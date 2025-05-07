package com.example.notes.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle

/**
 * A composable that displays text with highlighted search terms
 *
 * @param text The text to display
 * @param searchTerms List of search terms to highlight
 * @param style The text style to use
 * @param maxLines Maximum number of lines to display (0 for unlimited)
 */
@Composable
fun HighlightedText(
    text: String,
    searchTerms: List<String>,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    maxLines: Int = 0,
    modifier: Modifier = Modifier,
    onTextLayout: (TextLayoutResult) -> Unit = {}
) {
    val annotatedString = buildHighlightedText(text, searchTerms)

    Text(
        text = annotatedString,
        style = style,
        maxLines = if (maxLines > 0) maxLines else Int.MAX_VALUE,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
        onTextLayout = onTextLayout
    )
}

/**
 * Builds an AnnotatedString with highlighted search terms
 */
@Composable
private fun buildHighlightedText(text: String, searchTerms: List<String>): AnnotatedString {
    if (searchTerms.isEmpty()) return AnnotatedString(text)

    // Join search terms to create a regex pattern that matches any term
    // Use word boundaries for more accurate highlighting
    val searchPattern = searchTerms
        .map { term ->
            // Escape special regex characters to avoid matching errors
            val escaped = term.replace(Regex("[\\[\\](){}*+?^$|.]"), "\\\\$0")
            "($escaped)"
        }
        .joinToString("|")

    val regex = Regex(searchPattern, RegexOption.IGNORE_CASE)

    val isSystemInDark = isSystemInDarkTheme()
    val highlightColor = if (isSystemInDark) {
        Color(0xFFFDD663) // Dark theme highlight color
    } else {
        Color(0xFFFDE090) // Light theme highlight color
    }
    return buildAnnotatedString {
        var lastIndex = 0

        // Find all matches and apply highlighting
        regex.findAll(text).forEach { matchResult ->
            // Add text before the match
            append(text.substring(lastIndex, matchResult.range.first))

            // Add the highlighted match
            withStyle(
                SpanStyle(
                    background = highlightColor,
                    color=Color.Black
                )
            ) {
                append(matchResult.value)
            }

            lastIndex = matchResult.range.last + 1
        }

        // Add remaining text after the last match
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}