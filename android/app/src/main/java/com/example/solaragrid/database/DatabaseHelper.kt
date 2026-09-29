package com.example.solaragrid.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// REFERENCE: The implementation of SQLiteOpenHelper for local database creation 
// was adapted from the official Android Developers Guide.
// Source: https://developer.android.com/training/data-storage/sqlite
// The fundamental SQL queries (CREATE TABLE, DROP TABLE) were structured 
// based on syntax learned from W3Schools SQL tutorials.
// Source: https://www.w3schools.com/sql/
class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // A companion object 
    companion object {
        private const val DATABASE_NAME = "SolaraGrid.db"
        private const val DATABASE_VERSION = 2 // Bumped to 2 so it drops and recreates the table
        
        // Table and Column names
        const val TABLE_USER = "users"
        const val COL_ID = "id"
        const val COL_TOKEN = "token"
        const val COL_ROLE = "role"
        const val COL_NAME = "name"
        const val COL_NIC = "nic"
    }

    // This runs only ONCE when the app is installed and the database is first created.
    override fun onCreate(db: SQLiteDatabase) {
        // write standard SQL to create a table.
        val createTableQuery = """
            CREATE TABLE $TABLE_USER (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TOKEN TEXT,
                $COL_ROLE TEXT,
                $COL_NAME TEXT,
                $COL_NIC TEXT
            )
        """.trimIndent()
        
        // Execute the SQL statement
        db.execSQL(createTableQuery)
    }

    // runs if you ever change DATABASE_VERSION (e.g. add a new column later).
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop the old table if it exists
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        // Create the new one by calling onCreate again
        onCreate(db)
    }
}
