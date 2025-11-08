package com.l2loot.di

import co.touchlab.kermit.Logger
import com.l2loot.Config
import com.l2loot.data.firebase.AnalyticsServiceImpl
import com.l2loot.data.logging.FileLogger
import com.l2loot.domain.firebase.FirebaseAuthService
import com.l2loot.data.firebase.FirebaseAuthServiceImpl
import com.l2loot.data.repository.UpdateCheckerRepositoryImpl
import com.l2loot.domain.firebase.AnalyticsService
import com.l2loot.domain.repository.UpdateCheckerRepository
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.core.qualifier.named
import org.koin.dsl.module

val jvmModule = module {
    if (Config.IS_DEBUG) {
        Logger.addLogWriter(FileLogger())
    }
    
    // HttpClient Engine
    single<HttpClientEngine> { OkHttp.create() }
    
    // Services
    single<AnalyticsService> { AnalyticsServiceImpl(get(), get()) }
    single<UpdateCheckerRepository> { UpdateCheckerRepositoryImpl(httpClient = get(), logger = get()) }
    single<FirebaseAuthService> { FirebaseAuthServiceImpl(get(named("unauthenticated")), get()) }
}
