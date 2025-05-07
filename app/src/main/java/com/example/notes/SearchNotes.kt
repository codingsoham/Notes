package com.example.notes.utils

import com.example.notes.data.Note

/**
 * Improved search function for notes that mimics behavior similar to Apple Notes and Google Keep
 *
 * Features:
 * - Ranks results by relevance
 * - Handles partial word matches
 * - Prioritizes title matches over content matches
 * - Supports multi-word search terms
 * - Scores exact matches higher than partial matches
 *
 * @param notes List of notes to search within
 * @param searchString The search query
 * @return List of notes sorted by relevance score
 */
fun searchNotes(notes: List<Note>, searchString: String): List<Note> {
    if (searchString.isBlank()) return emptyList()

    // Normalize and split search query into terms
    val searchTerms = searchString.trim().lowercase().split(Regex("\\s+"))

    // Map to store search results with their relevance scores
    val searchResults = mutableMapOf<Note, Double>()

    for (note in notes) {
        // Calculate relevance score for each note
        val score = calculateRelevanceScore(note, searchTerms)

        // Add note to results if it has a positive score
        if (score > 0) {
            searchResults[note] = score
        }
    }

    // Sort results by score (descending) and return notes
    return searchResults.entries
        .sortedByDescending { it.value }
        .map { it.key }
}

/**
 * Calculates a relevance score for a note based on the search terms
 *
 * Scoring factors:
 * - Title matches are weighted higher than content matches (3x)
 * - Exact word matches score higher than partial matches
 * - Multiple term matches increase the score
 * - More terms matched = higher score
 * - Consecutive terms found in same order score bonus points
 *
 * @param note The note to calculate score for
 * @param searchTerms List of search terms
 * @return Relevance score (higher is better match)
 */
private fun calculateRelevanceScore(note: Note, searchTerms: List<String>): Double {
    var score = 0.0

    // Normalize note text for comparison
    val titleLower = note.title.lowercase()
    val contentLower = note.content.lowercase()

    // Check if the entire search query appears as is
    val fullQuery = searchTerms.joinToString(" ")
    if (titleLower.contains(fullQuery)) {
        score += 10.0  // Highest priority for exact phrase in title
    }
    if (contentLower.contains(fullQuery)) {
        score += 5.0   // High priority for exact phrase in content
    }

    // Track matched terms for consecutive matching
    val matchedTermsInTitle = mutableListOf<Int>()
    val matchedTermsInContent = mutableListOf<Int>()

    // Score each term individually
    searchTerms.forEachIndexed { index, term ->
        // Check title matches (weighted 3x)
        if (titleLower == term) {
            score += 3.0  // Exact title match
            matchedTermsInTitle.add(index)
        } else if (titleLower.contains(" $term ") ||
            titleLower.startsWith("$term ") ||
            titleLower.endsWith(" $term") ||
            titleLower == term) {
            score += 2.5  // Exact word in title
            matchedTermsInTitle.add(index)
        } else if (titleLower.contains(term)) {
            score += 2.0  // Partial match in title
            matchedTermsInTitle.add(index)
        }

        // Split title into words for word-boundary matching
        val titleWords = titleLower.split(Regex("[\\s,.!?;:]+"))
        for (word in titleWords) {
            if (word.startsWith(term)) {
                score += 1.5  // Term appears at start of a word in title
                matchedTermsInTitle.add(index)
                break
            }
        }

        // Check content matches
        if (contentLower.contains(" $term ") ||
            contentLower.startsWith("$term ") ||
            contentLower.endsWith(" $term") ||
            contentLower == term) {
            score += 1.0  // Exact word in content
            matchedTermsInContent.add(index)
        } else if (contentLower.contains(term)) {
            score += 0.7  // Partial match in content
            matchedTermsInContent.add(index)
        }

        // Split content into words for word-boundary matching
        val contentWords = contentLower.split(Regex("[\\s,.!?;:]+"))
        for (word in contentWords) {
            if (word.startsWith(term)) {
                score += 0.5  // Term appears at start of a word in content
                matchedTermsInContent.add(index)
                break
            }
        }
    }

    // Bonus for percentage of terms matched
    val uniqueTermsMatched = (matchedTermsInTitle + matchedTermsInContent).distinct().size
    val percentMatched = uniqueTermsMatched.toDouble() / searchTerms.size
    score += percentMatched * 2.0

    // Bonus for consecutive terms appearing in the same order
    score += calculateConsecutiveMatchBonus(matchedTermsInTitle)
    score += calculateConsecutiveMatchBonus(matchedTermsInContent) * 0.5 // Half weight for content

    return score
}

/**
 * Calculates bonus score for consecutive term matches in the same order
 */
private fun calculateConsecutiveMatchBonus(matchedIndices: List<Int>): Double {
    if (matchedIndices.size <= 1) return 0.0

    val sortedIndices = matchedIndices.sorted()
    var consecutiveCount = 0
    var bonus = 0.0

    for (i in 1 until sortedIndices.size) {
        if (sortedIndices[i] == sortedIndices[i-1] + 1) {
            consecutiveCount++
            // Exponential bonus for longer consecutive matches
            bonus += 0.5 * consecutiveCount
        } else {
            consecutiveCount = 0
        }
    }

    return bonus
}

