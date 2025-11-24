package com.example.mavpulse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JoinRequest(
    @SerialName("id") val id: String,
    @SerialName("room_id") val roomId: String,
    @SerialName("requester_id") val requesterId: String,
    @SerialName("requester_public_key") val requesterPublicKey: String,
    @SerialName("room_owner_id") val roomOwnerId: String,
    @SerialName("status") val status: String = "pending",
    // You may need to join tables to get this, but adding for UI purposes
    @SerialName("requester_username") val requesterUsername: String? = null,
    @SerialName("room_name") val roomName: String? = null
)
