package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.vertx.web.FailedRequestResult
import com.lezhnin.project.vertx.web.JsonRequestResult
import io.vertx.core.Handler
import io.vertx.core.eventbus.Message
import io.vertx.core.logging.Logger

data class Success(val message: String)
data class Failure(val message: String)

class EventBusRequestHandler(private val dataService: DataService, val logger: Logger) : Handler<Message<String>> {
    override fun handle(message: Message<String>) {
        logger.debug("EventBus Request: ${message.body()}")

        if (message.body().contains('/')) {
            val r = message.body().split('/', limit = 2)
            dataService.chain(r.first()).request(r.last())
        } else {
            dataService.request(message.body())
        }.setHandler {
            reply(
                message,
                if (it.succeeded()) when (val request = it.result()) {
                    is JsonRequestResult -> Success(request.result.encodePrettily())
                    is FailedRequestResult -> Failure(request.status.message)
                    else -> Failure("Unknown result error!")
                }
                else Failure(it.cause().message.orEmpty())
            )
        }
    }

    private fun reply(message: Message<String>, result: Any) {
        when (result) {
            is Success -> message.reply(result.message)
            is Failure -> message.fail(404, result.message)
        }
    }
}