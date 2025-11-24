package com.example.mavpulse.viewmodels

import android.app.Application
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mavpulse.JoinRequest
import com.example.mavpulse.security.CryptoManager
import com.example.mavpulse.security.RealtimeManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val realtimeManager = RealtimeManager()
    private val cryptoManager = CryptoManager()

    private val _joinRequests = MutableStateFlow<List<JoinRequest>>(emptyList())
    val joinRequests: StateFlow<List<JoinRequest>> = _joinRequests.asStateFlow()

    fun startListeningForRequests(userId: String) {
        viewModelScope.launch {
            // Fetch initial requests
            realtimeManager.getInitialJoinRequests(userId)
                .catch { e -> Log.e("NotificationViewModel", "Error fetching initial requests", e) }
                .collect { initialRequests ->
                    _joinRequests.value = initialRequests
                }

            // Listen for new requests
            realtimeManager.listenForJoinRequests(userId)
                .catch { e -> Log.e("NotificationViewModel", "Error listening for new requests", e) }
                .collect { newRequest ->
                    _joinRequests.value = _joinRequests.value + newRequest
                }
        }
    }

    fun acceptJoinRequest(request: JoinRequest, ownerId: String) {
        viewModelScope.launch {
            try {
                // 1. Get the encrypted room key for the owner (This needs to be fetched from your database)
                // For now, we will assume you have a way to get this key.
                // This is a placeholder and will need to be replaced with your actual logic.
                val encryptedRoomKeyForOwner: ByteArray = byteArrayOf() // Placeholder

                // 2. Decrypt the room key with the owner's private key
                val roomKeyBytes = cryptoManager.decryptWithPrivateKey(ownerId, encryptedRoomKeyForOwner)

                // 3. Re-encrypt the room key with the requester's public key
                // The public key needs to be decoded from Base64 if it is stored as a string
                val requesterPublicKey = cryptoManager.getPublicKeyFromString(request.requesterPublicKey) // You'll need to implement this
                val encryptedRoomKeyForRequester = cryptoManager.encryptWithPublicKey(roomKeyBytes, requesterPublicKey)
                val encryptedRoomKeyString = Base64.encodeToString(encryptedRoomKeyForRequester, Base64.DEFAULT)

                // 4. Add the new member to the room with their encrypted key
                realtimeManager.addMemberToRoom(request.roomId, request.requesterId, encryptedRoomKeyString)

                // 5. Update the request status to 'accepted'
                realtimeManager.updateJoinRequestStatus(request.id, "accepted")

                // 6. Remove the request from the UI
                _joinRequests.value = _joinRequests.value.filter { it.id != request.id }

            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error accepting join request", e)
            }
        }
    }

    fun denyJoinRequest(requestId: String) {
        viewModelScope.launch {
            try {
                realtimeManager.updateJoinRequestStatus(requestId, "denied")
                _joinRequests.value = _joinRequests.value.filter { it.id != requestId }
            } catch (e: Exception) {
                Log.e("NotificationViewModel", "Error denying join request", e)
            }
        }
    }
}
