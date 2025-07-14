package com.diettrackr.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.diettrackr.app.ml.FoodRecognitionHelper
import com.diettrackr.app.ml.FoodRecognitionResult
import com.diettrackr.app.ui.components.ManualEntryData
import com.diettrackr.app.ui.components.ModernCard
import com.diettrackr.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodRecognitionScreen(
    navController: NavController,
    onFoodRecognized: (ManualEntryData) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val foodRecognitionHelper = remember { FoodRecognitionHelper(context) }
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var recognitionResult by remember { mutableStateOf<FoodRecognitionResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            scope.launch {
                isProcessing = true
                recognitionResult = foodRecognitionHelper.recognizeFoodFromImage(it)
                isProcessing = false
            }
        }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && selectedImageUri != null) {
            scope.launch {
                isProcessing = true
                recognitionResult = foodRecognitionHelper.recognizeFoodFromImage(selectedImageUri!!)
                isProcessing = false
            }
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
                            text = "Food Recognition",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
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
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Image capture section
                item {
                    ModernCard {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Take Food Photo",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Camera and Gallery buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        // For camera, we need to create a temporary URI
                                        // In a real app, you'd use FileProvider
                                        galleryLauncher.launch("image/*")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CardOrange
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gallery")
                                }
                                
                                Button(
                                    onClick = {
                                        // Camera functionality would go here
                                        // For now, just launch gallery
                                        galleryLauncher.launch("image/*")
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CardBlue
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Camera")
                                }
                            }
                        }
                    }
                }
                
                // Selected image
                selectedImageUri?.let { uri ->
                    item {
                        ModernCard {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Selected Image",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected food image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
                
                // Processing indicator
                if (isProcessing) {
                    item {
                        ModernCard {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = CardOrange,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Analyzing food...",
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
                
                // Recognition result
                recognitionResult?.let { result ->
                    item {
                        ModernCard {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Text(
                                    text = "Detected Food",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Food name and confidence
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = result.foodName,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    
                                    Text(
                                        text = "${(result.confidence * 100).toInt()}%",
                                        fontSize = 14.sp,
                                        color = CardOrange
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Nutrition info
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    NutritionItem("Calories", result.estimatedCalories, CardOrange)
                                    NutritionItem("Protein", result.estimatedProtein, ProteinColor)
                                    NutritionItem("Carbs", result.estimatedCarbs, CarbsColor)
                                    NutritionItem("Fats", result.estimatedFats, FatsColor)
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                // Action buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            onFoodRecognized(
                                                ManualEntryData(
                                                    calories = result.estimatedCalories,
                                                    protein = result.estimatedProtein,
                                                    carbs = result.estimatedCarbs,
                                                    fats = result.estimatedFats
                                                )
                                            )
                                            navController.navigateUp()
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CompletedColor
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Use This")
                                    }
                                    
                                    Button(
                                        onClick = {
                                            selectedImageUri = null
                                            recognitionResult = null
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SkippedColor
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Try Again")
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Manual food search
                item {
                    ModernCard {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            Text(
                                text = "Or Search Food",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OutlinedTextField(
                                                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    showSuggestions = it.isNotEmpty()
                                    println("UI: Search query changed to: '$it'")
                                },
                                placeholder = {
                                    Text(
                                        text = "Search for food...",
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = CardOrange,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    cursorColor = CardOrange
                                ),
                                singleLine = true
                            )
                            
                            if (showSuggestions) {
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Show both database suggestions and AI analysis
                                val suggestions = foodRecognitionHelper.getFoodSuggestions(searchQuery)
                                
                                // Database suggestions
                                suggestions.forEach { suggestion ->
                                    val nutrition = foodRecognitionHelper.getNutritionForFood(suggestion)
                                    if (nutrition != null) {
                                        FoodSuggestionItem(
                                            foodName = suggestion,
                                            nutrition = nutrition,
                                            onClick = {
                                                onFoodRecognized(
                                                    ManualEntryData(
                                                        calories = nutrition.calories,
                                                        protein = nutrition.protein,
                                                        carbs = nutrition.carbs,
                                                        fats = nutrition.fats
                                                    )
                                                )
                                                navController.navigateUp()
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                                
                                // Separator if we have both database suggestions and AI analysis
                                if (suggestions.isNotEmpty() && searchQuery.length > 3) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "AI Analysis:",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = CardOrange
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                                
                                // AI analysis for complex queries (always show if query is long enough)
                                if (searchQuery.length > 3) {
                                    var aiResult by remember(searchQuery) { mutableStateOf<FoodRecognitionResult?>(null) }
                                    var isAnalyzing by remember(searchQuery) { mutableStateOf(false) }
                                    var aiError by remember(searchQuery) { mutableStateOf<String?>(null) }
                                    
                                    LaunchedEffect(searchQuery) {
                                        if (searchQuery.length > 3) {
                                            isAnalyzing = true
                                            aiError = null
                                            aiResult = null
                                            try {
                                                println("UI: Starting AI analysis for: '$searchQuery'")
                                                val result = foodRecognitionHelper.analyzeFoodDescription(searchQuery)
                                                aiResult = result
                                                if (result == null) {
                                                    aiError = "AI analysis returned no result"
                                                    println("UI: AI analysis returned null")
                                                } else {
                                                    println("UI: AI analysis success: ${result.foodName}")
                                                }
                                            } catch (e: Exception) {
                                                aiError = "AI analysis failed: ${e.message}"
                                                println("UI: Error during AI analysis: ${e.message}")
                                                e.printStackTrace()
                                            }
                                            isAnalyzing = false
                                        }
                                    }
                                    
                                    if (isAnalyzing) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                color = CardOrange,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Analyzing with AI...",
                                                fontSize = 14.sp,
                                                color = Color.White.copy(alpha = 0.7f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    
                                    // Show AI result if available
                                    aiResult?.let { result ->
                                        FoodSuggestionItem(
                                            foodName = "${result.foodName} (AI)",
                                            nutrition = com.diettrackr.app.ml.FoodNutrition(
                                                calories = result.estimatedCalories,
                                                protein = result.estimatedProtein,
                                                carbs = result.estimatedCarbs,
                                                fats = result.estimatedFats
                                            ),
                                            onClick = {
                                                onFoodRecognized(
                                                    ManualEntryData(
                                                        calories = result.estimatedCalories,
                                                        protein = result.estimatedProtein,
                                                        carbs = result.estimatedCarbs,
                                                        fats = result.estimatedFats
                                                    )
                                                )
                                                navController.navigateUp()
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    
                                    // Show error message if AI analysis failed
                                    if (!isAnalyzing && aiResult == null && aiError != null) {
                                        Text(
                                            text = "⚠️ $aiError",
                                            fontSize = 12.sp,
                                            color = Color.Red.copy(alpha = 0.8f),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                    
                                    // Show fallback message if no AI result but no error
                                    if (!isAnalyzing && aiResult == null && aiError == null && searchQuery.length > 3) {
                                        Text(
                                            text = "💡 Try: '1 cup rice', '2 slices bread', '1 bowl pasta'",
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.6f),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NutritionItem(
    label: String,
    value: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value.toString(),
            fontSize = 18.sp,
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

@Composable
private fun FoodSuggestionItem(
    foodName: String,
    nutrition: com.diettrackr.app.ml.FoodNutrition,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = foodName,
                fontSize = 16.sp,
                color = Color.White
            )
            
            Text(
                text = "${nutrition.calories} cal",
                fontSize = 14.sp,
                color = CardOrange
            )
        }
    }
} 