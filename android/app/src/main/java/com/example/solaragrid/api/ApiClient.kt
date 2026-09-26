package com.example.solaragrid.api

import android.content.Context
import com.example.solaragrid.database.UserManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// creates a singleton Retrofit instance.
object ApiClient {
    //special IP that tells the Android Emulator to connect to Mac's localhost.
    private const val BASE_URL = "http://10.0.2.2:5000/"

    private var retrofit: Retrofit? = null

    //pass Context so we can access SQLite through UserManager
    // REFERENCE: The setup for the Retrofit Builder and OkHttpClient was adapted 
    // from the official Retrofit documentation and generic REST API tutorials.
    // Source: https://android-app-development-documentation.readthedocs.io/en/latest/retrofit.html


    fun getClient(context: Context): Retrofit {
        if (retrofit == null) {
            
            // OkHttpClient acts as the middleman for our HTTP requests.
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    // This block runs automatically for EVERY request.
                    val requestBuilder = chain.request().newBuilder()
                    
                    //grab the saved JWT token from our local SQLite database
                    val token = UserManager(context).getToken()
                    
                    // If the user is logged in (token is not empty), attach it as a Bearer header!
                    if (!token.isNullOrEmpty()) {
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }
                    
                    // Proceed to actually send the request to the backend
                    chain.proceed(requestBuilder.build())
                }
                .build()

            // Build the Retrofit instance
            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient) // Attach our interceptor
                .addConverterFactory(GsonConverterFactory.create()) // Automatically convert JSON to Kotlin Data Classes
                .build()
        }
        return retrofit!!
    }
}
