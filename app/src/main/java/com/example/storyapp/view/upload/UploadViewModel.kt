package com.example.storyapp.view.upload

import androidx.lifecycle.ViewModel
import com.example.storyapp.data.repository.StoryRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class UploadViewModel(
    private val storyRepository: StoryRepository,
): ViewModel() {
    fun uploadStory(file : MultipartBody.Part, description: RequestBody) = storyRepository.uploadStory(file,description)
}