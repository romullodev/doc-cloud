package com.demo.doccloud.ui.edit.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.demo.doccloud.databinding.EditItemBinding
import com.demo.doccloud.domain.Photo

class EditAdapter(
    private val widthScreen: Int,
    private val onClick: OnEditClickListener
): ListAdapter<Photo, EditAdapter.EditAdapterViewHolder>(DeliveryDiffCallback()){

    class DeliveryDiffCallback : DiffUtil.ItemCallback<Photo>() {
        override fun areItemsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Photo, newItem: Photo): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, view: Int): EditAdapterViewHolder {
        return EditAdapterViewHolder.from(parent)
    }

    override fun onBindViewHolder(viewHolder: EditAdapterViewHolder, position: Int) {
        viewHolder.bindView(getItem(position), position, widthScreen, onClick)
    }

    class EditAdapterViewHolder private constructor(private val binding: EditItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(photo: Photo, position: Int, width: Int, action: OnEditClickListener) {
            binding.cardView.apply {
                layoutParams.height = (3*width)/4
                setOnClickListener {
                    action.onEditClick(photo, this)
                }
            }
            binding.number = if (position + 1 < 10) "0${(position + 1)}" else (position + 1).toString()
            Glide.with(binding.root.context).load(photo.path)
                .thumbnail(0.01f)
                .centerCrop()
                .into(binding.photo)

            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): EditAdapterViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = EditItemBinding.inflate(layoutInflater, parent, false)
                return EditAdapterViewHolder(binding)
            }
        }
    }

    interface OnEditClickListener {
        fun onEditClick(photo: Photo, view: View)
    }
}