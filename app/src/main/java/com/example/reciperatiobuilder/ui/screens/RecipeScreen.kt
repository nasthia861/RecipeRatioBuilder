package com.example.reciperatiobuilder.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciperatiobuilder.data.Recipe
import com.example.reciperatiobuilder.ui.navigation.Navigation
import com.example.reciperatiobuilder.viewmodels.RecipeViewModel
import kotlin.text.isNotBlank

@Composable
fun RecipeScreen(
    navController: NavController,
    viewModel: RecipeViewModel = viewModel()) {
    var recipes = viewModel.recipes.collectAsState()
    var newRecipeName: State<String> = viewModel.newRecipeName.collectAsState()
    var showIngredientModal: State<Boolean> = viewModel.showIngredientAdditionModal.collectAsState()

    Scaffold(
        topBar = { /* ... */ },
        floatingActionButton = {
            // Your existing FAB to (for example) show the recipe name input field
            // Or change this entirely to be part of the main screen layout
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Input for new recipe name
            OutlinedTextField(
                value = newRecipeName.value,
                onValueChange = { viewModel.onNewRecipeNameChange(it) },
                label = { Text("Recipe Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.onShowIngredientModal() },
                enabled = newRecipeName.value.isNotBlank(), // Enable only if recipe name is entered
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Next: Add Ingredients")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Existing Recipes", style = MaterialTheme.typography.headlineSmall)
            // Your existing LazyColumn for recipes
            LazyColumn {
                items(recipes.value, key = { it.id }) { recipe ->
                    RecipeItem(
                        navController = navController,
                        recipe = recipe,
                        onDeleteClick = { viewModel.deleteRecipe(recipe) }
                    )
                }
            }
        }
        if (showIngredientModal.value) {
            NewRecipeIngredientsModal(
                viewModel = viewModel, // Pass the ViewModel or specific states and event handlers
                onDismiss = { viewModel.onHideIngredientModal() }
            )
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
            modifier = Modifier
                .weight(1f)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewRecipeIngredientsModal(
    viewModel: RecipeViewModel,
    onDismiss: () -> Unit
) {
    val ingredientsForNewRecipe by viewModel.currentIngredientsForNewRecipe.collectAsState()
    var tempIngredientName by remember { mutableStateOf("") }
    var tempIngredientRatioStr by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Ingredients for '${viewModel.newRecipeName.collectAsState().value}'") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Input for current ingredient
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = tempIngredientName,
                        onValueChange = { tempIngredientName = it },
                        label = { Text("Ingredient") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = tempIngredientRatioStr,
                        onValueChange = { tempIngredientRatioStr = it },
                        label = { Text("Ratio") },
                        modifier = Modifier.width(100.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
                Button(
                    onClick = {
                        val ratio = tempIngredientRatioStr.toDoubleOrNull()
                        if (tempIngredientName.isNotBlank() && ratio != null && ratio > 0) {
                            viewModel.addTemporaryIngredient(tempIngredientName, ratio)
                            tempIngredientName = "" // Reset
                            tempIngredientRatioStr = ""
                        } else {
                            // TODO: Show error for this ingredient input
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp),
                    enabled = tempIngredientName.isNotBlank() && tempIngredientRatioStr.isNotBlank()
                ) {
                    Text("Add to List")
                }

                Spacer(modifier = Modifier.height(16.dp))
                // List of ingredients added so far for this new recipe
                if (ingredientsForNewRecipe.isNotEmpty()) {
                    Text("Ingredients to be added:", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp)) { // Limit height
                        items(ingredientsForNewRecipe, key = { it.tempId }) { ingredient ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("${ingredient.name} - Ratio: ${ingredient.ratio}")
                                IconButton(onClick = { viewModel.removeTemporaryIngredient(ingredient) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Remove ingredient")
                                }
                            }
                        }
                    }
                } else {
                    Text("No ingredients added yet.")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.saveRecipeWithIngredients()
                    // onDismiss() // saveRecipeWithIngredients now also hides the modal
                },
                enabled = ingredientsForNewRecipe.isNotEmpty() // Enable save only if there are ingredients
            ) {
                Text("Save Recipe & Ingredients")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

