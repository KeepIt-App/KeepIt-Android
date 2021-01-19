package com.haero_kim.pickmeup.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.haero_kim.pickmeup.MyApplication
import com.haero_kim.pickmeup.R
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.data.ItemRepository
import com.haero_kim.pickmeup.ui.AddActivity

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ItemRepository(application)
    private val items = repository.getAll()

    private var isSortedByLatest: ObservableBoolean = ObservableBoolean(true)
    private var isSortedByPriority: ObservableBoolean = ObservableBoolean(false)
    private var isSortedByPrice: ObservableBoolean = ObservableBoolean(false)

    fun getAll(): LiveData<List<ItemEntity>> {
        return this.items
    }

    fun getSortedList(filter: String) {
        when (filter) {
            SORT_BY_LATEST -> {
                isSortedByLatest.set(true)
                isSortedByPriority.set(false)
                isSortedByPrice.set(false)
            }

            SORT_BY_PRIORITY -> {
                isSortedByLatest.set(false)
                isSortedByPriority.set(true)
                isSortedByPrice.set(false)
            }

            SORT_BY_PRICE -> {
                isSortedByLatest.set(false)
                isSortedByPriority.set(false)
                isSortedByPrice.set(true)
            }
        }
    }

    fun insert(itemEntity: ItemEntity) {
        repository.insert(itemEntity)
    }

    fun delete(itemEntity: ItemEntity) {
        repository.delete(itemEntity)
    }

    fun getSortByLatestColor(): Int {
        return if (isSortedByLatest.get()) {
            ContextCompat.getColor(MyApplication.applicationContext(), R.color.main_color)
        } else {
            ContextCompat.getColor(MyApplication.applicationContext(), R.color.gray)
        }
    }

    fun getSortByPriorityColor(): Int {
        return if (isSortedByPriority.get()) {
            ContextCompat.getColor(MyApplication.applicationContext(), R.color.main_color)
        } else {
            ContextCompat.getColor(MyApplication.applicationContext(), R.color.gray)
        }
    }

    fun getSortByPriceColor(): Int {
        return if (isSortedByPrice.get()) {
            ContextCompat.getColor(MyApplication.applicationContext(), R.color.main_color)
        } else {
            ContextCompat.getColor(MyApplication.applicationContext(), R.color.gray)
        }
    }

    companion object {
        const val SORT_BY_LATEST = "LATEST"
        const val SORT_BY_PRIORITY = "PRIORITY"
        const val SORT_BY_PRICE = "PRICE"
    }

}