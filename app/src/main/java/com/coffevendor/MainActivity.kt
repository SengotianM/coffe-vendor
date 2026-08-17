package com.coffevendor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.coffevendor.data.local.OrderDao
import com.coffevendor.data.local.UserDao
import com.coffevendor.data.local.toEntity
import com.coffevendor.data.model.*
import com.coffevendor.ui.auth.LoginScreen
import com.coffevendor.ui.auth.SignUpScreen
import com.coffevendor.ui.beverages.BeveragePickerScreen
import com.coffevendor.ui.dashboard.DashboardScreen
import com.coffevendor.ui.orderconfig.OrderConfigScreen
import com.coffevendor.ui.settings.UserSettingsScreen
import com.coffevendor.ui.vendor.BeverageManageScreen
import com.coffevendor.ui.vendor.VendorDashboardScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var orderDao: OrderDao
    @Inject lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(orderDao = orderDao, userDao = userDao)
                }
            }
        }
    }
}

enum class Screen {
    LOGIN,
    SIGN_UP,
    DASHBOARD,
    BEVERAGE_PICKER,
    ORDER_CONFIG,
    SETTINGS,
    VENDOR_DASHBOARD,
    BEVERAGE_MANAGE
}

@Composable
fun AppNavigation(
    orderDao: OrderDao,
    @Suppress("UNUSED_PARAMETER") userDao: UserDao
) {
    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
    var selectedBeverage by remember { mutableStateOf<Beverage?>(null) }
    var selectedSugar by remember { mutableStateOf(SugarOption.WITH_SUGAR) }
    var loggedInUserId by remember { mutableStateOf("") }
    var loggedInUsername by remember { mutableStateOf("") }
    var loggedInRole by remember { mutableStateOf(UserRole.CUSTOMER) }
    val coroutineScope = rememberCoroutineScope()

    when (currentScreen) {
        Screen.LOGIN -> {
            LoginScreen(
                onLoginSuccess = { username, userId, role ->
                    loggedInUsername = username
                    loggedInUserId = userId
                    loggedInRole = role
                    currentScreen = if (role == UserRole.VENDOR) {
                        Screen.VENDOR_DASHBOARD
                    } else {
                        Screen.DASHBOARD
                    }
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

        Screen.DASHBOARD -> {
            DashboardScreen(
                onOrderBeverage = { currentScreen = Screen.BEVERAGE_PICKER },
                onSettingsClick = { currentScreen = Screen.SETTINGS },
                onLogout = {
                    currentScreen = Screen.LOGIN
                    loggedInUserId = ""
                    loggedInUsername = ""
                }
            )
        }

        Screen.BEVERAGE_PICKER -> {
            BeveragePickerScreen(
                onBeverageSelected = { beverage, sugar ->
                    selectedBeverage = beverage
                    selectedSugar = sugar
                    currentScreen = Screen.ORDER_CONFIG
                },
                onBack = { currentScreen = Screen.DASHBOARD },
                onSettingsClick = { currentScreen = Screen.SETTINGS }
            )
        }

        Screen.ORDER_CONFIG -> {
            OrderConfigScreen(
                beverage = selectedBeverage!!,
                sugarOption = selectedSugar,
                userId = loggedInUserId,
                onBack = { currentScreen = Screen.BEVERAGE_PICKER },
                onOrderPlaced = { request ->
                    val order = Order(
                        id = java.util.UUID.randomUUID().toString(),
                        userId = loggedInUserId,
                        beverageId = request.beverageId,
                        beverageName = BeverageData.beverages.find { it.id == request.beverageId }?.name ?: "",
                        quantity = request.quantity,
                        location = request.location,
                        targetTime = request.targetTime,
                        recurrence = request.recurrence,
                        createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        status = OrderStatus.RECEIVED,
                        specialInstructions = request.specialInstructions
                    )
                    coroutineScope.launch {
                        orderDao.insert(order.toEntity())
                    }
                    currentScreen = Screen.DASHBOARD
                }
            )
        }

        Screen.SETTINGS -> {
            UserSettingsScreen(
                onBack = { currentScreen = Screen.DASHBOARD },
                onLogout = {
                    currentScreen = Screen.LOGIN
                    loggedInUserId = ""
                    loggedInUsername = ""
                }
            )
        }

        Screen.VENDOR_DASHBOARD -> {
            VendorDashboardScreen(
                onManageBeverages = { currentScreen = Screen.BEVERAGE_MANAGE },
                onLogout = {
                    currentScreen = Screen.LOGIN
                    loggedInUserId = ""
                    loggedInUsername = ""
                }
            )
        }

        Screen.BEVERAGE_MANAGE -> {
            BeverageManageScreen(
                onBack = { currentScreen = Screen.VENDOR_DASHBOARD }
            )
        }
    }
}
