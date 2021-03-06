package com.haero_kim.keepit.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.work.WorkManager
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.haero_kim.keepit.MyApplication.Companion.prefs
import com.haero_kim.keepit.R
import com.haero_kim.keepit.adapter.ItemListAdapter
import com.haero_kim.keepit.base.BaseActivity
import com.haero_kim.keepit.data.ItemEntity
import com.haero_kim.keepit.databinding.ActivityMainBinding
import com.haero_kim.keepit.ui.ItemDetailActivity.Companion.EXTRA_ITEM
import com.haero_kim.keepit.util.ShoppingMallList
import com.haero_kim.keepit.ui.ItemViewModel.Companion.SORT_BY_LATEST
import com.haero_kim.keepit.ui.ItemViewModel.Companion.SORT_BY_PRICE
import com.haero_kim.keepit.ui.ItemViewModel.Companion.SORT_BY_PRIORITY
import com.haero_kim.keepit.util.setDebounceOnEditText
import com.jakewharton.rxbinding4.widget.textChanges
import es.dmoral.toasty.Toasty
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.util.concurrent.TimeUnit

class MainActivity : BaseActivity<ActivityMainBinding, ItemViewModel>() {
    override val layoutResourceId: Int
        get() = R.layout.activity_main
    override val viewModel: ItemViewModel by viewModel()

    private lateinit var itemList: List<ItemEntity>  // 픽미업 아이템 리스트
    private lateinit var itemListAdapter: ItemListAdapter

    override fun initStartView() {
        binding.viewModel = this.viewModel
        binding.lifecycleOwner = this

        // RecyclerView Adapter
        itemListAdapter = ItemListAdapter(
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

        binding.recyclerView.apply {
            this.adapter = itemListAdapter
            this.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            this.setHasFixedSize(true)
        }

        // Button 애니메이션 (효과)
        YoYo.with(Techniques.ZoomIn)
            .duration(400)
            .playOn(binding.addButton)

        // Android 8.0 이상 기기일 경우 NotificationChannel 인스턴스를 시스템에 등록
        createNotificationChannel()

        // iOS 스타일의 리사이클러뷰 오버스크롤 바운스 효과 적용
        OverScrollDecoratorHelper.setUpOverScroll(
            binding.recyclerView,
            OverScrollDecoratorHelper.ORIENTATION_VERTICAL
        )

    }

    override fun initDataBinding() {
        /**
         * Item List 를 LiveData 형태로 받아오나, 사용자가 선택한 필터에 따라
         * 받아온 LiveData 를 적절히 정렬(가공) 하여 RecyclerView Adapter 에 적용
         */
        viewModel.getItemList().observe(this) { list ->
            binding.noticeEmptyList.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            itemList = list
            itemListAdapter.setItems(itemList)
        }

        // 필터에 따라 정렬된 리스트를 어댑터로 보낼 것
        viewModel.sortFilter.observe(this) { filter ->
            when (filter) {
                SORT_BY_LATEST -> {
                    itemListAdapter.setItems(sortByLatest(itemList))
                }
                SORT_BY_PRIORITY -> {
                    itemListAdapter.setItems(sortByPriority(itemList))
                }
                SORT_BY_PRICE -> {
                    itemListAdapter.setItems(sortByPrice(itemList))
                }
            }
        }
    }

    override fun initAfterBinding() {
        binding.searchView.apply {
            this.hint = "검색어를 입력해주세요"

            // EditText 에 포커스가 갔을 때 ClearButton 활성화
            this.setOnFocusChangeListener { v, hasFocus ->
                binding.textClearButton.visibility = if (hasFocus) View.VISIBLE else View.GONE
            }
            // EditText 쿼리 디바운싱 적용 (RxJava, RxBinding)
            addDisposable(setDebounceOnEditText {
                runOnUiThread {
                    viewModel.onChangeQuery(searchQuery = it)
                }
            })
        }

        // ClearButton 눌렀을 때
        binding.textClearButton.setOnClickListener {
            binding.searchView.text.clear()
        }

        // BottomAppBar - 설정 버튼 눌렀을 때
        binding.bottomAppBar.setNavigationOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        // BottomAppBar - 검색, 필터 버튼 눌렀을 때
        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.search -> {
                    YoYo.with(Techniques.FadeInLeft)
                        .duration(400)
                        .playOn(binding.searchViewLayout)
                    // 검색 버튼 눌렀을 때마다 모드 전환
                    viewModel.isSearchMode.value = !viewModel.isSearchMode.value!!
                    true
                }
                R.id.delete -> {
                    true
                }
                else -> false
            }
        }

