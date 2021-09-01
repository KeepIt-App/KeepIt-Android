package com.haero_kim.pickmeup

import android.app.Application
import android.content.Context
import android.util.Log.ERROR
import androidx.databinding.library.BuildConfig
import com.haero_kim.pickmeup.data.ItemRepository
import com.haero_kim.pickmeup.di.repositoryModule
import com.haero_kim.pickmeup.di.viewModelModule
import com.haero_kim.pickmeup.util.MySharedPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import java.util.logging.Level

/**
 * 앱 실행 시 가장 먼저 진입
 */
class MyApplication : Application() {
    init {
        instance = this
    }

    companion object {
        lateinit var instance: MyApplication
        lateinit var prefs: MySharedPreferences
        fun applicationContext(): Context {
            return instance.applicationContext
        }
        fun getRepository(): ItemRepository{
            return ItemRepository.getInstance()
        }
    }

    override fun onCreate() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // SharedPreferences 가 가장 먼저 생성되어야 데이터 저장에 문제 발생 안함
        prefs = MySharedPreferences(applicationContext)
        super.onCreate()

        startKoin {
            androidLogger(org.koin.core.logger.Level.ERROR)
            androidContext(this@MyApplication)
            androidFileProperties()
            modules(listOf(repositoryModule, viewModelModule))
        }
    }
}