package com.sistemapersonal.data.dao

import androidx.room.*
import com.sistemapersonal.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaObjetivoDao {
    @Query("SELECT * FROM obj_area ORDER BY orden ASC")
    fun observarTodas(): Flow<List<AreaObjetivoEntity>>

    @Insert
    suspend fun insertar(a: AreaObjetivoEntity): Long

    @Update
    suspend fun actualizar(a: AreaObjetivoEntity)

    @Delete
    suspend fun eliminar(a: AreaObjetivoEntity)

    @Query("SELECT COUNT(*) FROM obj_area")
    suspend fun contar(): Int
}

@Dao
interface PuntoObjetivoDao {
    @Query("SELECT * FROM obj_punto WHERE areaId = :areaId ORDER BY orden ASC")
    fun deArea(areaId: Long): Flow<List<PuntoObjetivoEntity>>

    @Insert
    suspend fun insertar(p: PuntoObjetivoEntity): Long

    @Update
    suspend fun actualizar(p: PuntoObjetivoEntity)

    @Delete
    suspend fun eliminar(p: PuntoObjetivoEntity)
}

@Dao
interface MicroObjetivoDao {
    @Query("SELECT * FROM obj_micro WHERE puntoId = :puntoId ORDER BY orden ASC")
    fun dePunto(puntoId: Long): Flow<List<MicroObjetivoEntity>>

    @Insert
    suspend fun insertar(m: MicroObjetivoEntity): Long

    @Update
    suspend fun actualizar(m: MicroObjetivoEntity)

    @Delete
    suspend fun eliminar(m: MicroObjetivoEntity)
}

@Dao
interface IndicadorDao {
    @Query("SELECT * FROM obj_indicador WHERE tipo = :tipo ORDER BY id ASC")
    fun deTipo(tipo: String): Flow<List<IndicadorEntity>>

    @Insert
    suspend fun insertar(i: IndicadorEntity): Long

    @Update
    suspend fun actualizar(i: IndicadorEntity)

    @Query("SELECT COUNT(*) FROM obj_indicador WHERE tipo = :tipo")
    suspend fun contarPorTipo(tipo: String): Int
}
