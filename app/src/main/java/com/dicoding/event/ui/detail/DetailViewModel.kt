package com.dicoding.event.ui.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.event.data.response.DetailEventResponse
import com.dicoding.event.data.response.Event
import com.dicoding.event.data.response.EventResponse
import com.dicoding.event.data.response.ListEventsItem
import com.dicoding.event.data.retrofit.ApiConfig
import com.dicoding.event.ui.finished.FinishedViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class DetailViewModel : ViewModel() {

    companion object {
        private const val TAG = "DetailViewModel"
    }

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event> = _event

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    fun fetchDetailEvent(id: Int) {
        _isLoading.value = true
        _errorMessage.value = null
        val client = ApiConfig.getApiService().getEventById(id)
        client.enqueue(object : Callback<DetailEventResponse> {
            override fun onResponse(call: Call<DetailEventResponse>, response: Response<DetailEventResponse>) {
                _isLoading.value = false
                if (response.isSuccessful) {
                    _event.value = response.body()?.event
                } else {
                    _errorMessage.value = "Terjadi kesalahan: ${response.message()}"
                }
            }

            override fun onFailure(call: Call<DetailEventResponse>, t: Throwable) {
                _isLoading.value = false
                handleError(client, t)
            }
        })
    }

    private fun handleError(call: Call<DetailEventResponse>, t: Throwable) {
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
            Log.e(DetailViewModel.TAG, "handleError: ${e.message}")
        }
        Log.e(DetailViewModel.TAG, "onFailure: ${t.message}")
    }
}