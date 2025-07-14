# 🔑 API Key Setup Guide

## How to Add Your Gemini API Key (Safely!)

### Step 1: Get Your API Key
1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Sign in with your Google account
3. Click "Create API Key"
4. Copy the generated key

### Step 2: Add to Your App (Safely!)
1. Open the file `local.properties` in your project root
2. Add this line (replace with your actual key):
   ```
   GEMINI_API_KEY=your_actual_api_key_here
   ```
3. Save the file

### Step 3: Rebuild Your App
- Clean and rebuild your project
- The API key will be automatically encrypted and secured

## 🔒 Security Features Built-In:

✅ **Never Committed**: `local.properties` is in `.gitignore`  
✅ **Encrypted Storage**: Keys are obfuscated in the app  
✅ **Fallback Safe**: App works even without API key  
✅ **No Hardcoding**: Keys are never in source code  

## 🚀 How It Works:

1. **local.properties** → Your API key (local only)
2. **BuildConfig** → Compiles key into app (obfuscated)
3. **SecureKeyManager** → Encrypts and manages keys
4. **GeminiFoodAnalyzer** → Uses keys securely

## 💡 Example Usage:

Once set up, users can type:
- "1 bowl of rice" → AI analyzes and gives calories
- "2 slices of bread" → AI calculates nutrition
- "large apple" → AI estimates portion size

The app will automatically use Gemini AI for complex queries and fall back to the database for simple ones.

## 🛡️ Security Notes:

- Your API key is **never** uploaded to GitHub
- Keys are **encrypted** in the app
- App works **offline** for basic features
- **No backend** required - everything runs on device 