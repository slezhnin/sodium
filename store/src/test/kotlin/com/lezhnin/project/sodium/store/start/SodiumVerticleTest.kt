package com.lezhnin.project.sodium.store.start

import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.lang.Thread.sleep
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals

class SodiumVerticleTest {

    companion object {
        const val TEST = "test"
        const val TEST1 = "test1"
        const val TEST2 = "test2"
        const val TEST1V = "Test 1"
        const val TEST2V = "Test 2"
        const val UNTEST = "untest"

        private val vertx = Vertx.vertx()!!
        val logger = LoggerFactory.getLogger(SodiumVerticleTest::class.java)!!
        val client = WebClient.create(vertx)!!
        private var sodiumVerticleId = ""

        @BeforeClass
        @JvmStatic
        fun setUp() {
            val future = CompletableFuture<String>()
            val config = JsonObject(
                    """
                        {
                            "stores": [
                                {
                                    "type": "json",
                                    "config": {
                                        "$TEST": {
                                            "stores": [
                                                {
                                                    "type": "json",
                                                    "config": {
                                                        "$TEST1": "$TEST1V",
                                                        "$TEST2": "$TEST2V"
                                                    }
                                                }
                                            ]
                                        }
                                    }
                                }
                            ]
                        }
                    """.trimIndent()
            )
            val options = DeploymentOptions().setConfig(config)
            vertx.deployVerticle(SodiumVerticle(), options) {
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
    fun testSuccess() {
        val future = CompletableFuture<JsonObject>()
        client.get(8080, "localhost", "/dictionary/$TEST").send {
            if (it.succeeded()) {
                future.complete(it.result().bodyAsJsonObject())
            } else {
                future.completeExceptionally(it.cause())
            }
        }
        val jsonObject = future.get()
        logger.info(jsonObject)
        assertEquals(TEST1V, jsonObject.getString(TEST1))
        assertEquals(TEST2V, jsonObject.getString(TEST2))
    }

    @Test
    fun testFailure() {
        val future = CompletableFuture<HttpResponse<Buffer>>()
        client.get(8080, "localhost", "/dictionary/$UNTEST").send {
            if (it.succeeded()) {
                future.complete(it.result())
            } else {
                future.completeExceptionally(it.cause())
            }
        }
        val response = future.get()
        assertEquals(404, response.statusCode())
        assertEquals("Error get value for: $UNTEST", response.statusMessage())
    }
}
