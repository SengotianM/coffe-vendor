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
    data object SignUpSuccess : SignUpUiState()
    data class Error(val message: String) : SignUpUiState()
}

data class FieldErrors(
    val userId: String = "",
    val username: String = "",
    val empId: String = "",
    val seatNumber: String = "",
    val mobileNumber: String = "",
    val password: String = "",
    val confirmPassword: String = ""
) {
    fun hasAny(): Boolean = userId.isNotEmpty() || username.isNotEmpty() || empId.isNotEmpty() ||
            seatNumber.isNotEmpty() || mobileNumber.isNotEmpty() ||
            password.isNotEmpty() || confirmPassword.isNotEmpty()
}

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userDao: UserDao,
    private val repository: SupabaseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<SignUpUiState>(SignUpUiState.Idle)
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    private val _fieldErrors = MutableStateFlow(FieldErrors())
    val fieldErrors: StateFlow<FieldErrors> = _fieldErrors.asStateFlow()

    fun validate(
        userId: String,
        username: String,
        empId: String,
        seatNumber: String,
        mobileNumber: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        val errors = FieldErrors(
            userId = when {
                userId.isBlank() -> "User ID is required"
                userId.length < 3 -> "User ID must be at least 3 characters"
                !userId.matches(Regex("^[a-zA-Z0-9_]+$")) -> "User ID can only contain letters, numbers, underscore"
                else -> ""
            },
            username = when {
                username.isBlank() -> "Full name is required"
                username.length < 2 -> "Name must be at least 2 characters"
                else -> ""
            },
            empId = when {
                empId.isBlank() -> "Employee ID is required"
                else -> ""
            },
            seatNumber = when {
                seatNumber.isBlank() -> "Seat number is required"
                else -> ""
            },
            mobileNumber = when {
                mobileNumber.isBlank() -> "Mobile number is required"
                mobileNumber.length != 10 -> "Enter valid 10-digit mobile number"
                !mobileNumber.all { it.isDigit() } -> "Mobile number can only contain digits"
                else -> ""
            },
            password = when {
                password.isBlank() -> "Password is required"
                password.length < 6 -> "Password must be at least 6 characters"
                !password.any { it.isUpperCase() } -> "Password must contain at least 1 uppercase letter"
                !password.any { it.isDigit() } -> "Password must contain at least 1 digit"
                else -> ""
            },
            confirmPassword = when {
                confirmPassword.isBlank() -> "Confirm your password"
                confirmPassword != password -> "Passwords do not match"
                else -> ""
            }
        )
        _fieldErrors.value = errors
        return !errors.hasAny()
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
            _uiState.value = SignUpUiState.Idle

            val existingUser = userDao.getUserByUserId(userId)
            if (existingUser != null) {
                _fieldErrors.value = _fieldErrors.value.copy(userId = "User ID already exists")
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

            val success = repository.signUp(
                userId = user.userId,
                username = user.username,
                empId = user.empId,
                seatNumber = user.seatNumber,
                mobileNumber = user.mobileNumber,
                password = user.password
            )
            if (success) {
                _uiState.value = SignUpUiState.SignUpSuccess
            } else {
                _uiState.value = SignUpUiState.Error("Sign up failed - check connection")
            }
        }
    }

    fun clearFieldError(field: String) {
        val current = _fieldErrors.value
        _fieldErrors.value = when (field) {
            "userId" -> current.copy(userId = "")
            "username" -> current.copy(username = "")
            "empId" -> current.copy(empId = "")
            "seatNumber" -> current.copy(seatNumber = "")
            "mobileNumber" -> current.copy(mobileNumber = "")
            "password" -> current.copy(password = "")
            "confirmPassword" -> current.copy(confirmPassword = "")
            else -> current
        }
    }

    fun resetState() {
        _uiState.value = SignUpUiState.Idle
        _fieldErrors.value = FieldErrors()
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
    val fieldErrors by viewModel.fieldErrors.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is SignUpUiState.SignUpSuccess -> {
                android.widget.Toast.makeText(context, "Registration successful!", android.widget.Toast.LENGTH_LONG).show()
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
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = userId,
                onValueChange = {
                    userId = it
                    viewModel.clearFieldError("userId")
                },
                label = { Text("User ID") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = fieldErrors.userId.isNotEmpty(),
                supportingText = if (fieldErrors.userId.isNotEmpty()) {
                    { Text(fieldErrors.userId, color = MaterialTheme.colorScheme.error) }
                } else null
            )

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    viewModel.clearFieldError("username")
                },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = fieldErrors.username.isNotEmpty(),
                supportingText = if (fieldErrors.username.isNotEmpty()) {
                    { Text(fieldErrors.username, color = MaterialTheme.colorScheme.error) }
                } else null
            )

            OutlinedTextField(
                value = empId,
                onValueChange = {
                    empId = it
                    viewModel.clearFieldError("empId")
                },
                label = { Text("Employee ID") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = fieldErrors.empId.isNotEmpty(),
                supportingText = if (fieldErrors.empId.isNotEmpty()) {
                    { Text(fieldErrors.empId, color = MaterialTheme.colorScheme.error) }
                } else null
            )

            OutlinedTextField(
                value = seatNumber,
                onValueChange = {
                    seatNumber = it
                    viewModel.clearFieldError("seatNumber")
                },
                label = { Text("Seat Number") },
                placeholder = { Text("e.g., A-12") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = fieldErrors.seatNumber.isNotEmpty(),
                supportingText = if (fieldErrors.seatNumber.isNotEmpty()) {
                    { Text(fieldErrors.seatNumber, color = MaterialTheme.colorScheme.error) }
                } else null
            )

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = {
                    if (it.length <= 10) mobileNumber = it
                    viewModel.clearFieldError("mobileNumber")
                },
                label = { Text("Mobile Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = fieldErrors.mobileNumber.isNotEmpty(),
                supportingText = if (fieldErrors.mobileNumber.isNotEmpty()) {
                    { Text(fieldErrors.mobileNumber, color = MaterialTheme.colorScheme.error) }
                } else null
            )

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    viewModel.clearFieldError("password")
                },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = fieldErrors.password.isNotEmpty(),
                supportingText = if (fieldErrors.password.isNotEmpty()) {
                    { Text(fieldErrors.password, color = MaterialTheme.colorScheme.error) }
                } else null
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    viewModel.clearFieldError("confirmPassword")
                },
                label = { Text("Confirm Password") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = fieldErrors.confirmPassword.isNotEmpty(),
                supportingText = if (fieldErrors.confirmPassword.isNotEmpty()) {
                    { Text(fieldErrors.confirmPassword, color = MaterialTheme.colorScheme.error) }
                } else null
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (viewModel.validate(userId, username, empId, seatNumber, mobileNumber, password, confirmPassword)) {
                        viewModel.signUp(userId, username, empId, seatNumber, mobileNumber, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Up", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
