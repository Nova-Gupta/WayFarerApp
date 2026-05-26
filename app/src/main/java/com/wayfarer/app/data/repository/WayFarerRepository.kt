package com.wayfarer.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.wayfarer.app.data.models.*
import com.wayfarer.app.utils.Resource
import kotlinx.coroutines.tasks.await

class WayFarerRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ── Auth ──────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): Resource<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Login failed")
            Resource.Success(User(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "",
                email = firebaseUser.email ?: ""
            ))
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Resource.Error("Incorrect email or password")
        } catch (e: FirebaseAuthInvalidUserException) {
            Resource.Error("No account found with this email")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Login failed")
        }
    }

    suspend fun register(name: String, email: String, password: String): Resource<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Registration failed")

            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(name).build()
            firebaseUser.updateProfile(profileUpdates).await()

            db.collection("users").document(firebaseUser.uid).set(mapOf(
                "name" to name,
                "email" to email,
                "role" to "user",
                "createdAt" to System.currentTimeMillis()
            )).await()

            Resource.Success(User(id = firebaseUser.uid, name = name, email = email))
        } catch (e: FirebaseAuthUserCollisionException) {
            Resource.Error("An account already exists with this email")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Registration failed")
        }
    }

    fun getCurrentUser(): User? {
        val firebaseUser = auth.currentUser ?: return null
        return User(
            id = firebaseUser.uid,
            name = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: ""
        )
    }

    fun logout() = auth.signOut()

    // ── Tours (bundled static data) ────────────────────────────────────────

    suspend fun getTours(): Resource<List<Tour>> = Resource.Success(TOURS)

    // ── Bookings (Firestore) ───────────────────────────────────────────────

    suspend fun getMyBookings(): Resource<List<Booking>> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Please sign in to view bookings")
        return try {
            val snapshot = db.collection("users").document(uid)
                .collection("bookings")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            val bookings = snapshot.documents.mapNotNull { doc ->
                try {
                    Booking(
                        id = doc.id,
                        tourId = doc.getString("tourId") ?: "",
                        tourName = doc.getString("tourName") ?: "",
                        tourImage = doc.getString("tourImage") ?: "",
                        tourDifficulty = doc.getString("tourDifficulty") ?: "",
                        tourDuration = (doc.getLong("tourDuration") ?: 0L).toInt(),
                        price = doc.getDouble("price") ?: 0.0,
                        checkInDate = doc.getLong("checkInDate") ?: 0L,
                        checkOutDate = doc.getLong("checkOutDate") ?: 0L,
                        hotelId = doc.getString("hotelId") ?: "",
                        hotelName = doc.getString("hotelName") ?: "",
                        hotelPricePerNight = doc.getDouble("hotelPricePerNight") ?: 0.0,
                        nights = (doc.getLong("nights") ?: 0L).toInt(),
                        rooms = (doc.getLong("rooms") ?: 1L).toInt(),
                        guests = (doc.getLong("guests") ?: 1L).toInt(),
                        tripType = doc.getString("tripType") ?: "Solo",
                        paymentMethod = doc.getString("paymentMethod") ?: "Card",
                        totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                        createdAt = doc.getLong("createdAt") ?: 0L,
                        paid = doc.getBoolean("paid") ?: true
                    )
                } catch (e: Exception) { null }
            }
            Resource.Success(bookings)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load bookings")
        }
    }

    // ── Reviews (Firestore) ───────────────────────────────────────────────

    suspend fun getReviews(tourId: String): Resource<List<Review>> {
        return try {
            val snapshot = db.collection("tours").document(tourId)
                .collection("reviews")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get().await()
            val reviews = snapshot.documents.mapNotNull { doc ->
                try {
                    Review(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "Traveller",
                        rating = (doc.getLong("rating") ?: 5L).toInt(),
                        comment = doc.getString("comment") ?: "",
                        createdAt = doc.getLong("createdAt") ?: 0L
                    )
                } catch (e: Exception) { null }
            }
            Resource.Success(reviews)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to load reviews")
        }
    }

    suspend fun submitReview(tourId: String, rating: Int, comment: String): Resource<Boolean> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Please sign in to leave a review")
        val userName = auth.currentUser?.displayName?.ifEmpty { "Traveller" } ?: "Traveller"
        return try {
            db.collection("tours").document(tourId)
                .collection("reviews")
                .add(mapOf(
                    "userId" to uid,
                    "userName" to userName,
                    "rating" to rating,
                    "comment" to comment,
                    "createdAt" to System.currentTimeMillis()
                )).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to submit review")
        }
    }

    suspend fun cancelBooking(bookingId: String): Resource<Boolean> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Not signed in")
        return try {
            db.collection("users").document(uid)
                .collection("bookings").document(bookingId)
                .delete().await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to cancel booking")
        }
    }

    suspend fun createBooking(req: BookingRequest): Resource<Boolean> {
        val uid = auth.currentUser?.uid ?: return Resource.Error("Please sign in to book a tour")
        val tour = TOURS.find { it.id == req.tourId } ?: return Resource.Error("Tour not found")
        return try {
            db.collection("users").document(uid)
                .collection("bookings")
                .add(mapOf(
                    "tourId" to tour.id,
                    "tourName" to tour.name,
                    "tourImage" to tour.image,
                    "tourDifficulty" to tour.difficulty,
                    "tourDuration" to tour.duration,
                    "price" to req.tourPrice,
                    "checkInDate" to req.checkInDate,
                    "checkOutDate" to req.checkOutDate,
                    "hotelId" to req.hotelId,
                    "hotelName" to req.hotelName,
                    "hotelPricePerNight" to req.hotelPricePerNight,
                    "nights" to req.nights,
                    "rooms" to req.rooms,
                    "guests" to req.guests,
                    "tripType" to req.tripType,
                    "paymentMethod" to req.paymentMethod,
                    "totalAmount" to req.totalAmount,
                    "createdAt" to System.currentTimeMillis(),
                    "paid" to true
                )).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Booking failed. Please try again.")
        }
    }

    // ── Hotels (bundled static data) ──────────────────────────────────────

    suspend fun getHotels(): Resource<List<Hotel>> = Resource.Success(HOTELS)

    // ── Locations (Firestore) ─────────────────────────────────────────────

    suspend fun addLocation(
        description: String,
        lat: Double,
        lng: Double,
        category: String,
        whyVisit: String
    ): Resource<Boolean> {
        return try {
            db.collection("locations").add(mapOf(
                "description" to description,
                "latitude" to lat,
                "longitude" to lng,
                "category" to category,
                "whyVisit" to whyVisit,
                "submittedBy" to (auth.currentUser?.uid ?: "anonymous"),
                "createdAt" to System.currentTimeMillis()
            )).await()
            Resource.Success(true)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save location")
        }
    }

    // ── User Profile (Firestore) ──────────────────────────────────────────

    suspend fun updateUserName(newName: String): Resource<User> {
        val firebaseUser = auth.currentUser ?: return Resource.Error("Not logged in")
        return try {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName).build()
            firebaseUser.updateProfile(profileUpdates).await()
            db.collection("users").document(firebaseUser.uid)
                .update("name", newName).await()
            Resource.Success(User(
                id = firebaseUser.uid,
                name = newName,
                email = firebaseUser.email ?: ""
            ))
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update profile")
        }
    }

    // ── Static data ───────────────────────────────────────────────────────

    companion object {

        val TOURS = listOf(
            Tour(id = "1", name = "Classic Italy Tour Package", duration = 10, maxGroupSizeString = "Small Group (Max 12)", difficulty = "City", ratingsAverage = 4.8f, price = 1200.0, image = "https://images.unsplash.com/photo-1523906834658-6e24ef2386f9?auto=format&fit=crop&w=800&q=80", summary = "A journey to visit Rome, Florence, and Venice, with guided tours of famous landmarks like the Colosseum, Vatican City, and the Leaning Tower of Pisa.", route = listOf("Rome", "Siena", "Florence", "Venice", "Milan")),
            Tour(id = "2", name = "Discover Japan Tour Package", duration = 14, maxGroupSizeString = "Medium Group (Max 20)", difficulty = "Culture", ratingsAverage = 4.9f, price = 2500.0, image = "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=800&q=80", summary = "Visit Tokyo, Kyoto, Hiroshima, and the Japanese Alps with activities like sushi making class, a visit to a traditional onsen, and a bullet train ride.", route = listOf("Tokyo", "Hakone", "Kyoto", "Hiroshima", "Osaka")),
            Tour(id = "3", name = "Great Barrier Reef & Sydney", duration = 9, maxGroupSizeString = "Private Tour", difficulty = "Nature", ratingsAverage = 4.7f, price = 3400.0, image = "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?auto=format&fit=crop&w=800&q=80", summary = "A journey that includes a visit to the Great Barrier Reef, a tour of the Sydney Opera House, and a day trip to the Blue Mountains.", route = listOf("Sydney", "Cairns", "Great Barrier Reef", "Blue Mountains")),
            Tour(id = "4", name = "Northern Lights & Glacier", duration = 7, maxGroupSizeString = "Small Group (Max 8)", difficulty = "Nature", ratingsAverage = 4.9f, price = 2200.0, image = "https://images.unsplash.com/photo-1476610182048-b716b8518aae?auto=format&fit=crop&w=800&q=80", summary = "A tour that includes visits to Reykjavik, the Golden Circle, and Vatnajökull National Park, with activities like glacier hiking and ice caving.", route = listOf("Reykjavik", "Vik", "Jokulsarlon", "Golden Circle")),
            Tour(id = "5", name = "Machu Picchu & Amazon", duration = 12, maxGroupSizeString = "Backpacker Group (Max 25)", difficulty = "Adventure", ratingsAverage = 4.8f, price = 1800.0, image = "https://images.unsplash.com/photo-1526392060635-9d6019884377?auto=format&fit=crop&w=800&q=80", summary = "A long journey that includes a visit to Machu Picchu, a stay in the Amazon rainforest, and a tour of the historic city of Cusco.", route = listOf("Lima", "Cusco", "Sacred Valley", "Machu Picchu", "Amazon")),
            Tour(id = "6", name = "Temples & Beaches Tour", duration = 10, maxGroupSizeString = "Medium Group (Max 18)", difficulty = "Relaxation", ratingsAverage = 4.6f, price = 900.0, image = "https://images.unsplash.com/photo-1552465011-b4e21bf6e79a?auto=format&fit=crop&w=800&q=80", summary = "Enjoy historical relics and natural riches by visiting Bangkok, Chiang Mai, and Phuket, with activities like temple tours and island hopping.", route = listOf("Bangkok", "Chiang Mai", "Phuket", "Phi Phi Islands")),
            Tour(id = "7", name = "Halong Bay & Mekong Delta", duration = 8, maxGroupSizeString = "Standard Group (Max 20)", difficulty = "Nature", ratingsAverage = 4.7f, price = 1400.0, image = "https://images.unsplash.com/photo-1528127269322-539801943592?auto=format&fit=crop&w=800&q=80", summary = "A week packed with Vietnam cultural adventures. Enjoy a visit to the UNESCO-listed Halong Bay and a cruise through the Mekong Delta.", route = listOf("Hanoi", "Halong Bay", "Da Nang", "Hoi An", "Ho Chi Minh")),
            Tour(id = "8", name = "India Golden Triangle", duration = 7, maxGroupSizeString = "Private or Small Group", difficulty = "Culture", ratingsAverage = 4.5f, price = 1000.0, image = "https://images.unsplash.com/photo-1524492412937-b28074a5d7da?auto=format&fit=crop&w=800&q=80", summary = "Explore historical sites from Delhi, Agra (home of the Taj Mahal), and Jaipur, with activities like a cooking class and rickshaw ride.", route = listOf("Delhi", "Agra", "Jaipur", "Delhi")),
            Tour(id = "9", name = "Swiss Alps Adventure", duration = 8, maxGroupSizeString = "Small Group (Max 15)", difficulty = "Adventure", ratingsAverage = 4.9f, price = 2800.0, image = "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?auto=format&fit=crop&w=800&q=80", summary = "Experience the breathtaking Swiss Alps with visits to Zurich, Interlaken, and Zermatt. Enjoy skiing, mountain hiking, and scenic train rides.", route = listOf("Zurich", "Lucerne", "Interlaken", "Zermatt", "Geneva")),
            Tour(id = "10", name = "Morocco Desert Expedition", duration = 10, maxGroupSizeString = "Medium Group (Max 16)", difficulty = "Adventure", ratingsAverage = 4.7f, price = 1600.0, image = "https://images.unsplash.com/photo-1489749798305-4fea3ae63d43?auto=format&fit=crop&w=800&q=80", summary = "Journey through Morocco's imperial cities and the Sahara Desert. Experience camel rides, ancient medinas, and traditional Berber culture.", route = listOf("Casablanca", "Marrakech", "Sahara Desert", "Fes", "Chefchaouen")),
            Tour(id = "11", name = "New Zealand Nature Tour", duration = 14, maxGroupSizeString = "Standard Group (Max 20)", difficulty = "Nature", ratingsAverage = 4.8f, price = 3200.0, image = "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=800&q=80", summary = "Discover the stunning landscapes of New Zealand from fjords to geothermal parks. Visit both North and South Islands.", route = listOf("Auckland", "Rotorua", "Wellington", "Queenstown", "Milford Sound")),
            Tour(id = "12", name = "Greek Island Hopping", duration = 9, maxGroupSizeString = "Small Group (Max 14)", difficulty = "Relaxation", ratingsAverage = 4.6f, price = 2100.0, image = "https://images.unsplash.com/photo-1613395877344-13d4a8e0d49e?auto=format&fit=crop&w=800&q=80", summary = "Sail through the beautiful Greek islands including Santorini, Mykonos, and Crete. Enjoy crystal-clear waters and ancient ruins.", route = listOf("Athens", "Santorini", "Mykonos", "Crete", "Rhodes")),
            Tour(id = "13", name = "Canadian Rockies Explorer", duration = 8, maxGroupSizeString = "Small Group (Max 12)", difficulty = "Nature", ratingsAverage = 4.8f, price = 2600.0, image = "https://images.unsplash.com/photo-1503614472-8c93d56e92ce?auto=format&fit=crop&w=800&q=80", summary = "Explore the majestic Canadian Rockies with visits to Banff, Jasper, and Lake Louise. Wildlife viewing and glacier walks included.", route = listOf("Calgary", "Banff", "Lake Louise", "Jasper", "Vancouver")),
            Tour(id = "14", name = "Egypt Nile Cruise", duration = 10, maxGroupSizeString = "Medium Group (Max 25)", difficulty = "Culture", ratingsAverage = 4.7f, price = 1900.0, image = "https://images.unsplash.com/photo-1572252009286-268acec5ca0a?auto=format&fit=crop&w=800&q=80", summary = "Cruise along the Nile River visiting ancient temples and pyramids. Experience the wonders of ancient Egypt in luxury.", route = listOf("Cairo", "Luxor", "Aswan", "Abu Simbel", "Alexandria")),
            Tour(id = "15", name = "Patagonia Wilderness Trek", duration = 12, maxGroupSizeString = "Small Group (Max 10)", difficulty = "Adventure", ratingsAverage = 4.9f, price = 2400.0, image = "https://images.unsplash.com/photo-1517748991590-3830a6c7c630?auto=format&fit=crop&w=800&q=80", summary = "Trek through the stunning landscapes of Patagonia including glaciers, mountains, and pristine lakes in Argentina and Chile.", route = listOf("Buenos Aires", "El Calafate", "Torres del Paine", "Ushuaia")),
            Tour(id = "16", name = "Bali Cultural Retreat", duration = 8, maxGroupSizeString = "Small Group (Max 12)", difficulty = "Relaxation", ratingsAverage = 4.6f, price = 1300.0, image = "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=800&q=80", summary = "Immerse yourself in Balinese culture with temple visits, rice terrace walks, and traditional ceremonies in this tropical paradise.", route = listOf("Denpasar", "Ubud", "Tegallalang", "Seminyak", "Nusa Dua")),
            Tour(id = "17", name = "Norway Fjords Expedition", duration = 9, maxGroupSizeString = "Standard Group (Max 18)", difficulty = "Nature", ratingsAverage = 4.8f, price = 2900.0, image = "https://images.unsplash.com/photo-1601439678777-b2b3c56fa627?auto=format&fit=crop&w=800&q=80", summary = "Sail through Norway's magnificent fjords and visit charming coastal towns. Experience the midnight sun or northern lights depending on season.", route = listOf("Bergen", "Geiranger", "Flam", "Alesund", "Trondheim")),
            Tour(id = "18", name = "South African Safari", duration = 11, maxGroupSizeString = "Small Group (Max 12)", difficulty = "Adventure", ratingsAverage = 4.9f, price = 3100.0, image = "https://images.unsplash.com/photo-1516426122078-c23e76319801?auto=format&fit=crop&w=800&q=80", summary = "Experience the Big Five on safari in Kruger National Park, then explore Cape Town and the Garden Route.", route = listOf("Johannesburg", "Kruger Park", "Cape Town", "Garden Route", "Stellenbosch")),
            Tour(id = "19", name = "Portugal Coastal Journey", duration = 9, maxGroupSizeString = "Medium Group (Max 20)", difficulty = "City", ratingsAverage = 4.5f, price = 1700.0, image = "https://images.unsplash.com/photo-1555881400-74d7acaacd8b?auto=format&fit=crop&w=800&q=80", summary = "Discover Portugal's beautiful coastline, historic cities, and renowned wine regions from Lisbon to Porto.", route = listOf("Lisbon", "Sintra", "Coimbra", "Porto", "Douro Valley")),
            Tour(id = "20", name = "Costa Rica Eco Adventure", duration = 10, maxGroupSizeString = "Small Group (Max 14)", difficulty = "Nature", ratingsAverage = 4.7f, price = 2000.0, image = "https://images.unsplash.com/photo-1519330377309-9ee1c6783348?auto=format&fit=crop&w=800&q=80", summary = "Experience the biodiversity of Costa Rica with rainforest hikes, volcano visits, and beach relaxation.", route = listOf("San Jose", "Arenal Volcano", "Monteverde", "Manuel Antonio", "Tortuguero")),
            Tour(id = "21", name = "Turkey Heritage Trail", duration = 10, maxGroupSizeString = "Medium Group (Max 18)", difficulty = "Culture", ratingsAverage = 4.6f, price = 1500.0, image = "https://images.unsplash.com/photo-1524231757912-21f4fe3a7200?auto=format&fit=crop&w=800&q=80", summary = "Journey through Turkey's rich history from Istanbul's grand bazaars to the fairy chimneys of Cappadocia.", route = listOf("Istanbul", "Cappadocia", "Pamukkale", "Ephesus", "Antalya")),
            Tour(id = "22", name = "Scotland Highlands Tour", duration = 8, maxGroupSizeString = "Small Group (Max 16)", difficulty = "City", ratingsAverage = 4.7f, price = 1900.0, image = "https://images.unsplash.com/photo-1533105079780-92b9be482077?auto=format&fit=crop&w=800&q=80", summary = "Explore the mystical Scottish Highlands with castle visits, whisky tastings, and dramatic landscapes.", route = listOf("Edinburgh", "Loch Ness", "Isle of Skye", "Glencoe", "Stirling")),
            Tour(id = "23", name = "Jordan Ancient Wonders", duration = 7, maxGroupSizeString = "Small Group (Max 12)", difficulty = "Culture", ratingsAverage = 4.8f, price = 1800.0, image = "https://images.unsplash.com/photo-1547234935-80c7145ec969?auto=format&fit=crop&w=800&q=80", summary = "Discover the ancient city of Petra, float in the Dead Sea, and explore the Wadi Rum desert.", route = listOf("Amman", "Petra", "Wadi Rum", "Aqaba", "Dead Sea")),
            Tour(id = "24", name = "Cambodia Temple Discovery", duration = 8, maxGroupSizeString = "Standard Group (Max 20)", difficulty = "Culture", ratingsAverage = 4.5f, price = 1100.0, image = "https://images.unsplash.com/photo-1528181304800-259b08848526?auto=format&fit=crop&w=800&q=80", summary = "Explore the magnificent temples of Angkor Wat and experience Cambodia's rich Khmer heritage and culture.", route = listOf("Phnom Penh", "Siem Reap", "Angkor Wat", "Tonle Sap", "Battambang")),
            Tour(id = "25", name = "Kerala Backwaters & Hill Stations", duration = 8, maxGroupSizeString = "Small Group (Max 12)", difficulty = "Nature", ratingsAverage = 4.8f, price = 850.0, image = "https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=800&q=80", summary = "Experience the serene backwaters of Alleppey and the lush tea plantations of Munnar in 'God's Own Country'.", route = listOf("Kochi", "Munnar", "Thekkady", "Alleppey", "Kochi")),
            Tour(id = "26", name = "Royal Rajasthan Heritage", duration = 12, maxGroupSizeString = "Medium Group (Max 15)", difficulty = "Culture", ratingsAverage = 4.9f, price = 1500.0, image = "https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&w=800&q=80", summary = "A royal journey through the land of kings, visiting majestic forts, palaces, and the vibrant Thar Desert.", route = listOf("Jaipur", "Jodhpur", "Jaisalmer", "Udaipur", "Pushkar")),
            Tour(id = "27", name = "Ladakh High Altitude Adventure", duration = 9, maxGroupSizeString = "Small Group (Max 10)", difficulty = "Adventure", ratingsAverage = 4.9f, price = 1200.0, image = "https://images.unsplash.com/photo-1581791534721-e599df4417f7?auto=format&fit=crop&w=800&q=80", summary = "Explore the breathtaking landscapes of Ladakh, including high mountain passes, crystal clear lakes, and ancient monasteries.", route = listOf("Leh", "Nubra Valley", "Pangong Lake", "Khardung La", "Leh")),
            Tour(id = "28", name = "Goa Beach & Portuguese Heritage", duration = 6, maxGroupSizeString = "Private Tour", difficulty = "Relaxation", ratingsAverage = 4.7f, price = 700.0, image = "https://images.unsplash.com/photo-1512341689857-198e7e2f3ca8?auto=format&fit=crop&w=800&q=80", summary = "Relax on the golden sands of Goa and explore its unique blend of Indian and Portuguese culture and architecture.", route = listOf("Panaji", "Old Goa", "Calangute", "Palolem")),
            Tour(id = "29", name = "Spiritual Varanasi & Ganges", duration = 5, maxGroupSizeString = "Small Group (Max 8)", difficulty = "Culture", ratingsAverage = 4.8f, price = 600.0, image = "https://images.unsplash.com/photo-1561361513-2d000a50f0dc?auto=format&fit=crop&w=800&q=80", summary = "Experience the spiritual heart of India in one of the world's oldest living cities, featuring the famous evening Ganga Aarti.", route = listOf("Varanasi", "Sarnath", "Ganges River")),
            Tour(id = "30", name = "Andaman Tropical Paradise", duration = 7, maxGroupSizeString = "Medium Group (Max 14)", difficulty = "Nature", ratingsAverage = 4.8f, price = 1100.0, image = "https://images.unsplash.com/photo-1589330273594-fade1ee91647?auto=format&fit=crop&w=800&q=80", summary = "Discover the white sandy beaches and turquoise waters of the Andaman Islands, perfect for snorkeling and relaxation.", route = listOf("Port Blair", "Havelock Island", "Neil Island", "Port Blair"))
        )

        val HOTELS = listOf(
            // ── Iconic world hotels ───────────────────────────────────────────
            Hotel("h1",  "The Grand Palace",        "Rome, Italy",           4.9f,  250.0, "https://images.unsplash.com/photo-1551882547-ff43c63efe81?auto=format&fit=crop&w=800&q=80", "Luxury hotel in the heart of Rome, offering exquisite dining and historical elegance."),
            Hotel("h2",  "Harbour View Suite",       "Sydney, Australia",     4.7f,  180.0, "https://images.unsplash.com/photo-1506059612708-99d6c258160e?auto=format&fit=crop&w=800&q=80", "Stunning views of the Sydney Opera House and premium waterfront amenities."),
            Hotel("h3",  "Matterhorn Alpine Lodge",  "Zermatt, Switzerland",  4.8f,  320.0, "https://images.unsplash.com/photo-1590490359683-658d3d23f972?auto=format&fit=crop&w=800&q=80", "Cozy lodge with breathtaking Matterhorn views and ski-in/ski-out access."),
            Hotel("h4",  "Taj Mahal Palace",         "Mumbai, India",         4.9f,  350.0, "https://images.unsplash.com/photo-1582719508461-905c673771fd?auto=format&fit=crop&w=800&q=80", "India's first luxury hotel, a landmark of hospitality and architectural grandeur."),
            Hotel("h5",  "Burj Al Arab Jumeirah",    "Dubai, UAE",            5.0f, 1200.0, "https://images.unsplash.com/photo-1597659840241-37e2b9c2f55f?auto=format&fit=crop&w=800&q=80", "The world's most luxurious hotel, standing on an artificial island with sail-shaped silhouette."),
            Hotel("h6",  "Marina Bay Sands",         "Singapore",             4.8f,  550.0, "https://images.unsplash.com/photo-1542314831-068cd1dbfeeb?auto=format&fit=crop&w=800&q=80", "Iconic hotel featuring the world's largest infinity pool and stunning skyline views."),
            Hotel("h7",  "Ritz Paris",               "Paris, France",         4.9f,  850.0, "https://images.unsplash.com/photo-1551882547-ff43c63efe81?auto=format&fit=crop&w=800&q=80", "Symbol of French refinement and elegance, located in the historic Place Vendôme."),
            Hotel("h8",  "The Plaza Hotel",          "New York, USA",         4.7f,  600.0, "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?auto=format&fit=crop&w=800&q=80", "A legendary New York landmark at the corner of Fifth Avenue and Central Park South."),
            Hotel("h9",  "Atlantis The Palm",        "Dubai, UAE",            4.8f,  450.0, "https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?auto=format&fit=crop&w=800&q=80", "Majestic hotel on the iconic Palm Jumeirah, featuring underwater suites and a water park."),
            Hotel("h10", "Aman Tokyo",               "Tokyo, Japan",          4.9f,  900.0, "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?auto=format&fit=crop&w=800&q=80", "A sanctuary in the heart of Tokyo, blending traditional Japanese style with contemporary luxury."),
            Hotel("h11", "Oberoi Amarvilas",         "Agra, India",           5.0f,  650.0, "https://images.unsplash.com/photo-1548013146-72479768bbf4?auto=format&fit=crop&w=800&q=80", "Unrivaled luxury with every room offering an uninterrupted view of the Taj Mahal."),
            Hotel("h12", "Fairmont Banff Springs",   "Banff, Canada",         4.8f,  500.0, "https://images.unsplash.com/photo-1517748991590-3830a6c7c630?auto=format&fit=crop&w=800&q=80", "A majestic castle in the heart of the Canadian Rockies, offering world-class amenities."),
            // ── Tour-destination hotels ───────────────────────────────────────
            Hotel("h13", "Aurora Northern Lights Hotel", "Reykjavik, Iceland",       4.7f, 280.0, "https://images.unsplash.com/photo-1476610182048-b716b8518aae?auto=format&fit=crop&w=800&q=80", "Modern hotel in central Reykjavik with guided aurora-hunting excursions available nightly."),
            Hotel("h14", "Cusco Inca Heritage Hotel",    "Cusco, Peru",              4.6f, 190.0, "https://images.unsplash.com/photo-1526392060635-9d6019884377?auto=format&fit=crop&w=800&q=80", "Colonial mansion turned boutique hotel near the Plaza de Armas, gateway to Machu Picchu."),
            Hotel("h15", "Bangkok Riverside Palace",     "Bangkok, Thailand",        4.7f, 220.0, "https://images.unsplash.com/photo-1552465011-b4e21bf6e79a?auto=format&fit=crop&w=800&q=80", "Elegant riverside retreat steps from the Grand Palace and Wat Pho temples."),
            Hotel("h16", "Hanoi Heritage Boutique",      "Hanoi, Vietnam",           4.5f, 150.0, "https://images.unsplash.com/photo-1528127269322-539801943592?auto=format&fit=crop&w=800&q=80", "French-colonial boutique hotel in the Old Quarter, ideal base for Halong Bay day trips."),
            Hotel("h17", "Marrakech Desert Riad",        "Marrakech, Morocco",       4.8f, 200.0, "https://images.unsplash.com/photo-1489749798305-4fea3ae63d43?auto=format&fit=crop&w=800&q=80", "Authentic riad with mosaic courtyards and rooftop terrace overlooking the medina."),
            Hotel("h18", "Queenstown Lakeside Lodge",    "Queenstown, New Zealand",  4.7f, 310.0, "https://images.unsplash.com/photo-1469854523086-cc02fe5d8800?auto=format&fit=crop&w=800&q=80", "Luxury lodge on the shores of Lake Wakatipu with panoramic Remarkables mountain views."),
            Hotel("h19", "Santorini Cliffside Villa",    "Santorini, Greece",        4.9f, 420.0, "https://images.unsplash.com/photo-1613395877344-13d4a8e0d49e?auto=format&fit=crop&w=800&q=80", "Cave-style villa carved into the caldera with private plunge pool and sunset views."),
            Hotel("h20", "Cairo Pyramids View Hotel",    "Cairo, Egypt",             4.6f, 180.0, "https://images.unsplash.com/photo-1572252009286-268acec5ca0a?auto=format&fit=crop&w=800&q=80", "Historic hotel with direct views of the Giza Pyramids and easy Nile cruise access."),
            Hotel("h21", "Buenos Aires Grand Hotel",     "Buenos Aires, Argentina",  4.7f, 250.0, "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80", "Art-deco landmark hotel in the heart of Palermo, ideal for Patagonia expedition base."),
            Hotel("h22", "Ubud Jungle Retreat",          "Ubud, Indonesia",          4.8f, 175.0, "https://images.unsplash.com/photo-1537996194471-e657df975ab4?auto=format&fit=crop&w=800&q=80", "Eco-resort nestled in lush rice terraces with daily yoga, spa, and cultural workshops."),
            Hotel("h23", "Bergen Fjord Hotel",           "Bergen, Norway",           4.7f, 340.0, "https://images.unsplash.com/photo-1601439678777-b2b3c56fa627?auto=format&fit=crop&w=800&q=80", "Boutique hotel in Bergen's colourful Bryggen wharf area, perfect for fjord explorations."),
            Hotel("h24", "Cape Town Waterfront Hotel",   "Cape Town, South Africa",  4.8f, 300.0, "https://images.unsplash.com/photo-1516426122078-c23e76319801?auto=format&fit=crop&w=800&q=80", "Contemporary hotel at the V&A Waterfront with Table Mountain views and safari transfers."),
            Hotel("h25", "Lisbon Heritage Boutique",     "Lisbon, Portugal",         4.7f, 230.0, "https://images.unsplash.com/photo-1555881400-74d7acaacd8b?auto=format&fit=crop&w=800&q=80", "Charming azulejo-tiled boutique hotel in historic Alfama, close to Belém Tower."),
            Hotel("h26", "Istanbul Bosphorus Palace",    "Istanbul, Turkey",         4.8f, 290.0, "https://images.unsplash.com/photo-1524231757912-21f4fe3a7200?auto=format&fit=crop&w=800&q=80", "Ottoman-inspired palace hotel on the Bosphorus shores, minutes from the Grand Bazaar."),
            Hotel("h27", "Edinburgh Castle View Hotel",  "Edinburgh, Scotland",      4.7f, 260.0, "https://images.unsplash.com/photo-1533105079780-92b9be482077?auto=format&fit=crop&w=800&q=80", "Georgian townhouse hotel on the Royal Mile with panoramic Edinburgh Castle views."),
            Hotel("h28", "Petra Rose-Red Hotel",         "Amman, Jordan",            4.6f, 210.0, "https://images.unsplash.com/photo-1547234935-80c7145ec969?auto=format&fit=crop&w=800&q=80", "Boutique hotel in Amman's city centre offering full-day Petra and Wadi Rum excursions."),
            Hotel("h29", "Angkor Heritage Hotel",        "Siem Reap, Cambodia",      4.7f, 160.0, "https://images.unsplash.com/photo-1528181304800-259b08848526?auto=format&fit=crop&w=800&q=80", "Khmer-inspired resort 10 minutes from Angkor Wat with sunrise temple tour packages."),
            Hotel("h30", "Kochi Backwaters Resort",      "Kochi, India",             4.7f, 195.0, "https://images.unsplash.com/photo-1602216056096-3b40cc0c9944?auto=format&fit=crop&w=800&q=80", "Waterfront resort in Fort Kochi with houseboat transfers and Ayurvedic spa treatments."),
            Hotel("h31", "Jaipur Royal Haveli",          "Jaipur, India",            4.8f, 230.0, "https://images.unsplash.com/photo-1599661046289-e31897846e41?auto=format&fit=crop&w=800&q=80", "Restored 18th-century haveli with courtyards, elephant rides, and city palace views."),
            Hotel("h32", "Leh Himalayan Retreat",        "Leh, India",               4.6f, 160.0, "https://images.unsplash.com/photo-1581791534721-e599df4417f7?auto=format&fit=crop&w=800&q=80", "Mud-brick retreat at 3500 m altitude with monastery views and high-altitude trekking base."),
            Hotel("h33", "Goa Beach Resort",             "Panaji, India",            4.6f, 185.0, "https://images.unsplash.com/photo-1512341689857-198e7e2f3ca8?auto=format&fit=crop&w=800&q=80", "Beachfront resort in North Goa with Portuguese colonial architecture and sunset cruises."),
            Hotel("h34", "Varanasi Ganga View Hotel",    "Varanasi, India",          4.5f, 140.0, "https://images.unsplash.com/photo-1561361513-2d000a50f0dc?auto=format&fit=crop&w=800&q=80", "Heritage haveli on the ghats with rooftop Ganga views and evening Aarti ceremony access."),
            Hotel("h35", "Andaman Sea Resort",           "Port Blair, India",        4.7f, 200.0, "https://images.unsplash.com/photo-1589330273594-fade1ee91647?auto=format&fit=crop&w=800&q=80", "Tropical resort facing the Andaman Sea with snorkelling packages to Havelock Island."),
            Hotel("h36", "San Jose Eco Lodge",           "San Jose, Costa Rica",     4.6f, 195.0, "https://images.unsplash.com/photo-1519330377309-9ee1c6783348?auto=format&fit=crop&w=800&q=80", "Eco-certified lodge on the outskirts of San Jose, ideal base for Arenal and cloud forest trips.")
        )

        fun getHotelsForTour(tourId: String): List<Hotel> {
            val tour = TOURS.find { it.id == tourId } ?: return HOTELS
            val routeLower = tour.route?.map { it.trim().lowercase() } ?: return HOTELS

            val matched = HOTELS.filter { hotel ->
                val hotelCity = hotel.location.split(",")[0].trim().lowercase()
                routeLower.any { city ->
                    city == hotelCity || city.contains(hotelCity) || hotelCity.contains(city)
                }
            }
            return if (matched.isNotEmpty()) matched else HOTELS
        }
    }
}
