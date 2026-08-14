# Coffee Vendor - Beverage Ordering System

A real-time beverage ordering Android application for office environments, featuring a Client app for employees and a Vendor app for kitchen staff.

## Architecture

- **Language & UI:** Kotlin with Jetpack Compose
- **Local Storage:** Room Database (SQLite) for offline caching
- **Network:** Retrofit REST API + WebSockets for real-time updates
- **State Management:** ViewModel with Kotlin Flows
- **Dependency Injection:** Hilt
- **Push Notifications:** Firebase Cloud Messaging

## Project Structure

```
app/src/main/java/com/coffevendor/
├── CoffeeVendorApp.kt           # Hilt Application
├── MainActivity.kt              # Compose entry point
├── data/
│   ├── model/                   # Data classes (Beverage, Order, ApiModels)
│   ├── local/                   # Room DB, DAOs, entities
│   └── remote/                  # Retrofit API, WebSocket client
├── di/AppModule.kt              # Hilt DI module
├── service/
│   ├── OrderWebSocketService.kt # Foreground WS service + wake lock
│   └── FCMService.kt           # Firebase push notifications
├── receiver/BootReceiver.kt     # Restart WS on device reboot
└── ui/orderconfig/
    ├── OrderConfigScreen.kt     # Delivery configuration UI
    └── OrderConfigViewModel.kt  # Order placement logic
```

## Features

### Client App
- Beverage discovery with images, pricing, and ingredients
- Quick-add buttons for fast ordering
- Desk/Hall delivery configuration
- Real-time order status tracking

### Vendor App
- Live order queue dashboard with urgency color-coding
- One-tap fulfillment (MARK AS SERVED)
- Recurrence status board for subscription management

## Setup

### Prerequisites
- JDK 17
- Android SDK (API 34)
- Android Studio or CLI tools

### Build
```bash
# Windows
.\gradlew.bat assembleDebug

# macOS/Linux
./gradlew assembleDebug
```

### Install
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Configuration

Update the API base URL in `data/remote/ApiClient.kt`:
```kotlin
private const val BASE_URL = "https://api.coffevendor.com/v1/"
```

Update the WebSocket URL in `data/remote/WebSocketClient.kt`:
```kotlin
private const val WS_URL = "wss://api.coffevendor.com/ws/orders"
```

## Permissions

| Permission | Purpose |
|---|---|
| INTERNET | API and WebSocket communication |
| POST_NOTIFICATIONS | Order status alerts |
| WAKE_LOCK | Keep WebSocket connected in background |
| RECEIVE_BOOT_COMPLETED | Restart services after reboot |

## License

Private - All rights reserved.
