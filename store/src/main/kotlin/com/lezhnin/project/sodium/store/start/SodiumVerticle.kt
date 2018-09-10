package com.lezhnin.project.sodium.store.start

import com.lezhnin.project.sodium.store.manager.ManagerVerticle
import com.lezhnin.project.sodium.store.reader.ReaderVerticle
import io.vertx.core.AbstractVerticle
import io.vertx.core.CompositeFuture
import io.vertx.core.Future
import io.vertx.core.Launcher

class SodiumVerticle : AbstractVerticle() {
    private var readerId = ""
    private var managerId = ""

    override fun start() {
        val readerFuture = Future.future<String>()
        val managerFuture = Future.future<String>()

        getVertx().deployVerticle(ReaderVerticle(), readerFuture.completer())
        getVertx().deployVerticle(ManagerVerticle(), managerFuture.completer())

        CompositeFuture.join(readerFuture, managerFuture).setHandler {
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
