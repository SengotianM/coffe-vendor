package com.coffevendor.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import com.coffevendor.ui.icons.AppIcons
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffevendor.R
import com.coffevendor.data.local.OrderDao
import com.coffevendor.data.local.UserDao
import com.coffevendor.data.local.toDomain
import com.coffevendor.data.model.*
import com.coffevendor.data.remote.SupabaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardOrder(
    val id: String,
    val beverageName: String,
    val quantity: Int,
    val targetTime: String,
    val status: OrderStatus,
    val location: String,
    val recurrence: RecurrenceType
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val orderDao: OrderDao,
    private val userDao: UserDao,
    private val repository: SupabaseRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<DashboardOrder>>(emptyList())
    val orders: StateFlow<List<DashboardOrder>> = _orders.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    init {
        loadOrders()
        syncFromRemote()
        loadUsername()
    }

    private fun loadUsername() {
        viewModelScope.launch {
            val user = userDao.getLoggedInUser()
            if (user != null) {
                _username.value = user.toDomain().username
            }
        }
    }

    private fun loadOrders() {
        viewModelScope.launch {
            orderDao.getAllOrders().collect { orderEntities ->
                _orders.value = orderEntities.map { entity ->
                    DashboardOrder(
                        id = entity.id,
                        beverageName = entity.beverageName,
                        quantity = entity.quantity,
                        targetTime = entity.targetTime,
                        status = OrderStatus.valueOf(entity.status),
                        location = buildString {
                            if (entity.hallName != null) append(entity.hallName)
                            else if (entity.seatOrRow != null) append(entity.seatOrRow)
                        },
                        recurrence = RecurrenceType.valueOf(entity.recurrence)
                    )
                }
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            orderDao.updateStatus(orderId, OrderStatus.CANCELLED.name)
            repository.updateOrderStatusRemote(orderId, OrderStatus.CANCELLED)
        }
    }

    private fun syncFromRemote() {
        viewModelScope.launch {
            repository.syncOrdersFromRemote()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onOrderBeverage: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    dashboardViewModel: DashboardViewModel = hiltViewModel()
) {
    val orders by dashboardViewModel.orders.collectAsState()
    val username by dashboardViewModel.username.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(AppIcons.Logout, "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOrderBeverage,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, "Order Beverage")
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Welcome, $username",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Your beverage orders",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No orders yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to order a beverage",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(orders) { order ->
                        OrderCard(
                            order = order,
                            onCancel = { dashboardViewModel.cancelOrder(order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderCard(
    order: DashboardOrder,
    onCancel: () -> Unit
) {
    val statusColor = when (order.status) {
        OrderStatus.RECEIVED -> MaterialTheme.colorScheme.primary
        OrderStatus.PREPARING -> MaterialTheme.colorScheme.tertiary
        OrderStatus.OUT_FOR_DELIVERY -> MaterialTheme.colorScheme.secondary
        OrderStatus.SERVED -> MaterialTheme.colorScheme.primary
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }

    val statusText = when (order.status) {
        OrderStatus.RECEIVED -> "Received"
        OrderStatus.PREPARING -> "Preparing"
        OrderStatus.OUT_FOR_DELIVERY -> "Out for Delivery"
        OrderStatus.SERVED -> "Served"
        OrderStatus.CANCELLED -> "Cancelled"
    }

    val recurrenceText = when (order.recurrence) {
        RecurrenceType.NO_REPEAT -> ""
        RecurrenceType.EVERY_1_HOUR -> "Repeats every 1 hour"
        RecurrenceType.EVERY_2_HOURS -> "Repeats every 2 hours"
        RecurrenceType.EVERY_3_HOURS -> "Repeats every 3 hours"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = order.beverageName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Qty: ${order.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Time: ${order.targetTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (order.location.isNotBlank()) {
                        Text(
                            text = "Location: ${order.location}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (recurrenceText.isNotBlank()) {
                        Text(
                            text = recurrenceText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (order.status != OrderStatus.CANCELLED && order.status != OrderStatus.SERVED) {
                    TextButton(onClick = onCancel) {
                        Text("Cancel", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
