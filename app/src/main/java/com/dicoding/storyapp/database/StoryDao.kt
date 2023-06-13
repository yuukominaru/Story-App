package com.dicoding.storyapp.database

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.storyapp.api.ListStory

@Dao
interface StoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(quote: List<ListStory>)

    @Query("SELECT * FROM listStory")
    fun getAllStory(): PagingSource<Int, ListStory>

    @Query("DELETE FROM listStory")
    suspend fun deleteAll()
}