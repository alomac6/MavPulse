package com.example.mavpulse.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mavpulse.Department
import com.example.mavpulse.network.ApiService
import com.example.mavpulse.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DepartmentState {
    object Loading : DepartmentState()
    data class Success(val departments: List<Department>) : DepartmentState()
    data class Error(val message: String) : DepartmentState()
}

class DepartmentViewModel : ViewModel() {

    private val apiService: ApiService = RetrofitInstance.api

    private val _departmentState = MutableStateFlow<DepartmentState>(DepartmentState.Loading)
    val departmentState = _departmentState.asStateFlow()

    init {
        fetchDepartments()
    }

    fun retry() {
        fetchDepartments()
    }

    private fun fetchDepartments() {
        viewModelScope.launch {
            _departmentState.value = DepartmentState.Loading
            try {
                val departments = apiService.getDepartments()
                _departmentState.value = DepartmentState.Success(departments)
            } catch (e: Exception) {
                _departmentState.value = DepartmentState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }
}
