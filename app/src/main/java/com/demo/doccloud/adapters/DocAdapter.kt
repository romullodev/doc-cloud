package com.demo.doccloud.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.demo.doccloud.databinding.HomeDocItemBinding
import com.demo.doccloud.domain.Doc
import java.util.*
import kotlin.collections.ArrayList

class DocAdapter(
    private var clickListener: OnDocClickListener
) :
    ListAdapter<Doc, DocAdapter.DocAdapterViewHolder>(DeliveryDiffCallback()), Filterable {

    var docs = mutableListOf<Doc>()

    fun setList(list: MutableList<Doc>){
        this.docs = list
        submitList(list)
    }

    class DeliveryDiffCallback : DiffUtil.ItemCallback<Doc>() {
        override fun areItemsTheSame(oldItem: Doc, newItem: Doc): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Doc, newItem: Doc): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, view: Int): DocAdapterViewHolder {
        return DocAdapterViewHolder.from(parent)
    }

    override fun onBindViewHolder(viewHolder: DocAdapterViewHolder, position: Int) {
        viewHolder.bindView(getItem(position), position, clickListener)
    }

    class DocAdapterViewHolder private constructor(private val binding: HomeDocItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(doc: Doc, position: Int, action: OnDocClickListener) {
            binding.doc = doc
            binding.number = if (position + 1 < 10) "0${(position + 1)}" else (position + 1).toString()
            binding.executePendingBindings()
            binding.moreOptions.setOnClickListener {
                action.onDocClick(doc, it)
            }
        }

        companion object {
            fun from(parent: ViewGroup): DocAdapterViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = HomeDocItemBinding.inflate(layoutInflater, parent, false)
                return DocAdapterViewHolder(binding)
            }
        }
    }

    interface OnDocClickListener {
        fun onDocClick(doc: Doc, view: View)
    }

    override fun getFilter(): Filter {
        return object : Filter() {

            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val filteredList = mutableListOf<Doc>()
                val charString = charSequence.toString()
                if (charString == "") {
                    filteredList.addAll(docs)
                } else {
                    for (row in docs) {
                        if (row.name.contains(charString)
                                    || row.date.lowercase(Locale.ROOT)
                                .contains(charString.lowercase(Locale.ROOT))
                                    || row.status.contains(charString)
                                    ) {
                            filteredList.add(row)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                submitList(filterResults.values as MutableList<Doc>)
            }
        }
    }

}