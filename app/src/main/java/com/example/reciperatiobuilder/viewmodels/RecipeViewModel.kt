package com.example.reciperatiobuilder.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.reciperatiobuilder.data.AppDatabase
import com.example.reciperatiobuilder.data.Recipe
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application): AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).recipeDao()
    private val _recipes = mutableStateListOf<Recipe>()
    val recipes: List<Recipe> get()= _recipes

    init {
        viewModelScope.launch {
            _recipes.addAll(dao.getAll())
        }
    }

    fun addRecipe(name: String) {
        viewModelScope.launch {
            val id = dao.insert(Recipe(name = name))
                .toLong()
            _recipes.add(Recipe(id, name))
        }
    }
    fun deleteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            dao.delete(recipe.id)
            _recipes.remove(recipe)
        }
    }

}