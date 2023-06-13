package com.dicoding.storyapp.view.main

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityMainBinding
import com.dicoding.storyapp.preference.UserPreference
import com.dicoding.storyapp.view.StoryViewModelFactory
import com.dicoding.storyapp.view.login.LoginActivity
import com.dicoding.storyapp.view.maps.MapsActivity
import com.dicoding.storyapp.view.upload.UploadActivity
import java.util.*
import kotlin.concurrent.schedule

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: MainAdapter
    private val mainViewModel: MainViewModel by viewModels {
        StoryViewModelFactory(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginCheck()
        setStories()
        addStory()

        binding.refreshLayout.setOnRefreshListener {
            refreshStory()
        }
    }

    private fun loginCheck() {
        if (UserPreference.getInstance(this).isLoggedIn) {
            supportActionBar?.title = StringBuilder("Hello, ").append(UserPreference.getInstance(this).user.name)
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setStories() {
        adapter = MainAdapter()
        binding.rvStory.layoutManager = LinearLayoutManager(this)

        binding.rvStory.setHasFixedSize(true)
        binding.rvStory.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )

        mainViewModel.stories.observe(this) {
            adapter.submitData(lifecycle, it)
        }
    }

    private fun addStory() {
        binding.fab.setOnClickListener {
            startActivity(Intent(this, UploadActivity::class.java))
        }
    }

    private fun refreshStory() {
        binding.refreshLayout.isRefreshing = true
        adapter.refresh()
        Timer().schedule(2000) {
            binding.refreshLayout.isRefreshing = false
            binding.rvStory.smoothScrollToPosition(0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_settings, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                UserPreference.getInstance(this).clearSession()
                Toast.makeText(this, resources.getString(R.string.success_logout), Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            R.id.action_language -> {
                startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
            }
            R.id.action_refresh -> {
                refreshStory()
            }
            R.id.action_explore -> {
                startActivity(Intent(this, MapsActivity::class.java))
            }
        }
        return super.onOptionsItemSelected(item)
    }
}