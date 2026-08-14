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
import com.coffevendor.ui.beverages.BeveragePickerScreen
import com.coffevendor.ui.orderconfig.OrderConfigScreen
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
                    var selectedBeverage by remember { mutableStateOf<Beverage?>(null) }
                    var selectedSugar by remember { mutableStateOf(SugarOption.WITH_SUGAR) }

                    when {
                        selectedBeverage == null -> {
                            BeveragePickerScreen(
                                onBeverageSelected = { beverage, sugar ->
                                    selectedBeverage = beverage
                                    selectedSugar = sugar
                                },
                                onBack = { finish() }
                            )
                        }
                        else -> {
                            OrderConfigScreen(
                                beverage = selectedBeverage!!,
                                sugarOption = selectedSugar,
                                onBack = { selectedBeverage = null },
                                onOrderPlaced = { request ->
                                    // TODO: Show confirmation or call ViewModel
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
