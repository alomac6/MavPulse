package com.example.mavpulse

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mavpulse.viewmodels.RoomsState
import com.example.mavpulse.viewmodels.RoomsViewModel

@Composable
fun RoomsPage(
    modifier: Modifier = Modifier,
    course_name: String,
    course_id: String,
    creator_id: String, // Changed to creator_id
    username: String,
    onRoomClick: (RoomChoice) -> Unit,
    onBackClick: () -> Unit,
    roomsViewModel: RoomsViewModel = viewModel()
) {
    var showCreateRoomDialog by remember { mutableStateOf(false) }
    val roomsState by roomsViewModel.roomsState.collectAsState()

    if (showCreateRoomDialog) {
        CreateRoomDialog(
            onDismiss = { showCreateRoomDialog = false },
            onCreate = { room_name ->
                roomsViewModel.createRoom(course_id, creator_id, room_name, username)
                showCreateRoomDialog = false
            }
        )
    }

    // Handle the system back gesture
    BackHandler(onBack = onBackClick)

    LaunchedEffect(course_name) {
        if (course_name.isNotEmpty()) {
            roomsViewModel.fetchRooms(course_name)
        }
    }

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { showCreateRoomDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = "Add Room")
            Spacer(Modifier.height(8.dp))
            Text("Add Room")
        }
        
        Spacer(Modifier.height(16.dp))

        when (val state = roomsState) {
            is RoomsState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
            is RoomsState.Success -> {
                if (state.rooms.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No rooms found for this course. Be the first to create one!")
                    }
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(state.rooms) { room ->
                            RoomItem(room = room, onClick = { onRoomClick(room) })
                        }
                    }
                }
            }
            is RoomsState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { roomsViewModel.fetchRooms(course_name) }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
fun RoomItem(room: RoomChoice, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(room.name, style = MaterialTheme.typography.titleLarge)
            Text("Owner: ${room.owner}")
            Text("Members: ${room.members}")
        }
    }
}

@Composable
fun CreateRoomDialog(
    onDismiss: () -> Unit,
    onCreate: (room_name: String) -> Unit
) {
    var room_name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Create a New Room")
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
        },
        text = {
            OutlinedTextField(
                value = room_name,
                onValueChange = { room_name = it },
                label = { Text("Room Name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = { onCreate(room_name) },
                enabled = room_name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {}
    )
}