        // addButton 눌렀을 때 진입
        binding.addButton.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        const val CHANNEL_ID = "NOTIFICATION_CHANNEL"
        const val notificationId = 5603
    }

    /**
     * 앱이 백그라운드가 아닌 Focus 되어있는 상태에서만 클립보드 진입이 가능
     */
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // 앱이 포커싱되어 실행되는 상태라면
        if (hasFocus) {
            // ClipboardManger 객체 생성
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            var pasteData: String = ""

            // 클립보드에 아무것도 없거나 PlainText 가 아닌 데이터가 들어있을 경우 예외처리
            if (!clipboard.hasPrimaryClip()) {
                Timber.d("클립보드 비어있음")
            } else if ((clipboard.primaryClipDescription?.hasMimeType(MIMETYPE_TEXT_PLAIN)) == false) {
                Timber.d("생성은 됐는데 텍스트가 아님")
            } else {
                // 클립보드에 PlainText 가 담겨있어 데이터를 가져올 수 있는 경우
                val itemLink =
                    clipboard.primaryClip?.getItemAt(0)!!.coerceToText(applicationContext)
                if (!itemLink.isNullOrEmpty()) {
                    pasteData = itemLink.toString()
                    // 쇼핑몰 링크가 감지되면 사용자에게 아이템 등록 권유 메세지 표시 (단, 최근에 사용자에 의해 취소한 이력이 있는 링크라면 띄우지 않음)
                    for (link in ShoppingMallList.shoppingMallList) {
                        if (pasteData.contains(
                                link.key,
                                true
                            ) && pasteData != prefs.latestCanceledLink
                        ) {
                            Timber.d("쇼핑몰 링크 감지 : ${link.value}")
                            showItemRegisterPopup(itemLink, link.value)
                        }
                    }
                }
            }
        }
    }

    /**
     * 사용자 클립보드에 쇼핑몰 링크가 발견되면 아이템 등록 권유 메세지 표시해줌
     * - 만약 취소 버튼이 눌리면, 팝업이 다시 뜨지 않도록 SharedPreferences 에 추가
     */
    private fun showItemRegisterPopup(siteLink: CharSequence, siteName: String) {
        YoYo.with(Techniques.BounceInUp)
            .duration(600)
            .playOn(binding.registerItemPopup)

        binding.registerItemPopupMessage.text = "$siteName 링크가 발견되었습니다!"

        binding.registerItemPopup.setOnClickListener {
            val intent = Intent(this, AddItemActivity::class.java)
            intent.putExtra(AddItemActivity.AUTO_ITEM, siteLink)
            startActivity(intent)

            // 해당 링크에 대한 아이템 추가 권유 팝업창 다시 뜨지 않도록 함
            prefs.latestCanceledLink = siteLink.toString()

            binding.registerItemPopup.visibility = View.GONE
        }

        binding.registerItemCancelButton.setOnClickListener {
            YoYo.with(Techniques.FadeOutDown)
                .duration(400)
                .playOn(binding.registerItemPopup)

            // 한 번 더 물어보는 일이 없도록 최근 자동 등록 취소된 링크에 추가
            prefs.latestCanceledLink = siteLink.toString()
        }

        binding.registerItemPopup.visibility = View.VISIBLE
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
                viewModel.delete(item)
                Toasty.success(this@MainActivity, "아이템이 삭제되었습니다", Toast.LENGTH_SHORT, true).show()
                val workManager: WorkManager = WorkManager.getInstance(context)
                // WorkRequest 등록 시, 아이템 명으로 고유 태그를 달아줬기 때문에
                // 아래와 같이 item.name 을 통해 주기적인 푸시알림 작업을 취소할 수 있음
                workManager.cancelAllWorkByTag(item.id.toString())
            }
        }
        builder.show()
    }

    /**
     * Android 8.0 이상에서 알림을 제공하려면 Notification Channel 등록해야함
     */
    private fun createNotificationChannel() {
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }

        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}