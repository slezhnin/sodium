package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.sodium.store.Sodium
import com.lezhnin.project.sodium.store.Web
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

        router.route("${Web.PATH}:${Web.PARAMETER}")
                .handler { context ->
                    sendData(
                            name = context.request().getParam(Web.PARAMETER),
                            response = context.response()
                    )
                }

        getVertx().createHttpServer().requestHandler { router.accept(it) }.listen(8080)
    }

    private fun sendData(name: String, response: HttpServerResponse) {
        vertx.sharedData().getAsyncMap<String, JsonObject>(Sodium.MAP_NAME) {
            if (it.succeeded()) {
                it.result().get(name) { ar ->
                    if (ar.succeeded() && ar.result() != null) {
                        logger.debug("Success get map ${Sodium.MAP_NAME} key: $name")
                        ar.result().toBuffer().apply {
                            response.putHeader("content-type", "application/json")
                                    .putHeader("content-length", length().toString())
                                    .write(this)
                                    .end()
                        }
                    } else {
                        logger.error("Error get map ${Sodium.MAP_NAME} key: $name")
                        response.setStatusCode(404)
                                .setStatusMessage("Error get value for: $name")
                                .end()
                    }
                }
            } else {
                logger.error("Error getting AsyncMap: ${Sodium.MAP_NAME}", it.cause())
                response.setStatusCode(404)
                        .setStatusMessage("Error get value for: $name")
                        .end()
            }
        }
    }
}
