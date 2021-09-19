package com.haero_kim.keepit.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "item")
data class ItemEntity(
        @PrimaryKey(autoGenerate = true)
        var id: Long?,

        @ColumnInfo(name = "name")
        var name: String,

        // 사용할 때 Uri.parse() 를 통해 Uri 형태로 변환해야함
        @ColumnInfo(name = "image")
        var image: String,

        @ColumnInfo(name = "price")
        var price: Long,

        @ColumnInfo(name = "link")
        var link: String,

        @ColumnInfo(name = "priority")
        var priority: Int,

        @ColumnInfo(name = "memo")
        var memo: String

) : Serializable