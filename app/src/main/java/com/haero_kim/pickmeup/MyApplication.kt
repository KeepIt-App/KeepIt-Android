package com.haero_kim.pickmeup

import android.app.Application
import android.content.Context
import com.haero_kim.pickmeup.data.ItemRepository
import com.haero_kim.pickmeup.di.repositoryModule
import com.haero_kim.pickmeup.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class MyApplication : Application() {
    init {
        instance = this
    }

    companion object {
        lateinit var instance: MyApplication
        fun applicationContext(): Context {
            return instance.applicationContext
        }
        fun getRepository(): ItemRepository{
            return ItemRepository.getInstance()
        }
    }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MyApplication)
            androidFileProperties()
            modules(listOf(repositoryModule, viewModelModule))
        }
    }
}