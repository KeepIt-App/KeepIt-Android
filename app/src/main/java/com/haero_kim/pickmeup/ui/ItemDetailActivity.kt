package com.haero_kim.pickmeup.ui

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.webkit.URLUtil
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.haero_kim.pickmeup.R
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.databinding.ActivityItemDetailBinding
import com.haero_kim.pickmeup.databinding.ActivityMainBinding
import com.haero_kim.pickmeup.viewmodel.ItemViewModel
import org.koin.android.ext.android.bind
import org.w3c.dom.Text
import java.text.DecimalFormat

class ItemDetailActivity : AppCompatActivity() {
    private lateinit var bottomAppBar: BottomAppBar
    private lateinit var shareButton: FloatingActionButton
    private lateinit var backButton: ImageButton
    private lateinit var itemImage: ImageView
    private lateinit var itemName: TextView
    private lateinit var itemPrice: TextView
    private lateinit var itemLink: TextView
    private lateinit var itemMemo: TextView
    private lateinit var itemPriority: RatingBar
    private lateinit var itemLinkLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)

        bottomAppBar = findViewById(R.id.bottomAppBar)
        shareButton = findViewById(R.id.shareButton)
        backButton = findViewById(R.id.backButton)
        itemImage = findViewById(R.id.itemImage)
        itemName = findViewById(R.id.itemName)
        itemPrice = findViewById(R.id.itemPrice)
        itemLink = findViewById(R.id.itemLink)
        itemMemo = findViewById(R.id.itemMemo)
        itemPriority = findViewById(R.id.itemRatingBar)
        itemLinkLayout = findViewById(R.id.itemLinkLayout)


        val item = intent.getSerializableExtra(EXTRA_ITEM) as ItemEntity

        val decimalFormat = DecimalFormat("#,###")
        val itemPriceFormatted = decimalFormat.format(item.price)

        itemName.text = item.name
        itemPrice.text = "₩${itemPriceFormatted}원"

        if (item.link.isEmpty()) {
            itemLink.text = "링크가 없습니다"
        } else {
            itemLink.text = item.link
            itemLinkLayout.setOnClickListener {
                when {
                    // HTTP Url 이 맞을 때
                    URLUtil.isHttpUrl(item.link) -> {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.link)))
                    }
                    // Url 은 맞지만 HTTP Url 이 아닐 때
                    !URLUtil.isHttpUrl(item.link) and Patterns.WEB_URL.matcher(item.link).matches() -> {
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
            itemMemo.text = "메모가 없습니다"
        } else {
            itemMemo.text = item.note
        }

        itemPriority.rating = item.priority.toFloat()

        Glide.with(this)
            .load(item.image)
            .into(itemImage)

        backButton.setOnClickListener { finish() }

    }


    companion object {
        const val EXTRA_ITEM = "EXTRA_ITEM"
    }
}