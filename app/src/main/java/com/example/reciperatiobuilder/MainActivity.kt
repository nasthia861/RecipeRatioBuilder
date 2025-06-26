package com.example.reciperatiobuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.reciperatiobuilder.data.AppDatabase
import com.example.reciperatiobuilder.data.Recipe
import com.example.reciperatiobuilder.ui.screens.RecipeScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            RecipeScreen()
        }

        val db = AppDatabase.getDatabase(this)
        val dao = db.recipeDao()

        lifecycleScope.launch {
            dao.insert(Recipe(name = "Pesto"))
            val recipes = dao.getAll()
            println(recipes)
        }
    }
}
