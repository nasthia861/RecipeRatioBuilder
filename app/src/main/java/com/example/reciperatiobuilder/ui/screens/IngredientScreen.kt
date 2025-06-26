package com.example.reciperatiobuilder.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciperatiobuilder.viewmodels.IngredientViewModel

@Composable
fun IngredienteScreen(
    recipeId: Long,
    navController: NavController) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: IngredientViewModel = viewModel(
        factory = IngredientViewModel.Factory(context, recipeId)
    )

    val ingredients by viewModel.ingredients.collectAsState()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Ingredientes de la receta $recipeId")
        LazyColumn {
            items(ingredients) {ing ->
                Text("${ing.name} - ${ing.ratio}")
            }
        }
    }
}