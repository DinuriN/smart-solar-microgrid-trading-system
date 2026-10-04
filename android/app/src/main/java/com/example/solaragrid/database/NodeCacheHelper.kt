/*
 * File Name    : NodeCacheHelper.kt
 * Description  : Local SQLite cache of microgrid node reference data (id -> name),
 *                so booking lists can show node names and still work offline.
 *                Uses its own database file so it never touches the users table.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-28
 */
package com.example.solaragrid.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.solaragrid.models.NodeDto

// One cached node row
data class CachedNode(val id: String, val name: String)

// REFERENCE: SQLiteOpenHelper usage follows the official Android Developers guide.
// Source: https://developer.android.com/training/data-storage/sqlite
class NodeCacheHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "SolaraReservation.db"
        private const val DATABASE_VERSION = 1
        const val TABLE_NODES = "nodes"
        const val COL_ID = "id"
        const val COL_NAME = "name"
    }

    // Creates the nodes table the first time the database is opened
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE $TABLE_NODES ($COL_ID TEXT PRIMARY KEY, $COL_NAME TEXT NOT NULL)")
    }

    // Rebuilds the table when DATABASE_VERSION changes
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NODES")
        onCreate(db)
    }

    // Replaces the cached nodes with the latest list from the server
    fun replaceAll(nodes: List<NodeDto>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            db.delete(TABLE_NODES, null, null)
            nodes.forEach { node ->
                val values = ContentValues().apply {
                    put(COL_ID, node.id)
                    put(COL_NAME, node.nodeName)
                }
                db.insert(TABLE_NODES, null, values)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            db.close()
        }
    }

    // Returns every cached node, sorted by name
    fun getAll(): List<CachedNode> {
        val db = readableDatabase
        val result = mutableListOf<CachedNode>()
        val cursor = db.query(TABLE_NODES, null, null, null, null, null, "$COL_NAME ASC")
        while (cursor.moveToNext()) {
            result.add(
                CachedNode(
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME))
                )
            )
        }
        cursor.close()
        db.close()
        return result
    }

    // Looks up one node's name by id (null if it is not cached)
    fun nameFor(id: String): String? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NODES, arrayOf(COL_NAME), "$COL_ID = ?", arrayOf(id), null, null, null
        )
        val name = if (cursor.moveToFirst()) cursor.getString(0) else null
        cursor.close()
        db.close()
        return name
    }
}
