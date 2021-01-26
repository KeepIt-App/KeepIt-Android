package com.haero_kim.pickmeup.data

import androidx.room.Entity
import androidx.room.Fts4

@Entity(tableName = "itemFts")
@Fts4(contentEntity = ItemEntity::class)
class ItemFtsEntity (
    val name: String
)