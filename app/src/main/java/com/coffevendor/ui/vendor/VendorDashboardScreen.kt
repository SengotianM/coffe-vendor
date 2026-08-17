package com.coffevendor.ui.vendor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffevendor.data.local.OrderDao
import com.coffevendor.data.local.toDomain
import com.coffevendor.data.model.OrderStatus
import com.coffevendor.data.model.RecurrenceType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VendorOrder(
    val id: String,
    val userId: String,
    val beverageName: String,
    val quantity: Int,
    val targetTime: String,
    val status: OrderStatus,
    val location: String,
    val specialInstructions: String?
)

@HiltViewModel
class VendorDashboardViewModel @Inject constructor(
    private val orderDao: OrderDao
) : ViewModel() {

    private val _orders = MutableStateFlow<List<VendorOrder>>(emptyList())
    val orders: StateFlow<List<VendorOrder>> = _orders.asStateFlow()

    init {
        loadOrders()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            orderDao.getAllOrders().collect { entities ->
                _orders.value = entities.map { e ->
                    VendorOrder(
                        id = e.id,
                        userId = e.userId,
                        beverageName = e.beverageName,
                        quantity = e.quantity,
                        targetTime = e.targetTime,
                        status = OrderStatus.valueOf(e.status),
                        location = buildString {
                            if (!e.hallName.isNullOrBlank()) append(e.hallName)
                            else if (!e.seatOrRow.isNullOrBlank()) append(e.seatOrRow)
                        },
                        specialInstructions = e.specialInstructions
                    )
                }
            }
        }
    }

    fun acceptOrder(orderId: String) {
        viewModelScope.launch {
            orderDao.updateStatus(orderId, OrderStatus.PREPARING.name)
        }
    }

    fun deliverOrder(orderId: String) {
        viewModelScope.launch {
            orderDao.updateStatus(orderId, OrderStatus.OUT_FOR_DELIVERY.name)
        }
    }

    fun rejectOrder(orderId: String) {
        viewModelScope.launch {
            orderDao.updateStatus(orderId, OrderStatus.CANCELLED.name)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VendorDashboardScreen(
    onManageBeverages: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VendorDashboardViewModel = hiltViewModel()
) {
    val orders by viewModel.orders.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vendor Dashboard") },
                actions = {
                    IconButton(onClick = onManageBeverages) {
                        Icon(Icons.Default.LocalCafe, "Manage Beverages")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, "Logout")
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Orders (${orders.size})",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No orders yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(orders) { order ->
                        VendorOrderCard(
                            order = order,
                            onAccept = { viewModel.acceptOrder(order.id) },
                            onDeliver = { viewModel.deliverOrder(order.id) },
                            onReject = { viewModel.rejectOrder(order.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorOrderCard(
    order: VendorOrder,
    onAccept: () -> Unit,
    onDeliver: () -> Unit,
    onReject: () -> Unit
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

            Text(
                text = "Qty: ${order.quantity} | Time: ${order.targetTime}",
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
            if (!order.specialInstructions.isNullOrBlank()) {
                Text(
                    text = "Note: ${order.specialInstructions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (order.status) {
                OrderStatus.RECEIVED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onAccept,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Accept")
                        }
                        Button(
                            onClick = onReject,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reject")
                        }
                    }
                }
                OrderStatus.PREPARING -> {
                    Button(
                        onClick = onDeliver,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Mark as Delivered")
                    }
                }
                else -> {}
            }
        }
    }
}
