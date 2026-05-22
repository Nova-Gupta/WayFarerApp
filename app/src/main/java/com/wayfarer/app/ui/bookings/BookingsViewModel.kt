package com.wayfarer.app.ui.bookings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wayfarer.app.data.models.Booking
import com.wayfarer.app.data.repository.WayFarerRepository
import com.wayfarer.app.utils.Resource
import kotlinx.coroutines.launch

class BookingsViewModel : ViewModel() {

    private val repo = WayFarerRepository()

    private val _bookingsState = MutableLiveData<Resource<List<Booking>>>()
    val bookingsState: LiveData<Resource<List<Booking>>> = _bookingsState

    fun loadBookings() {
        _bookingsState.value = Resource.Loading
        viewModelScope.launch {
            _bookingsState.value = repo.getMyBookings()
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            repo.cancelBooking(bookingId)
            loadBookings()
        }
    }
}
