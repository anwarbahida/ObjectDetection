package com.example.test.data.network

import com.example.test.data.model.Post
import com.example.test.data.model.User
import retrofit2.*
import retrofit2.http.*

interface ApiService {

    @GET("users")
    suspend fun getUsers(): List<User>

    @GET("posts?_limit=7")
    suspend fun getPosts(): List<Post>

    @POST("users")
    suspend fun createUser(@Body user: User): Response<User>

}