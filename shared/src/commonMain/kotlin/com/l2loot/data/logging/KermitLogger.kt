package com.l2loot.data.logging

import co.touchlab.kermit.Logger
import com.l2loot.Config
import com.l2loot.domain.logging.LootLogger

object KermitLogger: LootLogger {
    override fun debug(message: String) {
        if (Config.IS_DEBUG) {
            Logger.d(message)
        }
    }

    override fun info(message: String) {
        if (Config.IS_DEBUG) {
            Logger.i(message)
        }
    }

    override fun warn(message: String) {
        Logger.w(message)
    }

    override fun error(
        message: String,
        throwable: Throwable?
    ) {
        Logger.e(message, throwable)
    }

}