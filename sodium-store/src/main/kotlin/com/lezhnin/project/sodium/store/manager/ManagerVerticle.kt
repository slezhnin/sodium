package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.sodium.store.Web
import com.lezhnin.project.vertx.web.DefaultRequestHandler
import io.vertx.core.AbstractVerticle
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router

class ManagerVerticle : AbstractVerticle() {
    override fun start() {
        val logger = LoggerFactory.getLogger(ManagerVerticle::class.java)!!
        val router = Router.router(getVertx())

        router
            .route("${Web.PATH}:${Web.PARAMETER}")
            .handler(
                DefaultRequestHandler(PrimaryKeyRequestHandler(), config(), logger)
            )

        getVertx().createHttpServer().requestHandler {
            router.accept(it)
        }.exceptionHandler {
            logger.error("Connection error!", it)
        }.listen(
            config().getInteger(Web.PORT, 8080)
        )
    }
}
