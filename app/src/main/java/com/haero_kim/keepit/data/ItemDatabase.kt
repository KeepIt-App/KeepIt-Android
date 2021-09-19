package com.haero_kim.keepit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// Entity 정의 및 SQLite 버전 지정
@Database(entities = [ItemEntity::class, ItemFtsEntity::class], version = 7, exportSchema = false)
abstract class ItemDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    // 데이터 베이스 인스턴스를 싱글톤으로 사용하기 위해 companion object 사용
    companion object {
        private var INSTANCE: ItemDatabase? = null

        fun getInstance(context: Context): ItemDatabase? {
            if (INSTANCE == null) {
                // 여러 Thread 가 접근하지 못하도록 Synchronized 사용
                synchronized(ItemDatabase::class) {
                    // Room 인스턴스 생성
                    // 데이터 베이스가 갱신될 때 기존의 테이블을 버리고 새로 사용하도록 설정
                    INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            ItemDatabase::class.java, "item"
                    )
                            .fallbackToDestructiveMigration()
                            .build()
                }
            }
            // 만들어지는 DB 인스턴스는 Repository 에서 호출되어 사용
            return INSTANCE
        }
    }
}