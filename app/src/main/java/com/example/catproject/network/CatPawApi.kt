package com.example.catproject.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

// DATA MODELS
data class User(val id: Int, val username: String, val profile_picture_url: String?, val bio: String?)
data class Post(val id: Int, val username: String, val profile_picture_url: String?, val image_url: String, val caption: String?, var is_liked: Boolean = false, var like_count: Int = 0)
data class Comment(val id: Int, val username: String, val comment: String, val profile_picture_url: String?)
data class ProfileResponse(val user: User, val stats: Stats, val posts: List<GridPost>)
data class Stats(val posts: Int, val followers: Int, val following: Int)
data class GridPost(val id: Int, val image_url: String)

// REQUEST & RESPONSE MODELS
data class LoginRequest(val username: String, val password: String)
data class SignupRequest(val username: String, val password: String, val email: String)
data class LikeRequest(val user_id: Int, val post_id: Int)
data class CommentRequest(val user_id: Int, val post_id: Int, val comment: String)
data class BaseResponse(val status: String, val message: String?)
data class LoginResponse(val status: String, val message: String?, val user: User?)

// API SERVICE
interface ApiService {
    @POST("catpaw_api/login.php") suspend fun login(@Body r: LoginRequest): LoginResponse
    @POST("catpaw_api/signup.php") suspend fun signup(@Body r: SignupRequest): BaseResponse
    @GET("catpaw_api/get_posts.php") suspend fun getPosts(@Query("user_id") uid: Int): List<Post>
    @POST("catpaw_api/toggle_like.php") suspend fun toggleLike(@Body r: LikeRequest): BaseResponse
    @GET("catpaw_api/comment_actions.php") suspend fun getComments(@Query("post_id") pid: Int): List<Comment>
    @POST("catpaw_api/comment_actions.php") suspend fun addComment(@Body r: CommentRequest): BaseResponse
    @GET("catpaw_api/get_profile_data.php") suspend fun getProfile(@Query("user_id") uid: Int): ProfileResponse

    @Multipart @POST("catpaw_api/upload_post.php")
    suspend fun uploadPost(@Part("user_id") uid: RequestBody, @Part("caption") cap: RequestBody, @Part img: MultipartBody.Part): BaseResponse
}

object RetrofitClient {
    val instance: ApiService by lazy {
        Retrofit.Builder().baseUrl("http://10.0.2.2/") // IP Emulator
            .addConverterFactory(GsonConverterFactory.create()).build().create(ApiService::class.java)
    }
}