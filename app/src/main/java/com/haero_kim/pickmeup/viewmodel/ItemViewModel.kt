package com.haero_kim.pickmeup.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.data.ItemRepository

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ItemRepository(application)
    private val items = repository.getAll()

    fun getAll(): LiveData<List<ItemEntity>> {
        return this.items
    }

    fun insert(itemEntity: ItemEntity) {
        repository.insert(itemEntity)
    }

    fun delete(itemEntity: ItemEntity) {
        repository.delete(itemEntity)
    }
}