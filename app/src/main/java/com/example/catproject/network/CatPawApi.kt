package com.example.catproject.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

// --- MODELS ---
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val status: String, val message: String?, val user: User?)
data class User(val id: Int, val username: String, val profile_picture_url: String?, val bio: String?)
data class Post(val id: Int, val username: String, val profile_picture_url: String?, val image_url: String, val caption: String?)
data class ProfileResponse(val user: User, val stats: Stats, val posts: List<GridPost>)
data class Stats(val posts: Int, val followers: Int, val following: Int)
data class GridPost(val id: Int, val image_url: String)
data class UploadResponse(val status: String, val message: String?)

// --- API ---
interface ApiService {
    @POST("catpaw_api/login.php")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("catpaw_api/get_posts.php")
    suspend fun getPosts(): List<Post>

    @GET("catpaw_api/get_profile_data.php")
    suspend fun getProfile(@Query("user_id") userId: Int): ProfileResponse

    @Multipart
    @POST("catpaw_api/upload_post.php")
    suspend fun uploadPost(
        @Part("user_id") userId: RequestBody,
        @Part("caption") caption: RequestBody,
        @Part image: MultipartBody.Part
    ): UploadResponse
}

object RetrofitClient {
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2/") // IP Localhost untuk Emulator
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}