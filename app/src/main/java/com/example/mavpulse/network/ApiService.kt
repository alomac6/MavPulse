package com.example.mavpulse.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.example.mavpulse.Department
import com.example.mavpulse.Course
import com.example.mavpulse.Note
import com.example.mavpulse.RoomChoice
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(
    @SerialName("accessToken") val token: String? = null,
    @SerialName("user_id") val userId: String? = null,
    val error: String? = null,
    val username: String? = null
)

@Serializable
data class RegisterRequest(val username: String, val email: String, val password: String)

@Serializable
data class RegisterResponse(val message: String)

@Serializable
data class CreateRoomRequest(
    val course_id: String,
    @SerialName("creator_id") val creator_id: String,
    val name: String,
    val role: String,
    val encrypted_room_key: String
)

@Serializable
data class CreateRoomMember(
    @SerialName("encrypted_room_key") val encryptedRoomKey: String,
    @SerialName("joined_at") val joinedAt: String,
    val role: String,
    @SerialName("room_id") val roomId: String,
    @SerialName("user_id") val userId: String
)

@Serializable
data class RoomInfo(
    @SerialName("course_id") val courseId: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("creator_id") val creatorId: String,
    val id: String,
    @SerialName("room_name") val roomName: String,
    val size: Int
)
@Serializable
data class CreateRoomResponse(
    val member: CreateRoomMember,
    val room: RoomInfo
)

interface ApiService {
    @POST("auth/signup")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("courses/")
    suspend fun getDepartments(): List<Department>

    @GET("courses/{department}")
    suspend fun getCourses(@Path("department") department: String): List<Course>

    @GET("courses/{course_name_backend}/files")
    suspend fun getNotes(@Path("course_name_backend") course_name: String): List<Note>

    @GET("rooms/{course_name_backend}")
    suspend fun getRooms(@Path("course_name_backend") course_name: String): List<RoomChoice>

    @POST("rooms/new_room")
    suspend fun createRoom(@Body request: CreateRoomRequest): CreateRoomResponse

    @Multipart
    @POST("courses/{course_name}/files")
    suspend fun uploadNote(
        @Path("course_name") course_name: String,
        @Part file: MultipartBody.Part,
        @Part("title") title: RequestBody,
        @Part("user_id") user_id: RequestBody
    ): List<Note>
}

object RetrofitInstance {
    var baseUrl = "https://mavpulsebackend.onrender.com/"
        private set

    fun setBaseUrl(url: String) {
        baseUrl = url
    }

    private val json = Json { ignoreUnknownKeys = true }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val api: ApiService
        get() = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
}
