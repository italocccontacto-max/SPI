package com.sistemapersonal.data.dao

import androidx.room.*
import com.sistemapersonal.data.entity.BibliotecaItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BibliotecaDao {
    @Query("SELECT * FROM biblioteca_item ORDER BY actualizadoEn DESC")
    fun observarTodos(): Flow<List<BibliotecaItemEntity>>

    @Query("SELECT * FROM biblioteca_item WHERE categoria = :categoria ORDER BY actualizadoEn DESC")
    fun deCategoria(categoria: String): Flow<List<BibliotecaItemEntity>>

    @Query("SELECT * FROM biblioteca_item WHERE titulo LIKE '%' || :query || '%' OR resumen LIKE '%' || :query || '%' ORDER BY actualizadoEn DESC")
    fun buscar(query: String): Flow<List<BibliotecaItemEntity>>

    @Insert
    suspend fun insertar(i: BibliotecaItemEntity): Long

    @Update
    suspend fun actualizar(i: BibliotecaItemEntity)

    @Delete
    suspend fun eliminar(i: BibliotecaItemEntity)
}
