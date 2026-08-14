package com.coffevendor.ui.orderconfig

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.coffevendor.data.model.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderConfigScreen(
    beverage: Beverage,
    sugarOption: SugarOption = SugarOption.WITH_SUGAR,
    onBack: () -> Unit,
    onOrderPlaced: (CreateOrderRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var locationType by remember { mutableStateOf(LocationType.WORK_DESK) }
    var seatOrRow by remember { mutableStateOf("") }
    var selectedHall by remember { mutableStateOf<Boardroom?>(null) }
    var selectedTime by remember { mutableStateOf(LocalTime.now().plusMinutes(15)) }
    var recurrence by remember { mutableStateOf(RecurrenceType.NO_REPEAT) }
    var specialInstructions by remember { mutableStateOf("") }
    var quantity by remember { mutableIntStateOf(1) }

    val boardrooms = remember {
        listOf(
            Boardroom("1", "ORION", 3, 20),
            Boardroom("2", "SIRIUS", 5, 12),
            Boardroom("3", "VEGA", 2, 8),
            Boardroom("4", "ALTAIR", 4, 16)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configure Delivery") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BeverageSummaryCard(beverage = beverage, sugarOption = sugarOption, quantity = quantity, onQuantityChange = { quantity = it })

            Divider()

            SectionHeader(icon = Icons.Default.LocationOn, title = "Delivery Location")

            LocationTypeSelector(
                selectedType = locationType,
                onTypeSelected = { locationType = it }
            )

            when (locationType) {
                LocationType.WORK_DESK -> {
                    SeatInput(
                        value = seatOrRow,
                        onValueChange = { seatOrRow = it }
                    )
                }
                LocationType.CONFERENCE_HALL -> {
                    HallDropdown(
                        boardrooms = boardrooms,
                        selectedHall = selectedHall,
                        onHallSelected = { selectedHall = it }
                    )
                }
            }

            Divider()

            SectionHeader(icon = Icons.Default.Schedule, title = "Delivery Time")

            TimePickerRow(
                selectedTime = selectedTime,
                onTimeSelected = { selectedTime = it }
            )

            RecurrenceSelector(
                selectedRecurrence = recurrence,
                onRecurrenceSelected = { recurrence = it }
            )

            Divider()

            OutlinedTextField(
                value = specialInstructions,
                onValueChange = { specialInstructions = it },
                label = { Text("Special Instructions (optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val location = when (locationType) {
                        LocationType.WORK_DESK -> DeliveryLocation(
                            type = LocationType.WORK_DESK,
                            seatOrRow = seatOrRow
                        )
                        LocationType.CONFERENCE_HALL -> DeliveryLocation(
                            type = LocationType.CONFERENCE_HALL,
                            hallName = selectedHall?.name
                        )
                    }

                    val request = CreateOrderRequest(
                        beverageId = beverage.id,
                        quantity = quantity,
                        location = location,
                        targetTime = selectedTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
                        recurrence = recurrence,
                        specialInstructions = specialInstructions.ifBlank { null }
                    )
                    onOrderPlaced(request)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = isOrderValid(
                    locationType = locationType,
                    seatOrRow = seatOrRow,
                    selectedHall = selectedHall
                )
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Place Order", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun BeverageSummaryCard(
    beverage: Beverage,
    sugarOption: SugarOption,
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = if (beverage.hasSugarOption) {
                        "${beverage.description} - ${if (sugarOption == SugarOption.WITH_SUGAR) "With Sugar" else "Without Sugar"}"
                    } else {
                        beverage.description
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$${String.format("%.2f", beverage.price)} each",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            QuantitySelector(
                quantity = quantity,
                onQuantityChange = onQuantityChange
            )
        }
    }
}

@Composable
private fun QuantitySelector(
    quantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = { if (quantity > 1) onQuantityChange(quantity - 1) },
            enabled = quantity > 1
        ) {
            Text("-", style = MaterialTheme.typography.titleLarge)
        }
        Text(
            text = "$quantity",
            style = MaterialTheme.typography.titleMedium
        )
        IconButton(
            onClick = { if (quantity < 10) onQuantityChange(quantity + 1) },
            enabled = quantity < 10
        ) {
            Text("+", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun SectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationTypeSelector(
    selectedType: LocationType,
    onTypeSelected: (LocationType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedType == LocationType.WORK_DESK,
            onClick = { onTypeSelected(LocationType.WORK_DESK) },
            label = { Text("Work Desk") },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = selectedType == LocationType.CONFERENCE_HALL,
            onClick = { onTypeSelected(LocationType.CONFERENCE_HALL) },
            label = { Text("Conference Hall") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SeatInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Seat / Row (e.g., A-12)") },
        placeholder = { Text("Enter your seat or row number") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HallDropdown(
    boardrooms: List<Boardroom>,
    selectedHall: Boardroom?,
    onHallSelected: (Boardroom) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedHall?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Select Boardroom") },
            placeholder = { Text("Choose a hall") },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, "Expand dropdown")
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            boardrooms.forEach { hall ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(hall.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Floor ${hall.floor} | Capacity: ${hall.capacity}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        onHallSelected(hall)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerRow(
    selectedTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a")),
        onValueChange = {},
        readOnly = true,
        label = { Text("Delivery Time") },
        trailingIcon = {
            Icon(Icons.Default.Schedule, "Select time")
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showTimePicker = true }
    )

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Delivery Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecurrenceSelector(
    selectedRecurrence: RecurrenceType,
    onRecurrenceSelected: (RecurrenceType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val recurrenceLabels = mapOf(
        RecurrenceType.NO_REPEAT to "No Repeat",
        RecurrenceType.EVERY_1_HOUR to "Every 1 Hour",
        RecurrenceType.EVERY_2_HOURS to "Every 2 Hours",
        RecurrenceType.EVERY_3_HOURS to "Every 3 Hours"
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = recurrenceLabels[selectedRecurrence] ?: "No Repeat",
            onValueChange = {},
            readOnly = true,
            label = { Text("Repeat Order") },
            trailingIcon = {
                Icon(Icons.Default.ArrowDropDown, "Expand dropdown")
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            recurrenceLabels.forEach { (type, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onRecurrenceSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun isOrderValid(
    locationType: LocationType,
    seatOrRow: String,
    selectedHall: Boardroom?
): Boolean {
    return when (locationType) {
        LocationType.WORK_DESK -> seatOrRow.isNotBlank()
        LocationType.CONFERENCE_HALL -> selectedHall != null
    }
}
