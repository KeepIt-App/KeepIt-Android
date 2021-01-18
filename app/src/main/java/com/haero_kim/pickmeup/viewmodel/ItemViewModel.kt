package com.haero_kim.pickmeup.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.haero_kim.pickmeup.data.Item
import com.haero_kim.pickmeup.data.ItemRepository

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ItemRepository(application)
    private val items = repository.getAll()

    fun getAll(): LiveData<List<Item>> {
        return this.items
    }

    fun insert(item: Item) {
        repository.insert(item)
    }

    fun delete(item: Item) {
        repository.delete(item)
    }
}