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
        val primaryKeyRequestHandler = PrimaryKeyRequestHandler()
        val secondaryKeyRequestHandler = SecondaryKeyRequestHandler(primaryKeyRequestHandler)

        router
            .route("${Web.PATH}:${Web.PARAMETER}")
            .handler(
                DefaultRequestHandler(primaryKeyRequestHandler, config(), logger)
            )
        router
            .route("${Web.PATH}:${Web.PARAMETER}/:${Web.SECONDARY_PARAMETER}")
            .handler(
                DefaultRequestHandler(secondaryKeyRequestHandler, config(), logger)
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
