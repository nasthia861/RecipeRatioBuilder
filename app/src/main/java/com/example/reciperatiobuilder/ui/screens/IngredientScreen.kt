package com.example.reciperatiobuilder.ui.screens

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciperatiobuilder.ui.navigation.Navigation
import com.example.reciperatiobuilder.viewmodels.IngredientViewModel
import com.example.reciperatiobuilder.viewmodels.RecipeViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IngredienteScreen(
    recipeId: Long,
    navController: NavController) {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: IngredientViewModel = viewModel(
        factory = IngredientViewModel.Factory(context, recipeId)
    )

    val ingredients by viewModel.displayedIngredients.collectAsState()
    val recipeName by viewModel.recipeName.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = recipeName ?: "Ingredients") // Display recipe name, fallback if null
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            if (ingredients.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No ingredients yet for this recipe.")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp) // Padding for the list itself
                ) {
                    items(
                        ingredients,
                        key = {
                            it.ingredientName
                        }) { ingredientDsl -> // Use a stable key
                        val isBaseIngredient = ingredientDsl.isDynamicallyBase
                        val formattedRatio = DecimalFormat("#.##").format(ingredientDsl.displayedRatio)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .clickable{
                                    viewModel.selectNewBaseIngredient(ingredientDsl.ingredientName)
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = if (isBaseIngredient) 4.dp else 2.dp // Slightly more elevation for base
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isBaseIngredient) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ingredientDsl.ingredientName, // Ensure this property exists
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (isBaseIngredient) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isBaseIngredient) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Ratio: $formattedRatio",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = (if (isBaseIngredient) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant).copy(
                                            alpha = 0.8f
                                        )
                                    )
                                }
                                // If you want an action per ingredient (e.g., delete, edit)
                                // IconButton(onClick = { /* TODO: Handle action */ }) {
                                //     Icon(Icons.Filled.MoreVert, "Options")
                                // }
                            }
                        }
                    }
                }
            }
            // TODO: Add UI to add new ingredients (e.g., TextFields and a Button)
        }
    }
}