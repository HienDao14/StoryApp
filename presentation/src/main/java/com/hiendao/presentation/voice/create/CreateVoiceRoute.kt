package com.hiendao.presentation.voice.create

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CreateVoiceRoute(
    onBackClick: () -> Unit
) {
    val viewModel: CreateVoiceViewModel = hiltViewModel()
    val state = viewModel.uiState.collectAsState().value

    CreateVoiceScreen(
        state = state,
        onBackClick = onBackClick,
        onNameChange = viewModel::onNameChange,
        onStartRecording = viewModel::startRecording,
        onStopRecording = viewModel::stopRecording,
        onPlayRecording = viewModel::playRecording,
        onCreateVoice = viewModel::createVoice,
        onDismissSuccessDialog = viewModel::dismissSuccessDialog
    )
}
