package com.haero_kim.pickmeup.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.haero_kim.pickmeup.R

class ItemDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)
    }

    companion object{
        const val EXTRA_ITEM = "EXTRA_ITEM"
    }
}