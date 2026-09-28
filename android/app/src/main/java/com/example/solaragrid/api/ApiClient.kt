package com.example.solaragrid.api

import android.content.Context
import com.example.solaragrid.database.UserManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import org.json.JSONObject
import java.io.IOException

// creates a singleton Retrofit instance.
object ApiClient {
    // We now read the Base URL dynamically from local.properties -> BuildConfig
    private const val BASE_URL = com.example.solaragrid.BuildConfig.API_BASE_URL

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
                    val response = chain.proceed(requestBuilder.build())

                    // Global DataAnnotation Validation Error Extractor
                    // Intercepts 400 Bad Request and formats the `.errors` dictionary into an Exception
                    if (response.code() == 400) {
                        response.body()?.let { body ->
                            val bodyString = body.string()
                            try {
                                val json = JSONObject(bodyString)
                                if (json.has("errors")) {
                                    val errorsObj = json.getJSONObject("errors")
                                    val errorMessages = mutableListOf<String>()
                                    val keys = errorsObj.keys()
                                    while (keys.hasNext()) {
                                        val key = keys.next()
                                        val errorArray = errorsObj.getJSONArray(key)
                                        for (i in 0 until errorArray.length()) {
                                            errorMessages.add(errorArray.getString(i))
                                        }
                                    }
                                    if (errorMessages.isNotEmpty()) {
                                        throw IOException(errorMessages.joinToString(" | "))
                                    }
                                }
                            } catch (e: Exception) {
                                // If parsing fails or we threw the custom IOException, handle appropriately
                                if (e is IOException) throw e
                            }
                            
                            // Recreate the response body because calling .string() consumes the original stream!
                            val newBody = okhttp3.ResponseBody.create(body.contentType(), bodyString)
                            return@addInterceptor response.newBuilder().body(newBody).build()
                        }
                    }

                    response
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
