package com.wayfarer.app.ui.hotels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wayfarer.app.data.models.Hotel
import com.wayfarer.app.data.repository.WayFarerRepository
import com.wayfarer.app.utils.Resource
import kotlinx.coroutines.launch

class HotelViewModel : ViewModel() {

    private val repo = WayFarerRepository()

    private val _hotels = MutableLiveData<Resource<List<Hotel>>>()
    val hotels: LiveData<Resource<List<Hotel>>> = _hotels

    init {
        fetchHotels()
    }

    fun fetchHotels() {
        _hotels.value = Resource.Loading
        viewModelScope.launch {
            _hotels.value = repo.getHotels()
        }
    }
}
