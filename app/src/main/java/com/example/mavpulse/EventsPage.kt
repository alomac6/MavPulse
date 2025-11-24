package com.example.mavpulse

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun EventsPage(modifier: Modifier = Modifier, onBackClick: () -> Unit) {
    var selectedEventIndex by remember { mutableStateOf<Int?>(null) }
    var showAddEventForm by remember { mutableStateOf(false) }

    BackHandler(onBack = onBackClick)

    Box(modifier = modifier.fillMaxSize()) {
        val events = (1..4).toList()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                SuggestEventBox(
                    modifier = Modifier.height((LocalConfiguration.current.screenHeightDp * 0.2).dp),
                    onClick = { showAddEventForm = true }
                )
            }

            items(events.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { eventId ->
                        Box(modifier = Modifier.weight(1f)) {
                            EventItem(onClick = { selectedEventIndex = eventId })
                        }
                    }
                    if (rowItems.size < 2) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }

        AnimatedVisibility(visible = selectedEventIndex != null, enter = scaleIn() + fadeIn(), exit = scaleOut() + fadeOut()) {
            EventDetail(onClose = { selectedEventIndex = null })
        }

        AnimatedVisibility(visible = showAddEventForm, enter = fadeIn(), exit = fadeOut()) {
            AddEventForm(onClose = { showAddEventForm = false })
        }
    }
}

