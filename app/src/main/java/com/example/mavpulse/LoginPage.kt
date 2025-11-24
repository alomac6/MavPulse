package com.example.mavpulse

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mavpulse.viewmodels.AuthViewModel
import com.example.mavpulse.viewmodels.AuthState
import com.example.mavpulse.security.CryptoManager

@Composable
fun LoginPage(
    modifier: Modifier = Modifier,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit, // Added for back gesture
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()

    BackHandler(onBack = onBackClick)

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            val userId = authViewModel.userId.value
            if (userId != null) {
                val cryptoManager = CryptoManager()
                val userKeyPair = cryptoManager.getOrCreateAsymmetricKeyPair("user_$userId")

                // for debugging log the public key
                val publicKeyString = userKeyPair.public.encoded.joinToString(",") { it.toString() }
                println("User public key created: $publicKeyString")
            }

            onLoginSuccess()
            authViewModel.resetAuthState()
        }
    }

    /*
    To actually use the keys

    val userKeyPair = cryptoManager.getOrCreateAsymmetricKeyPair("user_$userId")
    val publicKey = userKeyPair.public
    val privateKey = userKeyPair.private
     */

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.primary
        )
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
            Column(modifier = Modifier.fillMaxWidth(0.8f)) {
                OutlinedTextField(
                    value = email,
                    onValueChange = { if (it.length <= 30) email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { if (it.length <= 30) password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                        }
                    }
                )
                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { authViewModel.login(email.trim(), password.trim()) },
                        shape = RectangleShape
                    ) {
                        Text("Login")
                    }
                    Text("or")
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onRegisterClick,
                        shape = RectangleShape
                    ) {
                        Text("Register")
                    }
                }
            }
        }
    }
}
