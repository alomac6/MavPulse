package com.example.mavpulse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomChoice(
    val members: Int,
    val owner: String,
    @SerialName("room_id") val id: String,
    @SerialName("room_name") val name: String
)
