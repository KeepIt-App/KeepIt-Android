package com.haero_kim.pickmeup.ui

import android.text.TextUtils
import androidx.arch.core.util.Function
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.haero_kim.pickmeup.base.BaseViewModel
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.data.ItemRepository


class ItemViewModel(private val repository: ItemRepository): BaseViewModel() {

    private val savedStateHandle: SavedStateHandle = SavedStateHandle()

    // Data Binding 을 위한 Boolean 변수들 (TextStyle, TextColor 지정에 필요)
    var isSortedByLatest: ObservableField<Boolean> = ObservableField<Boolean>(true)
    var isSortedByPriority: ObservableField<Boolean> = ObservableField<Boolean>(false)
    var isSortedByPrice: ObservableField<Boolean> = ObservableField<Boolean>(false)

    var sortFilter: MutableLiveData<Int> = MutableLiveData<Int>(1)

    var isSearchMode: ObservableField<Boolean> = ObservableField<Boolean>(false)

    /**
     * 현재 사용자가 선택한 필터에 따른 알맞은 리스트 반환
     */
    fun getAll(): LiveData<List<ItemEntity>> = Transformations.switchMap<CharSequence?, List<ItemEntity>>(
            savedStateHandle.getLiveData("QUERY", null),
            Function<CharSequence?, LiveData<List<ItemEntity>>> { query: CharSequence? ->
                if (TextUtils.isEmpty(query)) {
                    return@Function repository.getList()
                } else {
                    return@Function repository.searchItem("*$query*")
                }
            }
    )

    fun onChangeQuery(searchQuery: String) {
        setQuery(searchQuery)
    }

    fun setQuery(query: CharSequence?) {
        savedStateHandle.set("QUERY", query)
    }

    fun insert(itemEntity: ItemEntity) {
        repository.insert(itemEntity)
    }

    fun delete(itemEntity: ItemEntity) {
        repository.delete(itemEntity)
    }

    /**
     * 최신 순 정렬 버튼을 눌렀을 때 동작
     */
    fun onClickSetFilterLatest() {
        // RecyclerView 정렬을 위한 변수
        sortFilter.value = SORT_BY_LATEST

        // XML 변경을 위한 변수 (TextColor, TextStyle)
        isSortedByLatest.set(true)
        isSortedByPriority.set(false)
        isSortedByPrice.set(false)
    }

    /**
     * 중요도 순 정렬 버튼을 눌렀을 때 동작
     */
    fun onClickSetFilterPriority() {
        // RecyclerView 정렬을 위한 변수
        sortFilter.value = SORT_BY_PRIORITY

        // XML 변경을 위한 변수 (TextColor, TextStyle)
        isSortedByLatest.set(false)
        isSortedByPriority.set(true)
        isSortedByPrice.set(false)
    }

    /**
     * 가격 순 정렬 버튼을 눌렀을 때 동작
     */
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