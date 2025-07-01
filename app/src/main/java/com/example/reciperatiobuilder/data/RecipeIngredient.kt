package com.example.reciperatiobuilder.data

import androidx.room.Entity
import androidx.room.ForeignKey
import java.util.UUID

@Entity(
    tableName = "recipeIngredients",
    primaryKeys = ["recipeId", "ingredientId"],
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Ingredient::class,
            parentColumns = ["id"],
            childColumns = ["ingredientId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecipeIngredient(
    val recipeId: Long,
    val ingredientId: Long,
    val ratio: Double
)
data class RecipeIngredientDisplay (
    val ingredientId: Long,
    val name: String,
    val ratio: Double
)
data class TemporaryIngredient(
    val tempId: String = UUID.randomUUID().toString(),
    val name: String,
    val weightOunces: Double
)
data class DisplayedIngredient(
    val originalData: RecipeIngredientDisplay, // Keep original data for reference if needed
    val ingredientName: String,
    val displayedRatio: Double,
    val isDynamicallyBase: Boolean,
    val calculatedWeight: Double?
)
enum class WeightUnit(val displayName: String) {
    OZ("oz"),
    GRAMS("grams")
}