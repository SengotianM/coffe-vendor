package com.coffevendor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.coffevendor.ui.orderconfig.OrderConfigScreen
import com.coffevendor.data.model.*
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
                    val sampleBeverage = Beverage(
                        id = "1",
                        name = "Espresso",
                        description = "Rich, bold single-shot espresso",
                        price = 3.50,
                        imageUrl = "https://example.com/espresso.jpg",
                        ingredients = listOf("Coffee beans", "Water"),
                        category = BeverageCategory.COFFEE
                    )

                    OrderConfigScreen(
                        beverage = sampleBeverage,
                        onBack = { finish() },
                        onOrderPlaced = { request ->
                            // TODO: Navigate to confirmation or call ViewModel
                        }
                    )
                }
            }
        }
    }
}
