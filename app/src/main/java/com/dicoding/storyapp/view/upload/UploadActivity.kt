package com.dicoding.storyapp.view.upload

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dicoding.storyapp.R
import com.dicoding.storyapp.databinding.ActivityUploadBinding
import com.dicoding.storyapp.helper.createCustomTempFile
import com.dicoding.storyapp.helper.getAddress
import com.dicoding.storyapp.helper.reduceFileImage
import com.dicoding.storyapp.helper.uriToFile
import com.dicoding.storyapp.preference.UserPreference
import com.dicoding.storyapp.view.main.MainActivity
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class UploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding
    private val uploadViewModel by viewModels<UploadViewModel>()
    private var getFile: File? = null

    private var isPicked = false

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, resources.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = resources.getString(R.string.upload_pic)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }

        uploadViewModel.isPicked.observe(this) {
            showAddress(it)
        }

        binding.ivClearLocation.setOnClickListener {
            uploadViewModel.isPicked.value = false
            isPicked = false
        }

        val tempToken = UserPreference.getInstance(this).user.token
        val token = StringBuilder("Bearer ").append(tempToken).toString()

        binding.cameraButton.setOnClickListener { startTakePhoto() }
        binding.galleryButton.setOnClickListener { startGallery() }
        binding.addLocationButton.setOnClickListener { addLocation() }
        binding.uploadButton.setOnClickListener { uploadImage(token) }
    }

    private fun startTakePhoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also {
            val photoURI: Uri = FileProvider.getUriForFile(
                this@UploadActivity,
                "com.dicoding.storyapp",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            launcherIntentCamera.launch(intent)
        }
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)

            myFile.let { file ->
                getFile = file
                binding.previewImageView.setImageBitmap(BitmapFactory.decodeFile(file.path))
            }
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, resources.getString(R.string.choose_pic))
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val selectedImg: Uri = result.data?.data as Uri

            selectedImg.let { uri ->
                val myFile = uriToFile(uri, this@UploadActivity)
                getFile = myFile
                binding.previewImageView.setImageURI(uri)
            }
        }
    }

    private fun addLocation() {
        if (allPermissionsGranted()) {
            val intent = Intent(this, LocationActivity::class.java)
            launcherIntentLocation.launch(intent)
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    private val launcherIntentLocation = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data

            isPicked = intent!!.getBooleanExtra(EXTRA_PICKED, false)
            val lat = intent.getDoubleExtra(EXTRA_LAT, 0.0)
            val lon = intent.getDoubleExtra(EXTRA_LON, 0.0)
            binding.tvFetchLocation.text = getAddress(this, lat, lon)

            Log.i(TAG, "isPicked, lat, lon: $isPicked || $lat || $lon")

            uploadViewModel.isPicked.value = isPicked
            uploadViewModel.latValue.value = lat
            uploadViewModel.lonValue.value = lon

            Log.i(TAG, "uploadViewModel isPicked, lat, lon: ${uploadViewModel.isPicked.value} || ${uploadViewModel.latValue.value} || ${uploadViewModel.lonValue.value}")
        }
    }

    private lateinit var currentPhotoPath: String
    private fun uploadImage(token: String) {
        if (getFile != null) {
            val file = reduceFileImage(getFile as File)

            val tempDesc = binding.edAddDescription.text.toString()
            val description = tempDesc.toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )

            when {
                tempDesc.isEmpty() -> {
                    if (tempDesc.isEmpty())
                        binding.descContainer.error = resources.getString(R.string.null_desc)
                    else
                        binding.descContainer.error = null
                }
                else -> {
                    if (uploadViewModel.isPicked.value == true) {
                        val tempLat = uploadViewModel.latValue.value.toString()
                        val lat = tempLat.toRequestBody("text/plain".toMediaType())

                        val tempLon = uploadViewModel.lonValue.value.toString()
                        val lon = tempLon.toRequestBody("text/plain".toMediaType())

                        uploadViewModel.uploadStory(token, imageMultipart, description, lat, lon, true)
                    } else {
                        uploadViewModel.uploadStory(token, imageMultipart, description)
                    }
                    uploadViewModel.message.observe(this) {
                        Toast.makeText(this@UploadActivity, it, Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                    uploadViewModel.error.observe(this) {
                        if (it)
                            Toast.makeText(this@UploadActivity, resources.getString(R.string.retrofit_fail), Toast.LENGTH_SHORT).show()
                    }
                    uploadViewModel.isLoading.observe(this) {
                        showLoading(it)
                    }
                }
            }

        } else {
            Toast.makeText(this, resources.getString(R.string.insert_img), Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddress(isPick: Boolean) {
        binding.apply {
            storyLocationLayout.visibility = if (isPick) View.VISIBLE else View.GONE
            addLocationButton.visibility = if (isPick) View.GONE else View.VISIBLE
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar2.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    companion object {
        private const val TAG = "UploadActivity"

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        private const val REQUEST_CODE_PERMISSIONS = 10

        const val EXTRA_RESULT = "extra_result"
        const val EXTRA_PICKED = "extra_picked"
        const val EXTRA_LAT = "extra_lat"
        const val EXTRA_LON = "extra_lon"
    }
}