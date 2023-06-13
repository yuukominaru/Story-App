package com.dicoding.storyapp

import com.dicoding.storyapp.api.ListStory

object DataDummy {
    fun generateDummyStoryResponse(): List<ListStory> {
        val items: MutableList<ListStory> = arrayListOf()
        for (i in 0..100) {
            val story = ListStory(
                "user-yj5pc_LARC_AgK61",
                "Dimas",
                "Lorem Ipsum",
                "https://story-api.dicoding.dev/images/stories/photos-1641623658595_dummy-pic.png",
                "2022-01-08T06:34:18.598Z",
                "-10.212",
                "-16.002",
            )
            items.add(story)
        }
        return items
    }

//    fun
}