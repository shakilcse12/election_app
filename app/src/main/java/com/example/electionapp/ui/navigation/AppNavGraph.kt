package com.example.electionapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.electionapp.ui.centers.VoteCenterListScreen
import com.example.electionapp.ui.centers.VoteCenterDetailsScreen
import com.example.electionapp.ui.map.MapPlaceholderScreen
import com.example.electionapp.ui.law.LawEnforcementScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Centers.route,
        modifier = modifier
    ) {
        // Centers Tab
        composable(BottomNavItem.Centers.route) {
            VoteCenterListScreen(
                onCenterClick = { centerId ->
                    navController.navigate("details/$centerId")
                },
                onAdminLoginClick = {
                    navController.navigate("login")
                }
            )
        }

        // Map Tab
        composable(BottomNavItem.Map.route) {
            MapPlaceholderScreen()
        }

        // Law Tab
        composable(BottomNavItem.Law.route) {
            LawEnforcementScreen()
        }

        // Details Screen (from list click)
        composable(
            route = "details/{centerId}",
            arguments = listOf(navArgument("centerId") { type = NavType.IntType })
        ) { backStackEntry ->
            VoteCenterDetailsScreen(
                centerId = backStackEntry.arguments!!.getInt("centerId")
            )
        }
    }
}
