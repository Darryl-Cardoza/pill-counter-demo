package com.example.pillcountingnewmodels.feature.login.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pillcountingnewmodels.feature.login.data.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: LoginRepository
) : ViewModel() {

    var loginState by mutableStateOf<String?>(null)
        private set

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val success = repository.login(username, password)
            loginState = if (success) "SUCCESS" else "FAILED"
        }
    }
}
