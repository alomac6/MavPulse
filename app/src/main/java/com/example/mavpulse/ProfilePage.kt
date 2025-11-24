package com.example.mavpulse

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mavpulse.viewmodels.ProfileState
import com.example.mavpulse.viewmodels.ProfileViewModel

@Composable
fun ProfilePage(
    modifier: Modifier = Modifier,
    userId: String,
    username: String,
    onNoteClick: (note: Any) -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val profileState by profileViewModel.profileState.collectAsState()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            profileViewModel.fetchProfileData(userId)
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(screenHeight * 0.15f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = username.firstOrNull()?.uppercase() ?: "",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(username, style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(32.dp))

        when (val state = profileState) {
            is ProfileState.Loading -> CircularProgressIndicator()
            is ProfileState.Success -> {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    item { 
                        ExpandableSection(title = "Favorite Notes") {
                            if (state.favoriteNotes.isEmpty()) {
                                Text("No favorite notes yet.", modifier = Modifier.padding(16.dp))
                            } else {
                                state.favoriteNotes.forEach { favorite ->
                                    ProfileNoteItem(
                                        note = favorite.noteDetails,
                                        onStarClick = { profileViewModel.unfavoriteNote(favorite.noteId, userId) },
                                        onClick = { onNoteClick(favorite.noteDetails) }
                                    )
                                }
                            }
                        }
                    }
                    item { 
                        ExpandableSection(title = "My Notes") {
                            if (state.userNotes.isEmpty()) {
                                Text("You haven\'t uploaded any notes.", modifier = Modifier.padding(16.dp))
                            } else {
                                state.userNotes.forEach { note ->
                                    ProfileNoteItem(
                                        note = note,
                                        onDeleteClick = { profileViewModel.deleteUserNote(note.noteId, userId) },
                                        onClick = { onNoteClick(note) }
                                    )
                                }
                            }
                        }
                    }
                    item { 
                        ExpandableSection(title = "My Rooms") {
                            // Placeholder for rooms
                            Text("Rooms section coming soon.", modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
            is ProfileState.Error -> {
                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                    Button(onClick = { profileViewModel.fetchProfileData(userId) }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
fun ExpandableSection(title: String, content: @Composable () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }
        if (isExpanded) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun ProfileNoteItem(
    note: Any, 
    onStarClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val (title, filePath) = when (note) {
        is FavoriteNoteDetail -> note.title to note.filePath
        is UserNote -> note.title to note.filePath
        else -> "" to ""
    }

    val isImage = filePath.endsWith(".png", ignoreCase = true) || 
                  filePath.endsWith(".jpg", ignoreCase = true) || 
                  filePath.endsWith(".jpeg", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isImage) {
                AsyncImage(
                    model = filePath,
                    contentDescription = title,
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = "Document Icon",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                if (onStarClick != null) {
                     IconButton(onClick = onStarClick) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Unfavorite",
                            tint = Color.Yellow
                        )
                    }
                }
                if (onDeleteClick != null) {
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Close, contentDescription = "Delete Note", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
