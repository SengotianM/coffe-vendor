package com.coffevendor.ui.vendor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffevendor.data.local.BeverageDao
import com.coffevendor.data.local.toDomain
import com.coffevendor.data.local.toEntity
import com.coffevendor.data.model.Beverage
import com.coffevendor.data.model.BeverageCategory
import com.coffevendor.data.model.BeverageData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BeverageManageViewModel @Inject constructor(
    private val beverageDao: BeverageDao
) : ViewModel() {

    private val _beverages = MutableStateFlow<List<Beverage>>(emptyList())
    val beverages: StateFlow<List<Beverage>> = _beverages.asStateFlow()

    init {
        loadBeverages()
    }

    private fun loadBeverages() {
        viewModelScope.launch {
            beverageDao.getAllBeverages().collect { entities ->
                _beverages.value = entities.map { it.toDomain() }
            }
        }
    }

    fun toggleAvailability(beverage: Beverage) {
        viewModelScope.launch {
            beverageDao.updateAvailability(beverage.id, !beverage.isAvailable)
        }
    }

    fun deleteBeverage(beverage: Beverage) {
        viewModelScope.launch {
            beverageDao.deleteById(beverage.id)
        }
    }

    fun addBeverage(beverage: Beverage) {
        viewModelScope.launch {
            beverageDao.insert(beverage.toEntity())
        }
    }

    fun seedBeverages() {
        viewModelScope.launch {
            beverageDao.insertAll(BeverageData.beverages.map { it.toEntity() })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeverageManageScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BeverageManageViewModel = hiltViewModel()
) {
    val beverages by viewModel.beverages.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Beverages") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, "Add Beverage")
            }
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
                    text = "Beverages (${beverages.size})",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = { viewModel.seedBeverages() }) {
                    Text("Sync Defaults")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (beverages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No beverages",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add or Sync Defaults",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(beverages) { beverage ->
                        BeverageManageCard(
                            beverage = beverage,
                            onToggle = { viewModel.toggleAvailability(beverage) },
                            onDelete = { viewModel.deleteBeverage(beverage) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBeverageDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { beverage ->
                viewModel.addBeverage(beverage)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun BeverageManageCard(
    beverage: Beverage,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = beverage.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = beverage.category.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = beverage.isAvailable,
                    onCheckedChange = { onToggle() }
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBeverageDialog(
    onDismiss: () -> Unit,
    onAdd: (Beverage) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(BeverageCategory.OTHER) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Beverage") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = category.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        BeverageCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    category = cat
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(
                            Beverage(
                                id = System.currentTimeMillis().toString(),
                                name = name.trim(),
                                description = description.trim(),
                                price = 0.0,
                                imageUrl = "",
                                ingredients = emptyList(),
                                category = category,
                                drawableRes = "ic_beverage_coffee",
                                hasSugarOption = true,
                                isAvailable = true
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
