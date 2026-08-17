package com.sistemapersonal.data.dao

import androidx.room.*
import com.sistemapersonal.data.entity.StreakEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StreakDao {
    @Query("SELECT * FROM streak WHERE id = 1")
    fun observar(): Flow<StreakEntity?>

    @Query("SELECT * FROM streak WHERE id = 1")
    suspend fun obtener(): StreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(streak: StreakEntity)
}
