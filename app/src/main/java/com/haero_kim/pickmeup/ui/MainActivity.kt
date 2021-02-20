package com.haero_kim.pickmeup.ui

import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.haero_kim.pickmeup.R
import com.haero_kim.pickmeup.adapter.ItemListAdapter
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.databinding.ActivityMainBinding
import com.haero_kim.pickmeup.ui.ItemDetailActivity.Companion.EXTRA_ITEM
import com.haero_kim.pickmeup.util.ShoppingMallList
import com.haero_kim.pickmeup.viewmodel.ItemViewModel
import com.haero_kim.pickmeup.viewmodel.ItemViewModel.Companion.SORT_BY_LATEST
import com.haero_kim.pickmeup.viewmodel.ItemViewModel.Companion.SORT_BY_PRICE
import com.haero_kim.pickmeup.viewmodel.ItemViewModel.Companion.SORT_BY_PRIORITY
import com.jakewharton.rxbinding4.widget.textChanges
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    // Koin 모듈을 활용한 ViewModel 인스턴스 생성
    private val itemViewModel: ItemViewModel by viewModel()

    private lateinit var recyclerView: RecyclerView
    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var textNoticeEmptyList: TextView
    private lateinit var addButton: FloatingActionButton

    private lateinit var buttonSetFilterLatest: Button
    private lateinit var buttonSetFilterPriority: Button
    private lateinit var buttonSetFilterPrice: Button

    private lateinit var searchViewLayout: CardView
    private lateinit var searchEditText: EditText
    private lateinit var searchEditTextClearButton: ImageButton

    // Disposable 을 모두 한번에 관리하는 CompositeDisposable
    // 옵저버블 통합 제거를 위해 사용 (메모리 릭 방지하기 위해 onDestroy() 에서 clear() 필요)
    private var compositeDisposable = CompositeDisposable()

    private lateinit var itemList: List<ItemEntity>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(
            this,
            R.layout.activity_main
        )
        binding.viewModel = itemViewModel

        recyclerView = findViewById(R.id.recyclerView)
        bottomAppBar = findViewById(R.id.bottomAppBar)
        textNoticeEmptyList = findViewById(R.id.noticeEmptyList)
        addButton = findViewById(R.id.shareButton)
        buttonSetFilterLatest = findViewById(R.id.sortLatest)
        buttonSetFilterPriority = findViewById(R.id.sortPriority)
        buttonSetFilterPrice = findViewById(R.id.sortPrice)
        searchViewLayout = findViewById(R.id.searchViewLayout)
        searchEditText = findViewById(R.id.searchView)
        searchEditTextClearButton = findViewById(R.id.textClearButton)

        YoYo.with(Techniques.ZoomIn)
            .duration(400)
            .playOn(addButton)

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

        OverScrollDecoratorHelper.setUpOverScroll(
            recyclerView,
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL
        )

        /**
         * EditText 에 RxJava (feat. RxBinding, RxKotlin) 을 적용하여
         * 사용자의 검색 Query 에 즉각적으로 LiveData 가 변경될 수 있도록 함 (Debounce 를 적용하여 리소스 낭비 방지)
         */
        searchEditText.apply {
            this.hint = "검색어를 입력해주세요"

            // EditText 에 포커스가 갔을 때 ClearButton 활성화
            this.setOnFocusChangeListener { v, hasFocus ->
                if (hasFocus) {
                    searchEditTextClearButton.visibility = View.VISIBLE
                } else {
                    searchEditTextClearButton.visibility = View.GONE
                }
            }

            val editTextChangeObservable = searchEditText.textChanges()
            val searchEditTextSubscription: Disposable =
                // 생성한 Observable 에 Operator 추가
                editTextChangeObservable
                    // 마지막 글자 입력 0.8초 후에 onNext 이벤트로 데이터 스트리밍
                    .debounce(800, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.io())
                    // 구독을 통해 이벤트 응답 처리
                    .subscribeBy(
                        onNext = {
                            Log.d("Rx", "onNext : $it")
                            runOnUiThread {
                                itemViewModel.onChangeQuery(searchQuery = it.toString())
                            }
                        },
                        onComplete = {
                            Log.d("Rx", "onComplete")
                        },
                        onError = {
                            Log.d("Rx", "onError : $it")
                        }
                    )
            // CompositeDisposable 에 추가
            compositeDisposable.add(searchEditTextSubscription)
        }

        /**
         * Item List 를 LiveData 형태로 받아오나, 사용자가 선택한 필터에 따라
         * 받아온 LiveData 를 적절히 정렬(가공) 하여 RecyclerView Adapter 에 적용
         */
        itemViewModel.getAll().observe(this, Observer
        { list ->
            if (list.isEmpty()) {
                textNoticeEmptyList.visibility = View.VISIBLE
            } else {
                textNoticeEmptyList.visibility = View.GONE
            }
            itemList = list

            // 필터에 따라 정렬된 리스트를 어댑터로 보낼 것
            itemViewModel.sortFilter.observe(this, Observer { filter ->
                when (filter) {
                    SORT_BY_LATEST -> {
                        adapter.setItems(sortByLatest(itemList))
                    }
                    SORT_BY_PRIORITY -> {
                        adapter.setItems(sortByPriority(itemList))
                    }
                    SORT_BY_PRICE -> {
                        adapter.setItems(sortByPrice(itemList))
                    }
                }
            })
        })

        // ClearButton 눌렀을 때
        searchEditTextClearButton.setOnClickListener {
            searchEditText.text.clear()
        }

        // BottomAppBar - 설정 버튼 눌렀을 때
        bottomAppBar.setNavigationOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        // BottomAppBar - 검색, 필터 버튼 눌렀을 때
        bottomAppBar.setOnMenuItemClickListener{ menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {
                    val isSearchMode = itemViewModel.isSearchMode.get()!!

                    YoYo.with(Techniques.FadeInLeft)
                        .duration(400)
                        .playOn(searchViewLayout)

                    // 검색 버튼 눌렀을 때마다 모드 전환
                    itemViewModel.isSearchMode.set(!isSearchMode)
                    true
                }
                R.id.delete -> {  // TODO : 삭제 버튼 눌렀을 때 선택 삭제 기능 제공 예정
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
    }

    companion object{
        val TAG = "MainActivity"
    }

    /**
     * 앱이 백그라운드가 아닌 Focus 되어있는 상태에서만 클립보드 진입이 가능
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // 앱이 포커싱되어 실행되는 상태라면
        if (hasFocus){
            // ClipboardManger 객체 생성
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            var pasteData: String = ""

            // 클립보드에 아무것도 없거나 PlainText 가 아닌 데이터가 들어있을 경우 예외처리
            if (!clipboard.hasPrimaryClip()) {
                Log.d(TAG, "클립보드 생성조차 안됨")
            } else if ((clipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN)) == false) {
                Log.d(TAG, "생성은 됐는데 텍스트가 아님")
            } else {
                // 클립보드에 PlainText 가 담겨있어 데이터를 가져올 수 있는 경우
                val item = clipboard.primaryClip?.getItemAt(0)!!.coerceToText(applicationContext)
                if (!item.isNullOrEmpty()){
                    pasteData = item.toString()

                    for (mall in ShoppingMallList.shoppingMallList){
                        if (pasteData.contains(mall, true)){
                            Log.d(TAG,"쇼핑몰 링크입니다!")
                        }else{
                            Log.d(TAG, "쇼핑몰 링크가 아닙니다!")
                        }
                    }
                }
                Log.d(TAG, pasteData)
            }
        }
    }

    /**
     * 필터에 따른 각종 정렬 메소드 (등록 순, 중요도 순, 가격 순)
     */

    private fun sortByLatest(itemList: List<ItemEntity>): List<ItemEntity> {
        return itemList.sortedByDescending { it.id }
    }

    private fun sortByPriority(itemList: List<ItemEntity>): List<ItemEntity> {
        return itemList.sortedByDescending { it.priority }
    }

    private fun sortByPrice(itemList: List<ItemEntity>): List<ItemEntity> {
        return itemList.sortedByDescending { it.price }
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

    override fun onDestroy() {
        this.compositeDisposable.clear()
        super.onDestroy()
    }
}