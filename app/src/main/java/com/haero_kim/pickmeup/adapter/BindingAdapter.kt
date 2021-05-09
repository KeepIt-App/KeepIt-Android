package com.haero_kim.pickmeup.adapter

import android.graphics.Typeface
import android.widget.Button
import androidx.databinding.BindingAdapter

/**
 * DataBinding 시, 글자의 Style 속성을 적용해줌 (bold, normal 등)
 */
@BindingAdapter("android:textStyle")
fun setTypeface(textView: Button, style: String?) {
    when (style) {
        "bold" -> textView.setTypeface(null, Typeface.BOLD)
        else -> textView.setTypeface(null, Typeface.NORMAL)
    }
}