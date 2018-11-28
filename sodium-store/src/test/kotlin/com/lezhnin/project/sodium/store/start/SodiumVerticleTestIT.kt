package com.lezhnin.project.sodium.store.start

import com.lezhnin.project.sodium.store.Sodium
import com.lezhnin.project.sodium.store.Store
import com.lezhnin.project.sodium.store.Store.STORES
import com.lezhnin.project.sodium.store.Web
import com.lezhnin.project.sodium.store.Web.PORT
import com.lezhnin.project.sodium.store.start.TestData.TEST_PORT
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
import org.awaitility.Awaitility
import org.awaitility.Duration
import org.awaitility.Duration.ONE_SECOND
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.until
import org.awaitility.kotlin.withPollInterval
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit.SECONDS

object TestData {
    const val TEST0 = "test0"
    const val TEST1 = "test1"
    const val TEST2 = "test2"
    const val TEST1V = "Test 1"
    const val TEST2V = "Test 2"
    const val UNTEST = "untest"
    const val TEST_PORT = 8081

    val config = json {
        obj(
            PORT to TEST_PORT,
            STORES to array(
                obj(
                    Store.TYPE to Store.Type.JSON,
                    Store.CONFIG to obj(
                        TEST0 to obj(
                            STORES to array(
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

    fun waitForAsyncMap(vertx: Vertx, mapName: String, retries: Int = 5, minSize: Int = 1) {
        Awaitility.with().conditionEvaluationListener {
            logger.warn("Waiting for $mapName for ${it.elapsedTimeInMS}ms...")
        } atMost Duration(retries.toLong(), SECONDS) withPollInterval ONE_SECOND until {
            val mapSizeFuture = CompletableFuture<Int>()
            vertx.sharedData()
                .getAsyncMap<String, JsonObject>(mapName) { map ->
                    if (map.succeeded()) {
                        map.result().size { size ->
                            if (size.succeeded()) {
                                mapSizeFuture.complete(size.result())
                            } else {
                                logger.warn("Can't get size of $mapName!", size.cause())
                                mapSizeFuture.complete(0)
                            }
                        }
                    } else {
                        logger.warn("Can't get $mapName!", map.cause())
                        mapSizeFuture.complete(0)
                    }
                }
            mapSizeFuture.get() >= minSize
        }
    }

    override fun afterSpec(description: Description, spec: Spec) {
        vertx.undeploy(sodiumVerticle.deploymentID())
    }

    override fun beforeSpec(description: Description, spec: Spec) {
        val options = DeploymentOptions().setConfig(TestData.config)
        vertx.deployVerticle(sodiumVerticle, options)
        waitForAsyncMap(vertx, Sodium.DEFAULT_MAP_NAME)
    }
}

class SodiumVerticleTestIT : StringSpec() {

    override fun listeners(): List<TestListener> = listOf(VertxTester)

    init {
        "success" {
            val jsonFuture = CompletableFuture<JsonObject>()

            VertxTester.client.get(TEST_PORT, "localhost", "${Web.PATH}${TestData.TEST0}").send {
                if (it.succeeded()) {
                    try {
                        jsonFuture.complete(it.result().bodyAsJsonObject())
                    } catch (e: Exception) {
                        jsonFuture.completeExceptionally(e)
                    }
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

            VertxTester.client.get(TEST_PORT, "localhost", "${Web.PATH}${TestData.UNTEST}").send {
                if (it.succeeded()) {
                    responseFuture.complete(it.result())
                } else {
                    responseFuture.completeExceptionally(it.cause())
                }
            }

            whenReady(responseFuture) {
                it.statusCode() shouldBe 404
                it.statusMessage() shouldBe "Found no value in map: ${Sodium.DEFAULT_MAP_NAME} for key: ${TestData.UNTEST}"
            }
        }
    }
}
