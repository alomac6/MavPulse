package com.example.mavpulse.security

import android.util.Log
import com.example.mavpulse.JoinRequest
import com.example.mavpulse.network.SupabaseInstance
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.rpc
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class RealtimeManager {

    private val client = SupabaseInstance.client

    suspend fun getInitialJoinRequests(userId: String): Flow<List<JoinRequest>> = flow {
        val requests = client.from("join_requests").select {
            filter {
                eq("room_owner_id", userId)
                eq("status", "pending")
            }
        }.decodeList<JoinRequest>()
        emit(requests)
    }

    suspend fun listenForJoinRequests(userId: String): Flow<JoinRequest> {
        val channel = client.channel("join-requests-for-$userId")
        val changeFlow = channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
            table = "join_requests"
            filter = "room_owner_id=eq.$userId"
        }
        channel.subscribe()
        
        return flow {
            changeFlow.collect { action ->
                emit(action.record)
            }
        }.catch { e ->
            Log.e("RealtimeManager", "Error listening for join requests", e)
        }
    }

    suspend fun updateJoinRequestStatus(requestId: String, newStatus: String) {
        client.from("join_requests").update({
            set("status", newStatus)
        }) {
            filter {
                eq("id", requestId)
            }
        }
    }
    
    // You'll need a corresponding function in your database
    suspend fun addMemberToRoom(roomId: String, userId: String, encryptedRoomKey: String) {
        client.rpc("add_room_member", JsonObject(mapOf(
            "room_id" to Json.encodeToJsonElement(roomId),
            "user_id" to Json.encodeToJsonElement(userId),
            "encrypted_room_key" to Json.encodeToJsonElement(encryptedRoomKey)
        )))
    }
}
