package com.poy.chatters.services

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface IFireBaseAPI {

    @get:GET("/users.json")
    val allUsersAsJsonString: Call<String>

    @GET
    fun getUserFriendsListAsJsonString(@Url url: String): Call<String>

    @GET
    fun getSingleUserByEmail(@Url url: String): Call<String>

}