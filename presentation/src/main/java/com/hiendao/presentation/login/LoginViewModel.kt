package com.hiendao.presentation.login

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.hiendao.coreui.BaseViewModel
import com.hiendao.coreui.appPreferences.AppPreferences
import com.hiendao.domain.repository.LoginRepository
import com.hiendao.domain.utils.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository,
    private val appPreferences: AppPreferences
) : BaseViewModel() {

    private val _loginState: MutableStateFlow<Response<Pair<String, String>>> = MutableStateFlow(Response.None)
    val loginState = _loginState.asStateFlow()

    fun sendInfoLoginWithGoogle(credential: GoogleIdTokenCredential){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                loginRepository.loginWithGoogle(credential.idToken).collect { result ->
                    when(result){
                        is Response.Loading -> {
                            _loginState.emit(Response.Loading)
                        }
                        is Response.Success -> {
                            if(result.data.accessToken.isNullOrEmpty() || result.data.refreshToken.isNullOrEmpty()){
                                _loginState.emit(Response.Error("Login failed: Empty tokens", Exception("Empty tokens")))
                                return@collect
                            }
                            appPreferences.REFRESH_TOKEN.value = result.data.refreshToken!!
                            appPreferences.ACCESS_TOKEN.value = result.data.accessToken!!
                            result.data.userId?.let {
                                appPreferences.USER_ID.value = it
                            }
                            _loginState.emit(Response.Success(Pair(result.data.accessToken!!, result.data.refreshToken!!)))
                        }
                        is Response.Error -> {
                            _loginState.emit(Response.Error(result.message, result.exception))
                        }
                        is Response.None -> Unit
                    }
                }
            }
        }
    }

    fun signInWithGoogle(googleAuthUIClient: GoogleAuthUIClient){
        viewModelScope.launch {
            googleAuthUIClient.signIn()
        }
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val callbackManager = CallbackManager.Factory.create()

    fun getCallbackManager(): CallbackManager = callbackManager

    fun loginWithFacebook(activity: Activity) {
        LoginManager.getInstance()
            .logInWithReadPermissions(
                activity,
                listOf("email", "public_profile")
            )
    }

    fun onFacebookLoginResult(result: LoginResult) {
        val credential =
            FacebookAuthProvider.getCredential(result.accessToken.token)

        auth.signInWithCredential(credential)
            .addOnSuccessListener {
                _loginState.value = Response.Success(Pair(auth.currentUser?.email ?: "", auth.currentUser?.uid ?: ""))
            }
            .addOnFailureListener {
                _loginState.value = Response.Error(it.message ?: "Login failed", Exception(it))
            }
    }

    fun onFacebookLoginError(message: String) {
        _loginState.value = Response.Error(message, Exception(message))
    }
}