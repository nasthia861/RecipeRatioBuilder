package com.example.reciperatiobuilder.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reciperatiobuilder.viewmodels.RecipeViewModel

@Composable
fun RecipeScreen(recipeViewModel: RecipeViewModel = viewModel()) {
    var recipeName by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
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
        LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
            items(recipeViewModel.recipes) { recipe ->
                Text(recipe.name)
            }
        }
    }

}