@Composable
fun SuggestEventBox(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "You may suggest your own event",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        IconButton(
            onClick = onClick,
            modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Suggest Event",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun AddEventForm(onClose: () -> Unit) {
    var eventName by remember { mutableStateOf("") }
    var month by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var hour by remember { mutableStateOf("") }
    var minute by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var finalErrorMessage by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    val (eventNameFocus, dateFocus, costFocus, emailFocus, descriptionFocus) = remember { FocusRequester.createRefs() }

    val eventNameError = if (eventName.length > 30) "Name cannot exceed 30 characters." else null
    val emailError = if (email.isNotBlank() && !email.endsWith("@mavs.uta.edu")) "Must be a @mavs.uta.edu email." else null
    val descriptionError = if (description.length > 150) "Description cannot exceed 150 characters." else null
    
    val monthInt = month.toIntOrNull()
    val dayInt = day.toIntOrNull()
    val hourInt = hour.toIntOrNull()
    val minuteInt = minute.toIntOrNull()

    val monthIsError = month.isNotBlank() && (monthInt == null || monthInt !in 1..12)
    val dayIsError = day.isNotBlank() && (dayInt == null || dayInt !in 1..31)
    val yearIsError = year.isNotBlank() && year.length != 4
    val hourIsError = hour.isNotBlank() && (hourInt == null || hourInt !in 0..23)
    val minuteIsError = minute.isNotBlank() && (minuteInt == null || minuteInt !in 0..59)

    // Real-time validation for date and time fields
    LaunchedEffect(month, day, year, hour, minute) {
        dateError = when {
            monthIsError -> "Month must be 1-12"
            dayIsError -> "Day must be 1-31"
            yearIsError -> "Year must be 4 digits"
            hourIsError -> "Hour must be 0-23"
            minuteIsError -> "Minute must be 0-59"
            else -> null
        }
    }
    LaunchedEffect(dateError) {
        if (dateError != null) {
            delay(2000)
            dateError = null
        }
    }

    fun validate(): Boolean {
        finalErrorMessage = when {
            eventName.isBlank() -> "Event name is required."
            month.isBlank() || day.isBlank() || year.isBlank() -> "Full date is required."
            hour.isBlank() || minute.isBlank() -> "Full time is required."
            cost.isBlank() -> "Entry cost is required."
            email.isBlank() -> "Contact email is required."
            description.isBlank() -> "Event description is required."
            eventNameError != null -> eventNameError
            dateError != null -> dateError
            emailError != null -> emailError
            descriptionError != null -> descriptionError
            else -> null
        }

        if (finalErrorMessage != null) {
            when {
                eventName.isBlank() || eventNameError != null -> eventNameFocus.requestFocus()
                month.isBlank() || day.isBlank() || year.isBlank() || dateError != null -> dateFocus.requestFocus()
                cost.isBlank() -> costFocus.requestFocus()
                email.isBlank() || emailError != null -> emailFocus.requestFocus()
                description.isBlank() || descriptionError != null -> descriptionFocus.requestFocus()
            }
            return false
        }
        return true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close Form")
                }
            }

            Text("Please write name of the event*", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = eventName,
                onValueChange = { if (it.length <= 30) eventName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(eventNameFocus),
                isError = eventNameError != null,
                supportingText = { 
                    Text(
                        text = eventNameError ?: "${eventName.length} / 30", 
                        color = if (eventNameError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                }
            )
            Spacer(Modifier.height(16.dp))

            Text("Full date and time*", style = MaterialTheme.typography.titleMedium)
            Text(
                text = dateError ?: "",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp).height(24.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.focusRequester(dateFocus)) {
                OutlinedTextField(value = month, onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) month = it }, label = { Text("MM") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = monthIsError)
                OutlinedTextField(value = day, onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) day = it }, label = { Text("DD") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = dayIsError)
                OutlinedTextField(value = year, onValueChange = { if (it.length <= 4 && it.all(Char::isDigit)) year = it }, label = { Text("YYYY") }, modifier = Modifier.weight(1.5f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = yearIsError)
                Spacer(Modifier.weight(0.2f))
                OutlinedTextField(value = hour, onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) hour = it }, label = { Text("HH") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = hourIsError)
                Text(":", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(value = minute, onValueChange = { if (it.length <= 2 && it.all(Char::isDigit)) minute = it }, label = { Text("MM") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), isError = minuteIsError)
            }
            Spacer(Modifier.height(16.dp))

            Text("Entry cost*", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(value = cost, onValueChange = { if (it.all(Char::isDigit)) cost = it }, modifier = Modifier.fillMaxWidth().focusRequester(costFocus), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Spacer(Modifier.height(16.dp))

            Text("Contact email*", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = email, 
                onValueChange = { email = it }, 
                modifier = Modifier.fillMaxWidth().focusRequester(emailFocus), 
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), 
                isError = emailError != null, 
                supportingText = { 
                    if (emailError != null) {
                         Text(text = emailError, color = MaterialTheme.colorScheme.error) 
                    }
                }
            )
            Spacer(Modifier.height(16.dp))

            Text("Event description*", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = description, 
                onValueChange = { if (it.length <= 150) description = it }, 
                modifier = Modifier.fillMaxWidth().height(120.dp).focusRequester(descriptionFocus), 
                isError = descriptionError != null, 
                supportingText = { 
                     Text(
                        text = descriptionError ?: "${description.length} / 150", 
                        color = if (descriptionError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                }
            )
            Spacer(Modifier.height(16.dp))

            Text("Event related image (optional)", style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { /* TODO: Handle image picking */ },
                    modifier = Modifier
                        .size(56.dp)
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Image",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(visible = finalErrorMessage != null) {
                Text(
                    text = finalErrorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { if (validate()) { onClose() } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }
        }
    }
}

@Composable
fun EventItem(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // Make the whole card clickable
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Name and Date
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = "Sample Event",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "October 28, 2025 at 7:00 PM",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Divider(modifier = Modifier.padding(horizontal = 8.dp))

            // Image Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )

            Divider(modifier = Modifier.padding(horizontal = 8.dp))

            // Description
            Text(
                "This is a sample event description. It provides more details about what the event is about.",
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun EventDetail(onClose: () -> Unit) {
    // A semi-transparent background that consumes clicks
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { /* consume clicks */ },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // The content of the detail view (similar to EventItem)
                Column {
                    // Name and Date
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sample Event",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "October 28, 2025 at 7:00 PM",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Image Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f) // A more rectangular aspect ratio for detail view
                            .padding(16.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    )

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Description
                    Text(
                        "This is a sample event description. It provides more details about what the event is about. This text can be longer to show more information when the event card is expanded.",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                // Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }
        }
    }
}
