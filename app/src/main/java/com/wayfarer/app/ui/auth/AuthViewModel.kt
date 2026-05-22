package com.wayfarer.app.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wayfarer.app.data.models.User
import com.wayfarer.app.data.repository.WayFarerRepository
import com.wayfarer.app.utils.Resource
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repo = WayFarerRepository()

    private val _loginState = MutableLiveData<Resource<User>>()
    val loginState: LiveData<Resource<User>> = _loginState

    private val _registerState = MutableLiveData<Resource<User>>()
    val registerState: LiveData<Resource<User>> = _registerState

    fun login(email: String, password: String) {
        _loginState.value = Resource.Loading
        viewModelScope.launch {
            _loginState.value = repo.login(email, password)
        }
    }

    fun register(name: String, email: String, password: String) {
        _registerState.value = Resource.Loading
        viewModelScope.launch {
            _registerState.value = repo.register(name, email, password)
        }
    }
}
