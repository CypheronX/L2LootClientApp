package com.l2loot.di

import com.l2loot.data.repository.LoadDbDataRepositoryImpl
import com.l2loot.data.logging.KermitLogger
import com.l2loot.data.repository.monsters.MonsterRepositoryImpl
import com.l2loot.data.networking.HttpClientFactory
import com.l2loot.domain.logging.LootLogger
import com.l2loot.domain.repository.SellableRepository
import com.l2loot.data.repository.SellableRepositoryImpl
import com.l2loot.data.repository.UserSettingsRepositoryImpl
import com.l2loot.data.createDatabase
import com.l2loot.data.repository.ExternalLinksRepositoryImpl
import com.l2loot.domain.repository.ExternalLinksRepository
import com.l2loot.domain.repository.LoadDbDataRepository
import com.l2loot.domain.repository.MonsterRepository
import com.l2loot.domain.repository.UserSettingsRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    single { createDatabase(get()) }
    
    // Logger
    single<LootLogger> { KermitLogger }
    
    // HttpClient
    single { HttpClientFactory(get()) }
    single { get<HttpClientFactory>().create(get()) }
    
    // Repositories
    singleOf(::MonsterRepositoryImpl) bind MonsterRepository::class
    singleOf(::LoadDbDataRepositoryImpl) bind LoadDbDataRepository::class
    singleOf(::SellableRepositoryImpl) bind SellableRepository::class
    singleOf(::UserSettingsRepositoryImpl) bind UserSettingsRepository::class
    singleOf(::ExternalLinksRepositoryImpl) bind ExternalLinksRepository::class
}
