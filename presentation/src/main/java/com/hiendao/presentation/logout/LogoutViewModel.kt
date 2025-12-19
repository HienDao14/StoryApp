package com.hiendao.presentation.logout

import androidx.lifecycle.viewModelScope
import com.hiendao.coreui.BaseViewModel
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.domain.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LogoutViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val appPreferences: AppPreferences
) : BaseViewModel() {
    fun logout() {
        viewModelScope.launch {
            val refreshToken = appPreferences.REFRESH_TOKEN.value
            loginRepository.logout(refreshToken)
            // Clear tokens and user data from AppPreferences
            appPreferences.ACCESS_TOKEN.value = ""
            appPreferences.REFRESH_TOKEN.value = ""
            appPreferences.USER_ID.value = ""
        }
    }
}