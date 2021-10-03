package com.haero_kim.keepit.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.haero_kim.keepit.R
import com.haero_kim.keepit.data.ItemEntity
import com.haero_kim.keepit.databinding.ItemListBinding
import java.text.DecimalFormat

/**
 * 메인 화면에 보여지는 아이템 리사이클러뷰 어댑터
 *
 * @property itemClick      아이템의 상세 정보 액티비티로 이동
 * @property itemLongClick  아이템 삭제 다이얼로그 표시
 */
class ItemListAdapter(
    val itemClick: (ItemEntity) -> Unit,
    val itemLongClick: (ItemEntity) -> Unit
) : RecyclerView.Adapter<ItemListAdapter.ViewHolder>() {

    private var items: List<ItemEntity> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(private val binding: ItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ItemEntity) {
            YoYo.with(Techniques.ZoomIn)
                .duration(250)
                .playOn(itemView)

            // 화폐 단위 표시 포맷
            val decimalFormat = DecimalFormat("#,###")
            val itemPrice = decimalFormat.format(item.price)

            binding.itemName.text = item.name
            binding.itemPrice.text = "₩${itemPrice}원"

            when (item.image) {
                "null" -> {
                    binding.itemImageView.visibility = View.GONE
                }
                else -> {
                    binding.itemImageView.visibility = View.VISIBLE
                    Glide.with(itemView)
                        .load(item.image)
                        .into(binding.itemImageView)
                }
            }

            // 아이템 상세 정보로 이동
            itemView.setOnClickListener {
                itemClick(item)
            }

            // 아이템 삭제 다이얼로그 표시
            itemView.setOnLongClickListener {
                itemLongClick(item)
                true
            }
        }
    }

    fun setItems(items: List<ItemEntity>) {
        this.items = items
        notifyDataSetChanged()
    }
}