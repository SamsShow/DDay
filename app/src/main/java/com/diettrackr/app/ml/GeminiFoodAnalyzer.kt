package com.diettrackr.app.ml

import android.content.Context
import com.diettrackr.app.security.SecureKeyManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class GeminiFoodAnalyzer(private val context: Context) {
    
    private var generativeModel: GenerativeModel? = null
    private val secureKeyManager = SecureKeyManager(context)
    
    init {
        // Initialize Gemini model only if API key is properly configured
        if (secureKeyManager.isApiKeyConfigured()) {
            try {
                val apiKey = getApiKey()
                println("GeminiFoodAnalyzer: Initializing with API key length: ${apiKey.length}")
                generativeModel = GenerativeModel(
                    modelName = "gemini-pro",
                    apiKey = apiKey
                )
                println("GeminiFoodAnalyzer: Successfully initialized Gemini model")
            } catch (e: Exception) {
                println("GeminiFoodAnalyzer: Failed to initialize Gemini model: ${e.message}")
                e.printStackTrace()
            }
        } else {
            println("GeminiFoodAnalyzer: API key not configured")
        }
    }
    
    private fun getApiKey(): String {
        return secureKeyManager.getGeminiApiKey()
    }
    
    suspend fun analyzeFoodDescription(description: String): FoodRecognitionResult? = withContext(Dispatchers.IO) {
        try {
            if (generativeModel == null) {
                println("GeminiFoodAnalyzer: GenerativeModel is null, cannot analyze")
                return@withContext null
            }
            
            println("GeminiFoodAnalyzer: Analyzing description: $description")
            
            val prompt = """
                Analyze this food description and extract nutritional information. 
                Return ONLY a JSON object with the following structure:
                {
                    "foodName": "recognized food name",
                    "quantity": "quantity description",
                    "calories": estimated_calories_per_serving,
                    "protein": protein_grams,
                    "carbs": carbs_grams,
                    "fats": fats_grams,
                    "confidence": confidence_score_0_to_1,
                    "notes": "any relevant notes about portion size or preparation"
                }
                
                Food description: "$description"
                
                Common serving sizes to consider:
                - 1 cup rice = ~200 calories, 4g protein, 44g carbs, 0g fat
                - 1 slice bread = ~80 calories, 3g protein, 15g carbs, 1g fat
                - 1 medium banana = ~105 calories, 1g protein, 27g carbs, 0g fat
                - 1 large egg = ~70 calories, 6g protein, 0g carbs, 5g fat
                - 1 cup milk = ~150 calories, 8g protein, 12g carbs, 8g fat
                - 1 medium apple = ~95 calories, 0g protein, 25g carbs, 0g fat
                - 1 cup cooked pasta = ~200 calories, 7g protein, 40g carbs, 1g fat
                - 1 cup cooked chicken breast = ~165 calories, 31g protein, 0g carbs, 3g fat
                
                Be realistic with estimates and consider typical portion sizes.
            """.trimIndent()
            
            val response = generativeModel?.generateContent(prompt)
            val responseText = response?.text
            
            println("GeminiFoodAnalyzer: Response received: ${responseText?.take(100)}...")
            
            if (responseText != null) {
                parseGeminiResponse(responseText, description)
            } else {
                println("GeminiFoodAnalyzer: No response text received")
                null
            }
            
        } catch (e: Exception) {
            println("GeminiFoodAnalyzer: Error during analysis: ${e.message}")
            e.printStackTrace()
            // Fallback to simple keyword matching
            fallbackAnalysis(description)
        }
    }
    
    private fun parseGeminiResponse(responseText: String, originalDescription: String): FoodRecognitionResult? {
        try {
            // Extract JSON from response (Gemini might add extra text)
            val jsonStart = responseText.indexOf('{')
            val jsonEnd = responseText.lastIndexOf('}') + 1
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val jsonString = responseText.substring(jsonStart, jsonEnd)
                val json = JSONObject(jsonString)
                
                return FoodRecognitionResult(
                    foodName = json.optString("foodName", originalDescription),
                    confidence = json.optDouble("confidence", 0.7).toFloat(),
                    estimatedCalories = json.optInt("calories", 0),
                    estimatedProtein = json.optInt("protein", 0),
                    estimatedCarbs = json.optInt("carbs", 0),
                    estimatedFats = json.optInt("fats", 0)
                )
            }
        } catch (e: Exception) {
            // JSON parsing failed
        }
        
        return fallbackAnalysis(originalDescription)
    }
    
    private fun fallbackAnalysis(description: String): FoodRecognitionResult? {
        // Simple keyword-based fallback
        println("GeminiFoodAnalyzer: Using fallback analysis for: $description")
        val lowerDescription = description.lowercase()
        
        val foodMap = mapOf(
            "rice" to Triple(130, 3, 28),      // calories, protein, carbs
            "bread" to Triple(80, 3, 15),
            "banana" to Triple(105, 1, 27),
            "apple" to Triple(95, 0, 25),
            "egg" to Triple(70, 6, 0),
            "milk" to Triple(150, 8, 12),
            "chicken" to Triple(165, 31, 0),
            "pasta" to Triple(200, 7, 40),
            "fish" to Triple(120, 22, 0),
            "vegetables" to Triple(25, 2, 5)
        )
        
        for ((food, nutritionData) in foodMap) {
            if (lowerDescription.contains(food)) {
                // Estimate quantity multiplier
                val multiplier = estimateQuantityMultiplier(lowerDescription)
                
                println("GeminiFoodAnalyzer: Fallback found '$food' with multiplier $multiplier")
                
                val (calories, protein, carbs) = nutritionData
                return FoodRecognitionResult(
                    foodName = "${food.capitalize()} (estimated)",
                    confidence = 0.6f,
                    estimatedCalories = (calories * multiplier).toInt(),
                    estimatedProtein = (protein * multiplier).toInt(),
                    estimatedCarbs = (carbs * multiplier).toInt(),
                    estimatedFats = 1 // Simple default for fats
                )
            }
        }
        
        println("GeminiFoodAnalyzer: Fallback found no matches for: $description")
        return null
    }
    
    private fun estimateQuantityMultiplier(description: String): Double {
        // Simple quantity estimation
        return when {
            description.contains("1/2") || description.contains("half") -> 0.5
            description.contains("1/4") || description.contains("quarter") -> 0.25
            description.contains("2") && (description.contains("slice") || description.contains("piece")) -> 2.0
            description.contains("3") && (description.contains("slice") || description.contains("piece")) -> 3.0
            description.contains("bowl") || description.contains("cup") -> 1.0
            description.contains("large") -> 1.5
            description.contains("small") -> 0.7
            description.contains("medium") -> 1.0
            else -> 1.0
        }
    }
    
    private fun String.capitalize(): String {
        return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
} 