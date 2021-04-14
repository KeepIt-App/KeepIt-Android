package com.haero_kim.pickmeup.ui

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.webkit.URLUtil
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.work.WorkManager
import com.bumptech.glide.Glide
import com.haero_kim.pickmeup.R
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.databinding.ActivityItemDetailBinding
import com.haero_kim.pickmeup.ui.AddActivity.Companion.EDIT_ITEM
import com.haero_kim.pickmeup.viewmodel.ItemViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat

class ItemDetailActivity : AppCompatActivity() {

    // Koin 모듈을 활용한 ViewModel 인스턴스 생성
    private val itemViewModel: ItemViewModel by viewModel()
    private lateinit var binding: ActivityItemDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DataBinding
        binding = DataBindingUtil.setContentView<ActivityItemDetailBinding>(
            this,
            R.layout.activity_item_detail
        )

        binding.viewModel = itemViewModel

        val item = intent.getSerializableExtra(EXTRA_ITEM) as ItemEntity
        val decimalFormat = DecimalFormat("#,###")
        val itemPriceFormatted = decimalFormat.format(item.price)

        binding.itemName.text = item.name
        binding.itemPrice.text = "₩${itemPriceFormatted}원"

        if (item.link.isEmpty()) {
            binding.itemLink.text = "링크가 없습니다"
            binding.itemLink.setTextColor(ContextCompat.getColor(this, R.color.gray))
        } else {
            binding.itemLink.text = "등록한 링크로 이동하기"
            binding.itemLinkLayout.setOnClickListener {
                when {
                    // HTTP Url 이 맞을 때
                    URLUtil.isHttpUrl(item.link) -> {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.link)))
                    }
                    // Url 은 맞지만 HTTP 형식의 Url 이 아닐 때
                    !URLUtil.isHttpUrl(item.link) and Patterns.WEB_URL.matcher(item.link)
                        .matches() -> {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://" + item.link)))
                    }
                    // 아예 Url 형태가 아닐 때
                    else -> {
                        Toast.makeText(this, "올바르지 않은 URL 입니다", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        if (item.note.isEmpty()) {
            binding.itemMemo.text = "메모가 없습니다"
        } else {
            binding.itemMemo.text = item.note
        }

        binding.itemRatingBar.rating = item.priority.toFloat()

        Glide.with(this)
            .load(item.image)
            .into(binding.itemImage)

        // BottomAppBar - 삭제 버튼 눌렀을 때
        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.delete -> {
                    val builder = AlertDialog.Builder(this)
                    builder.apply {
                        this.setMessage("삭제하시겠습니까?")
                        this.setNegativeButton("NO") { _, _ -> }
                        this.setPositiveButton("YES") { _, _ ->
                            itemViewModel.delete(item)
                            val workManager: WorkManager = WorkManager.getInstance(context)
                            // WorkRequest 등록 시, 아이템 명으로 고유 태그를 달아줬기 때문에
                            // 아래와 같이 item.name 을 통해 주기적인 푸시알림 작업을 취소할 수 있음
                            workManager.cancelAllWorkByTag(item.name)
                            finish()
                        }
                    }
                    builder.show()
                    true
                }
                else -> false
            }
        }

        // BottomAppBar - 편집 버튼 눌렀을 때 (편집 화면으로 넘어감)
        binding.bottomAppBar.setNavigationOnClickListener {
            val intent = Intent(this, AddActivity::class.java)
            intent.putExtra(EDIT_ITEM, item)
            startActivity(intent)

            finish()  // 편집 후 복귀했을 때 정보 동기화를 위해 기존 정보 페이지는 닫아줌
        }
    }

    companion object {
        const val EXTRA_ITEM = "EXTRA_ITEM"
    }
}