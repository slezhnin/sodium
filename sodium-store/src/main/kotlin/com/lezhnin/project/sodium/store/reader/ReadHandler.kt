package com.lezhnin.project.sodium.store.reader

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.shareddata.SharedData

class ReadHandler(
    private val vertx: Vertx,
    private val mapName: String,
    private val key: String,
    private val logger: Logger
) : Handler<AsyncResult<JsonObject>> {

    override fun handle(event: AsyncResult<JsonObject>?) {
        event?.apply {
            read(result())
        }
    }

    fun read(json: JsonObject) {
        vertx.sharedData().getAsyncMap<String, JsonObject>(mapName) {
            if (it.succeeded()) {
                it.result().put(key, json) { result ->
                    if (result.succeeded()) {
                        logger.debug("Success put into map $mapName key: $key")
                    } else {
                        logger.error("Error put into map $mapName key: $key")
                    }
                }
            } else {
                logger.error("Error getting AsyncMap: $mapName", it.cause())
            }
        }
        vertx.eventBus().publish("sodium.out/$key", json.encodePrettily())
    }
}