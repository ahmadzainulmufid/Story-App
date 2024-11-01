package com.example.storyapp.view.maps

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.repository.StoryRepository
import com.example.storyapp.data.response.Story
import com.example.storyapp.data.result.Result
import kotlinx.coroutines.launch

class MapsViewModel(private val repository: StoryRepository) : ViewModel() {
    private val _storyLocation = MutableLiveData<Result<List<Story>>>()
    val storyLocation: MutableLiveData<Result<List<Story>>> = _storyLocation

    init {
        getStoryWithLocation()
    }

    private fun getStoryWithLocation() {
        viewModelScope.launch {
            _storyLocation.value = Result.Loading // Set loading state
            try {
                val response = repository.getStoriesWithLocation()
                if (response is Result.Success) {
                    val storyList: List<Story> = response.data.listStory.map { listItem ->
                        Story(
                            id = listItem.id,
                            name = listItem.name,
                            description = listItem.description,
                            lat = listItem.lat,
                            lon = listItem.lon,
                            photoUrl = listItem.photoUrl,
                            createdAt = listItem.createdAt
                        )
                    }

                    _storyLocation.value = if (storyList.isNotEmpty()) {
                        Result.Success(storyList)
                    } else {
                        Result.Error("Data not found")
                    }
                } else if (response is Result.Error) {
                    _storyLocation.value = Result.Error(response.error)
                }
            } catch (e: Exception) {
                _storyLocation.value = Result.Error(e.message.toString())
            }
        }
    }
}
