package com.diettrackr.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.diettrackr.app.data.db.AppDatabase
import com.diettrackr.app.data.models.*
import com.diettrackr.app.ui.components.GlassCard
import com.diettrackr.app.ui.components.WeightTracker
import com.diettrackr.app.ui.theme.DarkPrimary
import com.diettrackr.app.ui.theme.DarkSurface
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    
    // State
    var user by remember { mutableStateOf<User?>(null) }
    var weightEntries by remember { mutableStateOf<List<WeightEntry>>(emptyList()) }
    var weeklyStats by remember { mutableStateOf<WeeklyStats?>(null) }
    var showGoalDialog by remember { mutableStateOf(false) }
    
    // Load data
    LaunchedEffect(key1 = Unit) {
        // Load user data
        database.userDao().getUserFlow().collect { userData ->
            user = userData
        }
    }
    
    LaunchedEffect(key1 = Unit) {
        // Load weight entries
        database.userDao().getWeightEntriesFlow().collect { entries ->
            weightEntries = entries
        }
    }
    
    // Calculate weekly stats
    LaunchedEffect(key1 = weightEntries) {
        weeklyStats = calculateWeeklyStats(database)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Progress",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Weight tracker
            item {
                WeightTracker(
                    currentWeight = user?.currentWeight ?: 0f,
                    goalWeight = user?.goalWeight ?: 0f,
                    recentEntries = weightEntries,
                    onAddEntry = { weight ->
                        scope.launch {
                            // Update user's current weight
                            user?.let { currentUser ->
                                val updatedUser = currentUser.copy(currentWeight = weight)
                                database.userDao().updateUser(updatedUser)
                            } ?: run {
                                // Create user if not exist
                                val newUser = User(currentWeight = weight)
                                database.userDao().insertUser(newUser)
                            }
                            
                            // Add weight entry
                            val entry = WeightEntry(
                                weight = weight,
                                date = LocalDate.now()
                            )
                            database.userDao().insertWeightEntry(entry)
                        }
                    }
                )
            }
            
            // Set goal button
            item {
                Button(
                    onClick = { showGoalDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkPrimary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set Weight Goal")
                }
            }
            
            // Weekly summary
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Weekly Summary",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // Weekly stats
            item {
                weeklyStats?.let { stats ->
                    WeeklySummaryCard(stats)
                } ?: run {
                    GlassCard {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Not enough data to show weekly summary",
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            
            // Weight history
            if (weightEntries.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Weight History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                items(weightEntries.take(10)) { entry ->
                    WeightEntryItem(entry)
                }
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Set goal dialog
    if (showGoalDialog) {
        SetGoalDialog(
            currentGoal = user?.goalWeight ?: 0f,
            onDismiss = { showGoalDialog = false },
            onSetGoal = { goalWeight ->
                scope.launch {
                    user?.let { currentUser ->
                        val updatedUser = currentUser.copy(goalWeight = goalWeight)
                        database.userDao().updateUser(updatedUser)
                    } ?: run {
                        val newUser = User(goalWeight = goalWeight)
                        database.userDao().insertUser(newUser)
                    }
                    showGoalDialog = false
                }
            }
        )
    }
}

@Composable
fun WeightEntryItem(entry: WeightEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x33FFFFFF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date
            Text(
                text = entry.date.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                fontSize = 16.sp,
                color = Color.White
            )
            
            // Weight
            Text(
                text = "${entry.weight} kg",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun WeeklySummaryCard(stats: WeeklyStats) {
    GlassCard {
        Column(modifier = Modifier.padding(8.dp)) {
            // Meal completion rate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Meal Completion Rate:",
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "${if (stats.mealCompletionRate.isNaN()) 0 else (stats.mealCompletionRate * 100).toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // Weight change
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Weekly Weight Change:",
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = if (stats.weightChange.isNaN()) "0.0 kg" else if (stats.weightChange >= 0) "+${stats.weightChange} kg" else "${stats.weightChange} kg",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (stats.weightChange.isNaN() || stats.weightChange == 0f) Color.White else if (stats.weightChange > 0) Color.Red else Color.Green
                )
            }
            
            // Average daily protein
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Avg. Daily Protein:",
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "${stats.avgDailyProtein}g",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // Average daily calories
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Avg. Daily Calories:",
                    fontSize = 16.sp,
                    color = Color.White
                )
                Text(
                    text = "${stats.avgDailyCalories} kcal",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetGoalDialog(
    currentGoal: Float,
    onDismiss: () -> Unit,
    onSetGoal: (Float) -> Unit
) {
    var goalWeightInput by remember { mutableStateOf(if (currentGoal > 0) currentGoal.toString() else "") }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Set Weight Goal",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = goalWeightInput,
                    onValueChange = { 
                        if (it.isEmpty() || it.toFloatOrNull() != null) {
                            goalWeightInput = it 
                        }
                    },
                    label = { Text("Goal Weight (kg)") },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = DarkPrimary,
                        focusedLabelColor = DarkPrimary
                    )
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Button(
                        onClick = {
                            goalWeightInput.toFloatOrNull()?.let { goal ->
                                onSetGoal(goal)
                            }
                        },
                        enabled = goalWeightInput.toFloatOrNull() != null
                    ) {
                        Text("Set Goal")
                    }
                }
            }
        }
    }
}

data class WeeklyStats(
    val mealCompletionRate: Float,
    val weightChange: Float,
    val avgDailyProtein: Int,
    val avgDailyCalories: Int
)

suspend fun calculateWeeklyStats(database: AppDatabase): WeeklyStats? {
    val today = LocalDate.now()
    val weekAgo = today.minusDays(7)
    
    // Get weight entries for the week
    val weightEntries = database.userDao().getWeightEntriesInRange(weekAgo, today)
    if (weightEntries.size < 2) {
        return null
    }
    
    // Calculate weight change
    val latestWeight = weightEntries.maxByOrNull { it.date }?.weight ?: 0f
    val earliestWeight = weightEntries.minByOrNull { it.date }?.weight ?: 0f
    val weightChange = latestWeight - earliestWeight
    
    // Get daily logs for the week
    val dailyLogs = mutableListOf<DailyLog>()
    for (i in 0..6) {
        val date = today.minusDays(i.toLong())
        dailyLogs.addAll(database.dailyLogDao().getDailyLogsForDate(date))
    }
    
    // Calculate meal completion rate
    val completedMeals = dailyLogs.count { 
        it.status == MealStatus.COMPLETED || it.status == MealStatus.MODIFIED 
    }
    val totalMeals = dailyLogs.size
    val mealCompletionRate = if (totalMeals > 0) completedMeals.toFloat() / totalMeals else 0f
    
    // Calculate average macros
    var totalProtein = 0
    var totalCalories = 0
    val mealDao = database.mealDao()
    
    dailyLogs.filter { it.status == MealStatus.COMPLETED || it.status == MealStatus.MODIFIED }.forEach { log ->
        val meal = mealDao.getMealById(log.mealId) ?: return@forEach
        totalProtein += meal.protein
        totalCalories += meal.calories
    }
    
    // Calculate daily averages (for actual days in the past week)
    val daysInRange = ChronoUnit.DAYS.between(weekAgo, today).toInt() + 1
    val avgDailyProtein = if (daysInRange > 0) totalProtein / daysInRange else 0
    val avgDailyCalories = if (daysInRange > 0) totalCalories / daysInRange else 0
    
    return WeeklyStats(
        mealCompletionRate = mealCompletionRate,
        weightChange = weightChange,
        avgDailyProtein = avgDailyProtein,
        avgDailyCalories = avgDailyCalories
    )
} 