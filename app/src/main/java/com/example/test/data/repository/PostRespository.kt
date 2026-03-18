package com.example.test.data.repository

import com.example.test.data.network.RetrofitInstance

class PostRespository {

    private val api = RetrofitInstance.api
    suspend fun getPosts() = api.getPosts()

}