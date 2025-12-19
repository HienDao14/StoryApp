package com.hiendao.presentation.login

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            showLoading = true
            Toast.makeText(context, "Login failed: ${state.message}", Toast.LENGTH_LONG).show()
            // Handle error state if needed
        }
    }

    Box{
        LoginScreen(
            onFacebookClick = {
                onLoginWithoutToken.invoke()
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