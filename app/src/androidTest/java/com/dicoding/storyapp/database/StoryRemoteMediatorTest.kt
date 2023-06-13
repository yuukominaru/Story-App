package com.dicoding.storyapp.database

import androidx.paging.*
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dicoding.storyapp.api.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@ExperimentalPagingApi
@RunWith(AndroidJUnit4::class)
class StoryRemoteMediatorTest  {

    private var dummyToken = "user-yj5pc_LARC_AgK61"
    private var mockApi: ApiService = FakeApiService()
    private var mockDb: StoryDatabase = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        StoryDatabase::class.java
    ).allowMainThreadQueries().build()

    @Test
    fun refreshLoadReturnsSuccessResultWhenMoreDataIsPresent() = runTest {
        val remoteMediator = StoryRemoteMediator(
            mockDb,
            mockApi,
            dummyToken
        )
        val pagingState = PagingState<Int, ListStory>(
            listOf(),
            null,
            PagingConfig(10),
            10
        )
        val result = remoteMediator.load(LoadType.REFRESH, pagingState)
        assertTrue(result is RemoteMediator.MediatorResult.Success)
        assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
    }

    @After
    fun tearDown() {
        mockDb.clearAllTables()
    }
}

class FakeApiService : ApiService {
    override fun postRegister(name: String, email: String, password: String): Call<Register> {
        val client = ApiConfig.getApiService().postRegister(name, email, password)
        client.enqueue(object : Callback<Register> {
            override fun onResponse(call: Call<Register>, response: Response<Register>) {
                val responseBody = response.body()
            }

            override fun onFailure(call: Call<Register>, t: Throwable) {
            }
        })
        return client
    }

    override fun postLogin(email: String, password: String): Call<Login> {
        val client = ApiConfig.getApiService().postLogin(email, password)
        client.enqueue(object : Callback<Login> {
            override fun onResponse(call: Call<Login>, response: Response<Login>) {
                val responseBody = response.body()
            }

            override fun onFailure(call: Call<Login>, t: Throwable) {
            }
        })
        return client
    }

    override suspend fun getStoryList(token: String, page: Int, size: Int): FetchStory {
        val items: MutableList<ListStory> = arrayListOf()
        for (i in 0..100) {
            val story = ListStory(
                i.toString(),
                "name + $i",
                "description $i",
                "photoUrl $i",
                "createAt $i",
                "lat $i",
                "lon $i",
            )
            items.add(story)
        }
        return FetchStory(false, "Stories fetched successfully", items)
    }

    override fun getStories(token: String, size: Int): Call<FetchStory> {
        val client = ApiConfig.getApiService().getStories(token, size)
        client.enqueue(object : Callback<FetchStory> {
            override fun onResponse(call: Call<FetchStory>, response: Response<FetchStory>) {
                val responseBody = response.body()
            }

            override fun onFailure(call: Call<FetchStory>, t: Throwable) {
            }
        })
        return client
    }

    override fun uploadImage(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
    ): Call<UploadStory> {
        val client = ApiConfig.getApiService().uploadImage(token, file, description)
        client.enqueue(object : Callback<UploadStory> {
            override fun onResponse(call: Call<UploadStory>, response: Response<UploadStory>) {
                val responseBody = response.body()
            }

            override fun onFailure(call: Call<UploadStory>, t: Throwable) {
            }
        })
        return client
    }

    override fun uploadImage(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody,
        lon: RequestBody,
    ): Call<UploadStory> {
        val client = ApiConfig.getApiService().uploadImage(token, file, description, lat, lon)
        client.enqueue(object : Callback<UploadStory> {
            override fun onResponse(call: Call<UploadStory>, response: Response<UploadStory>) {
                val responseBody = response.body()
            }

            override fun onFailure(call: Call<UploadStory>, t: Throwable) {
            }
        })
        return client
    }

}