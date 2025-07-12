package com.diettrackr.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.diettrackr.app.data.db.AppDatabase
import com.diettrackr.app.data.models.DailyLog
import com.diettrackr.app.data.models.Meal
import com.diettrackr.app.data.models.MealComponent
import com.diettrackr.app.data.models.MealStatus
import com.diettrackr.app.ui.components.GlassCard
import com.diettrackr.app.ui.components.MacrosProgressBar
import com.diettrackr.app.ui.components.MealCard
import com.diettrackr.app.ui.components.ModernCard
import com.diettrackr.app.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    
    // State
    val today = remember { LocalDate.now() }
    val currentTime = remember { LocalTime.now() }
    
    var meals by remember { mutableStateOf<List<Meal>>(emptyList()) }
    var dailyLogs by remember { mutableStateOf<List<DailyLog>>(emptyList()) }
    var mealComponents by remember { mutableStateOf<Map<Int, List<MealComponent>>>(emptyMap()) }
    var expandedMealId by remember { mutableStateOf<Int?>(null) }
    
    // Daily macros summary
    var totalProtein by remember { mutableStateOf(0) }
    var totalCarbs by remember { mutableStateOf(0) }
    var totalFats by remember { mutableStateOf(0) }
    var totalCalories by remember { mutableStateOf(0) }
    
    // Target macros (sum of all meal macros)
    var targetProtein by remember { mutableStateOf(0) }
    var targetCarbs by remember { mutableStateOf(0) }
    var targetFats by remember { mutableStateOf(0) }
    var targetCalories by remember { mutableStateOf(0) }
    
    // Load data
    LaunchedEffect(key1 = today) {
        // Load meals
        database.mealDao().getAllMealsFlow().collectLatest { allMeals ->
            meals = allMeals
            
            // Calculate total macros
            targetProtein = allMeals.sumOf { it.protein }
            targetCarbs = allMeals.sumOf { it.carbs }
            targetFats = allMeals.sumOf { it.fats }
            targetCalories = allMeals.sumOf { it.calories }
            
            // Load components for each meal
            val componentsMap = mutableMapOf<Int, List<MealComponent>>()
            allMeals.forEach { meal ->
                val components = database.mealDao().getMealComponents(meal.id)
                componentsMap[meal.id] = components
            }
            mealComponents = componentsMap
        }
    }
    
    LaunchedEffect(key1 = today) {
        // Load daily logs
        database.dailyLogDao().getDailyLogsForDateFlow(today).collectLatest { logs ->
            dailyLogs = logs
            
            // Calculate consumed macros
            totalProtein = 0
            totalCarbs = 0
            totalFats = 0
            totalCalories = 0
            
            logs.filter { it.status == MealStatus.COMPLETED || it.status == MealStatus.MODIFIED }.forEach { log ->
                val meal = meals.find { it.id == log.mealId } ?: return@forEach
                totalProtein += meal.protein
                totalCarbs += meal.carbs
                totalFats += meal.fats
                totalCalories += meal.calories
            }
        }
    }
    
    // Update meal status
    val updateMealStatus = { meal: Meal, status: MealStatus ->
        scope.launch {
            // Find existing log or create new
            val existingLog = dailyLogs.find { it.mealId == meal.id }
            if (existingLog != null) {
                val updatedLog = existingLog.copy(status = status)
                database.dailyLogDao().updateDailyLog(updatedLog)
            } else {
                val newLog = DailyLog(
                    mealId = meal.id,
                    date = today,
                    status = status
                )
                database.dailyLogDao().insertDailyLog(newLog)
            }
        }
    }
    
    // Modern background with gradient
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DarkBackground,
                        Color(0xFF0F0F0F),
                        DarkBackground
                    )
                )
            )
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            text = "D Day",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Daily summary with modern styling
                item {
                    ModernCard(
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Today's Progress",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(20.dp))
                            
                            // Progress metrics in a modern layout
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Meals completed
                                val completedMeals = dailyLogs.count { 
                                    it.status == MealStatus.COMPLETED || it.status == MealStatus.MODIFIED 
                                }
                                val totalMeals = meals.size
                                
                                ModernMetricCard(
                                    value = "$completedMeals/$totalMeals",
                                    label = "meals",
                                    color = CardBlue,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                // Calories
                                ModernMetricCard(
                                    value = totalCalories.toString(),
                                    label = "calories",
                                    color = CardOrange,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Macros progress with modern styling
                            Text(
                                text = "Macros Summary",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            MacrosProgressBar(
                                protein = totalProtein,
                                carbs = totalCarbs,
                                fats = totalFats,
                                targetProtein = targetProtein,
                                targetCarbs = targetCarbs,
                                targetFats = targetFats
                            )
                        }
                    }
                }
                
                // Meals section header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Today's Meals",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        if (meals.isNotEmpty()) {
                            Text(
                                text = "${meals.size} meals",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                if (meals.isEmpty()) {
                    item {
                        EmptyMealsCard()
                    }
                } else {
                    items(meals) { meal ->
                        val mealStatus = dailyLogs.find { it.mealId == meal.id }?.status ?: MealStatus.PENDING
                        val components = mealComponents[meal.id] ?: emptyList()
                        val isExpanded = expandedMealId == meal.id
                        
                        MealCard(
                            meal = meal,
                            components = components,
                            status = mealStatus,
                            onStatusChange = { newStatus ->
                                updateMealStatus(meal, newStatus)
                            },
                            onEditClick = {
                                navController.navigate("edit_meal/${meal.id}")
                            },
                            expanded = isExpanded,
                            onExpandChange = { expand ->
                                expandedMealId = if (expand) meal.id else null
                            }
                        )
                    }
                }
                
                // Add bottom spacing
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
private fun ModernMetricCard(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun EmptyMealsCard() {
    ModernCard(
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Fastfood,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No meals found",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f)
            )
            Text(
                text = "Add some meals to get started",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
} 