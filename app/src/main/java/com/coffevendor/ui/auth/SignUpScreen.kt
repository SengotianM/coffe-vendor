package com.coffevendor.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffevendor.data.local.UserDao
import com.coffevendor.data.local.toEntity
import com.coffevendor.data.model.User
import com.coffevendor.data.remote.SupabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SignUpUiState {
    data object Idle : SignUpUiState()
    data object OtpSent : SignUpUiState()
    data object OtpVerified : SignUpUiState()
    data object SignUpSuccess : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userDao: UserDao,
    private val repository: SupabaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun requestOtp(mobileNumber: String) {
        viewModelScope.launch {
            if (mobileNumber.length != 10) {
                _uiState.value = SignUpUiState.Error("Enter valid 10-digit mobile number")
                return@launch
            }
            _uiState.value = SignUpUiState.OtpSent
        }
    }

    fun verifyOtp(otp: String) {
        viewModelScope.launch {
            if (otp == "123456") {
                _uiState.value = SignUpUiState.OtpVerified
            } else {
                _uiState.value = SignUpUiState.Error("Invalid OTP. Use 123456 for demo.")
            }
        }
    }

    fun signUp(
        userId: String,
        username: String,
        empId: String,
        seatNumber: String,
        mobileNumber: String,
        password: String
    ) {
        viewModelScope.launch {
            android.util.Log.d("SignUpVM", "signUp called: userId=$userId")
            if (userId.isBlank() || username.isBlank() || empId.isBlank() ||
                seatNumber.isBlank() || mobileNumber.isBlank() || password.isBlank()) {
                android.util.Log.d("SignUpVM", "Validation failed: blank fields")
                _uiState.value = SignUpUiState.Error("All fields are required")
                return@launch
            }

            if (password.length < 6) {
                android.util.Log.d("SignUpVM", "Validation failed: short password")
                _uiState.value = SignUpUiState.Error("Password must be at least 6 characters")
                return@launch
            }

            val existingUser = userDao.getUserByUserId(userId)
            android.util.Log.d("SignUpVM", "existingUser check: ${existingUser != null}")
            if (existingUser != null) {
                _uiState.value = SignUpUiState.Error("User ID already exists")
                return@launch
            }

            val user = User(
                id = System.currentTimeMillis().toString(),
                userId = userId,
                username = username,
                empId = empId,
                seatNumber = seatNumber,
                mobileNumber = mobileNumber,
                password = password
            )

            android.util.Log.d("SignUpVM", "Calling repository.signUp...")
            val success = repository.signUp(
                userId = user.userId,
                username = user.username,
                empId = user.empId,
                seatNumber = user.seatNumber,
                mobileNumber = user.mobileNumber,
                password = user.password
            )
            android.util.Log.d("SignUpVM", "repository.signUp result: $success")
            if (success) {
                _uiState.value = SignUpUiState.SignUpSuccess
            } else {
                _uiState.value = SignUpUiState.Error("Sign up failed - check connection")
            }
        }
    }

    fun resetState() {
        _uiState.value = SignUpUiState.Idle
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpComplete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val viewModel: SignUpViewModel = hiltViewModel()
    val context = androidx.compose.ui.platform.LocalContext.current
    var userId by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var empId by remember { mutableStateOf("") }
    var seatNumber by remember { mutableStateOf("") }
    var mobileNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is SignUpUiState.SignUpSuccess -> {
                android.widget.Toast.makeText(context, "Registration successful!", android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetState()
                onSignUpComplete()
            }
            is SignUpUiState.Error -> {
                android.widget.Toast.makeText(context, (uiState as SignUpUiState.Error).message, android.widget.Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign Up") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            if (uiState is SignUpUiState.Error) {
                Text(
                    text = (uiState as SignUpUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("User ID") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = empId,
                onValueChange = { empId = it },
                label = { Text("Employee ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = seatNumber,
                onValueChange = { seatNumber = it },
                label = { Text("Seat Number") },
                placeholder = { Text("e.g., A-12") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { if (it.length <= 10) mobileNumber = it },
                label = { Text("Mobile Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.signUp(userId, username, empId, seatNumber, mobileNumber, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = userId.isNotBlank() && username.isNotBlank() &&
                        empId.isNotBlank() && seatNumber.isNotBlank() &&
                        mobileNumber.length == 10 && password.length >= 6
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Up", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
