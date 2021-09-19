package com.haero_kim.keepit.di

import com.haero_kim.keepit.MyApplication
import com.haero_kim.keepit.data.ItemRepository
import com.haero_kim.keepit.ui.ItemViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        ItemViewModel(get())
    }
}

val repositoryModule = module {
    single {
        ItemRepository(MyApplication.instance)
    }
}