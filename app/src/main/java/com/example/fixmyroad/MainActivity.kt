package com.example.fixmyroad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fixmyroad.ui.navigation.NavGraph
import com.example.fixmyroad.ui.navigation.Screen
import com.example.fixmyroad.ui.theme.FixMyRoadTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FixMyRoadTheme {
                val navController = rememberNavController()
                
                val navigationItems = remember {
                    listOf(
                        NavigationItem("Dashboard", Screen.Dashboard.route, Icons.Rounded.GridView),
                        NavigationItem("Map", Screen.Map.route, Icons.Rounded.Map),
                        NavigationItem("Activity", Screen.History.route, Icons.Rounded.History),
                        NavigationItem("Profile", Screen.Profile.route, Icons.Rounded.Person)
                    )
                }

                Scaffold(
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        val showBottomBar = remember(currentDestination) {
                            currentDestination?.route !in listOf(Screen.Splash.route, Screen.Auth.route)
                        }

                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = slideInVertically { it } + fadeIn(),
                            exit = slideOutVertically { it } + fadeOut()
                        ) {
                            NavigationBar(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 24.dp)
                                    .clip(RoundedCornerShape(32.dp))
                                    .shadow(elevation = 16.dp, shape = RoundedCornerShape(32.dp)),
                                containerColor = Color.White,
                                tonalElevation = 8.dp
                            ) {
                                navigationItems.forEach { item ->
                                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                    NavigationBarItem(
                                        icon = { 
                                            Icon(
                                                imageVector = item.icon, 
                                                contentDescription = item.label,
                                                modifier = Modifier.size(24.dp)
                                            ) 
                                        },
                                        label = { 
                                            Text(
                                                text = item.label,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                            ) 
                                        },
                                        selected = isSelected,
                                        onClick = {
                                            if (currentDestination?.route != item.route) {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = Color(0xFF97A0AF), // Gray400
                                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        )
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Surface(
                            modifier = Modifier.padding(
                                bottom = if (innerPadding.calculateBottomPadding() > 0.dp) 0.dp else 0.dp
                            ),
                            color = Color.Transparent
                        ) {
                            NavGraph(navController = navController)
                        }
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val label: String,
    val route: String,
    val icon: ImageVector
)
