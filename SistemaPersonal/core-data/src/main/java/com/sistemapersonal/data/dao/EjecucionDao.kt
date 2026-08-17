package com.sistemapersonal.data.dao

import androidx.room.*
import com.sistemapersonal.data.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NutricionDao {
    @Insert
    suspend fun insertar(n: NutricionLogEntity): Long

    @Query("SELECT * FROM nutricion_log WHERE fecha = :fecha ORDER BY timestamp DESC")
    fun delDia(fecha: String): Flow<List<NutricionLogEntity>>

    @Query("SELECT fecha, SUM(kcal) as total FROM nutricion_log WHERE fecha BETWEEN :desde AND :hasta GROUP BY fecha")
    suspend fun kcalPorDia(desde: String, hasta: String): List<KcalPorDia>

    @Delete
    suspend fun eliminar(n: NutricionLogEntity)

    @Query("SELECT * FROM nutricion_metas WHERE id = 1")
    fun observarMetas(): Flow<NutricionMetasEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarMetas(metas: NutricionMetasEntity)

    @Query("SELECT * FROM agua_dia WHERE fecha = :fecha")
    fun aguaDelDia(fecha: String): Flow<AguaDiaEntity?>

    @Query("SELECT ml FROM agua_dia WHERE fecha = :fecha")
    suspend fun aguaDelDiaSuspend(fecha: String): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarAgua(agua: AguaDiaEntity)

    @Query("SELECT * FROM nutricion_notas WHERE fecha = :fecha")
    fun notasDelDia(fecha: String): Flow<NutricionNotasEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarNotas(notas: NutricionNotasEntity)
}

data class KcalPorDia(val fecha: String, val total: Float)

@Dao
interface EntrenamientoDao {
    @Insert
    suspend fun insertar(e: EntrenamientoLogEntity): Long

    @Query("SELECT * FROM entrenamiento_log WHERE fecha = :fecha ORDER BY timestamp DESC")
    fun delDia(fecha: String): Flow<List<EntrenamientoLogEntity>>
}

@Dao
interface RutinaBloqueDao {
    @Query("SELECT * FROM rutina_bloque WHERE tipoSemana = :tipoSemana ORDER BY inicio ASC")
    fun deTipo(tipoSemana: String): Flow<List<RutinaBloqueEntity>>

    @Query("SELECT * FROM rutina_bloque WHERE tipoSemana = :tipoSemana ORDER BY inicio ASC")
    suspend fun deTipoSuspend(tipoSemana: String): List<RutinaBloqueEntity>

    @Insert
    suspend fun insertar(b: RutinaBloqueEntity): Long

    @Update
    suspend fun actualizar(b: RutinaBloqueEntity)

    @Delete
    suspend fun eliminar(b: RutinaBloqueEntity)

    @Query("SELECT COUNT(*) FROM rutina_bloque")
    suspend fun contarTotal(): Int

    @Query("SELECT * FROM rutina_bloque_log WHERE fecha = :fecha")
    fun logsDelDia(fecha: String): Flow<List<RutinaBloqueLogEntity>>

    @Query("SELECT * FROM rutina_bloque_log WHERE fecha BETWEEN :desde AND :hasta")
    suspend fun logsEnRango(desde: String, hasta: String): List<RutinaBloqueLogEntity>

    @Insert
    suspend fun marcarCompletado(log: RutinaBloqueLogEntity)

    @Query("DELETE FROM rutina_bloque_log WHERE bloqueId = :bloqueId AND fecha = :fecha")
    suspend fun desmarcarCompletado(bloqueId: Long, fecha: String)
}

@Dao
interface DespertarDao {
    @Insert
    suspend fun insertar(d: DespertarEntity): Long

    @Query("SELECT * FROM despertar_log ORDER BY timestamp DESC LIMIT :limite")
    fun historial(limite: Int = 30): Flow<List<DespertarEntity>>
}

@Dao
interface CierreDiaDao {
    @Insert
    suspend fun insertar(c: CierreDiaEntity): Long

    @Query("SELECT * FROM cierre_dia_log ORDER BY timestamp DESC LIMIT :limite")
    fun historial(limite: Int = 30): Flow<List<CierreDiaEntity>>

    @Query("SELECT * FROM cierre_dia_log ORDER BY fecha DESC")
    suspend fun todosOrdenados(): List<CierreDiaEntity>

    @Insert
    suspend fun insertarAntesDormir(a: AntesDormirEntity): Long

    @Query("SELECT * FROM antes_dormir_log ORDER BY timestamp DESC LIMIT :limite")
    fun historialAntesDormir(limite: Int = 30): Flow<List<AntesDormirEntity>>
}

@Dao
interface RevisionDao {
    @Insert
    suspend fun insertar(r: RevisionSemanalEntity): Long

    @Query("SELECT * FROM revision_semanal ORDER BY timestamp DESC")
    fun revisiones(): Flow<List<RevisionSemanalEntity>>
}
