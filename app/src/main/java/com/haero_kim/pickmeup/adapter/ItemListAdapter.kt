package com.haero_kim.pickmeup.adapter

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.haero_kim.pickmeup.MyApplication
import com.haero_kim.pickmeup.R
import com.haero_kim.pickmeup.data.ItemEntity
import com.haero_kim.pickmeup.ui.ItemDetailActivity
import java.text.DecimalFormat

class ItemListAdapter(
        val itemClick: (ItemEntity) -> Unit,
        val itemLongClick: (ItemEntity) -> Unit
) : RecyclerView.Adapter<ItemListAdapter.ViewHolder>() {
    private var items: List<ItemEntity> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name = itemView.findViewById<TextView>(R.id.itemName)
        private val price = itemView.findViewById<TextView>(R.id.itemPrice)
        private val image = itemView.findViewById<ImageView>(R.id.itemImage)

        fun bind(item: ItemEntity) {
            YoYo.with(Techniques.ZoomIn)
                    .duration(250)
                    .playOn(itemView)
            // 화폐 단위 표시 포맷
            val decimalFormat = DecimalFormat("#,###")
            val itemPrice = decimalFormat.format(item.price)

            name.text = item.name
            price.text = "₩${itemPrice}원"

            if (item.image == "null") {
                image.visibility = View.GONE
            } else {
                image.visibility = View.VISIBLE
                Glide.with(itemView)
                        .load(item.image)
                        .into(image)
            }

            itemView.setOnClickListener {
                itemClick(item)
            }

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