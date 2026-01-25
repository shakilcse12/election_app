package com.example.electionapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.electionapp.ui.admin.*
import com.example.electionapp.ui.centers.VoteCenterListScreen

@Composable
fun AdminNavGraph(
    navController: NavHostController,
    mainNavController: NavHostController, // Global App navigation (details)
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = "admin/dashboard"
    ) {

        /* ---------------- DASHBOARD (WITH BOTTOM NAV) ---------------- */
        composable("admin/dashboard") {
            AdminScaffold(
                selectedIndex = 0,
                onDashboard = { /* already here */ },
                onAdd = {
                    navController.navigate("admin/add")
                },
                onLaw = {
                    navController.navigate("admin/law")
                },
                topBar = {
                    AdminTopBar(title = "Admin Dashboard", onLogout = onLogout)
                }
            ) { padding, _ ->
                VoteCenterListScreen(
                    modifier = Modifier.padding(padding), // APPLY PADDING HERE
                    isAdmin = true, // KEY CHANGE
                    onCenterClick = { id -> mainNavController.navigate("details/$id") },
                    onEditClick = { id -> navController.navigate("admin/edit/$id") }
                )
            }
        }

        /* ---------------- LAW TAB (WITH BOTTOM NAV) ---------------- */
        composable("admin/law") {
            AdminScaffold(
                selectedIndex = 2,
                onDashboard = {
                    navController.navigate("admin/dashboard") {
                        popUpTo("admin/dashboard") { inclusive = true }
                    }
                },
                onAdd = { navController.navigate("admin/add") },
                onLaw = { /* already here */ },
                topBar = {
                    // Pass "Contacts" as the title here
                    AdminTopBar(title = "Contacts", onLogout = onLogout)
                }
            ) { padding, snackbarHostState ->
                LawPlaceholderScreen()
            }
        }

        /* ---------------- FULL SCREEN: ADD ---------------- */
        composable("admin/add") {
            AddEditVoteCenterScreen(
                centerId = null,
                onDone = {
                    navController.popBackStack("admin/dashboard", false)
                }
            )
        }

        /* ---------------- FULL SCREEN: EDIT ---------------- */
        composable(
            route = "admin/edit/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entry ->
            AddEditVoteCenterScreen(
                centerId = entry.arguments!!.getInt("id"),
                onDone = {
                    navController.popBackStack("admin/dashboard", false)
                }
            )
        }
    }
}
