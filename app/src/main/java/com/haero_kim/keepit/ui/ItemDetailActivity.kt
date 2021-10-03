package com.haero_kim.keepit.ui

import android.content.Intent
import android.net.Uri
import android.util.Patterns
import android.view.MenuItem
import android.view.View
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.work.WorkManager
import com.anandwana001.ogtagparser.LinkSourceContent
import com.anandwana001.ogtagparser.LinkViewCallback
import com.anandwana001.ogtagparser.OgTagParser
import com.bumptech.glide.Glide
import com.haero_kim.keepit.R
import com.haero_kim.keepit.base.BaseActivity
import com.haero_kim.keepit.data.ItemEntity
import com.haero_kim.keepit.databinding.ActivityItemDetailBinding
import com.haero_kim.keepit.ui.AddItemActivity.Companion.EDIT_ITEM
import com.haero_kim.keepit.util.ViewUtil.playFailureAlert
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat


class ItemDetailActivity : BaseActivity<ActivityItemDetailBinding, ItemViewModel>() {
    override val layoutResourceId: Int
        get() = R.layout.activity_item_detail
    override val viewModel: ItemViewModel by viewModel()

    val item: ItemEntity by lazy {
        intent.getSerializableExtra(EXTRA_ITEM) as ItemEntity
    }

    override fun initStartView() {
        binding.viewModel = this.viewModel
    }

    override fun initDataBinding() {
    }

    override fun initAfterBinding() {
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
            openGraphParsing()
        }
        binding.itemMemo.text = if (item.memo.isEmpty()) "등록된 메모가 없습니다" else item.memo
        binding.itemRatingBar.rating = item.priority.toFloat()

        if (item.image.isNotBlank() && item.image != "null") {
            Glide.with(this)
                .load(item.image)
                .into(binding.itemImage)
        }

        // BottomAppBar - 삭제 버튼 눌렀을 때
        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            deleteItem(menuItem)
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
            shareToKakaoTalk()
        }
    }

    private fun deleteItem(menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.delete -> {
                val builder = AlertDialog.Builder(this)
                builder.apply {
                    this.setMessage("삭제하시겠습니까?")
                    this.setNegativeButton("NO") { _, _ -> }
                    this.setPositiveButton("YES") { _, _ ->
                        viewModel.delete(item)
                        val workManager: WorkManager = WorkManager.getInstance(context)
                        // WorkRequest 등록 시, 아이템 명으로 고유 태그를 달아줬기 때문에
                        // 아래와 같이 item.name 을 통해 주기적인 푸시알림 작업을 취소할 수 있음
                        workManager.cancelAllWorkByTag(item.id.toString())
                        finish()
                    }
                }
                builder.show()
                true
            }
            else -> false
        }
    }

    private fun shareToKakaoTalk() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        // 링크 등록이 되어있지 않다면
        if (item.link.isBlank()) {
            playFailureAlert(this, "공유할 컨텐츠가 부족합니다! 쇼핑몰 링크를 추가해보세요!")
        } else {
            sharingIntent.apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "[${item.name}]\n\n${item.link}")
                setPackage("com.kakao.talk")
            }
            startActivity(sharingIntent)
        }

    }

    private fun openGraphParsing() {
        when {
            Patterns.WEB_URL.matcher(item.link).matches() -> {
                var itemLink = item.link
                // 만약 http 형식이 아니라면 앞에 'http://' 를 붙여줘야함 ==> ex) www.naver.com 과 같은 상황
                if (!URLUtil.isHttpsUrl(itemLink) and !URLUtil.isHttpUrl(itemLink)) {
                    itemLink = "https://$itemLink"
                }
                binding.itemLinkLayout.setOnClickListener {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(itemLink)))
                }

                // Open Graph 태그를 불러오는 라이브러리 사용
                OgTagParser().execute(itemLink, object : LinkViewCallback {
                    override fun onAfterLoading(linkSourceContent: LinkSourceContent) {
                        var siteThumbnail = linkSourceContent.images
                        if (siteThumbnail.startsWith("//")) {
                            siteThumbnail = "https:" + siteThumbnail
                        }
                        Glide.with(this@ItemDetailActivity)
                            .load(siteThumbnail)
                            .placeholder(R.drawable.placeholder)
                            .into(binding.itemLinkImage)

                        binding.itemLinkTitle.text = linkSourceContent.ogTitle
                        binding.itemLinkDescription.text = linkSourceContent.ogDescription

                        // 로딩이 완료되지 않았는데 액티비티가 종료된 경우
                        // - 해당 코드가 없을 시 메모리 릭 발생 가능
                        if (this@ItemDetailActivity.isFinishing) {
                            return
                        }
                    }

                    override fun onBeforeLoading() {
                        /* no-op */
                    }
                })
            }
            else -> {
                binding.itemLinkLayout.setOnClickListener {
                    Toast.makeText(this, "올바르지 않은 URL 입니다", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    companion object {
        const val EXTRA_ITEM = "EXTRA_ITEM"
    }
}
