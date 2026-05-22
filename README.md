# WayFarer — Your AI-Powered Travel Companion

WayFarer is a premium native Android app for discovering, booking, and planning travel experiences. Built entirely in Kotlin with a clean MVVM architecture, it combines a curated catalog of 30+ global tours, real-time hotel matching, Firebase-backed bookings and reviews, and an AI travel assistant powered by the Groq API.

---

## Screenshots

> Run the app and take screenshots to fill this section.

---

## Features

### Tour Discovery & Browsing
- **30+ curated tour packages** spanning Adventure, Nature, Culture, City, and Relaxation categories across every major destination — Europe, Asia, the Americas, Africa, and India.
- **Live search** — filter tours instantly by name or description as you type.
- **Category chips** — one-tap filtering by Adventure, Nature, Culture, City, or Relaxation.
- **Rich tour cards** — cover image, price, duration, difficulty badge, and average rating at a glance.
- **Tour detail page** — full description, price, duration, rating breakdown, a scrollable route timeline showing every city in order, and a booking card.

### Hotel Directory
- **36 world-class hotels** — from the Burj Al Arab and Marina Bay Sands to boutique riads in Marrakech and eco-lodges in Costa Rica.
- **Smart hotel matching** — when you open the booking sheet for a tour, only hotels in the tour's destination cities are suggested; the full list is shown as a fallback.
- Per-hotel details: name, location, star rating, price per night, and a description.

### Booking Engine
- **End-to-end booking flow** via a bottom sheet — select hotel, check-in/check-out dates, number of guests, trip type (Solo / Couple / Family / Group), and payment method.
- **Dynamic total calculation** — tour base price + (hotel nightly rate × nights selected).
- **Firestore persistence** — every booking is saved to `users/{uid}/bookings` so it survives app restarts and device changes.
- **My Bookings tab** — chronological list of all past and upcoming bookings with tour name, hotel, dates, guest count, and total amount paid.
- **Cancel booking** — long-press or tap the Cancel button on any booking card; a confirmation dialog prevents accidental cancellations.
- **Booking notifications** — an instant push notification confirms the booking, and a WorkManager-scheduled reminder fires before the tour starts.

### Traveller Reviews
- **Per-tour reviews list** — each tour detail page shows all submitted reviews with reviewer initials avatar, name, star rating, relative date, and comment.
- **Aggregate rating card** — computed average score displayed as a large number alongside a 5-star visual and total review count.
- **Submit a review** — tap "Write a Review" to open a bottom sheet with a 5-star RatingBar and a comment field. Requires authentication.
- **Real-time update** — after a successful submission the reviews list reloads immediately.
- Reviews stored in Firestore at `tours/{tourId}/reviews` and publicly readable.

### AI Travel Assistant (Groq)
- **Llama 3.3 70B powered chatbot** (Groq API) with a travel-focused system prompt.
- Full conversation history — messages are kept in memory for context-aware replies throughout the session.
- Clean chat UI with sent/received bubble styling, timestamps, and a loading indicator while the model responds.
- The chat tab auto-hides the bottom navigation bar for a distraction-free experience.

### Authentication
- **Email / password sign-up and login** via Firebase Authentication.
- Input validation: email format check, minimum 6-character password, non-empty name.
- Friendly, specific error messages (wrong password, no account found, email already in use).
- Splash screen checks auth state on launch and routes directly to the home screen if already signed in.
- Secure session managed by `FirebaseAuth.currentUser` — no tokens stored manually.

### User Profile & Settings
- Displays the signed-in user's name and email; shows "Guest Traveller" for unauthenticated users.
- **Edit Profile** — update display name; change is persisted to both Firebase Auth and Firestore.
- **Dark Mode toggle** — switches between light and dark themes instantly with full Material 3 support; preference is saved across sessions.
- **Notifications toggle** — UI control for notification preferences.
- **Help & Support page** — FAQ content, one-tap email support, one-tap phone support.
- **App Settings shortcut** — opens the system settings page for the app directly.
- **Sign In / Sign Out** — context-aware button; signs out and returns to the auth screen.

### Location Recommendations
- Users can submit new travel spots with a description, GPS coordinates, category (Viewpoint, Restaurant, Hidden Gem, Historical, Hiking, Other), and a "Why Visit" blurb.
- Submissions are saved to Firestore's `locations` collection and attributed to the submitter's UID.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Min SDK | 24 (Android 7.0 Nougat) |
| Target SDK | 35 (Android 15) |
| Architecture | MVVM + Repository pattern |
| UI | Material Design 3, ViewBinding, XML layouts |
| Navigation | Jetpack Navigation Component 2.7.6 |
| Async | Kotlin Coroutines + Flow |
| Networking | Retrofit 2.9.0, OkHttp 4.12.0, Gson |
| Image Loading | Glide 4.16.0 |
| Backend / Auth | Firebase Authentication, Cloud Firestore |
| AI Chatbot | Groq API — llama-3.3-70b-versatile model |
| Background Tasks | WorkManager 2.9.0 |
| Layout Utilities | Google FlexboxLayout 3.0.0 |
| Build | AGP 8.5.0, Kotlin 2.1.20 |

