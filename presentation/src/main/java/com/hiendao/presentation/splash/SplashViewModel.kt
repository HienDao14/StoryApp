package com.hiendao.presentation.splash

import androidx.lifecycle.viewModelScope
import com.hiendao.coreui.BaseViewModel
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.data.remote.retrofit.login.LoginAPI
import com.hiendao.domain.repository.LoginRepository
import com.hiendao.domain.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashState {
    data object Loading : SplashState
    data object Authenticated : SplashState
    data object Unauthenticated : SplashState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val loginRepository: LoginRepository
) : BaseViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state = _state.asStateFlow()

    init {
        checkAuth()
    }

    private fun checkAuth() {
        viewModelScope.launch {
            // Simulate splash delay
            delay(2000)
            
            val loggedIn = appPreferences.LOGGED_IN.value
            if (loggedIn) {
                val refreshToken = appPreferences.REFRESH_TOKEN.value
                loginRepository.refreshToken(refreshToken).collect { response ->
                    when(response){
                        is Response.Loading, Response.None -> Unit
                        is Response.Error -> {
                            _state.value = SplashState.Unauthenticated
                        }
                        is Response.Success -> {
                            val newTokens = response.data
                            if(newTokens.accessToken.isNullOrEmpty() || newTokens.refreshToken.isNullOrEmpty()){
                                _state.value = SplashState.Unauthenticated
                                return@collect
                            }
                            appPreferences.ACCESS_TOKEN.value = newTokens.accessToken!!
                            appPreferences.REFRESH_TOKEN.value = newTokens.refreshToken!!
                            newTokens.userId?.let {
                                appPreferences.USER_ID.value = it
                            }
                            _state.value = SplashState.Authenticated
                        }
                    }
                }
            } else {
                _state.value = SplashState.Unauthenticated
            }
        }
    }
}
