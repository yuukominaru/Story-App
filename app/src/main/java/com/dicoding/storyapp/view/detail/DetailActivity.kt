package com.dicoding.storyapp.view.detail

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.dicoding.storyapp.databinding.ActivityDetailBinding
import com.dicoding.storyapp.helper.getAddress

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra(EXTRA_NAME)
        val desc = intent.getStringExtra(EXTRA_DESCRIPTION)
        val photoUrl = intent.getStringExtra(EXTRA_URL)
        val lat = intent.getStringExtra(EXTRA_LAT)
        val lon = intent.getStringExtra(EXTRA_LON)

        supportActionBar!!.title = name
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        setStoryDetail(name!!, desc!!, photoUrl!!, lat, lon)
    }

    private fun setStoryDetail(name: String, desc: String, url: String, lat: String?, lon: String?) {
        val doubleLat = lat?.toDouble()
        val doubleLon = lon?.toDouble()

        binding.apply {
            tvDetailName.text = name
            tvDetailDescription.text = desc
            Glide.with(this@DetailActivity)
                .load(url)
                .centerCrop()
                .into(ivDetailPhoto)

            if (lat != null && lon != null) {
                tvLocation.text = getAddress(this@DetailActivity, doubleLat!!, doubleLon!!)
                tvLocation.isVisible = true
            } else {
                tvLocation.isVisible = false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_DESCRIPTION = "extra_description"
        const val EXTRA_URL = "extra_url"
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LON = "extra_lon"
    }
}