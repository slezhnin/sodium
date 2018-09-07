package com.lezhnin.project.sodium.store.reader

import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory

class ReadHandler(private val vertx: Vertx, private val name: String) : Handler<AsyncResult<JsonObject>> {
    companion object {
        private val logger = LoggerFactory.getLogger(ReaderVerticle::class.java)
    }

    override fun handle(event: AsyncResult<JsonObject>?) {
        read(event?.result())
    }

    fun read(json: JsonObject?) {
        vertx.sharedData()
                .getAsyncMap<String, JsonObject>("sodiumMap") {
                    if (it.succeeded()) {
                        it.result().put(name, json) { result ->
                            if (result.succeeded()) {
                                logger.debug("Success put into map sodiumMap key: $name")
                            } else {
                                logger.error("Error put into map sodiumMap key: $name")
                            }
                        }
                    } else {
                        logger.error("Error getting AsyncMap: sodiumMap", it.cause())
                    }
                }
    }
}