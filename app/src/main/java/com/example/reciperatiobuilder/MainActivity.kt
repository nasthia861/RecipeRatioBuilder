package com.example.reciperatiobuilder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.reciperatiobuilder.data.AppDatabase
import com.example.reciperatiobuilder.data.Recipe
import com.example.reciperatiobuilder.ui.navigation.Navigation
import com.example.reciperatiobuilder.ui.screens.IngredienteScreen
import com.example.reciperatiobuilder.ui.screens.RecipeScreen
import com.example.reciperatiobuilder.ui.theme.RecipeRatioBuilderTheme
import com.example.reciperatiobuilder.viewmodels.IngredientViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            RecipeRatioBuilderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }

        }

        val db = AppDatabase.getDatabase(this)
        val dao = db.recipeDao()

    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Navigation.RECIPE_SCREEN) {
        composable(Navigation.RECIPE_SCREEN) {
            RecipeScreen(navController = navController)
        }
        composable(
            route = "${Navigation.INGREDIENT_SCREEN}/{${Navigation.RECIPE_ID_ARG}}",
            arguments = listOf (navArgument(Navigation.RECIPE_ID_ARG) { type = NavType.LongType })
        )  { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong(Navigation.RECIPE_ID_ARG)
                IngredienteScreen(navController = navController, recipeId = recipeId!!)

        }

    }
}








