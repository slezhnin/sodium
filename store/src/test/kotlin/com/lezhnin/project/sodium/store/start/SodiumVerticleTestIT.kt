package com.lezhnin.project.sodium.store.start

import com.lezhnin.project.sodium.store.Sodium
import com.lezhnin.project.sodium.store.Store
import com.lezhnin.project.sodium.store.Web
import io.kotlintest.Description
import io.kotlintest.Spec
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.whenReady
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object TestData {
    const val TEST = "test"
    const val TEST1 = "test1"
    const val TEST2 = "test2"
    const val TEST1V = "Test 1"
    const val TEST2V = "Test 2"
    const val UNTEST = "untest"

    val config = json {
        obj(
                Store.STORES to array(
                        obj(
                                Store.TYPE to Store.Type.JSON,
                                Store.CONFIG to obj(
                                        TEST to obj(
                                                Store.STORES to array(
                                                        obj(
                                                                Store.TYPE to Store.Type.JSON,
                                                                Store.CONFIG to obj(
                                                                        TEST1 to TEST1V,
                                                                        TEST2 to TEST2V
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        )
    }
}

object VertxTester : TestListener {
    private val vertx = Vertx.vertx()!!
    val logger = LoggerFactory.getLogger(VertxTester::class.java)!!
    val client = WebClient.create(vertx)!!
    private val sodiumVerticle = SodiumVerticle()

    private fun waitForMap(retries: Int = 5, minSize: Int = 1) {
        (1..retries).forEach {
            val mapSizeOkFuture = CompletableFuture<Void>()
            vertx.sharedData()
                    .getAsyncMap<String, JsonObject>(Sodium.MAP_NAME) { map ->
                        if (map.succeeded()) {
                            map.result().size { size ->
                                if (size.succeeded()) {
                                    if (size.result() >= minSize) {
                                        mapSizeOkFuture.complete(null)
                                    }
                                } else {
                                    logger.warn("Can't get size of ${Sodium.MAP_NAME}!", size.cause())
                                }
                            }
                        } else {
                            logger.warn("Can't get ${Sodium.MAP_NAME}!", map.cause())
                        }
                    }
            try {
                mapSizeOkFuture.get(1, TimeUnit.SECONDS)
            } catch (te: TimeoutException) {
                logger.warn("Timeout waiting for ${Sodium.MAP_NAME}, retry $it...")
            }
        }
    }

    override fun afterSpec(description: Description, spec: Spec) {
        vertx.undeploy(sodiumVerticle.deploymentID())
    }

    override fun beforeSpec(description: Description, spec: Spec) {
        val options = DeploymentOptions().setConfig(TestData.config)
        vertx.deployVerticle(sodiumVerticle, options)
        waitForMap()
    }
}

class SodiumVerticleTestIT : StringSpec() {

    override fun listeners(): List<TestListener> = listOf(VertxTester)

    init {
        "success" {
            val jsonFuture = CompletableFuture<JsonObject>()

            VertxTester.client.get(8080, "localhost", "${Web.PATH}${TestData.TEST}").send {
                if (it.succeeded()) {
                    jsonFuture.complete(it.result().bodyAsJsonObject())
                } else {
                    jsonFuture.completeExceptionally(it.cause())
                }
            }

            whenReady(jsonFuture) {
                VertxTester.logger.info(it)

                it.getString(TestData.TEST1) shouldBe TestData.TEST1V
                it.getString(TestData.TEST2) shouldBe TestData.TEST2V
            }
        }

        "failure" {
            val responseFuture = CompletableFuture<HttpResponse<Buffer>>()

            VertxTester.client.get(8080, "localhost", "${Web.PATH}${TestData.UNTEST}").send {
                if (it.succeeded()) {
                    responseFuture.complete(it.result())
                } else {
                    responseFuture.completeExceptionally(it.cause())
                }
            }

            whenReady(responseFuture) {
                it.statusCode() shouldBe 404
                it.statusMessage() shouldBe "Error get value for: ${TestData.UNTEST}"
            }
        }
    }
}