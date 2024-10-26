package com.example.storyapp.view.widget

import android.content.Intent
import android.widget.RemoteViewsService
import com.example.storyapp.data.pref.UserPreference
import com.example.storyapp.data.pref.dataStore
import com.example.storyapp.data.retrofit.ApiConfig

class StackWidgetService: RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent?): RemoteViewsFactory {
        val pref = UserPreference.getInstance(this.applicationContext.dataStore)
        val apiService = ApiConfig.getApiService()
        return StackRemoteViewsFactory(this.applicationContext, apiService,pref)
    }
}