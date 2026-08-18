# Architecture

## Overview
Coffee Vendor is an Android application for ordering beverages in an office/enterprise environment. It supports two user roles: **Customer** (places orders) and **Vendor** (manages beverages and fulfills orders).

## Tech Stack
| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material3 |
| State | ViewModel + StateFlow |
| DI | Hilt (Dagger) |
| Local DB | Room (SQLite) |
| Remote DB | Supabase (PostgreSQL) via REST API |
| HTTP | OkHttp |
| Auth | Custom JWT (HMAC-SHA256) |
| Images | Coil |
| Biometric | AndroidX Biometric |
| Push | Firebase Cloud Messaging |

## Project Structure
```
com.coffevendor/
├── data/
│   ├── local/          # Room entities, DAOs, Database
│   ├── model/          # Domain models (User, Beverage, Order, etc.)
│   └── remote/         # Supabase REST client + Repository
├── di/                 # Hilt dependency injection modules
├── service/            # OrderWebSocketService (OkHttp WebSocket)
├── ui/
│   ├── auth/           # LoginScreen, SignUpScreen
│   ├── beverages/      # BeveragePickerScreen (customer)
│   ├── dashboard/      # DashboardScreen (customer)
│   ├── icons/          # Custom AppIcons
│   ├── orderconfig/    # OrderConfigScreen
│   ├── settings/       # UserSettingsScreen
│   └── vendor/         # VendorDashboardScreen, BeverageManageScreen
└── util/               # BiometricHelper, JwtManager
```

## Authentication Flow
1. **Sign Up** → credentials stored in Supabase `users` table + local Room
2. **Login** → validates password against Supabase, generates JWT access token (30 days) + refresh token (7 days)
3. **JWT stored** → in local Room `users` table columns: `accessToken`, `refreshToken`, `accessTokenExpiry`, `refreshTokenExpiry`
4. **Token used** → on every Supabase REST call via `Authorization: Bearer <access_token>`
5. **Token refresh** → when access token expires, refresh token is used to generate new access token (up to 7 days)
6. **Logout** → tokens cleared from both local Room and Supabase remote

## Data Sync Strategy
- **Remote-first**: Every data operation hits Supabase first, falls back to Room on failure
- **Local cache**: Room serves as offline cache, always updated after successful remote write
- **On app start**: Beverages and orders are synced from remote Supabase

## Role-Based Access
| Feature | Customer | Vendor |
|---------|----------|--------|
| Order beverages | ✅ | ❌ |
| View own orders | ✅ | ❌ |
| Cancel own order | ✅ | ❌ |
| View all orders | ❌ | ✅ |
| Accept/reject/deliver orders | ❌ | ✅ |
| Add/delete/toggle beverages | ❌ | ✅ |
| Manage profile settings | ✅ | ✅ |

## Navigation
Single-activity with Compose navigation using a `currentScreen` state enum. `BackHandler` intercepts system back button to control navigation stack and prevent back-navigation to authenticated screens after logout.
