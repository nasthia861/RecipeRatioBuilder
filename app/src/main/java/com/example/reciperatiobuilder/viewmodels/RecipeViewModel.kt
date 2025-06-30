package com.example.reciperatiobuilder.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciperatiobuilder.data.AppDatabase
import com.example.reciperatiobuilder.data.Recipe
import com.example.reciperatiobuilder.data.TemporaryIngredient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class RecipeViewModel(application: Application): AndroidViewModel(application) {
    private val recipeDao = AppDatabase.getDatabase(application).recipeDao()
    private val recipeIngredientDao = AppDatabase.getDatabase(application).recipeIngredientDao()
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()
    private val _newRecipeName = MutableStateFlow("")
    val newRecipeName: StateFlow<String> = _newRecipeName.asStateFlow()
    private val _currentIngredientsForNewRecipe = MutableStateFlow<List<TemporaryIngredient>>(emptyList())
    val currentIngredientsForNewRecipe: StateFlow<List<TemporaryIngredient>> = _currentIngredientsForNewRecipe.asStateFlow()
    private val _showIngredientAdditionModal = MutableStateFlow(false)
    val showIngredientAdditionModal: StateFlow<Boolean> = _showIngredientAdditionModal.asStateFlow()
    init {
        loadRecipes()
    }
    fun loadRecipes() {
        viewModelScope.launch {
            try {
                _recipes.value = recipeDao.getAll() // Assuming getAll() is suspend and returns List<Recipe>
            } catch (e: Exception) {
                Log.e("RecipeViewModel", "Error loading recipes", e)
            }
        }
    }
    fun onNewRecipeNameChange(name: String) {
        _newRecipeName.value = name
    }

    fun onShowIngredientModal() {
        if (_newRecipeName.value.isNotBlank()) {
            _showIngredientAdditionModal.value = true
        } else {
            Log.e("RecipeViewModel", "Recipe name cannot be left blank")
        }
    }
    fun onHideIngredientModal() {
        _showIngredientAdditionModal.value = false
    }

    fun addTemporaryIngredient(name: String, ratio: Double) {
        if (name.isNotBlank() && ratio > 0) {
            val newIngredient = TemporaryIngredient(name = name, ratio = ratio)
            _currentIngredientsForNewRecipe.value += newIngredient
        }
    }

    fun removeTemporaryIngredient(ingredient: TemporaryIngredient) {
        _currentIngredientsForNewRecipe.value = _currentIngredientsForNewRecipe.value - ingredient
    }
    fun saveRecipeWithIngredients() {
        viewModelScope.launch {
            val recipeName = _newRecipeName.value
            val ingredients = _currentIngredientsForNewRecipe.value

            if (recipeName.isNotBlank()) {
                // This should ideally be a single transaction
                try {
                    val newRecipeId = recipeDao.insert(Recipe(name = recipeName)) // Assuming insertRecipe returns the new ID
                    ingredients.forEach { tempIngredient ->
                        recipeIngredientDao.insert(
                            recipeId = newRecipeId,
                            name = tempIngredient.name,
                            ratio = tempIngredient.ratio
                        )
                    }
                    // Reset fields after successful save
                    _newRecipeName.value = ""
                    _currentIngredientsForNewRecipe.value = emptyList()
                    _showIngredientAdditionModal.value = false
                    loadRecipes() // Refresh the recipe list
                } catch (e: Exception) {
                    Log.e("RecipeViewModel", "Error saving recipe with ingredients", e)
                }
            } else {
                Log.e("RecipeViewModel", "Recipe name cannot be left blank")
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeDao.delete(recipe.id)
            _recipes.value = _recipes.value.filter { it.id != recipe.id }
        }
    }

}