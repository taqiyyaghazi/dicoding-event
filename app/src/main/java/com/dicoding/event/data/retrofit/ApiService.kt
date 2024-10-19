package com.dicoding.event.data.retrofit

import com.dicoding.event.data.response.DetailEventResponse
import com.dicoding.event.data.response.EventResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("events")
    fun getEvents(
        @Query("active") active: Int
    ): Call<EventResponse>

    @GET("events/{id}")
    fun getEventById(
        @Path("id") eventId: Int
    ): Call<DetailEventResponse>

}