package com.sistemapersonal.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sistemapersonal.data.entity.PudSimulacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PudDao {
    @Insert
    suspend fun insertar(sim: PudSimulacionEntity): Long

    @Query("SELECT * FROM pud_simulacion ORDER BY timestamp DESC")
    fun historial(): Flow<List<PudSimulacionEntity>>
}
