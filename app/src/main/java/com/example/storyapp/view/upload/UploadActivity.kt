package com.example.storyapp.view.upload

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.OnBackPressedCallback
import com.example.storyapp.R
import com.example.storyapp.data.result.Result
import com.example.storyapp.databinding.ActivityUploadBinding
import com.example.storyapp.view.ViewModelFactory
import com.example.storyapp.view.getImageUri
import com.example.storyapp.view.main.MainActivity
import com.example.storyapp.view.reduceFileImage
import com.example.storyapp.view.uriToFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class UploadActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadBinding
    private var currentImageUri: Uri? = null
    private val viewModel by viewModels<UploadViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            title = getString(R.string.upload_story)
        }

        binding.btnGallery.setOnClickListener {
            galleryLaunch.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnCamera.setOnClickListener {
            currentImageUri = getImageUri(this)
            cameraLaunch.launch(currentImageUri!!)
        }

        binding.buttonAdd.setOnClickListener {
            if (!binding.edAddDescription.text.isNullOrBlank() && currentImageUri != null) {
                currentImageUri?.let { uri ->
                    val imageFile = uriToFile(uri, this).reduceFileImage()
                    val desc = binding.edAddDescription.text.toString()

                    showLoading(true)

                    val requestBody = desc.toRequestBody("text/plain".toMediaType())
                    val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())

                    val multipartBody =
                        MultipartBody.Part.createFormData("photo", imageFile.name, requestImageFile)

                    viewModel.uploadStory(multipartBody, requestBody).observe(this) { response ->
                        when (response) {
                            is Result.Error -> showToast(getString(R.string.upload_failed))
                            Result.Loading -> showLoading(true)
                            is Result.Success -> {
                                showToast(getString(R.string.upload_success))
                                runBlocking {
                                    delay(500)
                                }
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                            }
                        }
                    }
                }
            } else {
                showToast(getString(R.string.upload_not_valid))
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@UploadActivity).apply {
                    setMessage(R.string.confirmation_upload)
                    setPositiveButton(R.string.ya) { _, _ ->
                        val intent = Intent(this@UploadActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }
                    setNegativeButton(R.string.tidak) { dialog, _ ->
                        dialog.dismiss()
                    }
                    create()
                    show()
                }
            }
        })
    }

    private val galleryLaunch =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            if (uri != null) {
                currentImageUri = uri
                imageShow()
            } else {
                Log.d("Photo Picker", "No media selected")
            }
        }

    private val cameraLaunch =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isSuccess ->
            if (isSuccess) {
                imageShow()
            } else {
                currentImageUri = null
            }
        }

    private fun imageShow() {
        currentImageUri?.let { uri ->
            binding.imgPlaceholder.setImageURI(uri)
            binding.imgPlaceholder.background = null
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showToast(message: String) {
        showLoading(false)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
