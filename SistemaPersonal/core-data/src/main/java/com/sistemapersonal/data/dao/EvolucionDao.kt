package com.sistemapersonal.data.dao

import androidx.room.*
import com.sistemapersonal.data.entity.CarpetaEntity
import com.sistemapersonal.data.entity.EtiquetaEntity
import com.sistemapersonal.data.entity.EvolucionEventoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvolucionDao {
    @Query("SELECT * FROM evolucion_evento WHERE enPapelera = 0 ORDER BY fecha DESC")
    fun activos(): Flow<List<EvolucionEventoEntity>>

    @Query("SELECT * FROM evolucion_evento WHERE enPapelera = 0 AND carpetaId = :carpetaId ORDER BY fecha DESC")
    fun deCarpeta(carpetaId: Long): Flow<List<EvolucionEventoEntity>>

    @Query("SELECT * FROM evolucion_evento WHERE enPapelera = 1 ORDER BY eliminadoEn DESC")
    fun enPapelera(): Flow<List<EvolucionEventoEntity>>

    @Insert
    suspend fun insertar(e: EvolucionEventoEntity): Long

    @Update
    suspend fun actualizar(e: EvolucionEventoEntity)

    @Delete
    suspend fun eliminarDefinitivo(e: EvolucionEventoEntity)
}

@Dao
interface CarpetaDao {
    @Query("SELECT * FROM evolucion_carpeta ORDER BY nombre ASC")
    fun observarTodas(): Flow<List<CarpetaEntity>>

    @Insert
    suspend fun insertar(c: CarpetaEntity): Long

    @Delete
    suspend fun eliminar(c: CarpetaEntity)
}

@Dao
interface EtiquetaDao {
    @Query("SELECT * FROM evolucion_etiqueta ORDER BY nombre ASC")
    fun observarTodas(): Flow<List<EtiquetaEntity>>

    @Insert
    suspend fun insertar(e: EtiquetaEntity): Long

    @Delete
    suspend fun eliminar(e: EtiquetaEntity)
}
