package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.FriendChatRepository
import com.example.ui.components.BrandGradient
import com.example.ui.theme.AccentPink
import com.example.ui.theme.PrimaryIndigo
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repository: FriendChatRepository,
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    var email by remember { mutableStateOf("alex@example.com") }
    var password by remember { mutableStateOf("password123") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // App Brand Logo Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(BrandGradient),
                contentAlignment = Alignment.Center
            ) {
                Text("FC", color = Color.White, fontWeight = FontWeight.Black, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Friend Chat",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineMedium.copy(brush = BrandGradient)
            )

            Text(
                text = "Connect, share, and hang out with friends",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = AccentPink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryIndigo
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryIndigo
                )
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text("Forgot Password?", fontSize = 13.sp, color = PrimaryIndigo, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Please fill in all fields."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        val result = repository.login(email.trim(), password)
                        isLoading = false
                        if (result.isSuccess) {
                            onLoginSuccess()
                        } else {
                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Login failed"
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("login_submit_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                } else {
                    Text("Log In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account?", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(
                    onClick = onNavigateToSignup,
                    modifier = Modifier.testTag("navigate_signup_button")
                ) {
                    Text("Sign Up", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                }
            }
        }
    }
}

@Composable
fun SignupScreen(
    repository: FriendChatRepository,
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateToLogin) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Account", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = AccentPink,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; errorMessage = null },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("signup_fullname_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; errorMessage = null },
                label = { Text("Username") },
                prefix = { Text("@") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().testTag("signup_username_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().testTag("signup_email_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password (min 6 characters)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().testTag("signup_password_input")
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (fullName.isBlank() || username.isBlank() || email.isBlank() || password.length < 6) {
                        errorMessage = "Please enter valid details (password min 6 chars)."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        val result = repository.signup(fullName.trim(), username.trim(), email.trim(), password)
                        isLoading = false
                        if (result.isSuccess) {
                            onSignupSuccess()
                        } else {
                            errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Signup failed"
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("signup_submit_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                } else {
                    Text("Join Friend Chat", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account?", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Log In", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryIndigo)
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    repository: FriendChatRepository,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isSubmitted by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackToLogin) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset Password", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (isSubmitted) {
                Text(
                    text = "A password reset link has been dispatched to $email. Please check your inbox and spam folder.",
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onBackToLogin,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Return to Login", fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    text = "Enter your registered email address and we'll send you instructions to reset your password.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("forgot_password_email_input")
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        if (email.isNotBlank()) {
                            isLoading = true
                            scope.launch {
                                repository.forgotPassword(email.trim())
                                isLoading = false
                                isSubmitted = true
                            }
                        }
                    },
                    enabled = !isLoading && email.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    modifier = Modifier.fillMaxWidth().height(52.dp).testTag("forgot_password_submit_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Send Reset Link", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
