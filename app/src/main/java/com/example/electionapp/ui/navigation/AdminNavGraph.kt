package com.example.electionapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.electionapp.ui.admin.AddEditVoteCenterScreen
import com.example.electionapp.ui.admin.AdminDashboardScreen

@Composable
fun AdminNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "admin/dashboard"
    ) {

        composable("admin/dashboard") {
            AdminDashboardScreen(
                onAddClick = {
                    navController.navigate("admin/add")
                },
                onEditClick = { id ->
                    navController.navigate("admin/edit/$id")
                },
                onLogout = onLogout
            )
        }

        composable("admin/add") {
            AddEditVoteCenterScreen(
                centerId = null,
                onDone = { navController.popBackStack() }
            )
        }

        composable(
            route = "admin/edit/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.IntType }
            )
        ) {
            AddEditVoteCenterScreen(
                centerId = it.arguments!!.getInt("id"),
                onDone = { navController.popBackStack() }
            )
        }
    }
}
