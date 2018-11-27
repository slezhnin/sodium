package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.sodium.store.Sodium
import com.lezhnin.project.sodium.store.Web
import io.vertx.core.AbstractVerticle
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router

class ManagerVerticle : AbstractVerticle() {
    override fun start() {
        val logger = LoggerFactory.getLogger(ManagerVerticle::class.java)!!
        val router = Router.router(getVertx())
        val dataForKeyService = DataForKeyService(logger)
        val dataForPathService = DataForPathService(dataForKeyService, logger)

        router
            .route(
                "${Web.PATH}:${Web.PARAMETER}"
            )
            .handler(
                ManagerDataRequestHandler(
                    config().getString(Sodium.MAP_NAME, Sodium.DEFAULT_MAP_NAME),
                    dataForKeyService,
                    logger
                )
            )
        router
            .routeWithRegex(
                "${Web.PATH.replace("/", "\\/")}(?<${Web.PARAMETER}>[^\\/]+)/(?<${Web.VALUE_PATH}>.+)"
            )
            .handler(
                ManagerDataRequestHandler(
                    config().getString(Sodium.MAP_NAME, Sodium.DEFAULT_MAP_NAME),
                    dataForPathService,
                    logger
                )
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
