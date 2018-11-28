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
                    RequestResult(status = RequestStatus(404, ar.cause().message.orEmpty()))
                }.apply {
                    assert(buffer != null || status != null) {
                        "RequestResult buffer or status should be present!"
                    }
                    headers.forEach { t, u -> response().putHeader(t, u) }
                    buffer?.apply {
                        response()
                            .putHeader("content-length", length().toString())
                            .write(this)
                            .end()
                    }
                    status?.apply {
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
