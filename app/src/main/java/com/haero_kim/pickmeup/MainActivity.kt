package com.haero_kim.pickmeup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.haero_kim.pickmeup.AddActivity.Companion.EXTRA_ITEM
import com.haero_kim.pickmeup.adapter.ItemListAdapter
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.viewmodel.ItemViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var itemViewModel: ItemViewModel
    private var viewModelFactory: ViewModelProvider.AndroidViewModelFactory? = null
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val adapter = ItemListAdapter(
            // OnClickListener
            {
                val intent = Intent(this, AddActivity::class.java)
                intent.putExtra(EXTRA_ITEM, it)
                startActivity(intent)
            },

            {
                // OnLongClickListener
            })

        recyclerView = findViewById(R.id.recyclerView)
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