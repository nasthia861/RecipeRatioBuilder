package com.example.reciperatiobuilder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecipeIngredientDao {
    @Insert
    suspend fun insert(recipeIngredient: RecipeIngredient)

    @Query("SELECT * FROM recipeIngredients WHERE recipeId = :recipeId")
    suspend fun getByRecipe(recipeId: Long): List<RecipeIngredient>

}