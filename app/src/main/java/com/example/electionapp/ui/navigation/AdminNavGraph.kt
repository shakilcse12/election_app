package com.example.electionapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.electionapp.ui.admin.*

@Composable
fun AdminNavGraph(
    navController: NavHostController,
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
                    AdminTopBar(onLogout = onLogout)
                }
            ) { padding ->
                AdminDashboardScreen(
                    modifier = Modifier.padding(padding),
                    onAddClick = {
                        navController.navigate("admin/add")
                    },
                    onEditClick = { id ->
                        navController.navigate("admin/edit/$id")
                    }
                )
            }
        }

        /* ---------------- LAW TAB (WITH BOTTOM NAV) ---------------- */
        composable("admin/law") {
            AdminScaffold(
                selectedIndex = 1,
                onDashboard = {
                    navController.navigate("admin/dashboard") {
                        popUpTo("admin/dashboard") { inclusive = true }
                    }
                },
                onAdd = {
                    navController.navigate("admin/add")
                },
                onLaw = { /* already here */ },
                topBar = {
                    AdminTopBar(onLogout = onLogout)
                }
            ) { padding ->
                LawPlaceholderScreen(
                )
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
