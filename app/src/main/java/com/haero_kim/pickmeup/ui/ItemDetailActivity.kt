package com.haero_kim.pickmeup.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.work.WorkManager
import com.anandwana001.ogtagparser.LinkSourceContent
import com.anandwana001.ogtagparser.LinkViewCallback
import com.anandwana001.ogtagparser.OgTagParser
import com.bumptech.glide.Glide
import com.haero_kim.pickmeup.R
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.databinding.ActivityItemDetailBinding
import com.haero_kim.pickmeup.ui.AddItemActivity.Companion.EDIT_ITEM
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


        // ItemLink Layout Handling
        if (item.link.isEmpty()) {
            binding.noItemLinkLayout.visibility = View.VISIBLE
            binding.itemLinkLayout.visibility = View.GONE  // URL 썸네일 레이아웃 숨김
        } else {
            binding.noItemLinkLayout.visibility = View.GONE
            when {
                Patterns.WEB_URL.matcher(item.link).matches() -> {
                    var itemLink = item.link
                    // 만약 http 형식이 아니라면 앞에 'http://' 를 붙여줘야함 ==> ex) www.naver.com 과 같은 상황
                    if (!URLUtil.isHttpsUrl(itemLink) and !URLUtil.isHttpUrl(itemLink)){
                        itemLink = "https://" + itemLink
                    }
                    binding.itemLinkLayout.setOnClickListener {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(itemLink)))
                    }

                    // Open Graph 태그를 불러오는 라이브러리 사용
                    OgTagParser().execute(itemLink, object : LinkViewCallback {
                        override fun onAfterLoading(linkSourceContent: LinkSourceContent) {
                            Log.d("TEST", linkSourceContent.ogTitle)
                            Log.d("TEST", linkSourceContent.ogDescription)
                            Log.d("TEST", linkSourceContent.images)

                            val siteTitle = linkSourceContent.ogTitle
                            val siteDescription = linkSourceContent.ogDescription
                            var siteThumbnail = linkSourceContent.images

                            if (siteThumbnail.startsWith("//")) {
                                siteThumbnail = "https:" + siteThumbnail
                            }

                            binding.itemLinkTitle.text = siteTitle
                            binding.itemLinkDescription.text = siteDescription

                            Glide.with(this@ItemDetailActivity)
                                    .load(siteThumbnail)
                                    .placeholder(R.drawable.placeholder)
                                    .into(binding.itemLinkImage)
                        }

                        override fun onBeforeLoading() {
                            /* no-op */
                        }
                    })
                }
                else ->{
                    binding.itemLinkLayout.setOnClickListener {
                        Toast.makeText(this, "올바르지 않은 URL 입니다", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        if (item.memo.isEmpty()) {
            binding.itemMemo.text = "메모가 없습니다"
        } else {
            binding.itemMemo.text = item.memo
        }

        binding.itemRatingBar.rating = item.priority.toFloat()

        if (!item.image.isBlank() && item.image != "null"){
            Glide.with(this)
                .load(item.image)
                .into(binding.itemImage)
        }

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
            val intent = Intent(this, AddItemActivity::class.java)
            intent.putExtra(EDIT_ITEM, item)
            startActivity(intent)

            finish()  // 편집 후 복귀했을 때 정보 동기화를 위해 기존 정보 페이지는 닫아줌
        }

        // 공유 버튼 눌렀을 때 (카카오톡 공유 동작)
        binding.shareButton.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            // 링크 등록이 되어있지 않다면, 이미지 존재 여부 체크
            if (item.link.isBlank()) {
                // 만약 링크, 이미지가 모두 없다면 공유하기에 부적절함
                if (item.image.isBlank()) {
                    Toast.makeText(applicationContext, "공유할 컨텐츠가 부족합니다! 이미지, 링크 등을 추가해보세요!", Toast.LENGTH_LONG).show()
                } else {
                    sharingIntent.apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, Uri.parse(item.image))
                        Log.d("TEST", Uri.parse(item.image).toString())
                        setPackage("com.kakao.talk")
                    }
                    startActivity(sharingIntent) // 결과를 받고싶을 때
                }
            } else {
                sharingIntent.apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "[${item.name}]\n\n${item.link}")
                    setPackage("com.kakao.talk")
                }
                startActivity(sharingIntent)
            }

        }
    }

    companion object {
        const val EXTRA_ITEM = "EXTRA_ITEM"
    }
}