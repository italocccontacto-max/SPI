package com.sistemapersonal.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sistemapersonal.data.dao.*
import com.sistemapersonal.data.entity.*

@Database(
    entities = [
        ActivityLogEntity::class,
        StreakEntity::class,
        ScreenshotEntity::class,
        PointsLedgerEntity::class,
        AchievementEntity::class,
        AreaObjetivoEntity::class,
        PuntoObjetivoEntity::class,
        MicroObjetivoEntity::class,
        IndicadorEntity::class,
        RutinaBloqueEntity::class,
        RutinaBloqueLogEntity::class,
        NutricionLogEntity::class,
        NutricionMetasEntity::class,
        AguaDiaEntity::class,
        NutricionNotasEntity::class,
        EntrenamientoLogEntity::class,
        DespertarEntity::class,
        CierreDiaEntity::class,
        AntesDormirEntity::class,
        RevisionSemanalEntity::class,
        BibliotecaItemEntity::class,
        EvolucionEventoEntity::class,
        CarpetaEntity::class,
        EtiquetaEntity::class,
        IntentionContractEntity::class,
        PudSimulacionEntity::class
    ],
    version = 2,
    exportSchema = true

)
abstract class SistemaPersonalDatabase : RoomDatabase() {

    abstract fun activityDao(): ActivityDao
    abstract fun streakDao(): StreakDao
    abstract fun screenshotDao(): ScreenshotDao
    abstract fun pointsDao(): PointsDao
    abstract fun achievementDao(): AchievementDao
    abstract fun areaObjetivoDao(): AreaObjetivoDao
    abstract fun puntoObjetivoDao(): PuntoObjetivoDao
    abstract fun microObjetivoDao(): MicroObjetivoDao
    abstract fun indicadorDao(): IndicadorDao
    abstract fun rutinaBloqueDao(): RutinaBloqueDao
    abstract fun nutricionDao(): NutricionDao
    abstract fun entrenamientoDao(): EntrenamientoDao
    abstract fun despertarDao(): DespertarDao
    abstract fun cierreDiaDao(): CierreDiaDao
    abstract fun revisionDao(): RevisionDao
    abstract fun bibliotecaDao(): BibliotecaDao
    abstract fun evolucionDao(): EvolucionDao
    abstract fun carpetaDao(): CarpetaDao
    abstract fun etiquetaDao(): EtiquetaDao
    abstract fun intentionContractDao(): IntentionContractDao
    abstract fun pudDao(): PudDao

    companion object {
        @Volatile private var INSTANCE: SistemaPersonalDatabase? = null

        fun get(context: Context): SistemaPersonalDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SistemaPersonalDatabase::class.java,
                    "sistema_personal.db"
                )

                    .addMigrations(*Migrations.ALL)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build().also { INSTANCE = it }
            }
    }
}
