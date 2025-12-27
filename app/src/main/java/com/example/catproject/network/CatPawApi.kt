package com.example.catproject.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// --- DATA MODELS ---
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val profile_picture_url: String?,
    val bio: String?,
    val role: String = "user" // Default user, bisa "admin"
)
data class LoginResponse(val status: String, val message: String?, val user: User?)
data class BaseResponse(val status: String, val message: String?)
data class GenericResponse(val status: String, val message: String? = null)
data class ResponseModel(
    val status: String,
    val message: String? = null // Nullable karena delete_notification.php tidak mengirim pesan, tapi delete_post.php mengirim pesan
)

// --- NOTIFICATION MODEL (UPDATED) ---
data class NotificationItem(
    val id: Int,
    val type: String,
    val created_at: String,
    val actor_id: Int,
    val actor_name: String,
    val actor_pic: String?,
    val post_image: String?,
    val is_following: Boolean
)
// --- STORY MODELS ---
data class StoryUser(val id: Int, val username: String, val profile_picture_url: String?)
data class StoryItem(val id: Int, val image_url: String, val username: String?, val profile_picture_url: String?)

// --- POST MODELS ---
data class Post(val id: Int, val user_id: Int, val username: String, val profile_picture_url: String?, val image_url: String, val caption: String?, var is_liked: Boolean = false, var like_count: Int = 0)
data class GridPost(val id: Int, val image_url: String)
data class Comment(val id: Int, val username: String, val comment: String, val profile_picture_url: String?)
data class CommentRequest(val user_id: Int, val post_id: Int, val comment: String)
data class LikeRequest(val user_id: Int, val post_id: Int)

// --- OTHER MODELS ---
data class FollowRequest(val follower_id: Int, val following_id: Int)
data class FollowResponse(val status: String)
data class LoginRequest(val username: String, val password: String)
data class SignupRequest(val username: String, val password: String, val email: String)
data class DeletePostRequest(val post_id: Int, val user_id: Int)
data class ProfileResponse(val user: User, val stats: Stats, val posts: List<GridPost>, val adopted_cats: List<AdoptedCatItem>, val is_following: Boolean = false)
data class Stats(val posts: Int, val followers: Int, val following: Int)
data class AdoptedCatItem(val id: Int, val image_url: String, val cat_name: String)

// --- CHAT MODELS ---
data class ChatMessage(val id: Int, val sender_id: Int, val receiver_id: Int, val message: String, val created_at: String)
data class ChatUserItem(val id: Int, val username: String, val profile_picture_url: String?, val last_message: String?)
data class SendMessageRequest(val sender_id: Int, val receiver_id: Int, val message: String)

// --- ADOPT & EVENT MODELS ---
data class AdoptionPost(val id: Int, val user_id: Int, val cat_name: String, val description: String, val contact_info: String, val image_url: String, val username: String, val profile_picture_url: String?, val status: String)
data class ApplyAdoptionRequest(val adoption_id: Int, val applicant_id: Int, val message: String, val phone_number: String)
data class DeleteAdoptionRequest(val adoption_id: Int, val user_id: Int)
data class AdoptActionRequest(val adoption_id: Int, val adopter_id: Int)
data class EventPost(val id: Int, val user_id: Int, val title: String, val description: String, val event_date: String, val time: String, val location: String, val image_url: String?)
data class DeleteEventRequest(val event_id: Int, val user_id: Int)
data class DeleteNotificationRequest(
    val notification_id: Int,
    val user_id: Int
)

// Admin
data class AdminUpdateRoleRequest(val target_user_id: Int, val new_role: String)
data class AdminDeleteUserRequest(val target_user_id: Int)
// --- API INTERFACE ---
interface ApiService {
    @POST("catpaw_api/login.php") suspend fun login(@Body r: LoginRequest): LoginResponse
    @POST("catpaw_api/signup.php") suspend fun signup(@Body r: SignupRequest): BaseResponse

    @GET("catpaw_api/get_posts.php") suspend fun getPosts(@Query("user_id") uid: Int): List<Post>
    @GET("catpaw_api/get_explore.php") suspend fun getExplore(): List<GridPost>
    @GET("catpaw_api/get_post_detail.php") suspend fun getPostDetail(@Query("post_id") pid: Int, @Query("current_user_id") uid: Int): Post
    @Multipart @POST("catpaw_api/upload_post.php") suspend fun uploadPost(@Part("user_id") uid: RequestBody, @Part("caption") cap: RequestBody, @Part img: MultipartBody.Part): GenericResponse
    @POST("catpaw_api/delete_post.php") suspend fun deletePost(@Body r: DeletePostRequest): BaseResponse

