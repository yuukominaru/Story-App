package com.dicoding.storyapp.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.dicoding.storyapp.api.ListStory
import com.dicoding.storyapp.database.StoryRepository

class MainViewModel(storyRepository: StoryRepository) : ViewModel() {
    val stories: LiveData<PagingData<ListStory>> =
        storyRepository.getStory().cachedIn(viewModelScope)

    companion object {
        private const val TAG = "MainViewModel"
    }
}