package com.dicoding.storyapp.view.main

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dicoding.storyapp.api.ListStory
import com.dicoding.storyapp.databinding.ItemStoryBinding
import com.dicoding.storyapp.view.detail.DetailActivity

class MainAdapter : PagingDataAdapter<ListStory, MainAdapter.ViewHolder>(DIFF_CALLBACK) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val story = getItem(position)
        if (story != null) {
            holder.bind(story)
        }
    }

    class ViewHolder(private val view: ItemStoryBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(story: ListStory) {
            Glide.with(itemView)
                .load(story.photoUrl)
                .skipMemoryCache(true)
                .centerCrop()
                .into(view.ivItemPhoto)

            view.tvItemName.text = story.name

            itemView.setOnClickListener {
                val intent = Intent(itemView.context, DetailActivity::class.java).also {
                    it.putExtra(DetailActivity.EXTRA_NAME, story.name)
                    it.putExtra(DetailActivity.EXTRA_DESCRIPTION, story.description)
                    it.putExtra(DetailActivity.EXTRA_URL, story.photoUrl)

                    if (story.lat != null && story.lon != null) {
                        it.putExtra(DetailActivity.EXTRA_LAT, story.lat)
                        it.putExtra(DetailActivity.EXTRA_LON, story.lon)
                    } else {
                        Log.e(TAG, "null lat & lon")
                    }
                }

                val optionsCompat: ActivityOptionsCompat =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                        itemView.context as Activity,
                        Pair(view.ivItemPhoto, "image"),
                        Pair(view.tvItemName, "username")
                    )

                itemView.context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    companion object {
        private const val TAG = "MainAdapter"

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStory>() {
            override fun areItemsTheSame(oldItem: ListStory, newItem: ListStory): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: ListStory, newItem: ListStory): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}