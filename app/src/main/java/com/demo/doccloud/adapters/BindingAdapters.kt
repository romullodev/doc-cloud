package com.demo.doccloud.adapters

import android.content.Context
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.demo.doccloud.R
import com.demo.doccloud.domain.Doc
import com.demo.doccloud.domain.DocStatus
import com.demo.doccloud.utils.Global


/**
 * camera_fragment.xml
 */
@BindingAdapter("hideIfZero")
fun AppCompatImageButton.setHideIfZero(size: Int?) {
    size?.let {
        this.visibility = if (it > 0) View.VISIBLE else View.GONE
    }
}


/**
 * home_fragment.xml
 */
@BindingAdapter("loadDocs")
fun setLoadDocs(recyclerView: RecyclerView, data: List<Doc>?) {
    data?.let {
        val adapter = recyclerView.adapter as DocAdapter
        adapter.setList(it.toMutableList())
    }
}

/**
 * home_doc_item.xml
 */
//binds the route on recycler view
@BindingAdapter("docDetails")
fun TextView.setDocDetails(doc: Doc) {
    val name = this.context.getString(R.string.home_doc_name, doc.name)
    val date = this.context.getString(R.string.home_doc_date, doc.date)
    val status = this.context.getString(R.string.home_doc_status, Global.getDocStatus(doc.status, this.context))
    val spannableName = SpannableString(name)
    var startPoint = 0
    var endPoint = name.length - doc.name.length
    setBoldText(spannableName, startPoint, endPoint)

    val spannableDate = SpannableString(date)
    startPoint = 0
    endPoint = date.length - doc.date.length
    setBoldText(spannableDate, startPoint, endPoint)

    val spannableStatus = SpannableString(status)
    startPoint = 0
    endPoint = status.length - Global.getDocStatus(doc.status, this.context).length
    setBoldText(spannableStatus, startPoint, endPoint)

    val fullText = SpannableStringBuilder()// = "$spannableName\n$spannableDate"
    fullText.apply {
        append(spannableName)
        append("\n")
        append(spannableDate)
        append("\n")
        append(spannableStatus)
    }

    this.text = fullText
}

/**
 * util functions
 */
private fun setBoldText(spannable: Spannable, startPoint: Int, endPoint: Int) {
    spannable.setSpan(
        StyleSpan(Typeface.BOLD),
        startPoint, endPoint,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}

@BindingAdapter("visibleOrGone")
fun View.visibleOrGone(isVisible: Boolean?) {
    isVisible?.let {
        visibility = if (it) View.VISIBLE else View.GONE
    }
}


