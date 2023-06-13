package com.dicoding.storyapp.view.maps

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.storyapp.R
import com.dicoding.storyapp.api.ListStory
import com.dicoding.storyapp.databinding.ActivityMapsBinding
import com.dicoding.storyapp.preference.UserPreference
import com.dicoding.storyapp.view.detail.DetailActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val mapsViewModel by viewModels<MapsViewModel>()

    private val idnLocation = LatLng(-6.200000, 106.816666)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.explore)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true
        mMap.uiSettings.isTiltGesturesEnabled = true

        setMapStyle()
        getMyLocation()
        setStoriesMarker()

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(idnLocation, 5f))
    }

    private fun setMapStyle() {
        try {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    // Night mode is not active, we're using the light theme.
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_light))
                }
                Configuration.UI_MODE_NIGHT_YES -> {
                    // Night mode is active, we're using dark theme.
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))
                }
                else -> {
                    Log.e(TAG, "Style parsing failed.")
                }
            }
        } catch (exception: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", exception)
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getMyLocation()
            }
        }

    private fun getMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun setStoriesMarker() {
        val tempToken = UserPreference.getInstance(this@MapsActivity).user.token
        val token = StringBuilder("Bearer ").append(tempToken).toString()
        mapsViewModel.getLocationStory(token)

        mapsViewModel.stories.observe(this) { stories ->
            stories.forEach { story ->
                val latLng = LatLng(story.lat!!.toDouble(), story.lon!!.toDouble())
                mMap.addMarker(MarkerOptions()
                    .position(latLng)
                    .title(story.name)
                    .snippet(story.description)
                )?.tag = story
            }
        }

        mMap.setOnInfoWindowClickListener {
            val data: ListStory = it.tag as ListStory
            goToDetailPage(data)
        }
    }

    private fun goToDetailPage(data: ListStory) {
        val intent = Intent(this@MapsActivity, DetailActivity::class.java).also {
            it.putExtra(DetailActivity.EXTRA_NAME, data.name)
            it.putExtra(DetailActivity.EXTRA_DESCRIPTION, data.description)
            it.putExtra(DetailActivity.EXTRA_URL, data.photoUrl)
            it.putExtra(DetailActivity.EXTRA_LAT, data.lat)
            it.putExtra(DetailActivity.EXTRA_LON, data.lon)
        }

        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_maps, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_info -> {
                AlertDialog.Builder(this).apply {
                    setTitle(getString(R.string.information))
                    setMessage(getString(R.string.detail_information))
                    setPositiveButton(R.string.okay) { dialog, _ ->
                        dialog.dismiss()
                    }
                    create()
                    show()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "MapsActivity"
    }
}