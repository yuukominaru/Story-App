package com.dicoding.storyapp.view.maps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.api.ApiConfig
import com.dicoding.storyapp.api.FetchStory
import com.dicoding.storyapp.api.ListStory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsViewModel : ViewModel() {
    private val _stories = MutableLiveData<List<ListStory>>()
    val stories: LiveData<List<ListStory>> = _stories

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    fun getLocationStory(token: String) {
        val client = ApiConfig.getApiService().getStories(token, 50)
        client.enqueue(object : Callback<FetchStory> {
            override fun onResponse(call: Call<FetchStory>, response: Response<FetchStory>) {
                val responseBody = response.body()

                if (response.isSuccessful) {
                    Log.i(TAG, "getLocationStory responseBody: ${responseBody!!.listStory}")
                    _stories.value = responseBody.listStory
                } else {
                    _error.value = true
                    Log.e(TAG, "getLocationStory onFailure \"onResponse\": ${response.body().toString()} & ${response.message()}")
                }
            }

            override fun onFailure(call: Call<FetchStory>, t: Throwable) {
                _error.value = true
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    companion object {
        private const val TAG = "MapsViewModel"
    }
}