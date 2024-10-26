package com.example.storyapp.data.di

import android.content.Context
import com.example.storyapp.data.pref.UserPreference
import com.example.storyapp.data.pref.dataStore
import com.example.storyapp.data.repository.StoryRepository
import com.example.storyapp.data.retrofit.ApiConfig

object Injection {
    fun provideRepository(
        context: Context
    ): StoryRepository {
        val pref = UserPreference.getInstance(context.dataStore)
        val apiService = ApiConfig.getApiService()
        return StoryRepository.getInstance(pref, apiService)
    }
}