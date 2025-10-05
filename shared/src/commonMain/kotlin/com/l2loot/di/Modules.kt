package com.l2loot.di

import com.l2loot.data.LoadDbDataRepository
import com.l2loot.data.LoadDbDataRepositoryImpl
import com.l2loot.data.monsters.MonsterRepository
import com.l2loot.data.monsters.MonsterRepositoryImpl
import com.l2loot.data.sellable.SellableRepository
import com.l2loot.data.sellable.SellableRepositoryImpl
import com.l2loot.data.createDatabase
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val sharedModule = module {
    single { createDatabase(get()) }
    singleOf(::MonsterRepositoryImpl) bind MonsterRepository::class
    singleOf(::LoadDbDataRepositoryImpl) bind LoadDbDataRepository::class
    singleOf(::SellableRepositoryImpl) bind SellableRepository::class
}
