package com.example.reciperatiobuilder.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.ui.graphics.vector.Path
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reciperatiobuilder.data.AppDatabase
import com.example.reciperatiobuilder.data.DisplayedIngredient
import com.example.reciperatiobuilder.data.RecipeIngredient
import com.example.reciperatiobuilder.data.RecipeIngredientDisplay
import com.example.reciperatiobuilder.data.WeightUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class IngredientViewModel(
    application: Application,
    private val recipeId: Long
): AndroidViewModel(application) {
    private val _selectedUnit = MutableStateFlow(WeightUnit.OZ)
    val selectedUnit: StateFlow<WeightUnit> = _selectedUnit.asStateFlow()
    private val recipeIngredientDao = AppDatabase.getDatabase(application).recipeIngredientDao()
    private val recipeDao = AppDatabase.getDatabase(application).recipeDao()
    private val _originalIngredients = MutableStateFlow<List<RecipeIngredientDisplay>>(emptyList())
//    val ingredients: StateFlow<List<RecipeIngredientDisplay>> = _ingredients.asStateFlow()
    private val _recipeName = MutableStateFlow<String?>("")
    val recipeName: StateFlow<String?> = _recipeName.asStateFlow()
    private val _dynamicallySelectedBaseIngredientName = MutableStateFlow<String?>(null)
    private val _baseIngredientWeightInput = MutableStateFlow("")
    val baseIngredientWeightInput: StateFlow<String> = _baseIngredientWeightInput.asStateFlow()

    val displayedIngredients: StateFlow<List<DisplayedIngredient>> =
        combine(
            _originalIngredients,
            _dynamicallySelectedBaseIngredientName,
            _baseIngredientWeightInput
        ) { ingredients, selectedBaseName, weightInputString ->
            if (ingredients.isEmpty()) {
                return@combine emptyList<DisplayedIngredient>()
            }
            val currentBaseWeightOunces = weightInputString.toDoubleOrNull()
            val newBaseIngredient = ingredients.find { it.name == selectedBaseName }
            val baseRatioToUseForCalculation: Double
            val baseName: String?

            if (newBaseIngredient != null) {
                baseRatioToUseForCalculation = newBaseIngredient.ratio
                baseName = newBaseIngredient.name
            } else {
                val originalBase = ingredients.find { it.ratio == 1.0 } ?: ingredients.first()
                baseRatioToUseForCalculation = originalBase.ratio
                baseName = originalBase.name
            }
            if (baseRatioToUseForCalculation == 0.0) { // Avoid division by zero
                return@combine ingredients.map {
                    DisplayedIngredient(
                        originalData = it,
                        ingredientName = it.name,
                        displayedRatio = it.ratio,
                        isDynamicallyBase = (it.ratio == 1.0 && selectedBaseName == null),
                        calculatedWeight = null
                    )
                }
            }

            ingredients.map { currentIngredient ->
                val newDisplayedRatio = currentIngredient.ratio / baseRatioToUseForCalculation
                val isCurrentDynamicBase = currentIngredient.name == baseName
                val calculatedWeight = if (isCurrentDynamicBase && currentBaseWeightOunces != null) {
                    currentBaseWeightOunces // For the base, its weight is the input
                } else if (currentBaseWeightOunces != null) {
                    newDisplayedRatio * currentBaseWeightOunces // For others, calculate based on ratio
                } else {
                    null // No base weight input, so no calculated weight
                }

                DisplayedIngredient(
                    originalData = currentIngredient,
                    ingredientName = currentIngredient.name,
                    displayedRatio = newDisplayedRatio,
                    isDynamicallyBase = isCurrentDynamicBase,
                    calculatedWeight = calculatedWeight
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        ) // Keep it as StateFlow

    val totalYield: StateFlow<Double?> = displayedIngredients.map { ingredients ->
        if( ingredients.any {it.calculatedWeight == null} || ingredients.isEmpty()) {
            null
        } else {
            ingredients.sumOf { it.calculatedWeight ?: 0.0 }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = null
    )

    init {
        loadIngredients()
    }
    fun loadIngredients() {
        viewModelScope.launch {
            _originalIngredients.value = recipeIngredientDao.getRecipeIngredients(recipeId)
            val fetchedRecipe = recipeDao.getById(recipeId)
            _recipeName.value = fetchedRecipe?.name
        }
    }

    fun selectNewBaseIngredient(ingredientName: String) {
        _dynamicallySelectedBaseIngredientName.value = ingredientName
    }
    fun onBaseIngredientWeightChange(weight: String) {
        _baseIngredientWeightInput.value = weight // Allow any string for input flexibility, parse in combine
    }
    fun setSelectedUnit(unit: WeightUnit) {
        _selectedUnit.value = unit
    }
    class Factory(
        private val application: Application,
        private val recipeId: Long
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(IngredientViewModel::class.java)) {
                return IngredientViewModel(application, recipeId) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}