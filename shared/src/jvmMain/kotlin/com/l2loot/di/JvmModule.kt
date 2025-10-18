package com.l2loot.di

import com.l2loot.data.firebase.AnalyticsServiceImpl
import com.l2loot.domain.firebase.FirebaseAuthService
import com.l2loot.data.firebase.FirebaseAuthServiceImpl
import com.l2loot.data.repository.UpdateCheckerRepositoryImpl
import com.l2loot.domain.firebase.AnalyticsService
import com.l2loot.domain.repository.UpdateCheckerRepository
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.dsl.module

val jvmModule = module {
    // HttpClient Engine
    single<HttpClientEngine> { OkHttp.create() }
    
    // Services
    single<AnalyticsService> { AnalyticsServiceImpl(get(), get()) }
    single<UpdateCheckerRepository> { UpdateCheckerRepositoryImpl(get(), get()) }
    single<FirebaseAuthService> { FirebaseAuthServiceImpl(get(), get()) }
}
