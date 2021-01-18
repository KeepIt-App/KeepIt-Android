package com.haero_kim.pickmeup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.haero_kim.pickmeup.viewmodel.ItemViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var itemViewModel: ItemViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}