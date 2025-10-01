package com.l2loot.di

import com.l2loot.data.DriverFactory
import com.l2loot.features.explore.ExploreViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { DriverFactory() } bind DriverFactory::class
    viewModelOf(::ExploreViewModel)
}
