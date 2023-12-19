package com.cbi.gis.apps.ui.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cbi.gis.apps.data.model.LoginModel
import com.cbi.gis.apps.data.repository.LoginRepository

class LoginViewModel(private val repository: LoginRepository) : ViewModel() {
    private val _loginModel = MutableLiveData<LoginModel>()
    val loginModel: LiveData<LoginModel> get() = _loginModel

    fun loginUser(username: String, password: String) {
        repository.loginUser(username, password) { result ->
            _loginModel.postValue(result)
        }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(private val repository: LoginRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                return LoginViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}