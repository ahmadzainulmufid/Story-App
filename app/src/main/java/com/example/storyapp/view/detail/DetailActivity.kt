package com.example.storyapp.view.detail

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.data.response.Story // Make sure to import Story class
import com.example.storyapp.databinding.ActivityDetailBinding
import com.example.storyapp.view.ViewModelFactory
import com.example.storyapp.data.result.Result

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val detailViewModel: DetailViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivDetailPhoto.transitionName = "shared_image"
        binding.tvDetailName.transitionName = "shared_name"
        binding.tvDetailDescription.transitionName = "shared_description"

        supportActionBar?.apply {
            title = getString(R.string.detail_story)
            setDisplayHomeAsUpEnabled(true)
        }

        val storyId = intent.getStringExtra("EXTRA_STORY_ID")
        if (storyId != null) {
            detailViewModel.getStoryById(storyId)
            detailViewModel.storyDetail.observe(this) { result ->
                when (result) {
                    is Result.Loading -> showLoading(true)
                    is Result.Success -> {
                        showLoading(false)
                        populateStoryDetail(result.data.story) // Update to access the story from DetailResponse
                    }
                    is Result.Error -> {
                        showLoading(false)
                        showError(result.error)
                    }
                }
            }
        } else {
            showError("Story ID not found")
        }
    }

    private fun populateStoryDetail(story: Story) {
        binding.tvDetailName.text = story.name
        binding.tvDetailDescription.text = story.description
        Glide.with(this).load(story.photoUrl).into(binding.ivDetailPhoto)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        binding.tvDetailName.text = getString(R.string.error_message)
        binding.tvDetailDescription.text = message
    }

    @Suppress("DEPRECATION")
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
