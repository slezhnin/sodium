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
import io.vertx.ext.web.client.HttpResponse
import io.vertx.ext.web.client.WebClient
import io.vertx.kotlin.core.json.array
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import java.util.concurrent.CompletableFuture

object TestGitData {
    val config = json {
        obj(
                Store.STORES to array(
                        obj(
                                Store.TYPE to Store.Type.GIT,
                                Store.CONFIG to obj(
                                        "url" to "https://github.com/slezhnin/sodium.git",
                                        "branch" to "test-master-config",
                                        "path" to "target/test-master-config",
                                        "filesets" to array(
                                                obj("pattern" to "*.json")
                                        )
                                )
                        )
                )
        )
    }
}

object VertxGitTester : TestListener {
    private val vertx = Vertx.vertx()!!
    val client = WebClient.create(vertx)!!
    private val sodiumVerticle = SodiumVerticle()

    override fun afterSpec(description: Description, spec: Spec) {
        vertx.undeploy(sodiumVerticle.deploymentID())
    }

    override fun beforeSpec(description: Description, spec: Spec) {
        val options = DeploymentOptions().setConfig(TestGitData.config)
        vertx.deployVerticle(sodiumVerticle, options)
        VertxTester.waitForAsyncMap(vertx, Sodium.MAP_NAME, 10, 2)
    }
}

class SodiumVerticleGitTestFT : StringSpec() {

    override fun listeners(): List<TestListener> = listOf(VertxGitTester)

    init {
        "success for dictionary 0" {
            val jsonFuture = CompletableFuture<JsonObject>()

            VertxGitTester.client.get(8080, "localhost", "${Web.PATH}TEST0").send {
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

        "success for dictionary 1" {
            val jsonFuture = CompletableFuture<JsonObject>()

            VertxGitTester.client.get(8080, "localhost", "${Web.PATH}TEST1").send {
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
