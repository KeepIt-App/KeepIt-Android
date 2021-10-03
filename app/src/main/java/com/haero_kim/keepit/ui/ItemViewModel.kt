package com.haero_kim.keepit.ui

import android.text.TextUtils
import androidx.arch.core.util.Function
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.haero_kim.keepit.MyApplication.Companion.prefs
import com.haero_kim.keepit.base.BaseViewModel
import com.haero_kim.keepit.data.ItemEntity
import com.haero_kim.keepit.data.ItemRepository
import com.haero_kim.keepit.util.Event
import com.haero_kim.keepit.util.combineWith
import timber.log.Timber


class ItemViewModel(private val repository: ItemRepository) : BaseViewModel() {

    private val savedStateHandle: SavedStateHandle = SavedStateHandle()

    // Data Binding 을 위한 Boolean 변수들 (TextStyle, TextColor 지정에 필요)
    var isSortedByLatest: MutableLiveData<Boolean> = MutableLiveData(true)
    var isSortedByPriority: MutableLiveData<Boolean> = MutableLiveData(false)
    var isSortedByPrice: MutableLiveData<Boolean> = MutableLiveData(false)

    // 아이템 리스트 정렬 기준
    var sortFilter: MutableLiveData<Int> = MutableLiveData<Int>()

    // 검색 모드 여부에 따른 UI 변화 (EditText Visibility)
    var isSearchMode: MutableLiveData<Boolean> = MutableLiveData(false)

    val itemId: MutableLiveData<Long> = MutableLiveData()
    val itemName: MutableLiveData<String> = MutableLiveData()
    val itemPrice: MutableLiveData<String> = MutableLiveData()
    val itemLink: MutableLiveData<String> = MutableLiveData()
    val itemPriority: MutableLiveData<Float> = MutableLiveData()
    val itemMemo: MutableLiveData<String> = MutableLiveData()

    val itemAddComplete: MutableLiveData<Event<ItemEntity>> = MutableLiveData()

    val isValidForm = itemName.combineWith(itemPrice) { itemName, itemPrice ->
        !itemName.isNullOrBlank() && !itemPrice.isNullOrBlank()
    }

    /**
     * 현재 사용자가 선택한 필터에 따른 알맞은 리스트 반환
     */
    fun getItemList(): LiveData<List<ItemEntity>> =
        Transformations.switchMap<CharSequence?, List<ItemEntity>>(
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

    fun addItem(itemImage: String) {
        val newItem = ItemEntity(
            id = itemId.value,  // 새로운 Item 이면 Null 들어감 (자동 값 적용)
            name = itemName.value!!,
            image = itemImage,
            price = itemPrice.value!!.replace(",", "").toLong(),
            link = itemLink.value ?: "",
            priority = (itemPriority.value ?: 0).toInt(),
            memo = itemMemo.value ?: ""
        )

        // 해당 링크에 대한 아이템 추가 권유 팝업창 다시 뜨지 않도록 함
        // - null 검사하는 이유 : 기존 아이템을 수정한 것인지 구분하기 위함
        if (itemId.value == null) prefs.latestCanceledLink = newItem.link

        insert(newItem)
        itemAddComplete.postValue(Event(newItem))
    }


    /**
     * 최신 순 정렬 버튼을 눌렀을 때 동작
     */
    fun onClickSetFilterLatest() {
        // RecyclerView 정렬을 위한 변수
        sortFilter.value = SORT_BY_LATEST

        // XML 변경을 위한 변수 (TextColor, TextStyle)
        isSortedByLatest.value = true
        isSortedByPriority.value = false
        isSortedByPrice.value = false
    }

    /**
     * 중요도 순 정렬 버튼을 눌렀을 때 동작
     */
    fun onClickSetFilterPriority() {
        // RecyclerView 정렬을 위한 변수
        sortFilter.value = SORT_BY_PRIORITY

        // XML 변경을 위한 변수 (TextColor, TextStyle)
        isSortedByLatest.value = false
        isSortedByPriority.value = true
        isSortedByPrice.value = false
    }

    /**
     * 가격 순 정렬 버튼을 눌렀을 때 동작
     */
    fun onClickSetFilterPrice() {
        // RecyclerView 정렬을 위한 변수
        sortFilter.value = SORT_BY_PRICE

        // XML 변경을 위한 변수 (TextColor, TextStyle)
        isSortedByLatest.value = false
        isSortedByPriority.value = false
        isSortedByPrice.value = true
    }

    companion object {
        const val SORT_BY_LATEST = 1
        const val SORT_BY_PRIORITY = 2
        const val SORT_BY_PRICE = 3
    }

}