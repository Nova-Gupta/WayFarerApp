package com.wayfarer.app.ui.location

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wayfarer.app.data.repository.WayFarerRepository
import com.wayfarer.app.utils.Resource
import kotlinx.coroutines.launch

class LocationViewModel : ViewModel() {
    private val repo = WayFarerRepository()

    private val _addLocationState = MutableLiveData<Resource<Boolean>>()
    val addLocationState: LiveData<Resource<Boolean>> = _addLocationState

    fun addLocation(description: String, lat: Double, lng: Double, category: String, whyVisit: String) {
        _addLocationState.value = Resource.Loading
        viewModelScope.launch {
            _addLocationState.value = repo.addLocation(description, lat, lng, category, whyVisit)
        }
    }
}
