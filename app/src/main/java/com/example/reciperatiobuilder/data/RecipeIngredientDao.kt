package com.example.reciperatiobuilder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecipeIngredientDao {

    @Insert
    suspend fun insert(recipeId: Long, name: String, ratio: Double) {
        val ingredient = Ingredient(name = name)
        val ingredientId = insertIngredient(ingredient)
        val actualIngredientId = if (ingredientId == -1L) {
            getIngredientIdByName(name)
        } else {
            ingredientId
        }
        insertRecipeIngredient(RecipeIngredient(recipeId = recipeId, ingredientId = actualIngredientId, ratio = ratio))
    }

    @Insert
    suspend fun insertRecipeIngredient(recipeIngredient: RecipeIngredient)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIngredient(ingredient: Ingredient): Long

    @Query("""SELECT i.id as ingredientId, i.name as name, ri.ratio as ratio 
            FROM recipeIngredients ri 
            JOIN ingredients i ON ri.ingredientId = i.id 
            WHERE ri.recipeId = :recipeId""")
    suspend fun getRecipeIngredients(recipeId: Long): List<RecipeIngredientDisplay>

    @Query("SELECT id FROM ingredients WHERE name = :name LIMIT 1")
    suspend fun getIngredientIdByName(name: String): Long

}