package com.example.reciperatiobuilder.ui.screens

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.forEach
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.reciperatiobuilder.data.WeightUnit
import com.example.reciperatiobuilder.viewmodels.IngredientViewModel
import java.text.DecimalFormat
import kotlin.math.ceil
import kotlin.text.format

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
    val baseIngredientWeight by viewModel.baseIngredientWeightInput.collectAsState()
    val currentBaseIngredient = ingredients.find { it.isDynamicallyBase }
    val selectedUnit by viewModel.selectedUnit.collectAsState()
    val baseIngredientNameForLabel = currentBaseIngredient?.ingredientName ?: "Base Ingredient"
    val totalYield by viewModel.totalYield.collectAsState()
    val ozFormatter = remember { DecimalFormat("#.##") }
    val gramFormatter = remember { DecimalFormat("#") } // For whole numbers


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
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // --- Input Field for Base Ingredient Weight ---
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = baseIngredientWeight,
                    onValueChange = { viewModel.onBaseIngredientWeightChange(it) },
                    label = { Text("Available $baseIngredientNameForLabel") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                WeightUnit.entries.forEach { unit ->
                    TextButton(
                        onClick = { viewModel.setSelectedUnit(unit) },
                        modifier = Modifier.padding(horizontal = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = if (selectedUnit == unit) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            contentColor = if (selectedUnit == unit) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                        ),
                        border = if (selectedUnit == unit) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(unit.displayName, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()) {

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
                    ) {
                        items(
                            ingredients,
                            key = {
                                it.ingredientName
                            }) { ingredientDsl -> // Use a stable key
                            val isBaseIngredient = ingredientDsl.isDynamicallyBase
                            val formattedRatio = DecimalFormat("#.##").format(ingredientDsl.displayedRatio)
                            val formattedWeight: String = ingredientDsl.calculatedWeight?.let { weightValue ->
                                if (selectedUnit == WeightUnit.GRAMS) {
                                    val roundedUpGrams = ceil(weightValue)
                                    "${gramFormatter.format(roundedUpGrams)} ${selectedUnit.displayName}"
                                } else { // For OZ or any other unit
                                    "${ozFormatter.format(weightValue)} ${selectedUnit.displayName}"
                                }
                            } ?: ""
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {
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
                                        if(formattedWeight.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Weight: $formattedWeight",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = (if (isBaseIngredient) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant).copy(
                                                    alpha = 0.8f
                                                )
                                            )
                                        }
                                    }
                                    if (isBaseIngredient) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = "Base Ingredient",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier
                                                .padding(start = 8.dp)
                                                .size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // --- Display Total Yield ---
                // This section is now guaranteed to be AFTER the Box with weight(1f)
            }
            totalYield?.let { yieldValue ->
                if (ingredients.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Total Estimated Yield:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        val formattedYield: String = if (selectedUnit == WeightUnit.GRAMS) {
                            val roundedUpGrams = ceil(yieldValue)
                            "${gramFormatter.format(roundedUpGrams)} ${selectedUnit.displayName}"
                        } else {
                            "${ozFormatter.format(yieldValue)} ${selectedUnit.displayName}"
                        }
                        Text(
                            text = formattedYield,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}