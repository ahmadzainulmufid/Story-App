package com.example.storyapp.view.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.os.bundleOf
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.data.pref.UserPreference
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.data.retrofit.ApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

internal class StackRemoteViewsFactory (
    private val mContext: Context,
    private val apiService: ApiService,
    private val userPreference: UserPreference): RemoteViewsService.RemoteViewsFactory

{
    private var mWidgetItems = listOf<ListStoryItem>()

    override fun onCreate() {
    }

    override fun onDataSetChanged() {
        runBlocking {
            val token = userPreference.getSession().first().token
            try {
                mWidgetItems = apiService.getStories("Bearer $token").listStory
            }catch (_:Exception){

            }
        }
    }

    override fun onDestroy() {
        mWidgetItems = emptyList()
    }

    override fun getCount(): Int = mWidgetItems.size


    override fun getViewAt(position: Int): RemoteViews {
        val story = mWidgetItems[position]
        val extras = bundleOf(
            StoryAppWidget.EXTRA_ITEM to story.name
        )
        val fillIntent = Intent()
        fillIntent.putExtras(extras)

        return RemoteViews(mContext.packageName, R.layout.widget_item).apply {
            setTextViewText(R.id.widget_item_name, story.name)
            setOnClickFillInIntent(R.id.imageView, fillIntent)

            val bitmap = runBlocking {
                try {
                    Glide.with(mContext.applicationContext)
                        .asBitmap()
                        .load(story.photoUrl)
                        .submit()
                        .get()
                } catch (e: Exception) {
                    null
                }
            }

            bitmap?.let { setImageViewBitmap(R.id.imageView, it) }
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = position.toLong()

    override fun hasStableIds(): Boolean = true
}