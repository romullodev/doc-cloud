package com.demo.doccloud.adapters

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.demo.doccloud.R
import com.demo.doccloud.domain.Doc

// hide or show a view based on a boolean flag
@BindingAdapter("visibleOrGone")
fun View.visibleOrGone(isVisible: Boolean?) {
    if (isVisible != null) {
        visibility = if (isVisible) View.VISIBLE else View.GONE
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
    val status = this.context.getString(R.string.home_doc_status, doc.status)
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
    endPoint = status.length - doc.status.length
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

private fun setBoldText(spannable: Spannable, startPoint: Int, endPoint: Int) {
    spannable.setSpan(
        StyleSpan(Typeface.BOLD),
        startPoint, endPoint,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
}
