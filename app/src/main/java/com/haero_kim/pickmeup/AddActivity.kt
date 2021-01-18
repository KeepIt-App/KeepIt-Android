package com.haero_kim.pickmeup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class AddActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
    }

    companion object{
        const val EXTRA_ITEM = "EXTRA_ITEM"
    }
}