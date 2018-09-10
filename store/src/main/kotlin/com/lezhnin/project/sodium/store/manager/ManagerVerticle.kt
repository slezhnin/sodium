package com.lezhnin.project.sodium.store.manager

import io.vertx.core.AbstractVerticle
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router

class ManagerVerticle : AbstractVerticle() {
    companion object {
        private val logger = LoggerFactory.getLogger(ManagerVerticle::class.java)!!
    }

    override fun start() {
        val router = Router.router(getVertx())

        router.route("/dictionary/:name")
                .handler { context ->
                    sendData(
                            name = context.request().getParam("name"),
                            response = context.response()
                    )
                }

        getVertx().createHttpServer().requestHandler { router.accept(it) }.listen(8080)
    }

    private fun sendData(name: String, response: HttpServerResponse) {
        vertx.sharedData().getAsyncMap<String, JsonObject>("sodiumMap") {
            if (it.succeeded()) {
                it.result().get(name) { ar ->
                    if (ar.succeeded() && ar.result() != null) {
                        logger.debug("Success gut map sodiumMap key: $name")
                        val data = Buffer.buffer(ar.result().encode())
                        response.putHeader("content-type", "application/json")
                                .putHeader("content-length", data.length().toString())
                                .write(data)
                                .end()
                    } else {
                        logger.error("Error get map sodiumMap key: $name")
                        response.setStatusCode(404)
                                .setStatusMessage("Error get value for: $name")
                                .end()
                    }
                }
            } else {
                logger.error("Error getting AsyncMap: sodiumMap", it.cause())
                response.setStatusCode(404)
                        .setStatusMessage("Error get value for: $name")
                        .end()
            }
        }
    }
}
