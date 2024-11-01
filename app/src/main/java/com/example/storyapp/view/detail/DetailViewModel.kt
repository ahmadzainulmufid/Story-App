package com.example.storyapp.view.detail

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.storyapp.data.repository.StoryRepository
import com.example.storyapp.data.response.DetailResponse
import com.example.storyapp.data.result.Result
import kotlinx.coroutines.flow.flow

class DetailViewModel(private val repository: StoryRepository) : ViewModel() {
    private lateinit var _storyDetail: LiveData<Result<DetailResponse>>
    val storyDetail: LiveData<Result<DetailResponse>> get() = _storyDetail

    fun getStoryById(storyId: String, lat: Double? = null, lon: Double? = null) {
        _storyDetail = flow {
            emit(Result.Loading)
            try {
                val response = repository.getStoryDetail(storyId, lat, lon)
                emit(response)
            } catch (e: Exception) {
                emit(Result.Error(e.message ?: "Unknown error"))
            }
        }.asLiveData()
    }
}
