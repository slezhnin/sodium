package com.lezhnin.project.sodium.store.reader

import io.vertx.config.ConfigChange
import io.vertx.core.Handler

class ChangeHandler(private val readHandler: ReadHandler) : Handler<ConfigChange> {
    override fun handle(event: ConfigChange?) {
        event?.apply {
            readHandler.read(newConfiguration)
        }
    }
}