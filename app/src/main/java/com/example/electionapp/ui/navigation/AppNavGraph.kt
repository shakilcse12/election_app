package com.example.electionapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.electionapp.ui.centers.VoteCenterListScreen
import com.example.electionapp.ui.law.LawEnforcementScreen
import com.example.electionapp.ui.map.MapPlaceholderScreen

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Centers.route
    ) {
        composable(BottomNavItem.Centers.route) {
            VoteCenterListScreen()
        }
        composable(BottomNavItem.Map.route) {
            MapPlaceholderScreen()
        }
        composable(BottomNavItem.Law.route) {
            LawEnforcementScreen()
        }
    }
}
