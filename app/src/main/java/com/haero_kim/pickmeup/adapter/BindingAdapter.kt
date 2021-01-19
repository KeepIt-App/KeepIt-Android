package com.haero_kim.pickmeup.adapter

import android.graphics.Typeface
import android.widget.Button
import androidx.databinding.BindingAdapter

@BindingAdapter("android:textStyle")
fun setTypeface(textView: Button, style: String?) {
    when (style) {
        "bold" -> textView.setTypeface(null, Typeface.BOLD)
        else -> textView.setTypeface(null, Typeface.NORMAL)
    }
}