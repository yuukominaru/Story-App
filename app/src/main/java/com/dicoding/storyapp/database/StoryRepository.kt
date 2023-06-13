package com.dicoding.storyapp.database

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.paging.*
import com.dicoding.storyapp.api.ApiService
import com.dicoding.storyapp.api.ListStory

class StoryRepository(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService,
    private val token: String
) {
    fun getStory(): LiveData<PagingData<ListStory>> {
        Log.i(TAG, "getStory: $token")

        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(storyDatabase, apiService, token),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).liveData
    }

    companion object {
        private const val TAG = "StoryRepository"
    }
}