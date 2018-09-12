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

object VertxTester : TestListener {

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

    private val vertx = Vertx.vertx()!!
    val logger = LoggerFactory.getLogger(VertxTester::class.java)!!
    val client = WebClient.create(vertx)!!
    private val sodiumVerticle = SodiumVerticle()

    private fun waitForMap() {
        (1..5).forEach {
            val future = CompletableFuture<Int>()
            vertx.sharedData()
                    .getAsyncMap<String, JsonObject>(Sodium.MAP_NAME) { map ->
                        if (map.succeeded()) {
                            map.result().size { size ->
                                if (size.succeeded()) {
                                    future.complete(size.result())
                                } else {
                                    logger.warn("Can't get size of ${Sodium.MAP_NAME}!", size.cause())
                                    future.complete(0)
                                }
                            }
                        } else {
                            logger.warn("Can't get ${Sodium.MAP_NAME}!", map.cause())
                            future.complete(0)
                        }
                    }
            if (future.get() > 0) {
                return@forEach
            }
            Thread.sleep(1000)
        }
    }

    override fun afterSpec(description: Description, spec: Spec) {
        vertx.undeploy(sodiumVerticle.deploymentID())
    }

    override fun beforeSpec(description: Description, spec: Spec) {
        val options = DeploymentOptions().setConfig(config)
        vertx.deployVerticle(sodiumVerticle, options)
        waitForMap()
    }
}

class SodiumVerticleTestIT : StringSpec() {

    override fun listeners(): List<TestListener> = listOf(VertxTester)

    init {
        "success" {
            val jsonFuture = CompletableFuture<JsonObject>()

            VertxTester.client.get(8080, "localhost", "${Web.PATH}${VertxTester.TEST}").send {
                if (it.succeeded()) {
                    jsonFuture.complete(it.result().bodyAsJsonObject())
                } else {
                    jsonFuture.completeExceptionally(it.cause())
                }
            }

            whenReady(jsonFuture) {
                VertxTester.logger.info(it)

                it.getString(VertxTester.TEST1) shouldBe VertxTester.TEST1V
                it.getString(VertxTester.TEST2) shouldBe VertxTester.TEST2V
            }
        }

        "failure" {
            val responseFuture = CompletableFuture<HttpResponse<Buffer>>()

            VertxTester.client.get(8080, "localhost", "${Web.PATH}${VertxTester.UNTEST}").send {
                if (it.succeeded()) {
                    responseFuture.complete(it.result())
                } else {
                    responseFuture.completeExceptionally(it.cause())
                }
            }

            whenReady(responseFuture) {
                it.statusCode() shouldBe 404
                it.statusMessage() shouldBe "Error get value for: ${VertxTester.UNTEST}"
            }
        }
    }
}