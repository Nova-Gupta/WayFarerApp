# Way.Farer — Android App

A native Android application for the Way.Farer travel booking platform.
Built with Kotlin, using your existing Node.js/Express backend.

---

## Tech Stack

| Layer | Library |
|---|---|
| Language | Kotlin |
| UI | XML Layouts + Material Components |
| Navigation | Jetpack Navigation Component |
| HTTP | Retrofit 2 + OkHttp |
| Images | Glide |
| Auth Storage | EncryptedSharedPreferences |
| Architecture | MVVM (ViewModel + LiveData) |
| Async | Kotlin Coroutines |

> No Jetpack Compose — fully compatible with Android Studio Hedgehog and above on any machine.

---

## Features

- **Splash screen** — auto-detects login status, routes accordingly
- **Login & Register** — connects to your `/api/users/login` and `/api/users/signup` endpoints
- **JWT Auth** — token stored securely using AES-256 encrypted preferences
- **Home screen** — fetches and lists all tours from your backend
- **Tour Detail** — full tour info with Book Now button
- **My Bookings** — lists user's confirmed bookings
- **Profile** — shows user info with Sign Out

---

## Setup Instructions

### Step 1 — Open in Android Studio

1. Unzip `WayFarer.zip`
2. Open Android Studio → **File → Open** → select the `WayFarer` folder
3. Wait for Gradle sync to complete (first time may take 2–3 minutes)

### Step 2 — Configure Backend URL

Open `app/src/main/java/com/wayfarer/app/data/api/RetrofitClient.kt`:

```kotlin
// For your deployed Vercel backend (default):
const val BASE_URL = "https://traveltania.vercel.app/"

// For LOCAL backend on Android Emulator:
// const val BASE_URL = "http://10.0.2.2:5000/"
// (10.0.2.2 is the emulator's alias for your PC's localhost)
```

### Step 3 — Run on Emulator

1. In Android Studio, click **Device Manager** → create a Pixel 6 (API 34) emulator if you don't have one
2. Press the **▶ Run** button
3. The app will install and launch on the emulator

---

## Backend API Endpoints Used

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/users/login` | Login |
| POST | `/api/users/signup` | Register |
| GET | `/api/tours` | Get all tours |
| GET | `/api/bookings/my-tours` | Get user's bookings |
| POST | `/api/bookings` | Create booking |

---

## Project Structure

```
app/src/main/java/com/wayfarer/app/
├── WayFarerApp.kt              ← Application class
├── data/
│   ├── api/
│   │   ├── WayFarerApi.kt      ← Retrofit interface
│   │   └── RetrofitClient.kt   ← HTTP client + auth interceptor
│   ├── models/
│   │   └── Models.kt           ← All data classes
│   └── repository/
│       └── WayFarerRepository.kt
├── ui/
│   ├── auth/
│   │   ├── SplashActivity.kt
│   │   ├── AuthActivity.kt
│   │   ├── AuthViewModel.kt
│   │   ├── LoginFragment.kt
│   │   └── RegisterFragment.kt
│   ├── home/
│   │   ├── MainActivity.kt
│   │   ├── HomeFragment.kt
│   │   ├── HomeViewModel.kt
│   │   └── TourAdapter.kt
│   ├── detail/
│   │   ├── TourDetailFragment.kt
│   │   └── DetailViewModel.kt
│   ├── bookings/
│   │   ├── BookingsFragment.kt
│   │   ├── BookingsViewModel.kt
│   │   └── BookingAdapter.kt
│   └── profile/
│       └── ProfileFragment.kt
└── utils/
    ├── SessionManager.kt       ← Secure token storage
    └── Resource.kt             ← State wrapper (Loading/Success/Error)
```

---

## Troubleshooting

| Problem | Fix |
|---|---|
| Gradle sync fails | File → Invalidate Caches → Restart |
| Network error on emulator | Check that `traveltania.vercel.app` is reachable; try toggling WiFi on emulator |
| Login returns 401 | Verify email/password match your backend's users |
| Images not loading | Tour image URLs depend on your backend serving `/img/tours/` — update `RetrofitClient.BASE_URL` if needed |
| `EncryptedSharedPreferences` crash on first run | Wipe app data in emulator settings and re-run |

---

## Customisation Tips

- **Change backend URL**: `RetrofitClient.kt` → `BASE_URL`
- **Change colours**: `res/values/colors.xml`
- **Add a new screen**: Create a Fragment + ViewModel, add to `nav_graph.xml`, add menu item

---

*Built for Way.Farer — traveltania.vercel.app*
