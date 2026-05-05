package com.wayfarer.app.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wayfarer.app.data.models.Tour
import com.wayfarer.app.data.repository.WayFarerRepository
import com.wayfarer.app.utils.Resource
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repo = WayFarerRepository()

    private val _toursState = MutableLiveData<Resource<List<Tour>>>()
    val toursState: LiveData<Resource<List<Tour>>> = _toursState

    fun loadTours() {
        _toursState.value = Resource.Loading
        viewModelScope.launch {
            _toursState.value = repo.getTours()
        }
    }
}
