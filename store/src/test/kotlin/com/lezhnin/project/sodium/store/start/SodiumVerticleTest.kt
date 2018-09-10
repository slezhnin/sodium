package com.lezhnin.project.sodium.store.start

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.lang.Thread.sleep
import java.util.concurrent.CompletableFuture

class SodiumVerticleTest {

    companion object {
        val vertx = Vertx.vertx()!!
        val logger = LoggerFactory.getLogger(SodiumVerticleTest::class.java)!!
        val client = WebClient.create(vertx)!!
        var sodiumVerticleId = ""

        @BeforeClass
        @JvmStatic
        fun setUp() {
            val future = CompletableFuture<String>()
            vertx.deployVerticle(
                    SodiumVerticle(),
                    DeploymentOptions(
                            json {
                                obj {
                                    "stores" to array {
                                        obj {
                                            "type" to "json"
                                            "config" to obj {
                                                "test" to obj {
                                                    "type" to "json"
                                                    "config" to obj {
                                                        "test1" to "Test 1"
                                                        "test2" to "Test 2"
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                    )
            ) {
                if (it.succeeded()) {
                    future.complete(it.result())
                } else {
                    future.completeExceptionally(it.cause())
                }
            }
            sodiumVerticleId = future.get()
            waitForMap()
        }

        private fun waitForMap() {
            (1..5).forEach {
                val future = CompletableFuture<Int>()
                vertx.sharedData()
                        .getAsyncMap<String, JsonObject>("sodiumMap") { map ->
                            if (map.succeeded()) {
                                map.result().size { size ->
                                    if (size.succeeded()) {
                                        future.complete(size.result())
                                    } else {
                                        logger.warn("Can't get size of sodiumMap!", size.cause())
                                        future.complete(0)
                                    }
                                }
                            } else {
                                logger.warn("Can't get sodiumMap!", map.cause())
                                future.complete(0)
                            }
                        }
                if (future.get() > 0) {
                    return@forEach
                }
                sleep(1000)
            }
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            if (sodiumVerticleId != "") {
                vertx.undeploy(sodiumVerticleId)
            }
        }
    }

    @Test
    fun testStart() {
        val future = CompletableFuture<String>()
        client.get(8080, "localhost", "/dictionary/test").send {
            if (it.succeeded()) {
                future.complete(it.result().bodyAsString())
            } else {
                future.completeExceptionally(it.cause())
            }
        }
        logger.info(future.get())
    }
}
