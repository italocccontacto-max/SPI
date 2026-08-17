package com.sistemapersonal.data.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object Migrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE screenshot RENAME COLUMN remoteUrl TO remotePath")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2)
}
