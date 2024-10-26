package com.example.storyapp.view.login

import androidx.lifecycle.ViewModel
import com.example.storyapp.data.repository.StoryRepository

class LoginViewModel(private val repository: StoryRepository) : ViewModel() {
    fun login(email: String, password: String) = repository.login(email, password)
}
