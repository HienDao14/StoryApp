package com.hiendao.presentation.login

import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.hiendao.coreui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
) : BaseViewModel() {
    fun sendInfoLoginWithGoogle(credential: GoogleIdTokenCredential){
        viewModelScope.launch {
            withContext(Dispatchers.IO){

//                loginRepository.loginWithGoogle(
//                    fullName = credential.displayName ?: "",
//                    email = credential.id,
//                    phoneNumber = credential.phoneNumber ?: "",
//                    imageUrl = credential.profilePictureUri.toString()
//                ).collect {
//                    _loginState.emit(it)
//                }
            }
        }
    }

    fun signInWithGoogle(googleAuthUIClient: GoogleAuthUIClient){
        viewModelScope.launch {
            googleAuthUIClient.signIn()
        }
    }
}