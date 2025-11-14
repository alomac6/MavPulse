package com.example.mavpulse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class Course(val number: String, val name: String)

// Placeholder function to simulate fetching courses for a department
fun getCoursesForDepartment(departmentName: String): List<Course> {
    // In a real app, you would make a network request here based on the departmentName
    return getSampleCourses().filter { it.number.startsWith(departmentName) }
}

fun getSampleCourses(): List<Course> {
    return listOf(
        Course("CSE 1310", "Introduction to Programming"),
        Course("CSE 1320", "Intermediate Programming"),
        Course("CSE 2312", "Computer Organization and Assembly"),
        Course("CSE 3310", "Software Engineering"),
        Course("CSE 3311", "Object-Oriented Software Engineering"),
        Course("CSE 3320", "Operating Systems"),
        Course("CSE 4308", "Artificial Intelligence"),
        Course("MATH 1426", "Calculus I"),
        Course("MATH 2425", "Calculus II"),
        Course("PHYS 1443", "General Technical Physics I"),
        Course("PHYS 1444", "General Technical Physics II"),
        Course("IE 3301", "Engineering Probability and Statistics"),
        Course("EE 2340", "Electrical Circuits I"),
        Course("ENGL 1301", "Rhetoric and Composition I"),
        Course("HIST 1311", "History of the United States to 1865"),
        Course("ART 1301", "Art History I")
    )
}

@Composable
fun CoursesPage(modifier: Modifier = Modifier, departmentName: String) {
    var searchQuery by remember { mutableStateOf("") }
    var courses by remember { mutableStateOf<List<Course>>(emptyList()) }

    // Fetch courses when the departmentName changes
    LaunchedEffect(departmentName) {
        // Simulate a network call to get courses for the selected department
        courses = getCoursesForDepartment(departmentName)
    }

    val filteredCourses = if (searchQuery.isBlank()) {
        courses
    } else {
        courses.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.number.contains(searchQuery, ignoreCase = true) 
        }
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
        
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(0.95f),
            placeholder = { Text("Search in $departmentName") },
            trailingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            singleLine = true
        )
        Spacer(Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier.fillMaxWidth(0.95f),
            contentPadding = PaddingValues(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredCourses) { course ->
                CourseItem(course = course)
            }
        }
    }
}

@Composable
fun CourseItem(course: Course) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp), // Set a fixed height
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
