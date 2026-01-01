package com.example.electionapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.electionapp.ui.auth.LoginScreen
import com.example.electionapp.ui.centers.VoteCenterDetailsScreen
import com.example.electionapp.ui.centers.VoteCenterListScreen
import com.example.electionapp.ui.law.LawEnforcementScreen
import com.example.electionapp.ui.map.MapPlaceholderScreen

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
            MapPlaceholderScreen()
        }

        composable(BottomNavItem.Law.route) {
            LawEnforcementScreen()
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
            val adminNavController = rememberNavController()

            AdminNavGraph(
                navController = adminNavController,
                onLogout = {
                    navController.navigate(BottomNavItem.Centers.route) {
                        popUpTo("admin_root") { inclusive = true }
                    }
                }
            )
        }
    }
}
