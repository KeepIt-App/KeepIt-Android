package com.haero_kim.pickmeup.data

import android.graphics.Bitmap
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "item")
data class Item(
    @PrimaryKey(autoGenerate = true)
    var id: Long?,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "image")
    var image: Bitmap?,

    @ColumnInfo(name = "price")
    var price: Long,

    @ColumnInfo(name = "link")
    var link: String,

    @ColumnInfo(name = "priority")
    var priority: Int,

    @ColumnInfo(name = "note")
    var note: String?

) {
    constructor() : this(null, "", null, 0, "", 1, "")
}
