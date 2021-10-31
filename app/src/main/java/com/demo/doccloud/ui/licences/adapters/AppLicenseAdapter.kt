package com.demo.doccloud.ui.licences.adapters

import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.demo.doccloud.databinding.LicenseItemBinding
import com.demo.doccloud.domain.entities.AppLicense

class AppLicenseAdapter : ListAdapter<AppLicense, AppLicenseAdapter.AppLicenseViewHolder>(
    LicenseAdapterDiffCallback()
) {

    class LicenseAdapterDiffCallback : DiffUtil.ItemCallback<AppLicense>() {
        override fun areItemsTheSame(oldItem: AppLicense, newItem: AppLicense): Boolean {
            return oldItem.name == newItem.name
        }
        override fun areContentsTheSame(oldItem: AppLicense, newItem: AppLicense): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, view: Int): AppLicenseViewHolder {
        return AppLicenseViewHolder.from(parent)
    }
    override fun onBindViewHolder(viewHolder: AppLicenseViewHolder, position: Int) {
        viewHolder.bindView(getItem(position))
    }

    class AppLicenseViewHolder private constructor(private val binding: LicenseItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(appLicense: AppLicense) {
            binding.appLicense = appLicense
            binding.nameLib.movementMethod = LinkMovementMethod.getInstance()
            binding.nameLib.setLinkTextColor(Color.BLUE)

            val text = "<a href=\"${appLicense.url}\">${appLicense.name}</a>"
            binding.nameLib.text = HtmlCompat.fromHtml(text, HtmlCompat.FROM_HTML_MODE_COMPACT)
            binding.executePendingBindings()
        }
        companion object{
            fun from(parent: ViewGroup): AppLicenseViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = LicenseItemBinding.inflate(layoutInflater, parent, false)
                return AppLicenseViewHolder(binding)
            }
        }
    }
}