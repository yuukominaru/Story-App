package com.dicoding.storyapp.view.upload

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.api.ApiConfig
import com.dicoding.storyapp.api.UploadStory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UploadViewModel : ViewModel() {
    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _error = MutableLiveData<Boolean>()
    val error: LiveData<Boolean> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    val isPicked = MutableLiveData<Boolean>()
    val latValue = MutableLiveData<Double>()
    val lonValue = MutableLiveData<Double>()

    fun uploadStory(
        token: String,
        imageMultipart: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody? = null,
        lon: RequestBody? = null,
        isPicked: Boolean = false
    ) {
        val client = if (!isPicked) {
            ApiConfig.getApiService().uploadImage(token, imageMultipart, description)
        } else {
            ApiConfig.getApiService().uploadImage(token, imageMultipart, description, lat!!, lon!!)
        }
        client.enqueue(object : Callback<UploadStory> {
            override fun onResponse(call: Call<UploadStory>, response: Response<UploadStory>) {
                Log.i(TAG, "token, imageMultipart, description, lat, lon: $token || $imageMultipart || $description || $lat || $lon")
                val responseBody = response.body()

                if (response.isSuccessful) {
                    _isLoading.value = false
                    if (responseBody != null && !responseBody.error) {
                        Log.i(TAG, "uploadStory responseBody: ${responseBody.message}")
                        _message.value = responseBody.message
                    }
                } else {
                    _message.value = response.message()
                    Log.e(TAG, "uploadStory onFailure \"onResponse\": ${response.body().toString()} & ${response.message()}")
                }
            }
            override fun onFailure(call: Call<UploadStory>, t: Throwable) {
                _isLoading.value = false
                _error.value = true
                Log.e(TAG, "onFailure: ${t.message.toString()}")
            }
        })
    }

    companion object {
        private const val TAG = "UploadViewModel"
    }
}