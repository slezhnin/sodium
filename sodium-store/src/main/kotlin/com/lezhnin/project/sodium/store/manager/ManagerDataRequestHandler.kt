package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.sodium.store.Web
import io.vertx.core.Handler
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.ext.web.RoutingContext

class ManagerDataRequestHandler(private val mapName: String, private val logger: Logger) : Handler<RoutingContext> {
    override fun handle(event: RoutingContext?) {
        event?.apply {
            vertx().sharedData().getAsyncMap<String, JsonObject>(mapName) {
                val key = request().getParam(Web.PARAMETER)
                if (it.succeeded() && key != null) {
                    it.result().get(key) { ar ->
                        if (ar.succeeded() && ar.result() != null) {
                            logger.debug("Success get map $mapName key: $key")
                            endWithValueFound(response(), ar.result())
                        } else {
                            logger.error("Error get map $mapName key: $key")
                            endWithValueNotFound(response(), key)
                        }
                    }
                } else {
                    it.cause()?.apply {
                        logger.error("Error getting AsyncMap: $mapName", this)
                    }
                    endWithValueNotFound(response(), key)
                }
            }
        }
    }

    private fun endWithValueFound(response: HttpServerResponse, value: JsonObject) {
        value.toBuffer().apply {
            response.putHeader("content-type", "application/json")
                .putHeader("content-length", length().toString())
                .write(this)
                .end()
        }
    }

    private fun endWithValueNotFound(response: HttpServerResponse, key: String) {
        response.setStatusCode(404)
            .setStatusMessage("Error get value for: $key")
            .end()
    }
}