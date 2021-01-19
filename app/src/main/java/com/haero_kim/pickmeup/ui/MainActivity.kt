package com.haero_kim.pickmeup.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.haero_kim.pickmeup.R
import com.haero_kim.pickmeup.adapter.ItemListAdapter
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.databinding.ActivityMainBinding
import com.haero_kim.pickmeup.ui.ItemDetailActivity.Companion.EXTRA_ITEM
import com.haero_kim.pickmeup.viewmodel.ItemViewModel
import com.haero_kim.pickmeup.viewmodel.ItemViewModel.Companion.SORT_BY_LATEST
import com.haero_kim.pickmeup.viewmodel.ItemViewModel.Companion.SORT_BY_PRICE
import com.haero_kim.pickmeup.viewmodel.ItemViewModel.Companion.SORT_BY_PRIORITY

class MainActivity : AppCompatActivity() {

    private lateinit var itemViewModel: ItemViewModel
    private var viewModelFactory: ViewModelProvider.AndroidViewModelFactory? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var textNoticeEmptyList: TextView
    private lateinit var addButton: FloatingActionButton

    private lateinit var buttonSetFilterLatest: Button
    private lateinit var buttonSetFilterPriority: Button
    private lateinit var buttonSetFilterPrice: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewModelFactory 를 통해서 ViewModel 을 찍어내야 함
        if (viewModelFactory == null) {
            viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        }

        // 생성한 ViewModelFactory 를 통해 ViewModelProvider 호출
        itemViewModel = ViewModelProvider(this, viewModelFactory!!).get(ItemViewModel::class.java)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
                this,
                R.layout.activity_main
        )
        binding.viewModel = itemViewModel

        recyclerView = findViewById(R.id.recyclerView)
        bottomAppBar = findViewById(R.id.bottomAppBar)
        textNoticeEmptyList = findViewById(R.id.noticeEmptyList)
        addButton = findViewById(R.id.addButton)
        buttonSetFilterLatest = findViewById(R.id.sortLatest)
        buttonSetFilterPriority = findViewById(R.id.sortPriority)
        buttonSetFilterPrice = findViewById(R.id.sortPrice)

        val adapter = ItemListAdapter(
                // OnClickListener
                {
                    val intent = Intent(this, ItemDetailActivity::class.java)
                    intent.putExtra(EXTRA_ITEM, it)
                    startActivity(intent)
                },
                // OnLongClickListener
                {
                    deleteDialog(it)
                })

        recyclerView.apply {
            this.adapter = adapter
            this.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            this.setHasFixedSize(true)
        }

        // TODO 이 부분 고쳐야 함 (필터링 모드에 따른 Observer 교체)
        val observer = itemViewModel.getAll().observe(this, Observer {
            if (it.isEmpty()) {
                textNoticeEmptyList.visibility = View.VISIBLE
            } else {
                textNoticeEmptyList.visibility = View.GONE
            }

            // 필터에 따라 정렬된 리스트를 어댑터로 보낼 것
            adapter.setItems(it)

        })

        // BottomAppBar - 설정 버튼 눌렀을 때
        bottomAppBar.setNavigationOnClickListener {

        }

        // BottomAppBar - 검색, 필터 버튼 눌렀을 때
        bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {  // 검색 버튼 눌렀을 때 아이템 검색 기능 제공
                    true
                }
                R.id.delete -> {  // 삭제 버튼 눌렀을 때 선택 삭제 기능 제공
                    true
                }
                else -> false
            }
        }

        // addButton 눌렀을 때 진입
        addButton.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
        }

        // 최신 순으로 리스트를 정렬
        buttonSetFilterLatest.setOnClickListener {
            itemViewModel.sortFilter.set(SORT_BY_LATEST)
            itemViewModel.isSortedByLatest.set(true)
            itemViewModel.isSortedByPriority.set(false)
            itemViewModel.isSortedByPrice.set(false)
        }

        // 중요도 순으로 리스트를 정렬
        buttonSetFilterPriority.setOnClickListener {
            itemViewModel.sortFilter.set(SORT_BY_PRIORITY)
            itemViewModel.isSortedByLatest.set(false)
            itemViewModel.isSortedByPriority.set(true)
            itemViewModel.isSortedByPrice.set(false)
        }

        // 가격 순으로 리스트를 정렬
        buttonSetFilterPrice.setOnClickListener {
            itemViewModel.sortFilter.set(SORT_BY_PRICE)
            itemViewModel.isSortedByLatest.set(false)
            itemViewModel.isSortedByPriority.set(false)
            itemViewModel.isSortedByPrice.set(true)
        }
    }

    /**
     * 특정 아이템을 길게 눌렀을 때 동작하는 다이얼로그
     * - 확인 시 해당 아이템을 삭제함
     */
    private fun deleteDialog(item: ItemEntity) {
        val builder = AlertDialog.Builder(this)
        builder.apply {
            this.setMessage("삭제하시겠습니까?")
            this.setNegativeButton("NO") { _, _ -> }
            this.setPositiveButton("YES") { _, _ ->
                itemViewModel.delete(item)
            }
        }
        builder.show()
    }


}