    @POST("catpaw_api/toggle_like.php") suspend fun toggleLike(@Body r: LikeRequest): BaseResponse
    @GET("catpaw_api/comment_actions.php") suspend fun getComments(@Query("post_id") pid: Int): List<Comment>
    @POST("catpaw_api/comment_actions.php") suspend fun addComment(@Body r: CommentRequest): BaseResponse

    @GET("catpaw_api/get_profile_data.php") suspend fun getProfile(@Query("user_id") userId: Int, @Query("viewer_id") viewerId: Int): ProfileResponse
    @Multipart @POST("catpaw_api/update_profile.php") suspend fun updateProfile(@Part("user_id") u: RequestBody, @Part("username") n: RequestBody, @Part("bio") b: RequestBody, @Part img: MultipartBody.Part?): LoginResponse
    @POST("catpaw_api/toggle_follow.php") suspend fun toggleFollow(@Body r: FollowRequest): FollowResponse
    @GET("catpaw_api/search_users.php") suspend fun searchUsers(@Query("query") q: String): List<User>

    @GET("catpaw_api/get_adoptions.php") suspend fun getAdoptions(): List<AdoptionPost>
    @Multipart @POST("catpaw_api/upload_adoption.php") suspend fun uploadAdoption(@Part("user_id") u: RequestBody, @Part("cat_name") n: RequestBody, @Part("description") d: RequestBody, @Part("contact_info") c: RequestBody, @Part img: MultipartBody.Part): BaseResponse
    @POST("catpaw_api/apply_adoption.php") suspend fun applyAdoption(@Body r: ApplyAdoptionRequest): BaseResponse
    @POST("catpaw_api/delete_adoption.php") suspend fun deleteAdoption(@Body r: DeleteAdoptionRequest): BaseResponse
    @POST("catpaw_api/adopt_action.php") suspend fun confirmAdoption(@Body r: AdoptActionRequest): BaseResponse

    @POST("catpaw_api/send_message.php") suspend fun sendMessage(@Body r: SendMessageRequest): BaseResponse
    @GET("catpaw_api/get_chat_detail.php") suspend fun getChatDetail(@Query("my_id") myId: Int, @Query("other_id") otherId: Int): List<ChatMessage>
    @GET("catpaw_api/get_chat_list.php") suspend fun getChatList(@Query("user_id") myId: Int): List<ChatUserItem>

    @GET("catpaw_api/get_events.php") suspend fun getEvents(): List<EventPost>
    @Multipart @POST("catpaw_api/create_event.php") suspend fun createEvent(@Part("user_id") userId: RequestBody, @Part("title") title: RequestBody, @Part("description") desc: RequestBody, @Part("event_date") date: RequestBody, @Part("time") time: RequestBody, @Part("location") loc: RequestBody, @Part image: MultipartBody.Part): BaseResponse
    @POST("catpaw_api/delete_event.php") suspend fun deleteEvent(@Body r: DeleteEventRequest): BaseResponse

    @GET("catpaw_api/get_notifications.php") suspend fun getNotifications(@Query("user_id") uid: Int): List<NotificationItem>
    @POST("catpaw_api/delete_notification.php") suspend fun deleteNotification(@Body request: DeleteNotificationRequest): ResponseModel
    @Multipart @POST("catpaw_api/upload_story.php") suspend fun uploadStory(@Part("user_id") userId: RequestBody, @Part image: MultipartBody.Part): GenericResponse
    @GET("catpaw_api/get_active_stories.php") suspend fun getActiveStories(): List<StoryUser>
    @GET("catpaw_api/get_user_stories.php") suspend fun getUserStories(@Query("user_id") userId: Int): List<StoryItem>

    // ADMIN ENDPOINTS
    @GET("catpaw_api/admin_get_users.php")
    suspend fun adminGetUsers(@Query("admin_id") adminId: Int): List<User>

    @POST("catpaw_api/admin_update_role.php")
    suspend fun adminUpdateRole(@Body req: AdminUpdateRoleRequest): ResponseModel

    @POST("catpaw_api/admin_delete_user.php")
    suspend fun adminDeleteUser(@Body req: AdminDeleteUserRequest): ResponseModel

    @GET("catpaw_api/admin_get_all_posts.php")
    suspend fun adminGetAllPosts(): List<Post>
}

object RetrofitClient {
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}