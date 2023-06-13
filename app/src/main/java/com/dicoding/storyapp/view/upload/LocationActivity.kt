package com.dicoding.storyapp.view.upload

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityLocationBinding
import com.dicoding.storyapp.helper.getAddress
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class LocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityLocationBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val uploadViewModel by viewModels<UploadViewModel>()

    private var address: String = ""
    private val idnLocation = LatLng(-6.200000, 106.816666)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = getString(R.string.add_location)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setInformationAction()
    }

    private fun setInformationAction() {
        binding.tvCancel.setOnClickListener {
            finish()
        }
        binding.btnConfirm.setOnClickListener {
            if (uploadViewModel.isPicked.value == true) {
                val intent = Intent().also {
                    Log.i(TAG, "isPicked EXTRA_LAT & EXTRA_LON: ${uploadViewModel.isPicked.value} || ${uploadViewModel.latValue.value} || ${uploadViewModel.lonValue.value}")
                    it.putExtra(UploadActivity.EXTRA_PICKED, uploadViewModel.isPicked.value)
                    it.putExtra(UploadActivity.EXTRA_LAT, uploadViewModel.latValue.value)
                    it.putExtra(UploadActivity.EXTRA_LON, uploadViewModel.lonValue.value)
                }
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(this, getString(R.string.null_marker), Toast.LENGTH_SHORT).show()
            }
        }
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
        whenMapPressed()
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
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location.let {
                        if (location != null) {
                            mMap.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 16f))
                            parseToAddress(it.latitude, it.longitude)
                        } else
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(idnLocation, 5f))
                    }
                }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun whenMapPressed() {
        mMap.setOnMapClickListener { latLng ->
            val lat = latLng.latitude
            val lon = latLng.longitude
            mMap.clear()
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.unknown_pin))
                    .snippet(getAddress(this, lat, lon))
            )?.showInfoWindow()
        }

        mMap.setOnPoiClickListener { poi ->
            mMap.clear()
            val poiMarker = mMap.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
                    .snippet(getAddress(this, poi.latLng.latitude, poi.latLng.longitude))
            )
            poiMarker?.showInfoWindow()
        }

        mMap.setOnInfoWindowClickListener {
            parseToAddress(it.position.latitude, it.position.longitude)
        }
    }

    private fun parseToAddress(lat: Double, lon: Double) {
        address = getAddress(this, lat, lon)
        binding.tvUserLocation.text = address

        uploadViewModel.isPicked.value = true
        uploadViewModel.latValue.value = lat
        uploadViewModel.lonValue.value = lon
    }

    companion object {
        private const val TAG = "LocationActivity"
    }
}