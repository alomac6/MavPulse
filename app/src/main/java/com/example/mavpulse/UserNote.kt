package com.example.mavpulse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserNote(
    @SerialName("bucket_path") val bucketPath: String,
    @SerialName("course_name") val courseName: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("file_path") val filePath: String,
    @SerialName("is_public") val isPublic: Boolean,
    @SerialName("note_id") val noteId: String,
    @SerialName("room_id") val roomId: String? = null,
    @SerialName("title") val title: String,
    @SerialName("user_id") val userId: String
)
