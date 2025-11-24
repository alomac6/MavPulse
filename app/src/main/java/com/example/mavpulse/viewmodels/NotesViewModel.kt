package com.example.mavpulse.viewmodels

import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mavpulse.Note
import com.example.mavpulse.network.ApiService
import com.example.mavpulse.network.FavoriteRequest
import com.example.mavpulse.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed class NotesState {
    object Loading : NotesState()
    data class Success(val notes: List<Note>, val favoriteNoteIds: Set<String>) : NotesState()
    data class Error(val message: String) : NotesState()
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService: ApiService = RetrofitInstance.api
    private val context = application.applicationContext

    private val _notesState = MutableStateFlow<NotesState>(NotesState.Loading)
    val notesState = _notesState.asStateFlow()

    fun fetchNotesAndFavorites(course_name: String, userId: String) {
        viewModelScope.launch {
            _notesState.value = NotesState.Loading
            try {
                val notes = apiService.getNotes(course_name)
                val favorites = apiService.getFavoriteNotes(userId)
                val favoriteNoteIds = favorites.map { it.noteId }.toSet()
                _notesState.value = NotesState.Success(notes, favoriteNoteIds)
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Failed to fetch notes or favorites", e)
                _notesState.value = NotesState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun toggleFavorite(note: Note, userId: String) {
        viewModelScope.launch {
            val currentState = _notesState.value
            if (currentState is NotesState.Success) {
                val isFavorite = currentState.favoriteNoteIds.contains(note.id)
                val newFavorites = if (isFavorite) {
                    currentState.favoriteNoteIds - note.id
                } else {
                    currentState.favoriteNoteIds + note.id
                }
                _notesState.value = currentState.copy(favoriteNoteIds = newFavorites)

                try {
                    if (isFavorite) {
                        apiService.unfavoriteNote(note.id)
                    } else {
                        apiService.favoriteNote(FavoriteRequest(user_id = userId, note_id = note.id))
                    }
                } catch (e: Exception) {
                    Log.e("NotesViewModel", "Failed to toggle favorite", e)
                    // Revert UI change on error
                    _notesState.value = currentState
                }
            }
        }
    }

    fun uploadNote(uri: Uri, course_name: String, userId: String, title: String) {
        viewModelScope.launch {
            val fileSize = getFileSize(uri)
            if (fileSize != null && fileSize > 3 * 1024 * 1024) { // 3MB limit
                _notesState.value = NotesState.Error("File exceeds 3MB limit.")
                return@launch
            }

            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val fileBytes = inputStream.readBytes()
                    val fileName = getFileName(uri) ?: "Unnamed File"

                    val fileBody = fileBytes.toRequestBody(context.contentResolver.getType(uri)?.toMediaTypeOrNull())
                    val file = MultipartBody.Part.createFormData("file", fileName, fileBody)
                    val titleRequest = title.toRequestBody("text/plain".toMediaTypeOrNull()) // Use the custom title
                    val user_id = userId.toRequestBody("text/plain".toMediaTypeOrNull())

                    apiService.uploadNote(course_name, file, titleRequest, user_id)
                    fetchNotesAndFavorites(course_name, userId)
                }
            } catch (e: Exception) {
                Log.e("NotesViewModel", "File upload failed", e)
                _notesState.value = NotesState.Error("File upload failed: ${e.message}")
            }
        }
    }

    fun saveFile(uri: Uri, fileUrl: String) {
        viewModelScope.launch {
            try {
                val response = apiService.downloadFile(fileUrl)
                if (response.isSuccessful) {
                    val bytes = response.body()?.bytes()
                    if (bytes != null) {
                        context.contentResolver.openOutputStream(uri)?.use {
                            it.write(bytes)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Failed to save file", e)
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                if (result != null) {
                    result = result.substring(cut!! + 1)
                }
            }
        }
        return result
    }

    private fun getFileSize(uri: Uri): Long? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use { c ->
            val sizeIndex = c.getColumnIndex(OpenableColumns.SIZE)
            c.moveToFirst()
            if (!c.isNull(sizeIndex)) c.getLong(sizeIndex) else null
        }
    }
}
