package com.wayfarer.app.ui.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wayfarer.app.data.models.BookingRequest
import com.wayfarer.app.data.models.Review
import com.wayfarer.app.data.repository.WayFarerRepository
import com.wayfarer.app.utils.Resource
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {

    private val repo = WayFarerRepository()

    private val _bookingState = MutableLiveData<Resource<Boolean>>()
    val bookingState: LiveData<Resource<Boolean>> = _bookingState

    private val _reviewsState = MutableLiveData<Resource<List<Review>>>()
    val reviewsState: LiveData<Resource<List<Review>>> = _reviewsState

    private val _submitReviewState = MutableLiveData<Resource<Boolean>>()
    val submitReviewState: LiveData<Resource<Boolean>> = _submitReviewState

    fun bookTour(request: BookingRequest) {
        _bookingState.value = Resource.Loading
        viewModelScope.launch {
            _bookingState.value = repo.createBooking(request)
        }
    }

    fun loadReviews(tourId: String) {
        _reviewsState.value = Resource.Loading
        viewModelScope.launch {
            _reviewsState.value = repo.getReviews(tourId)
        }
    }

    fun submitReview(tourId: String, rating: Int, comment: String) {
        _submitReviewState.value = Resource.Loading
        viewModelScope.launch {
            val result = repo.submitReview(tourId, rating, comment)
            _submitReviewState.value = result
            if (result is Resource.Success) loadReviews(tourId)
        }
    }
}
