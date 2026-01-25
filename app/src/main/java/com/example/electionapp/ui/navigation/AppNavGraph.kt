package com.example.electionapp.ui.navigation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.electionapp.ui.about.AboutScreen
import com.example.electionapp.ui.admin.LawPlaceholderScreen
import com.example.electionapp.ui.auth.AuthViewModel
import com.example.electionapp.ui.auth.LoginScreen
import com.example.electionapp.ui.centers.VoteCenterDetailsScreen
import com.example.electionapp.ui.centers.VoteCenterListScreen
import com.example.electionapp.ui.law.LawEnforcementScreen
import com.example.electionapp.ui.map.MapPlaceholderScreen
import com.example.electionapp.ui.map.VoteCenterMapScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {

    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Centers.route,
        modifier = modifier
    ) {

        /* ---------- PUBLIC FLOW ---------- */

        composable(BottomNavItem.Centers.route) {
            VoteCenterListScreen(
                onCenterClick = { id ->
                    navController.navigate("details/$id")
                },
                onAdminLoginClick = {
                    navController.navigate("login")
                }
            )
        }

        composable(BottomNavItem.Map.route) {
            VoteCenterMapScreen()

        }

        composable(BottomNavItem.Law.route) {
            LawPlaceholderScreen()
        }

        composable(BottomNavItem.About.route) {
            AboutScreen()
        }

        composable(
            route = "details/{centerId}",
            arguments = listOf(
                navArgument("centerId") { type = NavType.IntType }
            )
        ) {
            VoteCenterDetailsScreen(
                centerId = it.arguments!!.getInt("centerId")
            )
        }

        /* ---------- AUTH ---------- */

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("admin_root") {
                        popUpTo(BottomNavItem.Centers.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        /* ---------- ADMIN ROOT ---------- */

        composable("admin_root") {
            val authViewModel: AuthViewModel = hiltViewModel()
            val adminNavController = rememberNavController()
            var showLogoutDialog by remember { mutableStateOf(false) }

            // LOGOUT CONFIRMATION DIALOG
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Confirm Logout") },
                    text = { Text("Are you sure you want to log out of the admin panel?") },
                    confirmButton = {
                        TextButton(onClick = {
                            showLogoutDialog = false
                            authViewModel.logout() // Clear ViewModel State
                            navController.navigate(BottomNavItem.Centers.route) {
                                popUpTo("admin_root") { inclusive = true }
                            }
                        }) {
                            Text("Logout", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            AdminNavGraph(
                navController = adminNavController,
                mainNavController = navController, // Pass the parent controller here!
                onLogout = {
                    showLogoutDialog = true
                }
            )
        }
    }
}
