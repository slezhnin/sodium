package com.lezhnin.project.sodium.store.manager

import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.ext.web.RoutingContext

class ManagerDataRequestHandler(
    private val mapName: String,
    private val dataService: DataService,
    private val logger: Logger
) : Handler<RoutingContext> {
    override fun handle(event: RoutingContext?) {
        event?.apply {
            vertx().sharedData().getAsyncMap<String, JsonObject>(mapName) {
                if (it.succeeded()) {
                    dataService.serviceData(it.result(), request(), response())
                } else {
                    it.cause()?.apply {
                        logger.error("Error getting AsyncMap: $mapName", this)
                    }
                    RequestUtil.endWithValueNotFound(response(), mapName)
                }
            }
        }
    }
}