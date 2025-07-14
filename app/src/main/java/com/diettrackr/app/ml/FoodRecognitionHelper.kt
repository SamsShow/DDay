package com.diettrackr.app.ml

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

data class FoodRecognitionResult(
    val foodName: String,
    val confidence: Float,
    val estimatedCalories: Int,
    val estimatedProtein: Int,
    val estimatedCarbs: Int,
    val estimatedFats: Int
)

class FoodRecognitionHelper(private val context: Context) {
    
    private val geminiAnalyzer = GeminiFoodAnalyzer(context)
    
    // Expanded food database with common foods and their nutritional values per 100g
    private val foodDatabase = mapOf(
        // Grains & Starches
        "rice" to FoodNutrition(calories = 130, protein = 3, carbs = 28, fats = 0),
        "bread" to FoodNutrition(calories = 265, protein = 9, carbs = 49, fats = 3),
        "pasta" to FoodNutrition(calories = 131, protein = 5, carbs = 25, fats = 1),
        "quinoa" to FoodNutrition(calories = 120, protein = 4, carbs = 22, fats = 2),
        "oats" to FoodNutrition(calories = 68, protein = 2, carbs = 12, fats = 1),
        
        // Proteins
        "chicken" to FoodNutrition(calories = 165, protein = 31, carbs = 0, fats = 3),
        "meat" to FoodNutrition(calories = 250, protein = 26, carbs = 0, fats = 15),
        "fish" to FoodNutrition(calories = 120, protein = 22, carbs = 0, fats = 4),
        "eggs" to FoodNutrition(calories = 155, protein = 13, carbs = 1, fats = 11),
        "tofu" to FoodNutrition(calories = 76, protein = 8, carbs = 2, fats = 5),
        "beans" to FoodNutrition(calories = 127, protein = 8, carbs = 23, fats = 1),
        
        // Vegetables
        "vegetables" to FoodNutrition(calories = 25, protein = 2, carbs = 5, fats = 0),
        "salad" to FoodNutrition(calories = 20, protein = 2, carbs = 4, fats = 0),
        "broccoli" to FoodNutrition(calories = 34, protein = 3, carbs = 7, fats = 0),
        "carrots" to FoodNutrition(calories = 41, protein = 1, carbs = 10, fats = 0),
        "spinach" to FoodNutrition(calories = 23, protein = 3, carbs = 4, fats = 0),
        
        // Fruits
        "fruits" to FoodNutrition(calories = 60, protein = 1, carbs = 15, fats = 0),
        "apple" to FoodNutrition(calories = 52, protein = 0, carbs = 14, fats = 0),
        "banana" to FoodNutrition(calories = 89, protein = 1, carbs = 23, fats = 0),
        "orange" to FoodNutrition(calories = 47, protein = 1, carbs = 12, fats = 0),
        
        // Dairy
        "milk" to FoodNutrition(calories = 42, protein = 3, carbs = 5, fats = 1),
        "yogurt" to FoodNutrition(calories = 59, protein = 10, carbs = 3, fats = 0),
        "cheese" to FoodNutrition(calories = 113, protein = 7, carbs = 1, fats = 9),
        
        // Prepared Foods
        "soup" to FoodNutrition(calories = 100, protein = 8, carbs = 12, fats = 3),
        "sandwich" to FoodNutrition(calories = 300, protein = 15, carbs = 35, fats = 12),
        "pizza" to FoodNutrition(calories = 266, protein = 11, carbs = 33, fats = 10),
        "burger" to FoodNutrition(calories = 354, protein = 16, carbs = 30, fats = 17),
        "fries" to FoodNutrition(calories = 365, protein = 4, carbs = 63, fats = 17),
        "ice_cream" to FoodNutrition(calories = 137, protein = 2, carbs = 16, fats = 7),
        "cake" to FoodNutrition(calories = 257, protein = 3, carbs = 38, fats = 10),
        "cookies" to FoodNutrition(calories = 502, protein = 6, carbs = 64, fats = 25)
    )
    