---

## Architecture

```
app/
├── data/
│   ├── api/           # Retrofit interfaces (GroqApi, WayFarerApi)
│   ├── models/        # Data classes (Tour, Booking, Review, Hotel, User, …)
│   └── repository/    # WayFarerRepository — single source of truth
│
├── ui/
│   ├── auth/          # SplashActivity, AuthActivity, LoginFragment, RegisterFragment
│   ├── home/          # HomeFragment, TourAdapter
│   ├── detail/        # TourDetailFragment, DetailViewModel, ReviewAdapter,
│   │                  #   WriteReviewSheet, BookingBottomSheet
│   ├── bookings/      # BookingsFragment, BookingsViewModel, BookingAdapter
│   ├── hotels/        # HotelFragment, HotelViewModel, HotelAdapter
│   ├── chat/          # ChatFragment, ChatViewModel
│   ├── profile/       # ProfileFragment, EditProfileFragment, HelpSupportFragment
│   └── location/      # AddLocationFragment
│
└── utils/
    ├── Resource.kt        # Sealed class: Success / Error / Loading
    ├── SessionManager.kt  # Auth state + dark mode preference
    ├── NotificationHelper.kt
    └── ReminderWorker.kt
```

**Data flow:** Fragment observes LiveData on ViewModel → ViewModel calls Repository → Repository calls Firebase / Retrofit → result wrapped in `Resource<T>` sealed class → ViewModel posts to LiveData → Fragment reacts.

---

## Firestore Schema

```
users/
  {uid}/
    bookings/
      {bookingId}   → tourId, tourName, tourImage, hotelName, checkInDate,
                       checkOutDate, nights, guests, totalAmount, paid, createdAt, …

tours/
  {tourId}/
    reviews/
      {reviewId}    → userId, userName, rating, comment, createdAt

locations/
  {locationId}      → description, latitude, longitude, category, whyVisit,
                       submittedBy, createdAt
```

### Security Rules
```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Each user can only read/write their own data
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }

    // Reviews are public to read; any authenticated user can write
    match /tours/{tourId}/reviews/{reviewId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

---

## Setup & Installation

### Prerequisites
- Android Studio Ladybug (2024.2.1) or newer
- A Firebase project with Authentication (Email/Password) and Firestore enabled
- A Groq API key from [x.ai](https://x.ai)

### Steps

**1. Clone the repository**
```bash
git clone https://github.com/your-username/WayFarerApp.git
cd WayFarerApp
```

**2. Connect Firebase**
- Download `google-services.json` from your Firebase project console.
- Place it in the `app/` directory.
- In Firebase Console → Firestore → Rules, publish the security rules shown above.

**3. Add the Groq API key**

Open (or create) `local.properties` in the project root and add:
```properties
GROK_API_KEY=your_groq_api_key_here
```
This key is injected at build time via `BuildConfig` and never committed to source control.

**4. Build and run**
- Open the project in Android Studio.
- Let Gradle sync finish.
- Run on an emulator or physical device (API 24+).

---

## Project Highlights

- **Zero hardcoded secrets** — API keys are read from `local.properties` via `BuildConfig` fields.
- **Cloud-first data** — all user-generated content (bookings, reviews, locations) lives in Firestore; the app works across devices with the same account.
- **Static tour & hotel catalog** — 30 tours and 36 hotels are bundled directly in the repository companion object for instant, offline-capable loading with no extra network calls.
- **Smart hotel suggestions** — `getHotelsForTour()` fuzzy-matches hotel city names against a tour's route list so relevant options surface first.
- **WorkManager reminders** — booking reminders are scheduled as deferred `OneTimeWorkRequest` tasks that survive process death.
- **Material 3 theming** — full light/dark theme support via `AppCompatDelegate`; switching themes triggers `Activity.recreate()` to re-inflate all views cleanly.

---

## Permissions

| Permission | Reason |
|---|---|
| `INTERNET` | API calls to Firestore and Groq |
| `ACCESS_NETWORK_STATE` | Check connectivity before requests |
| `POST_NOTIFICATIONS` | Booking confirmations and tour reminders (Android 13+) |

---

*Built with Kotlin · Firebase · Groq AI · Material Design 3*
