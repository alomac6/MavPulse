package com.example.mavpulse

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.mavpulse.viewmodels.NotesViewModel

@Composable
fun NoteViewerPage(
    modifier: Modifier = Modifier,
    fileUrl: String,
    title: String,
    onClose: () -> Unit,
    notesViewModel: NotesViewModel
) {
    val isImage = fileUrl.endsWith(".png", ignoreCase = true) ||
                  fileUrl.endsWith(".jpg", ignoreCase = true) ||
                  fileUrl.endsWith(".jpeg", ignoreCase = true)

    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*"),
        onResult = { uri: Uri? ->
            uri?.let { 
                notesViewModel.saveFile(it, fileUrl)
            }
        }
    )

    BackHandler(onBack = onClose)

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Custom styled controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center, // Center the button
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    onClick = { 
                        val extension = fileUrl.substringAfterLast('.', "")
                        val suggestedFilename = if (extension.isNotEmpty()) "$title.$extension" else title
                        saveFileLauncher.launch(suggestedFilename)
                    },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0064B1)),
                    border = BorderStroke(2.dp, Color(0xFFF58025))
                ) {
                    Icon(
                        Icons.Default.Download, 
                        contentDescription = "Download", 
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // Content viewer
            Box(modifier = Modifier.fillMaxSize()) {
                if (isImage) {
                    AsyncImage(
                        model = fileUrl,
                        contentDescription = title,
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        contentScale = ContentScale.FillWidth
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Preview not available for this file type.")
                        Text("Please use the download button.")
                    }
                }
            }
        }
    }
}
