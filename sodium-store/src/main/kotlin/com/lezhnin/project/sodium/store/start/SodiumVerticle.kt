package com.lezhnin.project.sodium.store.start

import com.lezhnin.project.sodium.store.Store
import com.lezhnin.project.sodium.store.manager.ManagerVerticle
import com.lezhnin.project.sodium.store.reader.ReaderVerticle
import io.vertx.core.AbstractVerticle
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Launcher
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

class SodiumVerticle : AbstractVerticle() {
    private val reader = ReaderVerticle()
    private val manager = ManagerVerticle()

    override fun start(startFuture: Future<Void>) {
        val options = DeploymentOptions(json { obj(Store.CONFIG to config()) })

        getVertx().deployVerticle(reader, options)
        getVertx().deployVerticle(manager, options)
    }

    override fun stop() {
        getVertx().undeploy(manager.deploymentID())
        getVertx().undeploy(reader.deploymentID())
    }
}

fun main(args: Array<String>) {
    Launcher.main(arrayOf("run", SodiumVerticle::class.java.canonicalName))
}
