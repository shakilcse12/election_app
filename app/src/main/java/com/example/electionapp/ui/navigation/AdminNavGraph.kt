package com.example.electionapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.electionapp.ui.admin.AddEditVoteCenterScreen
import com.example.electionapp.ui.admin.AdminDashboardScreen

@Composable
fun AdminNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "admin_dashboard"
    ) {

        composable("admin_dashboard") {
            AdminDashboardScreen(
                onAddClick = {
                    navController.navigate("admin_add_center")
                }
            )
        }

        composable("admin_add_center") {
            AddEditVoteCenterScreen(
                onDone = {
                    navController.popBackStack()
                }
            )
        }
    }
}
