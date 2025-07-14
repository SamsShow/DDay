package com.diettrackr.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.diettrackr.app.data.db.AppDatabase
import com.diettrackr.app.data.models.DailyLog
import com.diettrackr.app.data.models.Meal
import com.diettrackr.app.data.models.MealStatus
import com.diettrackr.app.ui.components.ModernCard
import com.diettrackr.app.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class DayData(
    val date: LocalDate,
    val totalCalories: Int = 0,
    val totalProtein: Int = 0,
    val completedMeals: Int = 0,
    val totalMeals: Int = 0,
    val isInCurrentMonth: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    
    // State
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var meals by remember { mutableStateOf<List<Meal>>(emptyList()) }
    var monthlyLogs by remember { mutableStateOf<List<DailyLog>>(emptyList()) }
    var dayDataList by remember { mutableStateOf<List<DayData>>(emptyList()) }
    var currentStreak by remember { mutableStateOf(0) }
    var longestStreak by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    
    // Load data
    LaunchedEffect(currentMonth) {
        // Load meals
        database.mealDao().getAllMealsFlow().collectLatest { allMeals ->
            meals = allMeals
        }
    }
    
    LaunchedEffect(currentMonth, meals) {
        if (meals.isNotEmpty()) {
            // Get first and last day of the calendar view (including previous/next month days)
            val firstDayOfMonth = currentMonth.atDay(1)
            val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Make Sunday = 0
            val startDate = firstDayOfMonth.minusDays(firstDayOfWeek.toLong())
            val endDate = startDate.plusDays(41) // 6 weeks worth of days
            
            // Load logs for the entire calendar period
            val logs = database.dailyLogDao().getLogsInDateRange(startDate, endDate)
            monthlyLogs = logs
            
            // Calculate day data
            val dayData = mutableListOf<DayData>()
            var currentDatePtr = startDate
            
            repeat(42) { // 6 weeks * 7 days
                val logsForDay = logs.filter { it.date == currentDatePtr }
                val completedLogs = logsForDay.filter { 
                    it.status == MealStatus.COMPLETED || it.status == MealStatus.MODIFIED 
                }
                
                var totalCalories = 0
                var totalProtein = 0
                
                completedLogs.forEach { log ->
                    val meal = meals.find { it.id == log.mealId }
                    if (meal != null) {
                        totalCalories += log.manualCalories ?: meal.calories
                        totalProtein += log.manualProtein ?: meal.protein
                    }
                }
                
                dayData.add(
                    DayData(
                        date = currentDatePtr,
                        totalCalories = totalCalories,
                        totalProtein = totalProtein,
                        completedMeals = completedLogs.size,
                        totalMeals = meals.size,
                        isInCurrentMonth = currentDatePtr.month == currentMonth.month
                    )
                )
                
                currentDatePtr = currentDatePtr.plusDays(1)
            }
            
            dayDataList = dayData
            
            // Calculate streaks inline
            val monthData = dayData.filter { it.isInCurrentMonth }
            val today = LocalDate.now()
            val sortedData = monthData.sortedBy { it.date }
            
            // Calculate current streak (consecutive days up to today)
            var streak = 0
            var datePtr = today
            
            while (datePtr >= sortedData.firstOrNull()?.date) {
                val dayData = sortedData.find { it.date == datePtr }
                if (dayData != null && dayData.completedMeals > 0) {
                    streak++
                    datePtr = datePtr.minusDays(1)
                } else {
                    break
                }
            }
            currentStreak = streak
            
            // Calculate longest streak in the month
            var maxStreak = 0
            var tempStreak = 0
            
            sortedData.forEach { day ->
                if (day.completedMeals > 0) {
                    tempStreak++
                    maxStreak = maxOf(maxStreak, tempStreak)
                } else {
                    tempStreak = 0
                }
            }
            longestStreak = maxStreak
        }
    }
    

    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
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
                            text = "Calendar",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Streak info
                ModernCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StreakCard(
                            title = "Current Streak",
                            value = currentStreak,
                            icon = Icons.Default.LocalFireDepartment,
                            color = CardOrange
                        )
                        
                        StreakCard(
                            title = "Best This Month",
                            value = longestStreak,
                            icon = Icons.Default.Star,
                            color = BadgeGold
                        )
                    }
                }
                
                // Month navigation
                ModernCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { 
                                currentMonth = currentMonth.minusMonths(1)
                                selectedDate = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronLeft,
                                contentDescription = "Previous month",
                                tint = Color.White
                            )
                        }
                        
                        Text(
                            text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        IconButton(
                            onClick = { 
                                currentMonth = currentMonth.plusMonths(1)
                                selectedDate = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Next month",
                                tint = Color.White
                            )
                        }
                    }
                }
                
                // Calendar grid
                ModernCard {
                    Column {
                        // Day headers
                        Row(modifier = Modifier.fillMaxWidth()) {
                            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                                Text(
                                    text = day,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Calendar days
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(dayDataList) { dayData ->
                                CalendarDayCard(
                                    dayData = dayData,
                                    isSelected = selectedDate == dayData.date,
                                    onClick = { selectedDate = dayData.date }
                                )
                            }
                        }
                    }
                }
                
                // Selected day details
                selectedDate?.let { date ->
                    val selectedDayData = dayDataList.find { it.date == date }
                    if (selectedDayData != null) {
                        DayDetailsCard(
                            dayData = selectedDayData,
                            onClose = { selectedDate = null }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StreakCard(
    title: String,
    value: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = title,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CalendarDayCard(
    dayData: DayData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val isToday = dayData.date == LocalDate.now()
    val hasCompletedMeals = dayData.completedMeals > 0
    
    Box(
        modifier = Modifier
            .size(45.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> DarkPrimary
                    isToday -> CardOrange.copy(alpha = 0.3f)
                    hasCompletedMeals -> CompletedColor.copy(alpha = 0.3f)
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = dayData.date.dayOfMonth.toString(),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (dayData.isInCurrentMonth) Color.White else Color.White.copy(alpha = 0.3f)
            )
            
            if (dayData.isInCurrentMonth && hasCompletedMeals) {
                Text(
                    text = "${dayData.completedMeals}/${dayData.totalMeals}",
                    fontSize = 8.sp,
                    color = CompletedColor
                )
            }
        }
    }
}

@Composable
private fun DayDetailsCard(
    dayData: DayData,
    onClose: () -> Unit
) {
    ModernCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dayData.date.format(DateTimeFormatter.ofPattern("EEEE, MMM dd")),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DayStatItem("Meals", "${dayData.completedMeals}/${dayData.totalMeals}", CardBlue)
                DayStatItem("Calories", dayData.totalCalories.toString(), CardOrange)
                DayStatItem("Protein", "${dayData.totalProtein}g", ProteinColor)
            }
        }
    }
}

@Composable
private fun DayStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
} 