package com.haero_kim.pickmeup.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.haero_kim.pickmeup.R
import com.haero_kim.pickmeup.data.ItemEntity

class ItemListAdapter(
    val contactItemClick: (ItemEntity) -> Unit,
    val contactItemLongClick: (ItemEntity) -> Unit
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
            name.text = item.name
            price.text = "${item.price}원"

            itemView.setOnClickListener {

            }

            itemView.setOnLongClickListener {
                true
            }
        }
    }

    fun setItems(items: List<ItemEntity>) {
        this.items = items
        notifyDataSetChanged()
    }
}