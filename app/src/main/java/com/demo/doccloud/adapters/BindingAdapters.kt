package com.demo.doccloud.adapters

import android.view.View
import androidx.databinding.BindingAdapter

// hide or show a view based on a boolean flag
@BindingAdapter("visibleOrGone")
fun View.visibleOrGone(isVisible: Boolean?) {
    if (isVisible != null) {
        visibility = if (isVisible) View.VISIBLE else View.GONE
    }
}