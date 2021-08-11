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
    private var docs: ArrayList<Doc>,
    private var clickListener: OnDocClickListener
) :
    ListAdapter<Doc, DocAdapter.DocAdapterViewHolder>(DeliveryDiffCallback()), Filterable {

    var docsFilterList = ArrayList<Doc>()

    init {
        docsFilterList = docs
        submitList(docsFilterList.toMutableList())
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
                val charString = charSequence.toString()
                docsFilterList = if (charString.isEmpty()) {
                    docs
                } else {
                    val resultList = ArrayList<Doc>()
                    for (row in docs) {
                        if (row.name.contains(charString)
                            || row.date.toLowerCase(Locale.ROOT)
                                .contains(charString.toLowerCase(Locale.ROOT))
                            || row.status.contains(charString)
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }

                val filterResults = FilterResults()
                filterResults.values = docsFilterList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                submitList(filterResults.values as MutableList<Doc>)
            }
        }
    }

}