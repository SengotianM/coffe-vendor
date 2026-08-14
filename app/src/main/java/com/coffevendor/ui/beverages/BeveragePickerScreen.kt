package com.coffevendor.ui.beverages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.coffevendor.R
import com.coffevendor.data.model.Beverage
import com.coffevendor.data.model.BeverageData
import com.coffevendor.data.model.SugarOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BeveragePickerScreen(
    onBeverageSelected: (Beverage, SugarOption) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSugar by remember { mutableStateOf(SugarOption.WITH_SUGAR) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Beverage") },
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
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sugar Preference:",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                    FilterChip(
                        selected = selectedSugar == SugarOption.WITH_SUGAR,
                        onClick = { selectedSugar = SugarOption.WITH_SUGAR },
                        label = { Text("With Sugar") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = selectedSugar == SugarOption.WITHOUT_SUGAR,
                        onClick = { selectedSugar = SugarOption.WITHOUT_SUGAR },
                        label = { Text("Without Sugar") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(BeverageData.beverages) { beverage ->
                    BeverageCard(
                        beverage = beverage,
                        sugarOption = selectedSugar,
                        onClick = { onBeverageSelected(beverage, selectedSugar) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BeverageCard(
    beverage: Beverage,
    sugarOption: SugarOption,
    onClick: () -> Unit
) {
    val drawableResId = remember(beverage.drawableRes) {
        when (beverage.drawableRes) {
            "ic_beverage_coffee" -> R.drawable.ic_beverage_coffee
            "ic_beverage_green_tea" -> R.drawable.ic_beverage_green_tea
            "ic_beverage_badam_milk" -> R.drawable.ic_beverage_badam_milk
            "ic_beverage_milk" -> R.drawable.ic_beverage_milk
            "ic_beverage_dry_ginger_tea" -> R.drawable.ic_beverage_dry_ginger_tea
            "ic_beverage_black_coffee" -> R.drawable.ic_beverage_black_coffee
            "ic_beverage_horlicks" -> R.drawable.ic_beverage_horlicks
            "ic_beverage_boost" -> R.drawable.ic_beverage_boost
            else -> R.drawable.ic_beverage_coffee
        }
    }

    val displayName = if (beverage.hasSugarOption) {
        "${beverage.name}\n(${if (sugarOption == SugarOption.WITH_SUGAR) "With Sugar" else "No Sugar"})"
    } else {
        beverage.name
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = beverage.name,
                modifier = Modifier.size(64.dp),
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$${String.format("%.0f", beverage.price)}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
