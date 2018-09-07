package com.lezhnin.project.sodium.store

import io.vertx.config.ConfigChange
import io.vertx.core.Handler

class ChangeHandler(private val readHandler: ReadHandler) : Handler<ConfigChange> {
    override fun handle(event: ConfigChange?) {
        readHandler.read(event?.newConfiguration)
    }
}