package com.haero_kim.pickmeup.di

import com.haero_kim.pickmeup.MyApplication
import com.haero_kim.pickmeup.data.ItemRepository
import com.haero_kim.pickmeup.ui.ItemViewModel
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