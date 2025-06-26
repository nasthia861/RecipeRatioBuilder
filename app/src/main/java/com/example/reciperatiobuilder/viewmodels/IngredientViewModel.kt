package com.example.reciperatiobuilder.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reciperatiobuilder.data.AppDatabase
import com.example.reciperatiobuilder.data.Ingredient
import com.example.reciperatiobuilder.data.Recipe
import com.example.reciperatiobuilder.data.RecipeIngredient
import com.example.reciperatiobuilder.data.RecipeIngredientDisplay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class IngredientViewModel(
    application: Application,
    private val recipeId: Long
): AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).recipeIngredientDao()
    private val _ingredients = MutableStateFlow<List<RecipeIngredientDisplay>>(emptyList())
    val ingredients: StateFlow<List<RecipeIngredientDisplay>> = _ingredients.asStateFlow()

    init {
        loadIngredients()
    }
    fun loadIngredients() {
        viewModelScope.launch {
            _ingredients.value = dao.getRecipeIngredients(recipeId)
        }
    }
    fun addIngredient(ingredient: RecipeIngredient, name: String, ratio: Double) {
        viewModelScope.launch {
            dao.insert(recipeId, name, ratio)
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