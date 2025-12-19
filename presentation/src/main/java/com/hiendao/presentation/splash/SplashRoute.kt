package com.hiendao.presentation.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SplashRoute(
    modifier: Modifier = Modifier,
    onNavigateToMain: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state) {
        when (state) {
            SplashState.Authenticated -> onNavigateToMain()
            SplashState.Unauthenticated -> onNavigateToLogin()
            SplashState.Loading -> Unit
        }
    }

    SplashScreen(modifier = modifier)
}
