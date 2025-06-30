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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class IngredientViewModel(
    application: Application,
    private val recipeId: Long
): AndroidViewModel(application) {
    private val recipeIngredientDao = AppDatabase.getDatabase(application).recipeIngredientDao()
    private val recipeDao = AppDatabase.getDatabase(application).recipeDao()
    private val _originalIngredients = MutableStateFlow<List<RecipeIngredientDisplay>>(emptyList())
//    val ingredients: StateFlow<List<RecipeIngredientDisplay>> = _ingredients.asStateFlow()
    private val _recipeName = MutableStateFlow<String?>("")
    val recipeName: StateFlow<String?> = _recipeName.asStateFlow()
    private val _dynamicallySelectedBaseIngredientName = MutableStateFlow<String?>(null)
    val displayedIngredients: StateFlow<List<DisplayedIngredient>> =
        combine(
            _originalIngredients,
            _dynamicallySelectedBaseIngredientName
        ) { ingredients, selectedBaseName ->
            if (ingredients.isEmpty()) {
                return@combine emptyList<DisplayedIngredient>()
            }

            val newBaseIngredient = ingredients.find { it.name == selectedBaseName }
            val baseRatioToUseForCalculation: Double

            if (newBaseIngredient != null) {
                // User has selected a dynamic base
                baseRatioToUseForCalculation = newBaseIngredient.ratio
            } else {
                // No dynamic base selected by user, find the original base ingredient (ratio == 1.0)
                // Or fallback to the first ingredient if none has ratio 1.0
                val originalBase = ingredients.find { it.ratio == 1.0 } ?: ingredients.first()
                baseRatioToUseForCalculation = originalBase.ratio
                // Optionally, set _dynamicallySelectedBaseIngredientName to this originalBase.ingredientName
                // if you want it to be "selected" by default. For now, let's keep it explicit.
            }
            if (baseRatioToUseForCalculation == 0.0) { // Avoid division by zero
                Log.w("IngredientViewModel", "Base ratio for calculation is zero. Displaying original ratios.")
                return@combine ingredients.map {
                    DisplayedIngredient(
                        originalData = it,
                        ingredientName = it.name,
                        displayedRatio = it.ratio,
                        isDynamicallyBase = (it.ratio == 1.0 && selectedBaseName == null) // approximate original base
                    )
                }
            }

            ingredients.map { currentIngredient ->
                val newDisplayedRatio = currentIngredient.ratio / baseRatioToUseForCalculation
                DisplayedIngredient(
                    originalData = currentIngredient,
                    ingredientName = currentIngredient.name,
                    displayedRatio = newDisplayedRatio,
                    // An ingredient is the "dynamic base" if its name matches the selected one,
                    // OR if no dynamic one is selected and this one was the original base.
                    isDynamicallyBase = if (newBaseIngredient != null) {
                        currentIngredient.name == newBaseIngredient.name
                    } else {
                        // if selectedBaseName is null, the one whose ratio leads to 1.0 is effectively base
                        (currentIngredient.ratio / baseRatioToUseForCalculation) == 1.0
                    }
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        ) // Keep it as StateFlow

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
    fun addIngredient(ingredient: RecipeIngredient, name: String, ratio: Double) {
        viewModelScope.launch {
            recipeIngredientDao.insert(recipeId, name, ratio)
            loadIngredients()
        }
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