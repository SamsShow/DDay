package com.diettrackr.app.security

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class SecureKeyManager(private val context: Context) {
    
    companion object {
        // This is a simple obfuscation - in production, you'd use more sophisticated methods
        private const val ENCRYPTION_KEY = "DietTrackr2024!"
        private const val GEMINI_KEY_PREFIX = "gemini_"
    }
    
    fun getGeminiApiKey(): String {
        return try {
            // Get the raw API key from BuildConfig
            val rawKey = com.diettrackr.app.BuildConfig.GEMINI_API_KEY
            
            if (rawKey.isBlank()) {
                return ""
            }
            
            // Return the raw key directly for now - obfuscation was causing issues
            // In production, implement proper key management
            rawKey
            
        } catch (e: Exception) {
            // If anything fails, return empty string
            ""
        }
    }
    
    private fun obfuscateKey(key: String): String {
        return try {
            // Simple XOR obfuscation with a fixed key
            val obfuscated = key.mapIndexed { index, char ->
                (char.code xor ENCRYPTION_KEY[index % ENCRYPTION_KEY.length].code).toChar()
            }.joinToString("")
            
            // Base64 encode for additional obfuscation
            Base64.encodeToString(obfuscated.toByteArray(), Base64.DEFAULT)
        } catch (e: Exception) {
            key // Fallback to original key if obfuscation fails
        }
    }
    
    private fun deobfuscateKey(obfuscatedKey: String): String {
        return try {
            // Decode from Base64
            val decoded = Base64.decode(obfuscatedKey, Base64.DEFAULT)
            val decodedString = String(decoded)
            
            // Reverse XOR obfuscation
            val deobfuscated = decodedString.mapIndexed { index, char ->
                (char.code xor ENCRYPTION_KEY[index % ENCRYPTION_KEY.length].code).toChar()
            }.joinToString("")
            
            deobfuscated
        } catch (e: Exception) {
            obfuscatedKey // Fallback to obfuscated key if deobfuscation fails
        }
    }
    
    fun isApiKeyConfigured(): Boolean {
        val key = getGeminiApiKey()
        val isConfigured = key.isNotBlank() && 
                          key != "your_gemini_api_key_here" && 
                          key.length > 10 // Basic API key length check
        println("SecureKeyManager: API key configured: $isConfigured (length: ${key.length})")
        return isConfigured
    }
} 