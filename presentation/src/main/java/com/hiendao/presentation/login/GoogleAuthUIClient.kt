package com.hiendao.presentation.login

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.hiendao.presentation.BuildConfig

import kotlinx.coroutines.CancellationException
import timber.log.Timber

class GoogleAuthUIClient(
    private val context: Context,
    private val doSignIn: (GoogleIdTokenCredential) -> Unit
) {

    private val tag = "GoogleAuthUIClient"
    private val credentialManager = CredentialManager.create(context)

    suspend fun signIn(): Boolean {
        try {
            val result = buildCredentialRequest()
            return handleSignIn(result)

        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d("${e.message}")
            if (e is CancellationException) throw e
            println(tag + "signIn error: ${e.message}")
            return false
        }
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): Boolean {
        val credential = result.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

            try {
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                doSignIn(tokenCredential)
                return true
            } catch (e: GoogleIdTokenParsingException) {
                println(tag + "GoogleIdTokenParsingException: ${e.message}")
                return false
            }
        } else {
            println(tag + "credential is not GoogleIdTokenCredential")
            return false
        }
    }

    private suspend fun buildCredentialRequest(): GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.SERVER_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()
        return credentialManager.getCredential(
            context, request
        )
    }

    suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }
}