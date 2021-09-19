package com.haero_kim.pickmeup.util

import android.app.Activity
import android.content.Context
import android.graphics.Color.RED
import android.graphics.Color.red
import android.hardware.camera2.params.RggbChannelVector.RED
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.haero_kim.pickmeup.R
import com.tapadoo.alerter.Alerter


object ViewUtil {
    fun setMargins(v: View, l: Int, t: Int, r: Int, b: Int) {
        if (v.layoutParams is ViewGroup.MarginLayoutParams) {
            val p = v.layoutParams as ViewGroup.MarginLayoutParams
            p.setMargins(l, t, r, b)
            v.requestLayout()
        }
    }

    fun showKeyboard(context: Context, editText: EditText){
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    fun hideKeyboard(context: Context, editText: EditText){
        val im = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        im!!.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    fun disableEnableControls(enable: Boolean, vg: ViewGroup) {
        for (i in 0 until vg.childCount) {
            val child = vg.getChildAt(i)
            child.isEnabled = enable
            if (child is ViewGroup) {
                disableEnableControls(
                    enable,
                    child
                )
            }
        }
    }

    /**
     * EditText Error Animation, Error Text 적용
     */
    fun setErrorOnEditText(editText: EditText, message: CharSequence) {
        YoYo.with(Techniques.Shake)
            .duration(400)
            .playOn(editText)
        editText.error = message
    }

    // 동작 성공 애니메이션
    fun playSuccessAlert(activity: Activity, text: String) {
        Alerter.create(activity)
            .setText(text)
            .setIcon(R.drawable.ic_baseline_thumb_up_24)
            .setBackgroundColorRes(R.color.main_color)
            .setIconColorFilter(0)
            .setIconSize(R.dimen.custom_icon_size)
            .show()
    }

    // 동작 실패 애니메이션
    fun playFailureAlert(activity: Activity, text: String) {
        Alerter.create(activity)
            .setText(text)
            .setIcon(R.drawable.ic_baseline_error_outline_24)
            .setBackgroundColorRes(R.color.red)
            .setIconColorFilter(0)
            .setIconSize(R.dimen.custom_icon_size)
            .show()
    }

}