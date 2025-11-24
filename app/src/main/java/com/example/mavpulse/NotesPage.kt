package com.example.mavpulse

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.mavpulse.viewmodels.NotesState
import com.example.mavpulse.viewmodels.NotesViewModel
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Note(
    @SerialName("note_id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("file_path") val filePath: String
)

@Composable
fun NotesPage(
    modifier: Modifier = Modifier,
    course_name: String,
    userId: String,
    onBackClick: () -> Unit,
    onNoteClick: (Note) -> Unit,
    notesViewModel: NotesViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val notesState by notesViewModel.notesState.collectAsState()
    val lazyGridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()
    var showUploadDialog by remember { mutableStateOf(false) }

    BackHandler(onBack = onBackClick)

    val showButton by remember {
        derivedStateOf {
            lazyGridState.firstVisibleItemIndex > 0
        }
    }

    if (showUploadDialog) {
        UploadNoteDialog(
            onDismiss = { showUploadDialog = false },
            onUpload = { title, uri ->
                notesViewModel.uploadNote(uri, course_name, userId, title)
                showUploadDialog = false
            }
        )
    }

    LaunchedEffect(course_name, userId) {
        if (course_name.isNotEmpty() && userId.isNotEmpty()) {
            notesViewModel.fetchNotesAndFavorites(course_name, userId)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { showUploadDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = "Add Note")
            Spacer(Modifier.height(8.dp))
            Text("Add Note")
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(0.95f),
            placeholder = { Text("Search Notes") },
            trailingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))

        when (val state = notesState) {
            is NotesState.Loading -> {
                CircularProgressIndicator()
            }
            is NotesState.Success -> {
                val filteredNotes = if (searchQuery.isBlank()) {
                    state.notes
                } else {
                    state.notes.filter {
                        it.title.contains(searchQuery, ignoreCase = true)
                    }
                }

                if (filteredNotes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No notes found for this course. Be the first to upload!")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        state = lazyGridState,
                        modifier = Modifier.fillMaxWidth(0.95f).weight(1f),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredNotes) { note ->
                            NoteItem(
                                note = note, 
                                isFavorite = state.favoriteNoteIds.contains(note.id),
                                onFavoriteClick = { notesViewModel.toggleFavorite(note, userId) },
                                onClick = { onNoteClick(note) }
                            )
                        }
                    }
                }
            }
            is NotesState.Error -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { notesViewModel.fetchNotesAndFavorites(course_name, userId) }) {
                        Text("Retry")
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)
        ) {
            Button(onClick = { 
                scope.launch {
                    lazyGridState.animateScrollToItem(0)
                }
            }) {
                Text("Go to Top")
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note, 
    isFavorite: Boolean, 
    onFavoriteClick: () -> Unit,
    onClick: () -> Unit
) {
    val isImage = note.filePath.endsWith(".png", ignoreCase = true) || 
                  note.filePath.endsWith(".jpg", ignoreCase = true) || 
                  note.filePath.endsWith(".jpeg", ignoreCase = true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                    model = note.filePath,
                    contentDescription = note.title,
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
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Yellow else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun UploadNoteDialog(
    onDismiss: () -> Unit,
    onUpload: (title: String, uri: Uri) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> selectedUri = uri }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Upload a New Note")
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { 
                    val mimeTypes = arrayOf("application/pdf", "image/jpeg", "image/png", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                    filePickerLauncher.launch(mimeTypes)
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Attach File")
                    Spacer(Modifier.size(8.dp))
                    Text(text = selectedUri?.lastPathSegment ?: "Attach File")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    selectedUri?.let { onUpload(title, it) } 
                },
                enabled = title.isNotBlank() && selectedUri != null
            ) {
                Text("Upload")
            }
        },
        dismissButton = {}
    )
}
