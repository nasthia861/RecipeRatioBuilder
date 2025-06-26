package com.example.reciperatiobuilder.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciperatiobuilder.data.Recipe
import com.example.reciperatiobuilder.ui.navigation.Navigation
import com.example.reciperatiobuilder.viewmodels.RecipeViewModel

@Composable
fun RecipeScreen(
    navController: NavController,
    recipeViewModel: RecipeViewModel = viewModel()) {
    var recipeName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = recipeName,
            onValueChange = { recipeName = it },
            label = { Text("Recipe Name") }
        )
        Button(
            onClick = {
                if(recipeName.isNotBlank())
                {
                    recipeViewModel.addRecipe(recipeName)
                    recipeName = ""
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Add Recipe")
        }
        LazyColumn(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth()
        ) {
            items(recipeViewModel.recipes, key = {recipe -> recipe.id}) { recipe ->
                RecipeItem(
                    navController = navController,
                    recipe = recipe,
                    onDeleteClick = { recipeViewModel.deleteRecipe(recipe) }
                )
            }
        }

    }

}

@Composable
fun RecipeItem(
    navController: NavController,
    recipe: Recipe, // Assuming Recipe has an 'id' and 'name'
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween // Pushes items to ends
    ) {
        Text(
            text = recipe.name,
            modifier = Modifier.weight(1f)
                .clickable {
                    navController.navigate("${Navigation.INGREDIENT_SCREEN}/${recipe.id}")
                }
                .padding(10.dp)
        )
        IconButton(onClick = onDeleteClick) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete Recipe"
            )
        }
    }
}
