package com.sistemapersonal.data.db

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val testDb = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SistemaPersonalDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun version1_seCreaSegunElEsquemaExportado() {
        helper.createDatabase(testDb, 1).use { db ->
            db.query("PRAGMA user_version").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(1, cursor.getInt(0))
            }
        }
    }

    @Test
    fun migrate1To2_renombraRemoteUrlYConservaDatosYValidaSchema() {
        helper.createDatabase(testDb, 1).use { db ->
            db.execSQL(
                "INSERT INTO screenshot(timestamp,filePath,width,height,sizeBytes,uploaded,remoteUrl,relevante) " +
                    "VALUES (1700000000000,'/tmp/a.jpg',100,200,1234,1,'familias/F/screenshots/1700000000000.jpg',1)"
            )
        }

        helper.runMigrationsAndValidate(
            testDb,
            2,
            true,
            Migrations.MIGRATION_1_2
        ).use { db ->
            val columns = mutableSetOf<String>()
            db.query("PRAGMA table_info(screenshot)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) columns += cursor.getString(nameIndex)
            }

            assertTrue(columns.contains("remotePath"))
            assertTrue(!columns.contains("remoteUrl"))

            db.query("SELECT remotePath FROM screenshot WHERE id = 1").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(
                    "familias/F/screenshots/1700000000000.jpg",
                    cursor.getString(0)
                )
            }

            db.query("PRAGMA user_version").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(2, cursor.getInt(0))
            }
        }
    }
}
