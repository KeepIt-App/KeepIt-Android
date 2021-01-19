package com.haero_kim.pickmeup.viewmodel

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.databinding.Bindable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.haero_kim.pickmeup.MyApplication
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.data.ItemRepository
import com.haero_kim.pickmeup.ui.AddActivity

class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ItemRepository(application)
    private val items = repository.getAll()

    fun getAll(): LiveData<List<ItemEntity>> {
        return this.items
    }

    fun getSortedList(filter: String){
        when(filter){
            SORT_BY_LATEST-> {

            }

            SORT_BY_PRIORITY-> {

            }

            SORT_BY_PRICE -> {

            }
        }
    }

    fun insert(itemEntity: ItemEntity) {
        repository.insert(itemEntity)
    }

    fun delete(itemEntity: ItemEntity) {
        repository.delete(itemEntity)
    }

    companion object{
        const val SORT_BY_LATEST = "LATEST"
        const val SORT_BY_PRIORITY = "PRIORITY"
        const val SORT_BY_PRICE = "PRICE"
    }

}