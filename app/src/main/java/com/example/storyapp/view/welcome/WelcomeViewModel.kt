package com.example.storyapp.view.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.storyapp.data.repository.StoryRepository

class WelcomeViewModel(private val storyRepository: StoryRepository): ViewModel() {
    fun getSession() = storyRepository.getSession().asLiveData()
}