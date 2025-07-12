# DietTrackr App Summary

## Overview

DietTrackr is a modern Android application built with Jetpack Compose that helps users track their diet plan. The app features a clean, minimal dark-themed UI with glass gradient cards for a premium feel. It was designed specifically to track a structured diet plan with fixed meals, macro tracking, and progress monitoring.

## Key Features

### 1. Diet Plan Tracking
- **Meal Status Tracking**: Mark meals as completed, skipped, or modified
- **Daily Progress View**: See your progress for the current day at a glance
- **Macro Tracking**: Monitor protein, carbs, fats, and calories

### 2. Meal Management
- **Customizable Meal Plans**: Edit existing meals or create new ones
- **Component-Level Tracking**: Add individual food components to meals with their macro information
- **Time-Based Organization**: Meals are organized chronologically through the day

### 3. Progress Monitoring
- **Weight Tracking**: Log your weight and see changes over time
- **Goal Setting**: Set weight goals and track progress
- **Weekly Statistics**: View meal completion rate, weight change, and average macro consumption

### 4. User Experience
- **Dark Theme**: Sleek dark theme with glass gradient cards
- **Notifications**: Get reminders about upcoming meals
- **Intuitive Interface**: Clean, minimal UI that's easy to navigate

## Technical Implementation

### Architecture
- **MVVM Pattern**: Clear separation between UI, business logic, and data
- **Room Database**: Local SQLite database for persistent storage
- **Jetpack Compose**: Modern UI toolkit for building native Android UIs
- **WorkManager**: For scheduling meal notifications

### Data Structure
- **User**: Profile information including current and goal weight
- **Meal**: Top-level meal information with time, name, and total macros
- **MealComponent**: Individual food items within a meal with their own macro information
- **DailyLog**: Daily tracking for each meal's completion status
- **WeightEntry**: Historical weight entries for progress tracking

### UI Components
- **GlassCard**: Custom UI component for glass-effect cards
- **MacrosProgressBar**: Visual representation of macro consumption vs. targets
- **MealCard**: Expandable cards showing meal details and components
- **WeightTracker**: Interactive weight tracking with graph visualization

## How to Use

### First-Time Setup
1. Launch the app - your default meal plan based on the provided diet chart will be loaded
2. Go to the Progress tab to set your weight goal

### Daily Usage
1. On the Home screen, view upcoming meals
2. Tap to expand meal details and mark as completed, skipped, or modified
3. See your daily macro and calorie consumption update in real time
4. Check the Progress tab to see your weekly stats and add weight entries

### Customization
1. Go to the Meal Plan tab to see all meals
2. Edit any meal to change time, name, or components
3. Add new meals as needed
4. For each meal, add individual components with their macro information

### Settings
1. Control notification preferences
2. Reset data if needed
3. View app information

## Edge Cases Handled

1. **Diet Modifications**: Users can mark meals as modified when not following exactly
2. **Substitutions**: Components can be modified or replaced
3. **Data Reset**: Option to reset all data and start fresh
4. **Notification Permissions**: Proper handling of notification permissions on Android 13+
5. **Empty States**: Appropriate empty state handling when no data is available

## Future Enhancements

1. **Cloud Sync**: Backup and sync data across devices
2. **Recipe Integration**: Add recipes and automatically calculate macros
3. **Photo Logging**: Take photos of meals for visual tracking
4. **Food Database**: Integration with a food database for easier meal component creation
5. **Social Features**: Share progress and meal plans with friends or coaches 