    suspend fun recognizeFoodFromImage(imageUri: Uri): FoodRecognitionResult? = withContext(Dispatchers.IO) {
        try {
            // Convert URI to bitmap
            val bitmap = getBitmapFromUri(imageUri)
            if (bitmap == null) return@withContext null
            
            // For now, we'll use a simple approach
            // In a real implementation, you could:
            // 1. Use a lightweight TensorFlow Lite model
            // 2. Use ML Kit for image labeling
            // 3. Use a cloud API (Google Vision, Azure, etc.)
            
            // Simple color-based food detection (placeholder)
            val detectedFood = detectFoodByColor(bitmap)
            
            // Get nutrition info from database
            val nutrition = foodDatabase[detectedFood] ?: foodDatabase["vegetables"]!!
            
            FoodRecognitionResult(
                foodName = detectedFood.replace("_", " ").capitalize(),
                confidence = 0.75f, // Placeholder confidence
                estimatedCalories = nutrition.calories,
                estimatedProtein = nutrition.protein,
                estimatedCarbs = nutrition.carbs,
                estimatedFats = nutrition.fats
            )
            
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (e: IOException) {
            null
        }
    }
    
    private fun detectFoodByColor(bitmap: Bitmap): String {
        // Improved color analysis with better food classification
        // This is still a placeholder - ideally you'd use a proper ML model
        
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        var redSum = 0
        var greenSum = 0
        var blueSum = 0
        var brightPixels = 0
        var darkPixels = 0
        
        for (pixel in pixels) {
            val red = (pixel shr 16) and 0xFF
            val green = (pixel shr 8) and 0xFF
            val blue = pixel and 0xFF
            
            redSum += red
            greenSum += green
            blueSum += blue
            
            val brightness = (red + green + blue) / 3
            if (brightness > 180) brightPixels++
            if (brightness < 80) darkPixels++
        }
        
        val totalPixels = pixels.size
        val avgRed = redSum / totalPixels
        val avgGreen = greenSum / totalPixels
        val avgBlue = blueSum / totalPixels
        val brightness = (avgRed + avgGreen + avgBlue) / 3
        
        // More sophisticated food classification based on color patterns
        return when {
            // Green dominant foods
            avgGreen > avgRed + 20 && avgGreen > avgBlue + 20 -> {
                if (brightness > 120) "vegetables" else "salad"
            }
            // Brown/beige foods (rice, bread, etc.)
            avgRed in (avgGreen - 15)..(avgGreen + 15) && 
            avgBlue in (avgGreen - 20)..(avgGreen + 10) && 
            brightness in 100..180 -> {
                if (avgRed > 140) "bread" else "rice"
            }
            // Yellow/orange foods
            avgRed > avgBlue + 30 && avgGreen > avgBlue + 20 && 
            avgRed > avgGreen - 20 -> {
                if (brightness > 150) "cheese" else "pasta"
            }
            // Red/pink foods
            avgRed > avgGreen + 25 && avgRed > avgBlue + 25 -> {
                if (brightness < 120) "meat" else "fruits"
            }
            // Dark foods
            brightness < 80 -> "soup"
            // White/light foods
            brightness > 200 && brightPixels > totalPixels * 0.6 -> "milk"
            // Mixed colors (likely complex dishes)
            (avgRed - avgGreen).coerceAtLeast(0) + 
            (avgGreen - avgBlue).coerceAtLeast(0) + 
            (avgBlue - avgRed).coerceAtLeast(0) > 60 -> "pizza"
            // Default fallback
            else -> "vegetables"
        }
    }
    
    fun getFoodSuggestions(query: String): List<String> {
        return foodDatabase.keys
            .filter { it.contains(query.lowercase()) }
            .map { it.replace("_", " ").capitalize() }
            .take(5)
    }
    
    fun getNutritionForFood(foodName: String): FoodNutrition? {
        val key = foodName.lowercase().replace(" ", "_")
        return foodDatabase[key]
    }
    
    suspend fun analyzeFoodDescription(description: String): FoodRecognitionResult? {
        return geminiAnalyzer.analyzeFoodDescription(description)
    }
}

data class FoodNutrition(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fats: Int
)

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
} 