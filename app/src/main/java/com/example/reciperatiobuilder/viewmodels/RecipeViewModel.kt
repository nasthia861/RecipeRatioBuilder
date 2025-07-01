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
    private val _snackBarMessage = MutableStateFlow<String?>(null)
    val snackBarMessage: StateFlow<String?> = _snackBarMessage.asStateFlow()


    init {
        loadRecipes()
    }
    fun loadRecipes() {
        viewModelScope.launch {
            try {
                _recipes.value = recipeDao.getAll() // Assuming getAll() is suspend and returns List<Recipe>
            } catch (e: Exception) {
                _snackBarMessage.value = "Error loading recipes"
            }
        }
    }
    fun onNewRecipeNameChange(name: String) {
        _newRecipeName.value = name
    }
    fun onSnackbarMessageShown() {
        _snackBarMessage.value = null
    }

    fun onShowIngredientModal() {
        if (_newRecipeName.value.isNotBlank()) {
            _showIngredientAdditionModal.value = true
        } else {
           _snackBarMessage.value = "Please enter a recipe name first"
        }
    }
    fun onHideIngredientModal() {
        _showIngredientAdditionModal.value = false
    }

    fun addTemporaryIngredient(name: String, weight: Double) {
        if (name.isNotBlank() && weight > 0) {
            val newIngredient = TemporaryIngredient(name = name, weightOunces = weight)
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
            val baseWeight = ingredients.first().weightOunces

            if (recipeName.isNotBlank()) {
                try {
                    val newRecipeId = recipeDao.insert(Recipe(name = recipeName))
                    ingredients.forEach { tempIngredient ->
                        val calculateRatio = tempIngredient.weightOunces / baseWeight
                        recipeIngredientDao.insert(
                            recipeId = newRecipeId,
                            name = tempIngredient.name,
                            ratio = calculateRatio
                        )
                    }
                    // Reset fields after successful save
                    _newRecipeName.value = ""
                    _currentIngredientsForNewRecipe.value = emptyList()
                    _showIngredientAdditionModal.value = false
                    loadRecipes()
                    _snackBarMessage.value = "Recipe saved"
                } catch (e: Exception) {
                    _snackBarMessage.value = "Error saving recipe"
                }
            } else {
                _snackBarMessage.value = "Please enter a recipe name first"
            }
        }
    }

    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            recipeDao.delete(recipe.id)
            _recipes.value = _recipes.value.filter { it.id != recipe.id }
            _snackBarMessage.value = "Recipe deleted"
        }
    }

}