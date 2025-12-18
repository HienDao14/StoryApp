package com.hiendao.presentation.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LoginRoute(
    modifier: Modifier = Modifier,
    onLoginSuccess: () -> Unit
) {
    val viewModel: LoginViewModel = hiltViewModel()

    val context = LocalContext.current
    val googleAuthUIClient = GoogleAuthUIClient(
        context,
        doSignIn = { credential ->
            //Navigate to Home Screen
            //localStorage.isSignedIn = true
//            viewModel.sendInfoLoginWithGoogle(credential)
            onLoginSuccess()
        }
    )


    LoginScreen(
        onFacebookClick = {
            onLoginSuccess()
        },
        onGoogleClick = {
            viewModel.signInWithGoogle(googleAuthUIClient)
        },
        modifier = modifier
    )
}