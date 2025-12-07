package com.example.catproject.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody

// --- DATA MODELS ---
data class User(val id: Int, val username: String, val profile_picture_url: String?, val bio: String?)
data class Post(val id: Int,val user_id: Int, val username: String, val profile_picture_url: String?, val image_url: String, val caption: String?, var is_liked: Boolean = false, var like_count: Int = 0)
data class GridPost(val id: Int, val image_url: String)
data class Comment(val id: Int, val username: String, val comment: String, val profile_picture_url: String?)
data class AdoptionPost(val id: Int, val cat_name: String, val description: String, val contact_info: String, val image_url: String, val username: String, val profile_picture_url: String?)

// --- RESPONSE MODELS ---
data class LoginResponse(val status: String, val message: String?, val user: User?)
data class BaseResponse(val status: String, val message: String?)
// Update: ProfileResponse
data class ProfileResponse(val user: User, val stats: Stats, val posts: List<GridPost>, val is_following: Boolean = false)
data class Stats(val posts: Int, val followers: Int, val following: Int)
data class FollowResponse(val status: String)

// --- REQUEST MODELS ---
data class LoginRequest(val username: String, val password: String)
data class SignupRequest(val username: String, val password: String, val email: String)
data class LikeRequest(val user_id: Int, val post_id: Int)
data class CommentRequest(val user_id: Int, val post_id: Int, val comment: String)
data class FollowRequest(val follower_id: Int, val following_id: Int)

// --- DELETE MODELS ---
data class DeletePostRequest(val post_id: Int, val user_id: Int)

// --- MODELS CHAT ---
data class ChatMessage(
    val id: Int,
    val sender_id: Int,
    val receiver_id: Int,
    val message: String,
    val created_at: String
)

data class ChatUserItem(
    val id: Int,
    val username: String,
    val profile_picture_url: String?,
    val last_message: String?
)

data class SendMessageRequest(val sender_id: Int, val receiver_id: Int, val message: String)

// --- API INTERFACE ---
interface ApiService {
    @POST("catpaw_api/login.php") suspend fun login(@Body r: LoginRequest): LoginResponse
    @POST("catpaw_api/signup.php") suspend fun signup(@Body r: SignupRequest): BaseResponse

    @GET("catpaw_api/get_posts.php") suspend fun getPosts(@Query("user_id") uid: Int): List<Post>
    @GET("catpaw_api/get_explore.php") suspend fun getExplore(): List<GridPost>
    @GET("catpaw_api/get_post_detail.php") suspend fun getPostDetail(@Query("post_id") pid: Int, @Query("current_user_id") uid: Int): Post

    @POST("catpaw_api/toggle_like.php") suspend fun toggleLike(@Body r: LikeRequest): BaseResponse

    @GET("catpaw_api/comment_actions.php") suspend fun getComments(@Query("post_id") pid: Int): List<Comment>
    @POST("catpaw_api/comment_actions.php") suspend fun addComment(@Body r: CommentRequest): BaseResponse

    @GET("catpaw_api/get_adoptions.php") suspend fun getAdoptions(): List<AdoptionPost>

    @Multipart @POST("catpaw_api/upload_post.php")
    suspend fun uploadPost(@Part("user_id") uid: RequestBody, @Part("caption") cap: RequestBody, @Part img: MultipartBody.Part): BaseResponse

    @Multipart @POST("catpaw_api/upload_adoption.php")
    suspend fun uploadAdoption(@Part("user_id") u: RequestBody, @Part("cat_name") n: RequestBody, @Part("description") d: RequestBody, @Part("contact_info") c: RequestBody, @Part img: MultipartBody.Part): BaseResponse

    @Multipart @POST("catpaw_api/update_profile.php")
    suspend fun updateProfile(@Part("user_id") u: RequestBody, @Part("username") n: RequestBody, @Part("bio") b: RequestBody, @Part img: MultipartBody.Part?): LoginResponse

    @GET("catpaw_api/search_users.php")
    suspend fun searchUsers(@Query("query") q: String): List<User>

    @POST("catpaw_api/toggle_follow.php")
    suspend fun toggleFollow(@Body r: FollowRequest): FollowResponse

    // --- PERBAIKAN DI SINI ---
    // Ubah nama parameter Kotlin menjadi 'userId' dan 'viewerId' agar sesuai dengan pemanggilan
    @GET("catpaw_api/get_profile_data.php")
    suspend fun getProfile(
        @Query("user_id") userId: Int,
        @Query("viewer_id") viewerId: Int
    ): ProfileResponse

    @POST("catpaw_api/delete_post.php")
    suspend fun deletePost(@Body r: DeletePostRequest): BaseResponse

    // --- NEW: CHAT ENDPOINTS ---
    @POST("catpaw_api/send_message.php")
    suspend fun sendMessage(@Body r: SendMessageRequest): BaseResponse

    @GET("catpaw_api/get_chat_detail.php")
    suspend fun getChatDetail(
        @Query("my_id") myId: Int,
        @Query("other_id") otherId: Int
    ): List<ChatMessage>

    @GET("catpaw_api/get_chat_list.php")
    suspend fun getChatList(@Query("user_id") myId: Int): List<ChatUserItem>
}

object RetrofitClient {
    val instance: ApiService by lazy {
        Retrofit.Builder().baseUrl("http://10.0.2.2/")
            .addConverterFactory(GsonConverterFactory.create()).build().create(ApiService::class.java)
    }
}