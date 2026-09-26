package com.example.solaragrid.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

// A simple data class to hold the retrieved user info
data class LocalUser(val token: String, val role: String, val name: String, val nic: String?)

class UserManager(context: Context) {
    //create an instance of our DatabaseHelper
    private val dbHelper = DatabaseHelper(context)

    // Save user after a successful login
    fun saveUser(token: String, role: String, name: String, nic: String?) {
        // Open the database for WRITING
        val db = dbHelper.writableDatabase
        
        // Clear old users first. only want ONE person logged in at a time.
        db.delete(DatabaseHelper.TABLE_USER, null, null)

        // ContentValues acts like a Dictionary/Map to hold row data
        val values = ContentValues().apply {
            put(DatabaseHelper.COL_TOKEN, token)
            put(DatabaseHelper.COL_ROLE, role)
            put(DatabaseHelper.COL_NAME, name)
            put(DatabaseHelper.COL_NIC, nic) // Save the NIC!
        }

        // Insert the row into the table
        db.insert(DatabaseHelper.TABLE_USER, null, values)
        db.close()
    }

    // Retrieve the currently logged-in user
    fun getLoggedInUser(): LocalUser? {
        // Open the database for READING
        val db = dbHelper.readableDatabase
        var user: LocalUser? = null

        // Query the table (SELECT * FROM users)
        val cursor: Cursor = db.query(
            DatabaseHelper.TABLE_USER, 
            null, null, null, null, null, null
        )

        // Move to the first row (if it exists)
        if (cursor.moveToFirst()) {
            // Extract the data from the columns
            val token = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TOKEN))
            val role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_ROLE))
            val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME))
            val nic = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NIC)) // Get the NIC!
            
            // Create our data class object
            user = LocalUser(token, role, name, nic)
        }
        
        // Always close the cursor and db to prevent memory leaks
        cursor.close()
        db.close()
        
        return user
    }

    // A quick helper just to get the token (useful for Retrofit headers)
    fun getToken(): String? {
        return getLoggedInUser()?.token
    }

    // Log the user out by deleting everything in the table
    fun clearUser() {
        val db = dbHelper.writableDatabase
        db.delete(DatabaseHelper.TABLE_USER, null, null)
        db.close()
    }
}
