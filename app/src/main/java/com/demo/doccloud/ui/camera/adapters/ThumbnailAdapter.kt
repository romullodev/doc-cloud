package com.demo.doccloud.ui.camera.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.demo.doccloud.databinding.PictureItemBinding
import com.demo.doccloud.domain.Photo


class ThumbnailAdapter(
    private val widthScreenDivided: Int
): ListAdapter<Photo, ThumbnailAdapter.ThumbnailAdapterViewHolder>(DeliveryDiffCallback()){

    class DeliveryDiffCallback : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, view: Int): ThumbnailAdapterViewHolder {

        return ThumbnailAdapterViewHolder.from(parent)
    }

    override fun onBindViewHolder(viewHolder: ThumbnailAdapterViewHolder, position: Int) {
        viewHolder.bindView(getItem(position), position, widthScreenDivided)
    }

    class ThumbnailAdapterViewHolder private constructor(private val binding: PictureItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(photo: Photo, position: Int, widthScreenDivided: Int) {
            binding.cardView.layoutParams.width = widthScreenDivided
            binding.number = if (position + 1 < 10) "0${(position + 1)}" else (position + 1).toString()
            Glide.with(binding.root.context).load(photo.path)
                .thumbnail(0.01f)
                .centerCrop()
                .into(binding.photo)

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ThumbnailAdapterViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PictureItemBinding.inflate(layoutInflater, parent, false)
                return ThumbnailAdapterViewHolder(binding)
            }
        }
    }
}