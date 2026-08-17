package com.sistemapersonal.data.dao

import androidx.room.*
import com.sistemapersonal.data.entity.AchievementEntity
import com.sistemapersonal.data.entity.PointsLedgerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PointsDao {
    @Insert
    suspend fun agregar(entry: PointsLedgerEntity)

    @Query("SELECT COALESCE(SUM(delta), 0) FROM points_ledger")
    fun totalObservable(): Flow<Int>

    @Query("SELECT COALESCE(SUM(delta), 0) FROM points_ledger")
    suspend fun total(): Int

    @Query(
        """
        SELECT strftime('%Y-%W', datetime(timestamp / 1000, 'unixepoch')) AS semana,
               SUM(delta) AS puntos
        FROM points_ledger
        GROUP BY semana
        ORDER BY semana DESC
        LIMIT 12
        """
    )
    fun rankingSemanalObservable(): Flow<List<PuntosPorSemana>>
}

data class PuntosPorSemana(val semana: String, val puntos: Int)

@Dao
interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(a: AchievementEntity)

    @Query("SELECT * FROM achievement ORDER BY desbloqueadoEn DESC")
    fun observarTodos(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievement WHERE clave = :clave")
    suspend fun obtener(clave: String): AchievementEntity?
}
