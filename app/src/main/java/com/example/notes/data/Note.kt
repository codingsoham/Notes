package com.example.notes.data

import android.os.Parcelable
import com.google.firebase.Timestamp
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    var id: String = "",
    var title: String = "",
    var content: String = "",
    var timestamp: Timestamp = Timestamp.now(),
    var color: Int = 0,
    var isPin: Boolean=false,
    var isArchived: Boolean=false
): Parcelable

