package com.demo.doccloud.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.demo.doccloud.databinding.CropItemBinding
import com.demo.doccloud.domain.Photo

class CropAdapter(
    private var clickListener: OnCropClickListener
) :
    ListAdapter<Photo, CropAdapter.CopAdapterViewHolder>(DeliveryDiffCallback()) {

    class DeliveryDiffCallback : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, view: Int): CopAdapterViewHolder {
        return CopAdapterViewHolder.from(parent)
    }

    override fun onBindViewHolder(viewHolder: CopAdapterViewHolder, position: Int) {
        viewHolder.bindView(getItem(position), position, clickListener)
    }

    class CopAdapterViewHolder private constructor(private val binding: CropItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(photo: Photo, position: Int, action: OnCropClickListener) {
            binding.cardView.setOnClickListener{
                action.onCropClick(photo, position)
            }
            binding.deletePhoto.setOnClickListener {
                action.onDeleteClick(photo)
            }
            Glide
                .with(binding.root.context)
                .load(photo.path)
                .centerCrop()
                .into(binding.photo)

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): CopAdapterViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CropItemBinding.inflate(layoutInflater, parent, false)
                return CopAdapterViewHolder(binding)
            }
        }
    }

    interface OnCropClickListener {
        fun onCropClick(photo: Photo, position: Int)
        fun onDeleteClick(photo: Photo)
    }

}