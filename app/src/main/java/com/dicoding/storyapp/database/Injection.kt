package com.dicoding.storyapp.database

import android.content.Context
import android.util.Log
import com.dicoding.storyapp.api.ApiConfig
import com.dicoding.storyapp.preference.UserPreference

object Injection {
    fun provideRepository(context: Context): StoryRepository {
        val database = StoryDatabase.getDatabase(context)
        val apiService = ApiConfig.getApiService()
        val tempToken = UserPreference.getInstance(context).user.token
        val token = StringBuilder("Bearer ").append(tempToken).toString()
        Log.i("Injection", "provideRepository: token $token")

        return StoryRepository(database, apiService, token)
    }
}