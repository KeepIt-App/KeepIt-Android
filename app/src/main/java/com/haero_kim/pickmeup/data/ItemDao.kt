package com.haero_kim.pickmeup.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ItemDao {
    @Query("SELECT * FROM item ORDER BY id DESC")
    fun getList(): LiveData<List<ItemEntity>>

    @Query("SELECT item.* FROM item JOIN itemFts ON (item.name = itemFts.name) WHERE itemFts MATCH :query")
    fun searchByName(query: String?): LiveData<List<ItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(itemEntity: ItemEntity)

    @Delete
    fun delete(itemEntity: ItemEntity)
}