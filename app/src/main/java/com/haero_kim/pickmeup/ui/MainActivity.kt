package com.haero_kim.pickmeup.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.haero_kim.pickmeup.R
import com.haero_kim.pickmeup.adapter.ItemListAdapter
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.ui.ItemDetailActivity.Companion.EXTRA_ITEM
import com.haero_kim.pickmeup.viewmodel.ItemViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var itemViewModel: ItemViewModel
    private var viewModelFactory: ViewModelProvider.AndroidViewModelFactory? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var addButton: FloatingActionButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        bottomAppBar = findViewById(R.id.bottomAppBar)
        addButton = findViewById(R.id.addButton)

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
            this.layoutManager = GridLayoutManager(context, 2)
            this.setHasFixedSize(true)
        }

        // ViewModelFactory 를 통해서 ViewModel 을 찍어내야 함
        if (viewModelFactory == null) {
            viewModelFactory = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        }

        // 생성한 ViewModelFactory 를 통해 ViewModelProvider 호출
        itemViewModel = ViewModelProvider(this, viewModelFactory!!).get(ItemViewModel::class.java)

        itemViewModel.getAll().observe(this, Observer {
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
                R.id.filter -> {  // 필터 버튼 눌렀을 때 정렬 기준 기능 제공
                    true
                }
                R.id.delete -> {  // 삭제 버튼 눌렀을 때 선택 삭제 기능 제공
                    true
                }
                else -> false
            }
        }

        addButton.setOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            startActivity(intent)
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