package com.l2loot.di

import com.l2loot.data.analytics.AnalyticsService
import com.l2loot.data.analytics.AnalyticsServiceImpl
import com.l2loot.data.firebase.FirebaseAuthService
import com.l2loot.data.firebase.FirebaseAuthServiceImpl
import com.l2loot.data.update.UpdateChecker
import com.l2loot.data.update.UpdateCheckerImpl
import org.koin.dsl.module

val jvmModule = module {
    single<AnalyticsService> { AnalyticsServiceImpl() }
    single<UpdateChecker> { UpdateCheckerImpl() }
    single<FirebaseAuthService> { FirebaseAuthServiceImpl() }
}
