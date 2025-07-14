package com.diettrackr.app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.diettrackr.app.ui.screens.*
import com.diettrackr.app.ui.screens.FoodRecognitionScreen
import com.diettrackr.app.ui.theme.DietTrackrTheme
import com.diettrackr.app.ui.theme.*

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.d(TAG, "Notification permission granted")
        } else {
            Log.w(TAG, "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Add simple logging
        Log.d(TAG, "onCreate called")
        
        // Request notification permission for Android 13+
        requestNotificationPermission()
        
        setContent {
            Log.d(TAG, "setContent called")
            DietTrackrTheme {
                Log.d(TAG, "DietTrackrTheme applied")
                DietTrackrApp()
            }
        }
    }
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d(TAG, "Notification permission already granted")
                }
                else -> {
                    Log.d(TAG, "Requesting notification permission")
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            Log.d(TAG, "Notification permission not required for this Android version")
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietTrackrApp() {
    val navController = rememberNavController()
    
    val items = listOf(
        Screen.Home,
        Screen.Calendar,
        Screen.MealPlan,
        Screen.Progress,
        Screen.Settings
    )
    
    // Orange-themed app background with gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        GradientStart,
                        DarkBackground
                    )
                )
            )
    ) {
        Scaffold(
            bottomBar = {
                // Modern bottom navigation with orange glassmorphism
                NavigationBar(
                    containerColor = GlassCardBackground,
                    contentColor = Color.White,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    DarkPrimary.copy(alpha = 0.1f),
                                    DarkPrimary.copy(alpha = 0.05f)
                                )
                            )
                        )
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    screen.icon, 
                                    contentDescription = null,
                                    tint = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) 
                                        DarkPrimary else Color.White.copy(alpha = 0.8f)
                                ) 
                            },
                            label = { 
                                Text(
                                    screen.title,
                                    color = if (currentDestination?.hierarchy?.any { it.route == screen.route } == true) 
                                        DarkPrimary else Color.White.copy(alpha = 0.8f)
                                ) 
                            },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DarkPrimary,
                                selectedTextColor = DarkPrimary,
                                unselectedIconColor = Color.White.copy(alpha = 0.8f),
                                unselectedTextColor = Color.White.copy(alpha = 0.8f),
                                indicatorColor = DarkPrimary.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) { 
                    HomeScreen(navController = navController) 
                }
                composable(Screen.Calendar.route) { 
                    CalendarScreen(navController = navController) 
                }
                composable(Screen.MealPlan.route) { 
                    MealPlanScreen(navController = navController) 
                }
                composable(Screen.Progress.route) { 
                    ProgressScreen(navController = navController) 
                }
                composable(Screen.Settings.route) { 
                    SettingsScreen(navController = navController) 
                }
                
                // Additional screens
                composable("edit_meal/{mealId}") { backStackEntry ->
                    val mealId = backStackEntry.arguments?.getString("mealId")?.toIntOrNull() ?: -1
                    EditMealScreen(
                        navController = navController,
                        mealId = mealId
                    )
                }
                
                composable("food_recognition") {
                    FoodRecognitionScreen(
                        navController = navController,
                        onFoodRecognized = { entryData ->
                            // This will be handled by the calling screen
                            // For now, we'll navigate back
                            navController.navigateUp()
                        }
                    )
                }
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
    object MealPlan : Screen("meal_plan", "Meal Plan", Icons.Default.Restaurant)
    object Progress : Screen("progress", "Progress", Icons.Default.BarChart)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Preview
@Composable
fun DietTrackrAppPreview() {
    DietTrackrTheme {
        DietTrackrApp()
    }
} 