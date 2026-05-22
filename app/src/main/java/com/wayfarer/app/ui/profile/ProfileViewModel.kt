package com.wayfarer.app.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wayfarer.app.data.models.User
import com.wayfarer.app.data.repository.WayFarerRepository
import com.wayfarer.app.utils.Resource
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val repo = WayFarerRepository()

    private val _updateState = MutableLiveData<Resource<User>>()
    val updateState: LiveData<Resource<User>> = _updateState

    fun updateName(newName: String) {
        _updateState.value = Resource.Loading
        viewModelScope.launch {
            _updateState.value = repo.updateUserName(newName)
        }
    }
}
