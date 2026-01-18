package com.hiendao.presentation.login

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.hiendao.domain.utils.Response

@Composable
fun LoginRoute(
    modifier: Modifier = Modifier,
    onLoginSuccess: (String, String) -> Unit,
    onLoginWithoutToken: () -> Unit = {}
) {
    val viewModel: LoginViewModel = hiltViewModel()

    val context = LocalContext.current
    val googleAuthUIClient = GoogleAuthUIClient(
        context,
        doSignIn = { credential ->
            //Navigate to Home Screen
            //localStorage.isSignedIn = true
            viewModel.sendInfoLoginWithGoogle(credential)
//            onLoginSuccess()
        }
    )

    val loginState = viewModel.loginState.collectAsStateWithLifecycle()
    var showLoading by remember {
        mutableStateOf(false)
    }
    when (val state = loginState.value) {
        is Response.Loading -> {
            showLoading = true
        }
        is Response.Success -> {
            if (state.data.first.isNotEmpty() && state.data.second.isNotEmpty()) {
                showLoading = false
                onLoginSuccess(state.data.first, state.data.second)
            }
        }
        is Response.None -> Unit
        is Response.Error -> {
            showLoading = false
            Toast.makeText(context, "Login failed: ${state.message}", Toast.LENGTH_LONG).show()
            // Handle error state if needed
        }
    }

    LaunchedEffect(Unit) {
        LoginManager.getInstance().registerCallback(
            viewModel.getCallbackManager(),
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    viewModel.onFacebookLoginResult(result)
                }

                override fun onCancel() {}

                override fun onError(error: FacebookException) {
                    viewModel.onFacebookLoginError(error.message ?: "")
                }
            }
        )
    }

    Box{
        LoginScreen(
            onFacebookClick = {
                onLoginWithoutToken.invoke()
//                viewModel.loginWithFacebook(activity = context as android.app.Activity)
            },
            onGoogleClick = {
                viewModel.signInWithGoogle(googleAuthUIClient)
            },
            modifier = modifier
        )
        if(showLoading){
            // show loading progress
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}