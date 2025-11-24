package com.example.mavpulse.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mavpulse.Favorite
import com.example.mavpulse.UserNote
import com.example.mavpulse.network.ApiService
import com.example.mavpulse.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileState {
    object Loading : ProfileState()
    data class Success(val favoriteNotes: List<Favorite>, val userNotes: List<UserNote>) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService: ApiService = RetrofitInstance.api

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState = _profileState.asStateFlow()

    fun fetchProfileData(userId: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val favorites = apiService.getFavoriteNotes(userId)
                val userNotes = apiService.getUserNotes(userId)
                _profileState.value = ProfileState.Success(favorites, userNotes)
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Failed to fetch profile data", e)
                _profileState.value = ProfileState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun unfavoriteNote(noteId: String, userId: String) {
        viewModelScope.launch {
            val currentState = _profileState.value
            if (currentState is ProfileState.Success) {
                val updatedFavorites = currentState.favoriteNotes.filter { it.noteId != noteId }
                _profileState.value = currentState.copy(favoriteNotes = updatedFavorites)

                try {
                    apiService.unfavoriteNote(noteId)
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Failed to unfavorite note", e)
                    // Revert UI change on error
                    fetchProfileData(userId)
                }
            }
        }
    }

    fun deleteUserNote(noteId: String, userId: String) {
        viewModelScope.launch {
            val currentState = _profileState.value
            if (currentState is ProfileState.Success) {
                val updatedUserNotes = currentState.userNotes.filter { it.noteId != noteId }
                _profileState.value = currentState.copy(userNotes = updatedUserNotes)

                try {
                    apiService.deleteNote(noteId)
                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Failed to delete note", e)
                    // Revert UI change on error
                    fetchProfileData(userId)
                }
            }
        }
    }
}
