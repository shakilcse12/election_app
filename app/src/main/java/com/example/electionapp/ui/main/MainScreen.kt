package com.example.electionapp.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.electionapp.ui.navigation.AppNavGraph
import com.example.electionapp.ui.navigation.BottomNavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define all tabs that should show the bottom bar
    val bottomNavItems = listOf(
        BottomNavItem.Centers,
        BottomNavItem.Map,
        BottomNavItem.Law,
        BottomNavItem.About // Add the new item here
    )

    // Check if the current route is one of our main bottom nav tabs
    val showBottomBar = currentRoute in bottomNavItems.map { it.route }
    // Get the ViewModel
    //val viewModel: VoteCenterViewModel = hiltViewModel()
    // One-time dummy data insertion
    /*LaunchedEffect(Unit) {
        val existingCenters = viewModel.repository.getAllCenters().first()
        if (existingCenters.isEmpty()) {
            viewModel.insertDummyData(DummyData.voteCenters())
        }
    }*/

    Scaffold(
        bottomBar = {
            // Animated visibility makes the transition smoother
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
            ) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        // Pop up to the start destination to avoid building up a huge stack
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(padding)
        )
    }
}