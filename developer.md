# Developer Guide

## Prerequisites
- Android Studio Hedgehog (2023.1) or later
- JDK 17
- Android SDK 34
- Kotlin 1.9.20

## Build & Run

### Debug build
```bash
./gradlew assembleDebug
```

### Release build (R8 minified)
```bash
./gradlew assembleRelease
```

### Install on emulator
```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Project Setup

### Environment
- **JDK**: `C:\Program Files\Microsoft\jdk-17.0.20.8-hotspot`
- **Android SDK**: `C:\Android\Sdk`
- **Emulator**: Pixel_7 AVD

### Supabase Configuration
- **URL**: `https://trxoycjvstwslwueltpb.supabase.co`
- **Anon Key**: in `SupabaseClient.kt`
- **Tables**: `users`, `beverages`, `orders`

### Default Accounts
| Role | User ID | Password |
|------|---------|----------|
| Vendor | `vendor` | `1234` |
| Customer | Register new | - |

## Architecture

### JWT Authentication
- **Access Token**: 30-day validity, HMAC-SHA256 signed
- **Refresh Token**: 7-day validity, UUID-based
- **Storage**: Room `users` table + Supabase `users` table
- **Validation**: Checked before every API call
- **Refresh**: Automatic when access token expires (if refresh token valid)

Key files:
- `util/JwtManager.kt` - Token generation, validation, refresh
- `data/remote/SupabaseClient.kt` - HTTP client with JWT header
- `data/remote/SupabaseRepository.kt` - Auth logic, token lifecycle

### Data Sync
Every write operation syncs to **both** local Room and remote Supabase:
- Room = offline cache
- Supabase = source of truth

### Navigation
Single-activity Compose navigation with `BackHandler` on every screen:
- Login → disabled (exits app)
- Dashboard/Vendor → back = logout
- Sub-screens → back = parent screen

### Role-Based Access
- **Customer**: Order beverages, view/cancel orders, manage profile
- **Vendor**: View/accept/reject/deliver orders, manage beverages

## Testing

### Unit Tests
```bash
./gradlew test
```

### Key Test Files
- `UserRegistrationTest.kt` - 11 test cases for signup validation

## APK Size
- Debug: ~54MB (Compose + Room + OkHttp + Coil + Biometric + Firebase)
- Release: ~25MB (R8 minified + resource shrunk)

## Common Issues

### Emulator not found
```bash
# Start emulator
& "C:\Android\Sdk\emulator\emulator.exe" -avd Pixel_7

# Wait for boot
adb wait-for-device shell getprop sys.boot_completed
```

### Build fails with Room migration
Room DB version must be incremented in `CoffeeDatabase.kt` when adding columns. Add migration in `AppModule.kt`.

### Supabase 401 errors
Token expired. Check `accessTokenExpiry` in `users` table. Force re-login by clearing tokens.

## Database Schema

### Supabase `users` table
```
id, user_id, username, emp_id, seat_number, mobile_number,
password, photo_uri, favorite_beverages, is_biometric_enabled,
is_logged_in, role, access_token, refresh_token,
access_token_expiry, refresh_token_expiry
```

### Room `users` table (same columns)
Local mirror with Room `UserEntity`.

## Git Workflow
- `master` = primary branch
- `main` = mirror (push with `git push origin master:main`)
- Tags: `v1.1.0` through `v1.8.0`
