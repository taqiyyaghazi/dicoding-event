package com.dicoding.event.ui.upcoming

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.event.data.response.EventResponse
import com.dicoding.event.data.response.ListEventsItem
import com.dicoding.event.data.retrofit.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class UpcomingViewModel : ViewModel() {

    companion object {
        private const val TAG = "UpcomingViewModel"
    }

    private val _upcomingEvents = MutableLiveData<List<ListEventsItem>>()
    val upcomingEvents: LiveData<List<ListEventsItem>> = _upcomingEvents

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        fetchUpcomingEvents()
    }

    private fun fetchUpcomingEvents() {
        _isLoading.value = true
        _errorMessage.value = null
        val client = ApiConfig.getApiService().getEvents(1)
        client.enqueue(object : Callback<EventResponse> {
            override fun onResponse(call: Call<EventResponse>, response: Response<EventResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _upcomingEvents.value = response.body()?.listEvents ?: emptyList()
                } else {
                    _errorMessage.value = "Terjadi kesalahan: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                _isLoading.value = false
                handleError(client, t)
            }
        })
    }

    private fun handleError(call: Call<EventResponse>, t: Throwable) {
        try {
            val response = call.execute()
            val message = when {
                !response.isSuccessful && response.code() == 404 -> "Event tidak ditemukan"
                !response.isSuccessful && response.code() == 500 -> "Server sedang dalam perbaikan"
                t is UnknownHostException -> "Perangkat tidak terhubung ke internet"
                t is SocketTimeoutException -> "Koneksi perangkat lemah"
                else -> "Terjadi kesalahan: ${t.localizedMessage}"
            }
            _errorMessage.value = message
        } catch (e: Exception) {
            _errorMessage.value = "Terjadi kesalahan: ${e.localizedMessage}"
            Log.e(TAG, "handleError: ${e.message}")
        }
        Log.e(TAG, "onFailure: ${t.message}")
    }
}