package com.example.notes

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.notes.data.Note
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.text.set


class NotesViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(Note())
    val uiState: StateFlow<Note> =_uiState.asStateFlow()

    fun setTitle(title: String) {
        _uiState.update {content->
            content.copy(title = title)
        }
        saveOrUpdateNote(_uiState.value)
    }
    fun setContent(body: String) {
        _uiState.update { content ->
            content.copy(content = body)
        }
        saveOrUpdateNote(_uiState.value)
    }
    fun initializeWithNote(note: Note) {
        _uiState.update {
            note.copy()
        }
        Log.d("NotesViewModel", "Initialized with note: ${note.id}, ${note.title}")
    }
    fun setArchive(){
        _uiState.update{
            content->
            content.copy(isArchived = content.isArchived.not(), isPin = false)
        }
        saveOrUpdateNote(_uiState.value,false)
    }
    fun setPin(){
        _uiState.update{
            content->
            content.copy(isPin = content.isPin.not(), isArchived = false)
        }
        saveOrUpdateNote(_uiState.value,false)
    }
    fun setColor(changedColor: Int){
        _uiState.update {
            content->
            content.copy(color = changedColor)
        }
        saveOrUpdateNote(_uiState.value,false)
    }
    // Keep this function for backward compatibility
    fun setNote(noteId: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users")
                .document(userId)
                .collection("notes")
                .document(noteId)
                .get()
                .addOnSuccessListener { document ->
                    document.toObject(Note::class.java)?.let { note ->
                        _uiState.update { content ->
                            content.copy(
                                id = noteId,
                                title = note.title,
                                content = note.content,
                                timestamp = note.timestamp,
                                color = note.color
                            )
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("SetNote", "Error getting note", e)
                }
        }
    }
    fun pinNotes(notes: Set<Note>,isNotPin: Boolean){
        notes.forEach {note->
            note.isPin=isNotPin
            if(isNotPin)
                note.isArchived=false
            saveOrUpdateNote(note,false)
        }
    }
    fun setArchive(notes: Set<Note>,isNotArchive: Boolean) {
        notes.forEach { note ->
            note.isArchived = isNotArchive
            if (isNotArchive)
                note.isPin = false
            saveOrUpdateNote(note, false)
        }
    }
    fun colorNotes(notes: Set<Note>,color: Int){
        notes.forEach {note->
            note.color=color
            saveOrUpdateNote(note,false)
        }
    }
    fun deleteNotes(notes: Set<Note>){
        notes.forEach{
            note->
            deleteNote(note)
        }
    }
    fun restoreNotes(notes: Set<Note>){
        notes.forEach{
            note->
            restoreNote(note)
        }
    }
    fun deleteForever(notes: Set<Note>){
        notes.forEach{note->
            deleteTrash(note.id)
        }
    }

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    fun copyNote(note: Note) {
        val copiedNote = note.copy(
            id = "",  // Empty ID so a new one will be generated
            timestamp = Timestamp.now(),
            isArchived = false,
            isPin=false
        )
        saveNote(copiedNote)
        _uiState.update{copiedNote}// Return the new ID
    }
    fun saveNote(note: Note){
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userNotesRef = db.collection("users").document(userId).collection("notes")

            // First update the UI state with a new ID
            val noteId = userNotesRef.document().id

            _uiState.update { it.copy(id = noteId) }

            // Then save the note with the pre-assigned ID
            val noteWithId = note.copy(id = noteId, timestamp = Timestamp.now())
            userNotesRef.document(noteId).set(noteWithId)
                .addOnSuccessListener {
                    Log.d("SaveNote", "Note saved successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("SaveNote", "Error saving note", e)
                }
            }
    }

    private fun updateNote(note: Note,cond: Boolean=true) {
        val userId = auth.currentUser?.uid
        if(cond)
            note.timestamp= Timestamp.now()

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .collection("notes")
                .document(note.id)
                .set(note) // This will replace the entire document with the new note
                .addOnSuccessListener {
                    Log.d("UpdateNote", "Note updated successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("UpdateNote", "Failed to update note", e)
                }
        }
    }
    private fun saveOrUpdateNote(note: Note,cond: Boolean=true) {
        if (note.id.isNotEmpty()) {
            updateNote(note,cond)
        } else {
            saveNote(note=note)
        }
    }
    fun deleteNote(note: Note){
        val userId = auth.currentUser?.uid
        if (userId != null && note.id.isNotEmpty()) {
            val batch = db.batch()

            // Update note with current timestamp before moving to trash
            val noteWithUpdatedTimestamp = note.copy(timestamp = Timestamp.now())

            // Reference to the note in 'notes' collection
            val noteRef = db.collection("users")
                .document(userId)
                .collection("notes")
                .document(note.id)

            // Reference to the note in 'trash' collection
            val trashRef = db.collection("users")
                .document(userId)
                .collection("trash")
                .document(note.id)

            // Add operations to batch
            batch.delete(noteRef) // Delete from notes collection
            batch.set(trashRef, noteWithUpdatedTimestamp) // Add to trash collection

            // Execute batch
            batch.commit()
                .addOnSuccessListener {
                    Log.d("DeleteNote", "Note moved to trash successfully: ${note.id}")
                    // Clear the UI state if the deleted note is currently being viewed
                    if (_uiState.value.id == note.id) {
                        _uiState.update { Note() }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DeleteNote", "Failed to move note to trash: ${note.id}", e)
                }
        } else {
            Log.e("DeleteNote", "Invalid note ID or user ID")
        }
    }
    fun restoreNote(note: Note){
        val userId = auth.currentUser?.uid
        if (userId != null && note.id.isNotEmpty()) {
            val batch = db.batch()

            // Update note with current timestamp before moving to trash
            val noteWithUpdatedTimestamp = note.copy(timestamp = Timestamp.now())

            // Reference to the note in 'notes' collection
            val trashRef = db.collection("users")
                .document(userId)
                .collection("trash")
                .document(note.id)

            // Reference to the note in 'trash' collection
            val noteRef = db.collection("users")
                .document(userId)
                .collection("notes")
                .document(note.id)

            // Add operations to batch
            batch.delete(trashRef) // Delete from notes collection
            batch.set(noteRef, noteWithUpdatedTimestamp) // Add to trash collection

            // Execute batch
            batch.commit()
                .addOnSuccessListener {
                    Log.d("RestoreNote", "Trash moved to notes successfully: ${note.id}")
                    // Clear the UI state if the deleted note is currently being viewed
                    if (_uiState.value.id == note.id) {
                        _uiState.update { Note() }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("RestoreNote", "Failed to move trash to notes: ${note.id}", e)
                }
        } else {
            Log.e("RestoreNote", "Invalid note ID or user ID")
        }
    }
    fun deleteTrash(noteId: String) {
        if(noteId.isEmpty())
            return
        val userId = auth.currentUser?.uid

        if (userId != null) {
            db.collection("users")
                .document(userId)
                .collection("trash")
                .document(noteId)
                .delete()
                .addOnSuccessListener {
                    Log.d("DeleteNote", "Note deleted successfully")
                }
                .addOnFailureListener { e ->
                    Log.e("DeleteNote", "Failed to delete note", e)
                }
        }
    }
    fun emptyTrash(){
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val trashRef = db.collection("users")
                .document(userId)
                .collection("trash")

            trashRef.get()
                .addOnSuccessListener { documents ->
                    // Create batches of 500 deletes (Firestore limit)
                    val batch = db.batch()
                    var operationCount = 0

                    for (document in documents) {
                        batch.delete(document.reference)
                        operationCount++

                        // Commit batch when reaching 500 operations
                        if (operationCount == 500) {
                            batch.commit()
                            operationCount = 0
                        }
                    }

                    // Commit any remaining operations
                    if (operationCount > 0) {
                        batch.commit()
                            .addOnSuccessListener {
                                Log.d("DeleteAllTrash", "Trash collection deleted successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e("DeleteAllTrash", "Failed to delete trash collection", e)
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("DeleteAllTrash", "Failed to get trash documents", e)
                }
        }
    }

    private val _notes= MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _archivedNotes = MutableStateFlow<List<Note>>(emptyList())
    val archivedNotes: StateFlow<List<Note>> = _archivedNotes.asStateFlow()

    private val _trash= MutableStateFlow<List<Note>>(emptyList())
    val trash: StateFlow<List<Note>> = _trash.asStateFlow()

    init{
        fetchNotes()
        fetchTrash()
    }

    private fun fetchNotes(){
        val userId= auth.currentUser?.uid

        if(userId!=null){
            db.collection("users")
                .document(userId)
                .collection("notes")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("FetchNotes", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    val notesList = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Note::class.java)
                    }?.sortedByDescending {
                        it.timestamp
                    } ?: emptyList()

                    _notes.value = notesList
                    _archivedNotes.value = notesList.filter { it.isArchived }
                }
        }
    }
    private fun fetchTrash(){
        val userId= auth.currentUser?.uid

        if(userId!=null){
            db.collection("users")
                .document(userId)
                .collection("trash")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.e("FetchNotes", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    val notesList = snapshot?.documents?.mapNotNull { doc ->
                        doc.toObject(Note::class.java)
                    }?.sortedByDescending {
                        it.timestamp
                    } ?: emptyList()

                    _trash.value = notesList
                }
        }
    }

}
