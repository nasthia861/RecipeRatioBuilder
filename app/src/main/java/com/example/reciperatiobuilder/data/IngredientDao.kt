package com.example.reciperatiobuilder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface IngredientDao {
    @Insert
    suspend fun insertIngredient(ingredient: Ingredient):Long

    @Query("SELECT * FROM ingredients")
    suspend fun getAll(): List<Ingredient>
}