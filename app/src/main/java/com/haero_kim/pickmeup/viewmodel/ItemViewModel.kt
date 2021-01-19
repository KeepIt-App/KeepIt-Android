package com.haero_kim.pickmeup.viewmodel

import android.app.Application
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.data.ItemRepository


class ItemViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ItemRepository(application)

    private val list = repository.getList()

    // Data Binding 을 위한 Boolean 변수들 (TextStyle, TextColor 지정에 필요)
    var isSortedByLatest: ObservableField<Boolean> = ObservableField<Boolean>(true)
    var isSortedByPriority: ObservableField<Boolean> = ObservableField<Boolean>(false)
    var isSortedByPrice: ObservableField<Boolean> = ObservableField<Boolean>(false)

    var sortFilter: MutableLiveData<Int> = MutableLiveData<Int>(1)

    /**
     * 현재 사용자가 선택한 필터에 따른 알맞은 리스트 반환
     */
    fun getAll(): LiveData<List<ItemEntity>> {
        return this.list
    }

    fun insert(itemEntity: ItemEntity) {
        repository.insert(itemEntity)
    }

    fun delete(itemEntity: ItemEntity) {
        repository.delete(itemEntity)
    }

    fun onClickSetFilterLatest() {
        // RecyclerView 정렬을 위한 변수
        sortFilter.value = SORT_BY_LATEST

        // XML 변경을 위한 변수 (TextColor, TextStyle)
        isSortedByLatest.set(true)
        isSortedByPriority.set(false)
        isSortedByPrice.set(false)
    }

    fun onClickSetFilterPriority() {
        // RecyclerView 정렬을 위한 변수
        sortFilter.value = SORT_BY_PRIORITY

        // XML 변경을 위한 변수 (TextColor, TextStyle)
        isSortedByLatest.set(false)
        isSortedByPriority.set(true)
        isSortedByPrice.set(false)
    }

    fun onClickSetFilterPrice() {
        // RecyclerView 정렬을 위한 변수
        sortFilter.value = SORT_BY_PRICE

        // XML 변경을 위한 변수 (TextColor, TextStyle)
        isSortedByLatest.set(false)
        isSortedByPriority.set(false)
        isSortedByPrice.set(true)
    }


    companion object {
        const val SORT_BY_LATEST = 1
        const val SORT_BY_PRIORITY = 2
        const val SORT_BY_PRICE = 3
    }

}