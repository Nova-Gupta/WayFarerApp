package com.wayfarer.app.data.repository

import com.wayfarer.app.data.api.RetrofitClient
import com.wayfarer.app.data.models.*
import com.wayfarer.app.utils.Resource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WayFarerRepository {

    private val api = RetrofitClient.api
    private val sessionManager = RetrofitClient.sessionManager
    private val gson = Gson()

    // In-memory storage for bookings in Demo Mode, backed by SharedPreferences
    private companion object {
        private var userBookings: MutableList<Booking>? = null
    }

    // ── Auth ──────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): Resource<AuthResponse> {
        // Mocked login for testing without backend
        return if (email.isNotEmpty() && password.isNotEmpty()) {
            Resource.Success(
                AuthResponse(
                    id = "mock_user_123",
                    name = "Test User",
                    email = email,
                    role = "user",
                    token = "mock_token_xyz",
                    message = "Login successful"
                )
            )
        } else {
            Resource.Error("Invalid email or password")
        }
    }

    suspend fun register(name: String, email: String, password: String): Resource<AuthResponse> {
        // Mocked registration for testing without backend
        return if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
            Resource.Success(
                AuthResponse(
                    id = "mock_user_123",
                    name = name,
                    email = email,
                    role = "user",
                    token = "mock_token_xyz",
                    message = "Account created successfully"
                )
            )
        } else {
            Resource.Error("All fields are required")
        }
    }

    // ── Tours ──────────────────────────────────────────────────────────────

    suspend fun getTours(): Resource<List<Tour>> {
        val tours = listOf(
            Tour(
                mongoId = "1", id = "1", name = "Classic Italy Tour Package", duration = 10,
                maxGroupSizeString = "Small Group (Max 12)", difficulty = "City",
                ratingsAverage = 4.8f, price = 1200.0, image = "https://images.unsplash.com/photo-1523906834658-6e24ef2386f9?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "A journey to visit Rome, Florence, and Venice, with guided tours of famous landmarks like the Colosseum, Vatican City, and the Leaning Tower of Pisa.",
                route = listOf("Rome", "Siena", "Florence", "Venice", "Milan")
            ),
            Tour(
                mongoId = "2", id = "2", name = "Discover Japan Tour Package", duration = 14,
                maxGroupSizeString = "Medium Group (Max 20)", difficulty = "Culture",
                ratingsAverage = 4.9f, price = 2500.0, image = "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Visit Tokyo, Kyoto, Hiroshima, and the Japanese Alps with activities like sushi making class, a visit to a traditional onsen, and a bullet train ride.",
                route = listOf("Tokyo", "Hakone", "Kyoto", "Hiroshima", "Osaka")
            ),
            Tour(
                mongoId = "3", id = "3", name = "Great Barrier Reef & Sydney", duration = 9,
                maxGroupSizeString = "Private Tour", difficulty = "Nature",
                ratingsAverage = 4.7f, price = 3400.0, image = "https://images.unsplash.com/photo-1506973035872-a4ec16b8e8d9?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "A journey that includes a visit to the Great Barrier Reef, a tour of the Sydney Opera House, and a day trip to the Blue Mountains.",
                route = listOf("Sydney", "Cairns", "Great Barrier Reef", "Blue Mountains")
            ),
            Tour(
                mongoId = "4", id = "4", name = "Northern Lights & Glacier", duration = 7,
                maxGroupSizeString = "Small Group (Max 8)", difficulty = "Nature",
                ratingsAverage = 4.9f, price = 2200.0, image = "https://images.unsplash.com/photo-1476610182048-b716b8518aae?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "A tour that includes visits to Reykjavik, the Golden Circle, and Vatnajökull National Park, with activities like glacier hiking and ice caving.",
                route = listOf("Reykjavik", "Vik", "Jokulsarlon", "Golden Circle")
            ),
            Tour(
                mongoId = "5", id = "5", name = "Machu Picchu & Amazon", duration = 12,
                maxGroupSizeString = "Backpacker Group (Max 25)", difficulty = "Adventure",
                ratingsAverage = 4.8f, price = 1800.0, image = "https://images.unsplash.com/photo-1526392060635-9d6019884377?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "A long journey that includes a visit to Machu Picchu, a stay in the Amazon rainforest, and a tour of the historic city of Cusco.",
                route = listOf("Lima", "Cusco", "Sacred Valley", "Machu Picchu", "Amazon")
            ),
            Tour(
                mongoId = "6", id = "6", name = "Temples & Beaches Tour", duration = 10,
                maxGroupSizeString = "Medium Group (Max 18)", difficulty = "Relaxation",
                ratingsAverage = 4.6f, price = 900.0, image = "https://images.unsplash.com/photo-1552465011-b4e21bf6e79a?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Enjoy historical relics and natural riches by visiting Bangkok, Chiang Mai, and Phuket, with activities like temple tours and island hopping.",
                route = listOf("Bangkok", "Chiang Mai", "Phuket", "Phi Phi Islands")
            ),
            Tour(
                mongoId = "7", id = "7", name = "Halong Bay & Mekong Delta", duration = 8,
                maxGroupSizeString = "Standard Group (Max 20)", difficulty = "Nature",
                ratingsAverage = 4.7f, price = 1400.0, image = "https://images.unsplash.com/photo-1528127269322-539801943592?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "A week packed with Vietnam cultural adventures. Enjoy a visit to the UNESCO-listed Halong Bay and a cruise through the Mekong Delta.",
                route = listOf("Hanoi", "Halong Bay", "Da Nang", "Hoi An", "Ho Chi Minh")
            ),
            Tour(
                mongoId = "8", id = "8", name = "India Golden Triangle", duration = 7,
                maxGroupSizeString = "Private or Small Group", difficulty = "Culture",
                ratingsAverage = 4.5f, price = 1000.0, image = "https://images.unsplash.com/photo-1524492412937-b28074a5d7da?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Explore historical sites from Delhi, Agra (home of the Taj Mahal), and Jaipur, with activities like a cooking class and rickshaw ride.",
                route = listOf("Delhi", "Agra", "Jaipur", "Delhi")
            ),
            Tour(
                mongoId = "9", id = "9", name = "Swiss Alps Adventure", duration = 8,
                maxGroupSizeString = "Small Group (Max 15)", difficulty = "Adventure",
                ratingsAverage = 4.9f, price = 2800.0, image = "https://images.unsplash.com/photo-1531366936337-7c912a4589a7?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Experience the breathtaking Swiss Alps with visits to Zurich, Interlaken, and Zermatt. Enjoy skiing, mountain hiking, and scenic train rides.",
                route = listOf("Zurich", "Lucerne", "Interlaken", "Zermatt", "Geneva")
            ),
            Tour(
                mongoId = "10", id = "10", name = "Morocco Desert Expedition", duration = 10,
                maxGroupSizeString = "Medium Group (Max 16)", difficulty = "Adventure",
                ratingsAverage = 4.7f, price = 1600.0, image = "https://images.unsplash.com/photo-1489749798305-4fea3ae63d43?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Journey through Morocco's imperial cities and the Sahara Desert. Experience camel rides, ancient medinas, and traditional Berber culture.",
                route = listOf("Casablanca", "Marrakech", "Sahara Desert", "Fes", "Chefchaouen")
            ),
            Tour(
                mongoId = "11", id = "11", name = "New Zealand Nature Tour", duration = 14,
                maxGroupSizeString = "Standard Group (Max 20)", difficulty = "Nature",
                ratingsAverage = 4.8f, price = 3200.0, image = "https://images.unsplash.com/photo-1469521669194-babb92d84d3e?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Discover the stunning landscapes of New Zealand from fjords to geothermal parks. Visit both North and South Islands.",
                route = listOf("Auckland", "Rotorua", "Wellington", "Queenstown", "Milford Sound")
            ),
            Tour(
                mongoId = "12", id = "12", name = "Greek Island Hopping", duration = 9,
                maxGroupSizeString = "Small Group (Max 14)", difficulty = "Relaxation",
                ratingsAverage = 4.6f, price = 2100.0, image = "https://images.unsplash.com/photo-1613395877344-13d4a8e0d49e?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Sail through the beautiful Greek islands including Santorini, Mykonos, and Crete. Enjoy crystal-clear waters and ancient ruins.",
                route = listOf("Athens", "Santorini", "Mykonos", "Crete", "Rhodes")
            ),
            Tour(
                mongoId = "13", id = "13", name = "Canadian Rockies Explorer", duration = 8,
                maxGroupSizeString = "Small Group (Max 12)", difficulty = "Nature",
                ratingsAverage = 4.8f, price = 2600.0, image = "https://images.unsplash.com/photo-1503614472-8c93d56e92ce?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Explore the majestic Canadian Rockies with visits to Banff, Jasper, and Lake Louise. Wildlife viewing and glacier walks included.",
                route = listOf("Calgary", "Banff", "Lake Louise", "Jasper", "Vancouver")
            ),
            Tour(
                mongoId = "14", id = "14", name = "Egypt Nile Cruise", duration = 10,
                maxGroupSizeString = "Medium Group (Max 25)", difficulty = "Culture",
                ratingsAverage = 4.7f, price = 1900.0, image = "https://images.unsplash.com/photo-1572252009286-268acec5ca0a?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Cruise along the Nile River visiting ancient temples and pyramids. Experience the wonders of ancient Egypt in luxury.",
                route = listOf("Cairo", "Luxor", "Aswan", "Abu Simbel", "Alexandria")
            ),
            Tour(
                mongoId = "15", id = "15", name = "Patagonia Wilderness Trek", duration = 12,
                maxGroupSizeString = "Small Group (Max 10)", difficulty = "Adventure",
                ratingsAverage = 4.9f, price = 2400.0, image = "https://images.unsplash.com/photo-1526392060635-9d6019884377?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Trek through the stunning landscapes of Patagonia including glaciers, mountains, and pristine lakes in Argentina and Chile.",
                route = listOf("Buenos Aires", "El Calafate", "Torres del Paine", "Ushuaia")
            ),
            Tour(
                mongoId = "16", id = "16", name = "Bali Cultural Retreat", duration = 8,
                maxGroupSizeString = "Small Group (Max 12)", difficulty = "Relaxation",
                ratingsAverage = 4.6f, price = 1300.0, image = "https://images.unsplash.com/photo-1537996194471-e657df975ab4?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Immerse yourself in Balinese culture with temple visits, rice terrace walks, and traditional ceremonies in this tropical paradise.",
                route = listOf("Denpasar", "Ubud", "Tegallalang", "Seminyak", "Nusa Dua")
            ),
            Tour(
                mongoId = "17", id = "17", name = "Norway Fjords Expedition", duration = 9,
                maxGroupSizeString = "Standard Group (Max 18)", difficulty = "Nature",
                ratingsAverage = 4.8f, price = 2900.0, image = "https://images.unsplash.com/photo-1601439678777-b2b3c56fa627?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Sail through Norway's magnificent fjords and visit charming coastal towns. Experience the midnight sun or northern lights depending on season.",
                route = listOf("Bergen", "Geiranger", "Flam", "Alesund", "Trondheim")
            ),
            Tour(
                mongoId = "18", id = "18", name = "South African Safari", duration = 11,
                maxGroupSizeString = "Small Group (Max 12)", difficulty = "Adventure",
                ratingsAverage = 4.9f, price = 3100.0, image = "https://images.unsplash.com/photo-1516426122078-c23e76319801?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Experience the Big Five on safari in Kruger National Park, then explore Cape Town and the Garden Route.",
                route = listOf("Johannesburg", "Kruger Park", "Cape Town", "Garden Route", "Stellenbosch")
            ),
            Tour(
                mongoId = "19", id = "19", name = "Portugal Coastal Journey", duration = 9,
                maxGroupSizeString = "Medium Group (Max 20)", difficulty = "City",
                ratingsAverage = 4.5f, price = 1700.0, image = "https://images.unsplash.com/photo-1555881400-74d7acaacd8b?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Discover Portugal's beautiful coastline, historic cities, and renowned wine regions from Lisbon to Porto.",
                route = listOf("Lisbon", "Sintra", "Coimbra", "Porto", "Douro Valley")
            ),
            Tour(
                mongoId = "20", id = "20", name = "Costa Rica Eco Adventure", duration = 10,
                maxGroupSizeString = "Small Group (Max 14)", difficulty = "Nature",
                ratingsAverage = 4.7f, price = 2000.0, image = "https://images.unsplash.com/photo-1558769132-cb1aea57c9c6?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Experience the biodiversity of Costa Rica with rainforest hikes, volcano visits, and beach relaxation.",
                route = listOf("San Jose", "Arenal Volcano", "Monteverde", "Manuel Antonio", "Tortuguero")
            ),
            Tour(
                mongoId = "21", id = "21", name = "Turkey Heritage Trail", duration = 10,
                maxGroupSizeString = "Medium Group (Max 18)", difficulty = "Culture",
                ratingsAverage = 4.6f, price = 1500.0, image = "https://images.unsplash.com/photo-1524231757912-21f4fe3a7200?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Journey through Turkey's rich history from Istanbul's grand bazaars to the fairy chimneys of Cappadocia.",
                route = listOf("Istanbul", "Cappadocia", "Pamukkale", "Ephesus", "Antalya")
            ),
            Tour(
                mongoId = "22", id = "22", name = "Scotland Highlands Tour", duration = 8,
                maxGroupSizeString = "Small Group (Max 16)", difficulty = "City",
                ratingsAverage = 4.7f, price = 1900.0, image = "https://images.unsplash.com/photo-1606150584169-d501c90067df?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Explore the mystical Scottish Highlands with castle visits, whisky tastings, and dramatic landscapes.",
                route = listOf("Edinburgh", "Loch Ness", "Isle of Skye", "Glencoe", "Stirling")
            ),
            Tour(
                mongoId = "23", id = "23", name = "Jordan Ancient Wonders", duration = 7,
                maxGroupSizeString = "Small Group (Max 12)", difficulty = "Culture",
                ratingsAverage = 4.8f, price = 1800.0, image = "https://images.unsplash.com/photo-1590668057042-6c3b82106d50?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Discover the ancient city of Petra, float in the Dead Sea, and explore the Wadi Rum desert.",
                route = listOf("Amman", "Petra", "Wadi Rum", "Aqaba", "Dead Sea")
            ),
            Tour(
                mongoId = "24", id = "24", name = "Cambodia Temple Discovery", duration = 8,
                maxGroupSizeString = "Standard Group (Max 20)", difficulty = "Culture",
                ratingsAverage = 4.5f, price = 1100.0, image = "https://images.unsplash.com/photo-1528181304800-259b08848526?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80",
                summary = "Explore the magnificent temples of Angkor Wat and experience Cambodia's rich Khmer heritage and culture.",
                route = listOf("Phnom Penh", "Siem Reap", "Angkor Wat", "Tonle Sap", "Battambang")
            )
        )
        return Resource.Success(tours)
    }

    // ── Bookings ──────────────────────────────────────────────────────────

    private suspend fun loadBookings() {
        if (userBookings == null) {
            val json = sessionManager.getDemoBookings()
            if (json != null) {
                val type = object : TypeToken<MutableList<Booking>>() {}.type
                userBookings = try {
                    gson.fromJson(json, type)
                } catch (e: Exception) {
                    mutableListOf()
                }
            } else {
                userBookings = mutableListOf()
                // Add initial mock bookings if empty
                val toursResult = try { getTours() } catch (e: Exception) { Resource.Error("") }
                if (toursResult is Resource.Success) {
                    val tours = toursResult.data
                    if (tours.size >= 2) {
                        userBookings?.add(Booking(id = "b1", tour = tours[0], user = null, price = tours[0].price, createdAt = "2026-05-01T10:00:00Z"))
                        userBookings?.add(Booking(id = "b2", tour = tours[1], user = null, price = tours[1].price, createdAt = "2026-05-02T14:30:00Z"))
                        saveBookings()
                    }
                }
            }
        }
    }

    private fun saveBookings() {
        userBookings?.let {
            sessionManager.saveDemoBookings(gson.toJson(it))
        }
    }

    suspend fun getMyBookings(): Resource<List<Booking>> {
        loadBookings()
        return Resource.Success(userBookings.orEmpty().toList())
    }

    suspend fun createBooking(tourId: String, price: Double): Resource<Boolean> {
        loadBookings()
        val toursResult = getTours()
        val tours = if (toursResult is Resource.Success) toursResult.data else emptyList()
        val selectedTour = tours.find { it.id == tourId || it.mongoId == tourId }
        
        if (selectedTour != null) {
            userBookings?.add(0, Booking(
                id = "b_${System.currentTimeMillis()}",
                tour = selectedTour,
                user = null,
                price = price,
                createdAt = java.util.Date().toString()
            ))
            saveBookings()
            return Resource.Success(true)
        }
        return Resource.Error("Tour not found")
    }

    // ── Hotels ──────────────────────────────────────────────────────────

    suspend fun getHotels(): Resource<List<Hotel>> {
        val mockHotels = listOf(
            Hotel("h1", "Grand Palace", "Rome, Italy", 4.9f, 250.0, "https://images.unsplash.com/photo-1566073771259-6a8506099945?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80", "Luxury hotel in the heart of Rome."),
            Hotel("h2", "Ocean View", "Sydney, Australia", 4.7f, 180.0, "https://images.unsplash.com/photo-1520250497591-112f2f40a3f4?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80", "Stunning views of the Sydney Opera House."),
            Hotel("h3", "Mountain Lodge", "Zermatt, Switzerland", 4.8f, 320.0, "https://images.unsplash.com/photo-1551882547-ff43c63efe81?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80", "Cozy lodge with Matterhorn views.")
        )
        return try {
            val response = api.getHotels()
            if (response.isSuccessful && response.body() != null) {
                Resource.Success(response.body()!!)
            } else {
                Resource.Success(mockHotels)
            }
        } catch (e: Exception) {
            Resource.Success(mockHotels)
        }
    }

    // ── Locations ────────────────────────────────────────────────────────

    suspend fun addLocation(
        description: String,
        lat: Double,
        lng: Double,
        category: String,
        whyVisit: String
    ): Resource<Boolean> {
        // Mocked for demo mode
        return Resource.Success(true)
    }
}
