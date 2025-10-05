package com.l2loot.di

import com.l2loot.data.DriverFactory
import com.l2loot.features.explore.ExploreViewModel
import com.l2loot.features.sellable.SellableViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    single { DriverFactory() } bind DriverFactory::class
    viewModelOf(::ExploreViewModel)
    viewModelOf(::SellableViewModel)
}
