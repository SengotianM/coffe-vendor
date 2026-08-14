package com.coffevendor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.coffevendor.data.model.*
import com.coffevendor.ui.auth.LoginScreen
import com.coffevendor.ui.auth.SignUpScreen
import com.coffevendor.ui.beverages.BeveragePickerScreen
import com.coffevendor.ui.orderconfig.OrderConfigScreen
import com.coffevendor.ui.settings.UserSettingsScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

enum class Screen {
    LOGIN,
    SIGN_UP,
    BEVERAGE_PICKER,
    ORDER_CONFIG,
    SETTINGS
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
    var selectedBeverage by remember { mutableStateOf<Beverage?>(null) }
    var selectedSugar by remember { mutableStateOf(SugarOption.WITH_SUGAR) }
    var loggedInUsername by remember { mutableStateOf("") }

    when (currentScreen) {
        Screen.LOGIN -> {
            LoginScreen(
                onLoginSuccess = { username ->
                    loggedInUsername = username
                    currentScreen = Screen.BEVERAGE_PICKER
                },
                onSignUpClick = { currentScreen = Screen.SIGN_UP }
            )
        }

        Screen.SIGN_UP -> {
            SignUpScreen(
                onSignUpComplete = { currentScreen = Screen.LOGIN },
                onBack = { currentScreen = Screen.LOGIN }
            )
        }

        Screen.BEVERAGE_PICKER -> {
            BeveragePickerScreen(
                onBeverageSelected = { beverage, sugar ->
                    selectedBeverage = beverage
                    selectedSugar = sugar
                    currentScreen = Screen.ORDER_CONFIG
                },
                onBack = {
                    currentScreen = Screen.LOGIN
                },
                onSettingsClick = { currentScreen = Screen.SETTINGS }
            )
        }

        Screen.ORDER_CONFIG -> {
            OrderConfigScreen(
                beverage = selectedBeverage!!,
                sugarOption = selectedSugar,
                onBack = { currentScreen = Screen.BEVERAGE_PICKER },
                onOrderPlaced = { request ->
                    // TODO: Show confirmation
                    currentScreen = Screen.BEVERAGE_PICKER
                }
            )
        }

        Screen.SETTINGS -> {
            UserSettingsScreen(
                onBack = { currentScreen = Screen.BEVERAGE_PICKER },
                onLogout = {
                    currentScreen = Screen.LOGIN
                    loggedInUsername = ""
                }
            )
        }
    }
}
