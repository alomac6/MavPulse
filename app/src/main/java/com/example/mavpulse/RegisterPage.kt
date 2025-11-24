package com.example.mavpulse

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mavpulse.viewmodels.AuthViewModel
import com.example.mavpulse.viewmodels.AuthState

@Composable
fun RegisterPage(
    modifier: Modifier = Modifier, 
    onRegisterSuccess: () -> Unit, 
    onBackToLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()

    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    BackHandler(onBack = onBackToLogin)

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onRegisterSuccess()
            authViewModel.resetAuthState()
        }
    }

    fun validate(): Boolean {
        usernameError = if (username.trim().isBlank()) "Username is required." else null
        emailError = when {
            email.trim().isBlank() -> "Email is required."
            !email.trim().endsWith("@mavs.uta.edu") -> "A valid @mavs.uta.edu email is required."
            else -> null
        }
        passwordError = when {
            password.trim().length < 8 -> "Password must be at least 8 characters."
            !password.trim().any { it.isUpperCase() } -> "Password must contain an uppercase letter."
            !password.trim().any { it.isDigit() } -> "Password must contain a number."
            !password.trim().any { !it.isLetterOrDigit() } -> "Password must contain a special character."
            else -> null
        }
        confirmPasswordError = when {
            confirmPassword.isBlank() -> "Please confirm your password."
            password.trim() != confirmPassword.trim() -> "Passwords do not match."
            else -> null
        }

        return usernameError == null && emailError == null && passwordError == null && confirmPasswordError == null
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Register", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(32.dp))

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        if (authState is AuthState.Loading) {
            CircularProgressIndicator()
        } else {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it; usernameError = null },
                    label = { Text("Username*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = usernameError != null,
                    supportingText = { if (usernameError != null) Text(usernameError!!, color = MaterialTheme.colorScheme.error) },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { if (it.length <= 30) email = it; emailError = null },
                    label = { Text("UTA Email*") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = emailError != null,
                    supportingText = { if (emailError != null) Text(emailError!!, color = MaterialTheme.colorScheme.error) },
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = null },
                    label = { Text("Password*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = passwordError != null,
                    supportingText = { if (passwordError != null) Text(passwordError!!, color = MaterialTheme.colorScheme.error) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                        }
                    }
                )
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; confirmPasswordError = null },
                    label = { Text("Confirm Password*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = confirmPasswordError != null,
                    supportingText = { if (confirmPasswordError != null) Text(confirmPasswordError!!, color = MaterialTheme.colorScheme.error) },
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password")
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (validate()) {
                            authViewModel.register(username.trim(), email.trim(), password.trim())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Register")
                }
            }
        }
    }
}
