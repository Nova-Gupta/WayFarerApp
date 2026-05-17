# WayFarer — Your Ultimate Travel Companion

WayFarer is a premium, native Android application designed to streamline travel planning, booking, and exploration. Built with Kotlin and following modern Android development practices (MVVM), it offers an innovative experience integrated with AI assistance.

---

## 🌟 Comprehensive Features

### 🏨 Travel & Bookings
- **Global & Indian Tours:** A curated catalog of 30+ high-end tour packages, including classic international destinations and premium Indian heritage sites (Kerala, Rajasthan, Ladakh, etc.).
- **Smart Booking Engine:** Instant booking capability directly from the tour detail screen.
- **Persistent Booking History:** A dedicated "My Bookings" tab that stores your travel history securely on your device, ensuring your data stays even after restarting the app.
- **Luxury Hotel Directory:** Explore a hand-picked list of 12 world-class hotels with detailed descriptions and pricing.

### 🤖 AI Integration
- **WayFarer AI Assistant:** A built-in chatbot powered by **Google Gemini 2.5 Flash**.
- **Context-Aware Conversations:** The AI remembers your previous messages and provides personalized travel advice, booking tips, and destination information.
- **Smart UI:** A dedicated chat interface with auto-hiding navigation for a focused conversation experience.

### 👤 Profile & Personalization
- **Account Management:** Secure JWT-based authentication flow (Login/Register).
- **Dynamic Profile Editing:** Update your personal information instantly from within the app.
- **Help & Support:** A comprehensive support hub featuring:
    - Frequently Asked Questions (FAQ).
    - One-tap Email support.
    - One-tap Phone support.
- **Dark Mode:** Full native support for system-wide dark mode for enhanced comfort and aesthetics.

### 📍 Exploration
- **Location Contributor:** Users can recommend and add new travel spots to the platform with descriptions and coordinates.

---

## ⚙️ How It Works (Technical Overview)

### 🏗 Architecture (MVVM)
The app follows the **Model-View-ViewModel** architecture to ensure a clean separation of concerns:
- **Repository Pattern:** Centralizes data access logic. While the app is currently in "Demo Mode" for local testing, the repository is designed to easily toggle between local mock data and a live Node.js/MongoDB backend.
- **LiveData:** Ensures the UI updates automatically whenever the data changes.

### 💾 Data Persistence
Unlike simple mock apps, WayFarer features **JSON-based local persistence**:
- **Secure Storage:** Sensitive data and session tokens are stored using `EncryptedSharedPreferences` (AES-256 encryption).
- **Bookings Storage:** When a user books a tour, the repository converts the object into a JSON string and saves it locally, allowing the data to persist across app sessions without a backend.

### 🛡 Security & API Management
- **Secrets Management:** The Gemini AI API key is never hardcoded. It is stored in `local.properties` and injected into the build via `BuildConfig`, keeping your credentials safe from version control leaks.
- **Retrofit Interceptors:** Includes a pre-configured Auth Interceptor to automatically attach JWT tokens to future API requests.

### 🖼 Image Loading
- **Glide Integration:** Uses the high-performance Glide library to load and cache verified, high-resolution imagery from Unsplash, ensuring a fast and visually stunning experience.

---

## 🛠 Tech Stack

| Component | Technology |
|---|---|
| **Language** | Kotlin |
| **Architecture** | MVVM |
| **Networking** | Retrofit 2, OkHttp 4, GSON |
| **AI Engine** | Google Gemini 2.5 Flash (v1beta) |
| **Navigation** | Jetpack Navigation Component |
| **Security** | EncryptedSharedPreferences (Security Crypto 1.1.0) |
| **Workers** | WorkManager (for background reminders/notifications) |
| **UI** | Material Design 3, ViewBinding, XML |

---

## 📥 Setup & Build

### Prerequisites
- Android Studio Ladybug+
- Gemini API Key ([Get one here](https://aistudio.google.com/))

### Configuration
1. **Clone & Open:** Import the project into Android Studio.
2. **Add API Key:** Open `local.properties` in your root folder and add:
   ```properties
   GEMINI_API_KEY=your_key_here
   ```
3. **Build APK:** 
   - Go to `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
   - The file will be located at `app/build/outputs/apk/debug/app-debug.apk`.

---

*Explore the world with WayFarer — Designed for the modern traveler.*
