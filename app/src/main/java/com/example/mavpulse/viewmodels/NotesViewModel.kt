package com.example.mavpulse.viewmodels

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mavpulse.Note
import com.example.mavpulse.network.ApiService
import com.example.mavpulse.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed class NotesState {
    object Loading : NotesState()
    data class Success(val notes: List<Note>) : NotesState()
    data class Error(val message: String) : NotesState()
}

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService: ApiService = RetrofitInstance.api
    private val context = application.applicationContext

    private val _notesState = MutableStateFlow<NotesState>(NotesState.Loading)
    val notesState = _notesState.asStateFlow()

    fun fetchNotes(course_name: String) {
        viewModelScope.launch {
            _notesState.value = NotesState.Loading
            try {
                val notes = apiService.getNotes(course_name)
                _notesState.value = NotesState.Success(notes)
            } catch (e: Exception) {
                Log.e("NotesViewModel", "Failed to fetch notes", e)
                _notesState.value = NotesState.Error(e.message ?: "An unknown error occurred")
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

                    // The server returns a list with the new note, so we take the first item
                    val uploadedNotes = apiService.uploadNote(course_name, file, titleRequest, user_id)
                    val newNote = uploadedNotes.firstOrNull()

                    if (newNote != null) {
                        val currentState = _notesState.value
                        if (currentState is NotesState.Success) {
                            _notesState.value = NotesState.Success(currentState.notes + newNote)
                        } else {
                            fetchNotes(course_name)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("NotesViewModel", "File upload failed", e)
                _notesState.value = NotesState.Error("File upload failed: ${e.message}")
            }
        }
    }

    fun downloadNote(fileUrl: String, title: String) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val request = DownloadManager.Request(Uri.parse(fileUrl))
                .setTitle(title)
                .setDescription("Downloading Note")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title)
            
            downloadManager.enqueue(request)
        } catch (e: Exception) {
            Log.e("NotesViewModel", "Failed to download note", e)
            // Optionally, update state to show a download failed message
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
