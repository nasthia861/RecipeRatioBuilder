package com.example.reciperatiobuilder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RecipeDao {
    @Insert
    suspend fun insert(recipe: Recipe): Long

    @Query("SELECT * FROM recipes")
    suspend fun getAll(): List<Recipe>

}