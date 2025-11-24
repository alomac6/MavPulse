package com.example.mavpulse.viewmodels

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mavpulse.RoomChoice
import com.example.mavpulse.network.ApiService
import com.example.mavpulse.network.CreateRoomRequest
import com.example.mavpulse.network.RetrofitInstance
import com.example.mavpulse.security.CryptoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RoomsState {
    object Loading : RoomsState()
    data class Success(val rooms: List<RoomChoice>) : RoomsState()
    data class Error(val message: String) : RoomsState()
}

class RoomsViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService: ApiService = RetrofitInstance.api
    private val cryptoManager = CryptoManager()

    private val _roomsState = MutableStateFlow<RoomsState>(RoomsState.Loading)
    val roomsState = _roomsState.asStateFlow()

    fun fetchRooms(course_name: String) {
        viewModelScope.launch {
            _roomsState.value = RoomsState.Loading
            try {
                val rooms = apiService.getRooms(course_name)
                _roomsState.value = RoomsState.Success(rooms)
            } catch (e: Exception) {
                Log.e("RoomsViewModel", "Failed to fetch rooms", e)
                _roomsState.value = RoomsState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun createRoom(course_id: String, creator_id: String, room_name: String, username: String) {
        viewModelScope.launch {
            try {
                // 1. Get or create user's keypair
                val userKeyPair = cryptoManager.getOrCreateAsymmetricKeyPair(creator_id)

                // 2. Generate a new symmetric key for the room
                val roomKey = cryptoManager.generateSymmetricKey()

                // 3. Encrypt the room key with the user's public key
                val encryptedRoomKey = cryptoManager.encryptWithPublicKey(roomKey.encoded, userKeyPair.public)
                val encryptedRoomKeyString = Base64.encodeToString(encryptedRoomKey, Base64.DEFAULT)

                val request = CreateRoomRequest(
                    course_id = course_id, 
                    creator_id = creator_id, 
                    name = room_name,
                    role = "owner",
                    encrypted_room_key = encryptedRoomKeyString
                )
                val response = apiService.createRoom(request)

                val newRoom = RoomChoice(
                    members = response.size,
                    owner = username, // Use the provided username
                    id = response.id,
                    name = response.roomName
                )

                val currentState = _roomsState.value
                if (currentState is RoomsState.Success) {
                    _roomsState.value = RoomsState.Success(currentState.rooms + newRoom)
                }

            } catch (e: Exception) {
                Log.e("RoomsViewModel", "Failed to create room", e)
                _roomsState.value = RoomsState.Error("Failed to create room: ${e.message}")
            }
        }
    }
}
