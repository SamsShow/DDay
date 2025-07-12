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
import com.diettrackr.app.data.models.Meal
import com.diettrackr.app.data.models.MealComponent
import com.diettrackr.app.ui.components.GlassCard
import com.diettrackr.app.ui.theme.DarkPrimary
import com.diettrackr.app.ui.theme.DarkSurface
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMealScreen(
    navController: NavController,
    mealId: Int
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    
    var meal by remember { mutableStateOf<Meal?>(null) }
    var components by remember { mutableStateOf<List<MealComponent>>(emptyList()) }
    var showAddComponentDialog by remember { mutableStateOf(false) }
    var showEditMealDialog by remember { mutableStateOf(false) }
    
    // Load data
    LaunchedEffect(key1 = mealId) {
        if (mealId > 0) {
            meal = database.mealDao().getMealById(mealId)
            components = database.mealDao().getMealComponents(mealId)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Edit Meal",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { showEditMealDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Meal Details",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddComponentDialog = true },
                containerColor = DarkPrimary,
                contentColor = Color.Black
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Component"
                )
            }
        }
    ) { paddingValues ->
        meal?.let { currentMeal ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Meal header
                item {
                    GlassCard {
                        Column {
                            Text(
                                text = currentMeal.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = currentMeal.time.format(DateTimeFormatter.ofPattern("h:mm a")),
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Macros
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                NutrientInfo("Protein", "${currentMeal.protein}g")
                                NutrientInfo("Carbs", "${currentMeal.carbs}g")
                                NutrientInfo("Fats", "${currentMeal.fats}g")
                                NutrientInfo("Calories", "${currentMeal.calories}")
                            }
                        }
                    }
                }
                
                // Components title
                item {
                    Text(
                        text = "Meal Components",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                // Components list
                if (components.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No components yet. Add some!",
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    items(components) { component ->
                        ComponentItem(
                            component = component,
                            onEditClick = { /* Will be implemented later */ },
                            onDeleteClick = {
                                scope.launch {
                                    database.mealDao().deleteMealComponent(component)
                                    components = database.mealDao().getMealComponents(mealId)
                                    
                                    // Update meal macros
                                    updateMealMacros(database, mealId, components)
                                }
                            }
                        )
                    }
                }
                
                // Bottom spacing
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        } ?: run {
            // Meal not found
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Meal not found",
                    fontSize = 18.sp,
                    color = Color.Gray
                )
            }
        }
    }
    
    // Add component dialog
    if (showAddComponentDialog && meal != null) {
        AddComponentDialog(
            mealId = mealId,
            onDismiss = { showAddComponentDialog = false },
            onAddComponent = { name, quantity, protein, carbs, fats ->
                scope.launch {
                    val component = MealComponent(
                        mealId = mealId,
                        name = name,
                        quantity = quantity,
                        protein = protein,
                        carbs = carbs,
                        fats = fats,
                        isDefault = false
                    )
                    database.mealDao().insertMealComponent(component)
                    
                    // Refresh components
                    components = database.mealDao().getMealComponents(mealId)
                    
                    // Update meal macros
                    updateMealMacros(database, mealId, components)
                }
            }
        )
    }
    
    // Edit meal dialog
    if (showEditMealDialog && meal != null) {
        EditMealDetailsDialog(
            meal = meal!!,
            onDismiss = { showEditMealDialog = false },
            onSave = { name, time, calories ->
                scope.launch {
                    val updatedMeal = meal!!.copy(
                        name = name,
                        time = time,
                        calories = calories
                    )
                    database.mealDao().updateMeal(updatedMeal)
                    meal = updatedMeal
                }
            }
        )
    }
}

suspend fun updateMealMacros(database: AppDatabase, mealId: Int, components: List<MealComponent>) {
    val meal = database.mealDao().getMealById(mealId) ?: return
    
    // Calculate macros from components
    val protein = components.sumOf { it.protein }
    val carbs = components.sumOf { it.carbs }
    val fats = components.sumOf { it.fats }
    
    // Update meal
    val updatedMeal = meal.copy(
        protein = protein,
        carbs = carbs,
        fats = fats
    )
    database.mealDao().updateMeal(updatedMeal)
}

@Composable
fun ComponentItem(
    component: MealComponent,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
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
            // Name and quantity
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = component.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = component.quantity,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            // Macros
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MacroText("P", component.protein)
                MacroText("C", component.carbs)
                MacroText("F", component.fats)
                
                // Delete icon
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Component",
                        tint = Color.Red.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
fun MacroText(label: String, value: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = "$value",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun NutrientInfo(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddComponentDialog(
    mealId: Int,
    onDismiss: () -> Unit,
    onAddComponent: (name: String, quantity: String, protein: Int, carbs: Int, fats: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("0") }
    var carbs by remember { mutableStateOf("0") }
    var fats by remember { mutableStateOf("0") }
    
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
                    text = "Add Component",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Component Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Quantity
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (e.g., 100g, 1 cup)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Macros
                Text(
                    text = "Macros",
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
                            onAddComponent(
                                name,
                                quantity,
                                protein.toIntOrNull() ?: 0,
                                carbs.toIntOrNull() ?: 0,
                                fats.toIntOrNull() ?: 0
                            )
                            onDismiss()
                        },
                        enabled = name.isNotEmpty() && quantity.isNotEmpty()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMealDetailsDialog(
    meal: Meal,
    onDismiss: () -> Unit,
    onSave: (name: String, time: LocalTime, calories: Int) -> Unit
) {
    var name by remember { mutableStateOf(meal.name) }
    
    // Time values
    val initialHour = if (meal.time.hour > 12) meal.time.hour - 12 else if (meal.time.hour == 0) 12 else meal.time.hour
    val initialIsAm = meal.time.hour < 12 || meal.time.hour == 0
    
    var timeHour by remember { mutableStateOf(initialHour.toString()) }
    var timeMinute by remember { mutableStateOf(meal.time.minute.toString().padStart(2, '0')) }
    var timeIsAm by remember { mutableStateOf(initialIsAm) }
    
    var calories by remember { mutableStateOf(meal.calories.toString()) }
    
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
                    text = "Edit Meal Details",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name
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
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Calories (editable, but protein/carbs/fats are calculated from components)
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
                            
                            onSave(
                                name,
                                time,
                                calories.toIntOrNull() ?: meal.calories
                            )
                            onDismiss()
                        },
                        enabled = name.isNotEmpty() &&
                                timeHour.isNotEmpty() &&
                                timeMinute.isNotEmpty() &&
                                calories.toIntOrNull() != null
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
} 