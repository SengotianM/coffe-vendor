package com.coffevendor.ui.auth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import com.coffevendor.ui.icons.AppIcons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffevendor.data.local.UserDao
import com.coffevendor.data.local.toDomain
import com.coffevendor.data.model.UserRole
import com.coffevendor.data.remote.SupabaseRepository
import com.coffevendor.util.BiometricHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LoginUiState {
    data object Idle : LoginUiState()
    data object Loading : LoginUiState()
    data class Success(val username: String, val userId: String, val role: UserRole = UserRole.CUSTOMER) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val userDao: UserDao,
    private val repository: SupabaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _shouldAutoBiometric = MutableStateFlow(false)
    val shouldAutoBiometric: StateFlow<Boolean> = _shouldAutoBiometric.asStateFlow()

    init {
        checkBiometricEnabled()
    }

    private fun checkBiometricEnabled() {
        viewModelScope.launch {
            val user = userDao.getLoggedInUser()
            if (user != null && user.isBiometricEnabled) {
                _shouldAutoBiometric.value = true
            }
        }
    }

    fun login(userId: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            if (userId.isBlank() || password.isBlank()) {
                _uiState.value = LoginUiState.Error("Enter User ID and Password")
                return@launch
            }

            val user = repository.login(userId, password)
            if (user != null) {
                repository.activateUserToken(userId)
                _uiState.value = LoginUiState.Success(user.userId, user.userId, user.role)
            } else {
                _uiState.value = LoginUiState.Error("Login failed")
            }
        }
    }

    fun biometricLogin(activity: FragmentActivity) {
        viewModelScope.launch {
            val biometricHelper = BiometricHelper(activity)

            if (!biometricHelper.isBiometricAvailable()) {
                _uiState.value = LoginUiState.Error("Biometric not available on this device")
                return@launch
            }

            biometricHelper.authenticate(
                title = "Login with Biometric",
                subtitle = "Verify your fingerprint to login",
                onSuccess = {
                    viewModelScope.launch {
                        val loggedInUser = userDao.getLoggedInUser()
                        if (loggedInUser != null) {
                            val domain = loggedInUser.toDomain()
                            _uiState.value = LoginUiState.Success(domain.username, domain.userId, domain.role)
                        } else {
                            _uiState.value = LoginUiState.Error("No saved user found. Please login with password first.")
                        }
                    }
                },
                onError = { error ->
                    _uiState.value = LoginUiState.Error(error)
                },
                onFailed = {
                    _uiState.value = LoginUiState.Error("Biometric authentication failed")
                }
            )
        }
    }

    fun onBiometricPromptShown() {
        _shouldAutoBiometric.value = false
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String, String, UserRole) -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val loginViewModel: LoginViewModel = hiltViewModel()
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val uiState by loginViewModel.uiState.collectAsState()
    val shouldAutoBiometric by loginViewModel.shouldAutoBiometric.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            val success = uiState as LoginUiState.Success
            onLoginSuccess(success.username, success.userId, success.role)
        }
    }

    LaunchedEffect(shouldAutoBiometric) {
        if (shouldAutoBiometric && activity != null) {
            loginViewModel.onBiometricPromptShown()
            loginViewModel.biometricLogin(activity)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Login to your account",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            if (uiState is LoginUiState.Error) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = (uiState as LoginUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { loginViewModel.login(userId, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = uiState !is LoginUiState.Loading
            ) {
                if (uiState is LoginUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Login", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (activity != null) {
                OutlinedButton(
                    onClick = { loginViewModel.biometricLogin(activity) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(AppIcons.Fingerprint, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Login with Biometric", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onSignUpClick) {
                Text("Don't have an account? Sign Up")
            }
        }
    }
}
