package com.diettrackr.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
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
import com.diettrackr.app.data.models.Meal
import com.diettrackr.app.data.models.MealComponent
import com.diettrackr.app.ui.components.GlassCard
import com.diettrackr.app.ui.theme.DarkPrimary
import com.diettrackr.app.ui.theme.DarkSurface
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    
    var meals by remember { mutableStateOf<List<Meal>>(emptyList()) }
    var mealComponents by remember { mutableStateOf<Map<Int, List<MealComponent>>>(emptyMap()) }
    var showAddMealDialog by remember { mutableStateOf(false) }
    
    // Load data
    LaunchedEffect(key1 = Unit) {
        database.mealDao().getAllMealsFlow().collect { allMeals ->
            meals = allMeals
            
            // Load components for each meal
            val componentsMap = mutableMapOf<Int, List<MealComponent>>()
            allMeals.forEach { meal ->
                val components = database.mealDao().getMealComponents(meal.id)
                componentsMap[meal.id] = components
            }
            mealComponents = componentsMap
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Meal Plan",
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMealDialog = true },
                containerColor = DarkPrimary,
                contentColor = Color.Black
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Meal"
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(
                    text = "Your Diet Plan",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Manage and customize your meal plan",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            items(meals) { meal ->
                MealPlanItem(
                    meal = meal,
                    components = mealComponents[meal.id] ?: emptyList(),
                    onEditClick = {
                        navController.navigate("edit_meal/${meal.id}")
                    }
                )
            }
            
            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
    
    if (showAddMealDialog) {
        AddMealDialog(
            onDismiss = { showAddMealDialog = false },
            onAddMeal = { name, time, protein, carbs, fats, calories ->
                scope.launch {
                    val newMeal = Meal(
                        name = name,
                        time = time,
                        protein = protein,
                        carbs = carbs,
                        fats = fats,
                        calories = calories,
                        isDefault = false
                    )
                    val mealId = database.mealDao().insertMeal(newMeal).toInt()
                    
                    // Navigate to edit screen to add components
                    navController.navigate("edit_meal/$mealId")
                    showAddMealDialog = false
                }
            }
        )
    }
}

@Composable
fun MealPlanItem(
    meal: Meal,
    components: List<MealComponent>,
    onEditClick: () -> Unit
) {
    GlassCard {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = meal.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = meal.time.format(DateTimeFormatter.ofPattern("h:mm a")),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Meal",
                        tint = DarkPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Macros summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroSummary("P", meal.protein)
                MacroSummary("C", meal.carbs)
                MacroSummary("F", meal.fats)
                MacroSummary("Cal", meal.calories)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Components summary
            if (components.isNotEmpty()) {
                Text(
                    text = "${components.size} components",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MacroSummary(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = value.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMealDialog(
    onDismiss: () -> Unit,
    onAddMeal: (name: String, time: LocalTime, protein: Int, carbs: Int, fats: Int, calories: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var timeHour by remember { mutableStateOf("12") }
    var timeMinute by remember { mutableStateOf("00") }
    var timeIsAm by remember { mutableStateOf(true) }
    var protein by remember { mutableStateOf("0") }
    var carbs by remember { mutableStateOf("0") }
    var fats by remember { mutableStateOf("0") }
    var calories by remember { mutableStateOf("0") }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Add New Meal",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Meal name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Meal Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Time picker
                Text(
                    text = "Time",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hour
                    OutlinedTextField(
                        value = timeHour,
                        onValueChange = { 
                            if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 1..12)) {
                                timeHour = it 
                            }
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Hour") },
                        singleLine = true
                    )
                    
                    Text(
                        text = ":",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontSize = 20.sp
                    )
                    
                    // Minute
                    OutlinedTextField(
                        value = timeMinute,
                        onValueChange = { 
                            if (it.isEmpty() || (it.toIntOrNull() != null && it.toInt() in 0..59)) {
                                timeMinute = it 
                            }
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Min") },
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // AM/PM
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = timeIsAm,
                            onClick = { timeIsAm = true }
                        )
                        Text("AM")
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        RadioButton(
                            selected = !timeIsAm,
                            onClick = { timeIsAm = false }
                        )
                        Text("PM")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Macros
                Text(
                    text = "Macronutrients",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Protein
                OutlinedTextField(
                    value = protein,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) protein = it },
                    label = { Text("Protein (g)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Carbs
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) carbs = it },
                    label = { Text("Carbs (g)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Fats
                OutlinedTextField(
                    value = fats,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) fats = it },
                    label = { Text("Fats (g)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Calories
                OutlinedTextField(
                    value = calories,
                    onValueChange = { if (it.isEmpty() || it.toIntOrNull() != null) calories = it },
                    label = { Text("Calories") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Buttons
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
                            val hour = timeHour.toIntOrNull() ?: 12
                            val minute = timeMinute.toIntOrNull() ?: 0
                            
                            // Convert to 24-hour format
                            val adjusted24Hour = when {
                                timeIsAm && hour == 12 -> 0 // 12 AM -> 0
                                !timeIsAm && hour < 12 -> hour + 12 // 1-11 PM -> 13-23
                                else -> hour // Keep as is for 1-11 AM and 12 PM
                            }
                            
                            val time = LocalTime.of(adjusted24Hour, minute)
                            
                            onAddMeal(
                                name,
                                time,
                                protein.toIntOrNull() ?: 0,
                                carbs.toIntOrNull() ?: 0,
                                fats.toIntOrNull() ?: 0,
                                calories.toIntOrNull() ?: 0
                            )
                        },
                        enabled = name.isNotEmpty() &&
                                timeHour.isNotEmpty() &&
                                timeMinute.isNotEmpty() &&
                                protein.toIntOrNull() != null &&
                                carbs.toIntOrNull() != null &&
                                fats.toIntOrNull() != null &&
                                calories.toIntOrNull() != null
                    ) {
                        Text("Add Meal")
                    }
                }
            }
        }
    }
} 