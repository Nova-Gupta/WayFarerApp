# WayFarer — Your Ultimate Travel Companion

WayFarer is a modern, native Android application designed to make travel planning, booking, and exploration seamless. Built with Kotlin and following the MVVM architecture, it offers a premium user experience with integrated AI assistance.

---

## 🚀 Key Features

### 🏨 Travel Management
- **Explore Tours:** Browse a wide range of curated travel packages with detailed itineraries and pricing.
- **Smart Booking:** Book your favorite tours instantly.
- **Hotel Discovery:** Find the best places to stay at your destination.
- **My Bookings:** Keep track of all your upcoming and past adventures with persistent local storage.

### 🤖 AI & Innovation
- **AI Assistant:** Integrated **Google Gemini AI** chatbot to help with travel queries, booking tips, and trip planning.
- **Location Explorer:** Add and discover new travel spots with coordinates and descriptions.

### 👤 User Experience
- **Secure Authentication:** JWT-based login and registration system.
- **Profile Management:** Personalize your profile, update your name, and manage your preferences.
- **Dark Mode Support:** Full support for system-wide dark mode for comfortable night-time browsing.
- **Help & Support:** Integrated FAQ and quick-contact options (Email/Call).

---

## 🛠 Tech Stack

| Component | Technology |
|---|---|
| **Language** | Kotlin |
| **Architecture** | MVVM (ViewModel, LiveData, Repository) |
| **UI** | Material Design 3, ViewBinding, XML Layouts |
| **Networking** | Retrofit 2, OkHttp 4, Gson |
| **AI Engine** | Google Gemini 2.5 Flash |
| **Navigation** | Jetpack Navigation Component |
| **Storage** | EncryptedSharedPreferences (Security Crypto) |
| **Images** | Glide |
| **Async** | Kotlin Coroutines |

---

## 📥 Getting Started

### Prerequisites
- Android Studio Ladybug or later
- JDK 17 or later

### Setup Instructions

1. **Clone the Repository:**
   ```bash
   git clone https://github.com/Nova-Gupta/WayFarerApp.git
   ```

2. **Configure Secrets:**
   WayFarer uses a secure method to store API keys. You must add your Gemini API key to your local environment:
   - Open the `local.properties` file in the root directory.
   - Add the following line:
     ```properties
     GEMINI_API_KEY=your_gemini_api_key_here
     ```
   *(Note: This file is ignored by Git to keep your key safe.)*

3. **Open & Build:**
   - Open the project in Android Studio.
   - Wait for Gradle sync to finish.
   - Click **Run** to install on your emulator or physical device.

---

## 📂 Project Structure

```
app/src/main/java/com/wayfarer/app/
├── data/
│   ├── api/          # Retrofit interfaces (WayFarer, Gemini)
│   ├── models/       # Data classes
│   └── repository/   # Data handling & logic (includes Demo Mode storage)
├── ui/
│   ├── auth/         # Login, Register, Splash
│   ├── home/         # Dashboard & Main Activity
│   ├── detail/       # Tour Details
│   ├── bookings/     # User Bookings
│   ├── hotels/       # Hotel Listings
│   ├── location/     # Add Location feature
│   ├── chat/         # AI Chatbot Assistant
│   └── profile/      # Profile, Edit Profile, Help & Support
└── utils/            # SessionManager, NotificationHelper, Resource wrappers
```

---

## 🔒 Security Note
This project uses `EncryptedSharedPreferences` to store sensitive data like JWT tokens. API keys are managed through `BuildConfig` and `local.properties` to ensure they are never leaked in the version control system.

---

## 🤝 Contributing
1. Fork the project.
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the Branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---

*Explore the world with WayFarer.*
