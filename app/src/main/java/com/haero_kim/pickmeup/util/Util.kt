package com.haero_kim.pickmeup.util

import android.widget.EditText
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo

class Util {
    companion object {
        /**
         * EditText Error Animation, Error Text 적용
         */
        fun setErrorOnEditText(editText: EditText, message: CharSequence) {
            YoYo.with(Techniques.Shake)
                .duration(400)
                .playOn(editText)
            editText.error = message
        }
    }
}