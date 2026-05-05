package com.wayfarer.app.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wayfarer.app.data.repository.WayFarerRepository
import com.wayfarer.app.utils.Resource
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    private val repo = WayFarerRepository()

    private val _bookingState = MutableLiveData<Resource<Boolean>>()
    val bookingState: LiveData<Resource<Boolean>> = _bookingState

    fun bookTour(tourId: String, price: Double) {
        _bookingState.value = Resource.Loading
        viewModelScope.launch {
            _bookingState.value = repo.createBooking(tourId, price)
        }
    }
}
