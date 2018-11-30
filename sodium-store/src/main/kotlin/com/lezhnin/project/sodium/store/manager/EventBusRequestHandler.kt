package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.vertx.web.FailedRequestResult
import com.lezhnin.project.vertx.web.JsonRequestResult
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.core.logging.Logger
import io.vertx.kotlin.core.json.json

class EventBusRequestHandler(private val dataService: DataService, val logger: Logger) : Handler<Message<String>> {
    override fun handle(event: Message<String>) {
        logger.debug("EventBus Request: ${event.body()}")

        if (event.body().isNullOrBlank()) {
            logger.error("EventBus Request: is empty!")
            event.reply(json { "error" to "Empty request!" })
            return
        }

        if (event.body().contains('/')) {
            val r = event.body().split('/', limit = 2)
            dataService.chain(r.first()).request(r.last())
        } else {
            dataService.request(event.body())
        }.setHandler {
            event.reply(
                if (it.succeeded()) {
                    when (val request = it.result()) {
                        is JsonRequestResult -> json {
                            "key" to event.body()
                            "result" to request.result
                        }
                        is FailedRequestResult -> json {
                            "key" to event.body()
                            "error" to request.status.message
                        }
                        else -> json {
                            "key" to event.body()
                            "error" to "Unknown result error!"
                        }
                    }
                } else {
                    json {
                        "key" to event.body()
                        "error" to it.cause().message
                    }
                }
            )
        }
    }
}