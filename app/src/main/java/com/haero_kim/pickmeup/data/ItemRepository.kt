package com.haero_kim.pickmeup.data

import android.app.Application
import androidx.lifecycle.LiveData
import java.lang.Exception

class ItemRepository(application: Application) {
    private val itemDatabase = ItemDatabase.getInstance(application)!!
    private val itemDao = itemDatabase.itemDao()
    private val items: LiveData<List<Item>> = itemDao.getAll()

    fun getAll(): LiveData<List<Item>> {
        return items
    }

    // Room DB 를 메인 쓰레드에서 접근하게 되면 크래쉬 발생 우려
    // - 따라서 별도의 Thread 를 생성하여 접근하는 것이 좋음
    fun insert(item: Item) {
        try {
            val thread = Thread(Runnable {
                itemDao.insert(item)
            })
            thread.start()
        } catch (e: Exception) {

        }
    }

    fun delete(item: Item) {
        try {
            val thread = Thread(Runnable {
                itemDao.delete(item)
            })
            thread.start()
        } catch (e: Exception) {

        }
    }
}