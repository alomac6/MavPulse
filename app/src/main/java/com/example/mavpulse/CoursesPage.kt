package com.example.mavpulse

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mavpulse.viewmodels.CourseState
import com.example.mavpulse.viewmodels.CourseViewModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Course(
    @SerialName("course_id") val course_id: String,
    @SerialName("course_code") val number: String,
    @SerialName("course_name") val name: String,
    @SerialName("course_name_backend") val backendName: String
)

@Composable
fun CoursesPage(
    modifier: Modifier = Modifier, 
    departmentName: String, 
    onCourseClick: (Course) -> Unit,
    onBackClick: () -> Unit,
    courseViewModel: CourseViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val courseState by courseViewModel.courseState.collectAsState()

    BackHandler(onBack = onBackClick)

    LaunchedEffect(departmentName) {
        courseViewModel.fetchCourses(departmentName.trim())
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "$departmentName Courses",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(0.95f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search...") },
                trailingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true
            )
        }

        when (val state = courseState) {
            is CourseState.Loading -> {
                Spacer(Modifier.height(16.dp))
                CircularProgressIndicator()
            }
            is CourseState.Success -> {
                val filteredCourses = if (searchQuery.isBlank()) {
                    state.courses
                } else {
                    state.courses.filter { 
                        it.name.contains(searchQuery, ignoreCase = true) || 
                        it.number.contains(searchQuery, ignoreCase = true) 
                    }
                }

                 if (filteredCourses.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No courses found for this department.")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier.fillMaxWidth(0.95f).weight(1f),
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredCourses) { course ->
                            CourseItem(course = course, onClick = { onCourseClick(course) })
                        }
                    }
                }
            }
            is CourseState.Error -> {
                Spacer(Modifier.height(8.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth(0.95f)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { courseViewModel.fetchCourses(departmentName.trim()) }) {
                        Text("Retry")
                    }
                }
            }
        }
    }
}

@Composable
fun CourseItem(course: Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = course.number,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = course.name,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
