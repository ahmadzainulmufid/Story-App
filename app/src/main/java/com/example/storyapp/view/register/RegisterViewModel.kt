package com.example.storyapp.view.register

import androidx.lifecycle.ViewModel
import com.example.storyapp.data.repository.StoryRepository

class RegisterViewModel (
    private val repository: StoryRepository) : ViewModel() {
        fun register(
            name: String,
            email: String,
            password: String) = repository.register(name, email, password)
}