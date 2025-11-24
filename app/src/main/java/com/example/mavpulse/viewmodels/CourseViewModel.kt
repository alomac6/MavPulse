package com.example.mavpulse.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mavpulse.Course
import com.example.mavpulse.network.ApiService
import com.example.mavpulse.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CourseState {
    object Loading : CourseState()
    data class Success(val courses: List<Course>) : CourseState()
    data class Error(val message: String) : CourseState()
}

class CourseViewModel : ViewModel() {

    private val apiService: ApiService = RetrofitInstance.api

    private val _courseState = MutableStateFlow<CourseState>(CourseState.Loading)
    val courseState = _courseState.asStateFlow()

    fun fetchCourses(department: String) {
        viewModelScope.launch {
            _courseState.value = CourseState.Loading
            try {
                val courses = apiService.getCourses(department)
                _courseState.value = CourseState.Success(courses)
            } catch (e: Exception) {
                _courseState.value = CourseState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}
