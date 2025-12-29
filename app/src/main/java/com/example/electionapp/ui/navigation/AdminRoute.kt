package com.example.electionapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.electionapp.ui.auth.AuthViewModel

@Composable
fun AdminRoute(
    onAuthorized: () -> Unit,
    onUnauthorized: () -> Unit
) {
    val authViewModel: AuthViewModel = hiltViewModel()

    if (authViewModel.isLoggedIn.collectAsState().value) {
        onAuthorized()
    } else {
        onUnauthorized()
    }
}
