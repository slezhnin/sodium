package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.sodium.store.Sodium
import com.lezhnin.project.sodium.store.Web
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.RoutingContext

class ManagerDataRequestHandler : Handler<RoutingContext> {
    companion object {
        private val logger = LoggerFactory.getLogger(ManagerVerticle::class.java)!!
    }

    override fun handle(event: RoutingContext?) {
        event?.vertx()?.sharedData()?.getAsyncMap<String, JsonObject>(Sodium.MAP_NAME) {
            val name = event.request().getParam(Web.PARAMETER)
            if (it.succeeded()) {
                it.result().get(name) { ar ->
                    if (ar.succeeded() && ar.result() != null) {
                        logger.debug("Success get map ${Sodium.MAP_NAME} key: $name")
                        ar.result().toBuffer().apply {
                            event.response().putHeader("content-type", "application/json")
                                .putHeader("content-length", length().toString())
                                .write(this)
                                .end()
                        }
                    } else {
                        logger.error("Error get map ${Sodium.MAP_NAME} key: $name")
                        event.response().setStatusCode(404)
                            .setStatusMessage("Error get value for: $name")
                            .end()
                    }
                }
            } else {
                logger.error("Error getting AsyncMap: ${Sodium.MAP_NAME}", it.cause())
                event.response().setStatusCode(404)
                    .setStatusMessage("Error get value for: $name")
                    .end()
            }
        }
    }
}