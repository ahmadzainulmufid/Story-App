package com.example.storyapp.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyapp.R
import com.example.storyapp.data.response.ListStoryItem

class StoryAdapter(
    private val onItemClick: (ListStoryItem, View, View, View) -> Unit
) : ListAdapter<ListStoryItem, StoryAdapter.StoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false)
        return StoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        val story = getItem(position)
        holder.bind(story, onItemClick)
    }

    class StoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivItemPhoto: ImageView = itemView.findViewById(R.id.iv_item_photo)
        private val tvItemName: TextView = itemView.findViewById(R.id.tv_item_name)
        private val tvItemDescription: TextView = itemView.findViewById(R.id.tv_item_description)

        fun bind(story: ListStoryItem, onItemClick: (ListStoryItem, View, View, View) -> Unit) {
            tvItemName.text = story.name
            tvItemDescription.text = story.description

            // Use Glide to load the image
            Glide.with(itemView.context)
                .load(story.photoUrl)
                .into(ivItemPhoto)

            // Set click listener
            itemView.setOnClickListener {
                // Call the onItemClick function with the views
                onItemClick(story, ivItemPhoto, tvItemName, tvItemDescription)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
