package com.lezhnin.project.sodium.store.start

import com.lezhnin.project.sodium.store.manager.ManagerVerticle
import com.lezhnin.project.sodium.store.reader.ReaderVerticle
import io.vertx.core.*
import io.vertx.core.json.JsonObject

class SodiumVerticle : AbstractVerticle() {
    private var readerId = ""
    private var managerId = ""

    override fun start(startFuture: Future<Void>) {
            val readerFuture = Future.future<String>()
            val managerFuture = Future.future<String>()
            val options = DeploymentOptions(JsonObject().put("config", config()))

            getVertx().deployVerticle(ReaderVerticle(), options, readerFuture.completer())
            getVertx().deployVerticle(ManagerVerticle(), options, managerFuture.completer())

            CompositeFuture.join(readerFuture, managerFuture).setHandler {
                startFuture.complete()
                if (it.succeeded()) {
                    readerId = it.result().resultAt(0)
                    managerId = it.result().resultAt(1)
                } else {
                    throw it.cause()
                }
            }
    }

    override fun stop() {
        if (managerId != "") {
            getVertx().undeploy(managerId)
        }
        if (readerId != "") {
            getVertx().undeploy(readerId)
        }
    }
}

fun main(args: Array<String>) {
    Launcher.main(arrayOf("run", SodiumVerticle::class.java.canonicalName))
}
