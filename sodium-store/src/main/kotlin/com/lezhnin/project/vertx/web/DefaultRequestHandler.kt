package com.lezhnin.project.vertx.web

import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.ext.web.RoutingContext

class DefaultRequestHandler(
    private val requestHandler: RequestHandler,
    private val config: JsonObject,
    private val logger: Logger
) : Handler<RoutingContext> {
    override fun handle(event: RoutingContext?) {
        event?.apply {
            requestHandler.handle(event, config, logger).setHandler { ar ->
                if (ar.succeeded()) {
                    ar.result()
                } else {
                    FailedRequestResult(status = RequestStatus(404, ar.cause().message.orEmpty()))
                }.apply {
                    headers.forEach { t, u -> response().putHeader(t, u) }
                    when (this) {
                        is JsonRequestResult -> result.toBuffer().apply {
                            response()
                                .putHeader("content-type", "application/json")
                                .putHeader("content-length", length().toString())
                                .write(this)
                                .end()
                        }
                        is FailedRequestResult -> status.apply {
                            logger.error(message)
                            response().setStatusCode(code)
                                .setStatusMessage(message)
                                .end()

                        }
                    }
                }
            }
        }
    }
}
