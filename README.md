# Coffee Vendor - Beverage Ordering System

A complete Android beverage ordering application for office environments, featuring a **Customer app** for employees and a **Vendor app** for kitchen staff. Orders are managed locally via Room Database with real-time status updates.

## Tech Stack

- **Language & UI:** Kotlin + Jetpack Compose + Material 3
- **Local Storage:** Room Database (SQLite) with migrations
- **Dependency Injection:** Hilt
- **Architecture:** MVVM (ViewModel + StateFlow)
- **Image Loading:** Coil
- **Biometric Auth:** AndroidX Biometric
- **Push Notifications:** Firebase Cloud Messaging
- **Real-time:** WebSocket support (foreground service)

## Project Structure

```
app/src/main/java/com/coffevendor/
├── CoffeeVendorApp.kt                    # Hilt Application
├── MainActivity.kt                        # Navigation host, Screen enum
│
├── data/
│   ├── model/
│   │   ├── Beverage.kt                    # Beverage, BeverageCategory, SugarOption, BeverageData
│   │   ├── Order.kt                       # Order, OrderStatus, LocationType, RecurrenceType
│   │   ├── User.kt                        # User, UserRole, SignUpRequest, LoginRequest
│   │   └── ApiModels.kt                   # CreateOrderRequest, Boardroom, etc.
│   ├── local/
│   │   ├── Entities.kt                    # BeverageEntity, OrderEntity + mappers
│   │   ├── UserEntity.kt                  # UserEntity + mappers
│   │   ├── Daos.kt                        # BeverageDao, OrderDao
│   │   ├── UserDao.kt                     # UserDao
│   │   └── CoffeeDatabase.kt             # Room DB (v4)
│   └── remote/
│       ├── ApiClient.kt                   # Retrofit singleton
│       ├── CoffeeApiService.kt            # REST API interface
│       └── WebSocketClient.kt            # WebSocket client
│
├── di/AppModule.kt                        # Hilt module (DB, DAOs, API)
│
├── service/
│   ├── OrderWebSocketService.kt           # Foreground WS service + wake lock
│   └── FCMService.kt                      # Firebase push notifications
│
├── receiver/BootReceiver.kt              # Restart WS on device reboot
│
├── ui/
│   ├── auth/
│   │   ├── LoginScreen.kt                 # Login + biometric + role routing
│   │   └── SignUpScreen.kt                # Customer registration
│   ├── dashboard/
│   │   └── DashboardScreen.kt             # Customer order list + status
│   ├── beverages/
│   │   └── BeveragePickerScreen.kt        # Beverage grid with sugar filter
│   ├── orderconfig/
│   │   ├── OrderConfigScreen.kt           # Delivery configuration
│   │   └── OrderConfigViewModel.kt        # Network order placement
│   ├── settings/
│   │   └── UserSettingsScreen.kt          # Profile, favorites, biometric toggle
│   └── vendor/
│       ├── VendorDashboardScreen.kt       # Vendor order management
│       └── BeverageManageScreen.kt        # Beverage CRUD for vendor
│
└── util/BiometricHelper.kt                # BiometricPrompt wrapper
```

## Features

### Customer App
- **Sign Up / Login** with password + biometric authentication
- **Beverage Picker** — 2-column grid with sugar preference filter (With/Without Sugar)
- **Order Configuration** — Select delivery location (Work Desk / Conference Hall), time picker, recurrence (every 1/2/3 hours), special instructions
- **Dashboard** — View all orders with status badges (Received, Preparing, Out for Delivery, Served, Cancelled)
- **Settings** — Profile photo, favorites, biometric toggle, logout
- **Pre-loaded beverages:** Tea, Green Tea, Badam Milk, Milk, Dry Ginger Tea, Black Coffee, Horlicks, Boost

### Vendor App
- **Vendor Login** — userId: `vendor`, password: `1234` (auto-routes to vendor dashboard)
- **Order Management Dashboard** — View all incoming orders with details (beverage, quantity, location, time, instructions)
- **Accept Order** — Change status: RECEIVED → PREPARING
- **Reject Order** — Change status: RECEIVED → CANCELLED
- **Deliver Order** — Change status: PREPARING → OUT_FOR_DELIVERY
- **Beverage Management** — Add new beverages, toggle availability, delete beverages, sync default list

## Navigation Flow

```
LOGIN ────── checks role ──────┐
  │                             │
  │ CUSTOMER                    │ VENDOR
  v                             v
DASHBOARD                  VENDOR_DASHBOARD
  │ │                          │
  │ │ FAB "+"                  │ Coffee icon
  │ v                          v
  │ BEVERAGE_PICKER        BEVERAGE_MANAGE
  │ │
  │ │ Select beverage
  │ v
  │ ORDER_CONFIG
  │ │
  │ │ Place order → saved to DB
  │ v
  └─── DASHBOARD (shows order)
```

## Database

Room Database `coffee_vendor_db` (version 4) with 3 tables:

| Table | Purpose |
|-------|---------|
| `users` | Customer & vendor accounts with role, biometric, favorites |
| `beverages` | Beverage catalog with availability |
| `orders` | Order records with status, location, recurrence |

**Migrations:** v1→v2 (users table), v2→v3 (userId on orders), v3→v4 (role on users + seed vendor)

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

## Default Accounts

| Role | User ID | Password |
|------|---------|----------|
| Vendor | `vendor` | `1234` |
| Customer | (sign up) | (set during signup) |

## Permissions

| Permission | Purpose |
|---|---|
| INTERNET | API and WebSocket communication |
| POST_NOTIFICATIONS | Order status alerts |
| WAKE_LOCK | Keep WebSocket connected in background |
| RECEIVE_BOOT_COMPLETED | Restart services after reboot |
| USE_BIOMETRIC | Fingerprint authentication |
| USE_FINGERPRINT | Legacy fingerprint support |

## Tags

- `v1.1.0` — Beverages + logos
- `v1.2.0` — Auth + settings
- `v1.3.0` — Biometric login toggle
- `v1.4.0` — Dashboard + order flow
- `v1.5.0` — Vendor role + dashboard + beverage management

## License

Private - All rights reserved.
