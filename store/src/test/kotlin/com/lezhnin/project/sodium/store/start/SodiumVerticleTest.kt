package com.lezhnin.project.sodium.store.start

import io.vertx.core.Vertx
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.WebClient
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
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
            vertx.deployVerticle(SodiumVerticle()) {
                if (it.succeeded()) {
                    future.complete(it.result())
                } else {
                    future.completeExceptionally(it.cause())
                }
            }
            sodiumVerticleId = future.get()
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
