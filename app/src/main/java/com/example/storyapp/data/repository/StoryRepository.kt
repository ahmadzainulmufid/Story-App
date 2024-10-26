package com.example.storyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import com.example.storyapp.data.pref.UserModel
import com.example.storyapp.data.pref.UserPreference
import com.example.storyapp.data.response.DetailResponse
import com.example.storyapp.data.response.ErrorResponse
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.data.response.RegisterResponse
import com.example.storyapp.data.retrofit.ApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import com.example.storyapp.data.result.Result
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    fun register(
        name: String,
        email: String,
        password: String,
    ): LiveData<Result<RegisterResponse>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.register(name = name, email = email, password = password)
            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message
            emit(Result.Error(errorMessage.toString()))
        }
    }

    fun login(email: String, password: String): LiveData<Result<UserModel>> = liveData {
        emit(Result.Loading)

        try {
            val response = apiService.login(email, password).loginResult
            userPreference.saveSession(UserModel(email, response.token, true))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message
            emit(Result.Error(errorMessage.toString()))
        }
        val data: LiveData<Result<UserModel>> = userPreference.getSession().asLiveData().map {
            Result.Success(it)
        }
        emitSource(data)
    }

    fun getStories():LiveData<Result<List<ListStoryItem>>> = liveData {
        emit(Result.Loading)
        try {
            val token = userPreference.getSession().first().token
            val response = apiService.getStories("Bearer $token").listStory
            emit(Result.Success(response))
        } catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message
            emit(Result.Error(errorMessage.toString()))
        }
    }

    fun uploadStory(file: MultipartBody.Part, description: RequestBody): LiveData<Result<ErrorResponse>> = liveData {
        emit(Result.Loading)
        try{
            val token = userPreference.getSession().first().token
            val response = apiService.uploadStory("Bearer $token",file,description)
            emit(Result.Success(response))
        }catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message
            emit(Result.Error(errorMessage.toString()))
        }
    }

    suspend fun getStoryDetail(storyId: String): Result<DetailResponse> {
        val userModel = userPreference.getSession().first()
        val token = userModel.token

        return if (token.isNotEmpty()) {
            try {
                val response = apiService.getStoryDetail(storyId, "Bearer $token")
                if (response.error) {
                    Result.Error(response.message)
                } else {
                    Result.Success(response)
                }
            } catch (e: Exception) {
                Result.Error(e.message ?: "Unknown error")
            }
        } else {
            Result.Error("User is not logged in")
        }
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(userPreference, apiService)
            }.also { instance = it }
    }
}