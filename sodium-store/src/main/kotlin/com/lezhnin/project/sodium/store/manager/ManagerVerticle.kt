package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.sodium.store.Sodium
import com.lezhnin.project.sodium.store.Web
import com.lezhnin.project.vertx.web.DefaultRequestHandler
import io.vertx.core.AbstractVerticle
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.kotlin.ext.web.handler.sockjs.BridgeOptions
import io.vertx.kotlin.ext.web.handler.sockjs.PermittedOptions

class ManagerVerticle : AbstractVerticle() {
    override fun start() {
        val mapName = config().getString(Sodium.MAP_NAME, Sodium.DEFAULT_MAP_NAME)
        val logger = LoggerFactory.getLogger(ManagerVerticle::class.java)!!
        val router = Router.router(getVertx())
        val dataService = PrimaryDataService(vertx, mapName, logger)
        val primaryKeyRequestHandler = PrimaryKeyRequestHandler(dataService, logger)
        val secondaryKeyRequestHandler = SecondaryKeyRequestHandler(dataService, logger)

        router
            .route()
            .handler(StaticHandler.create().setCachingEnabled(false))
        router
            .route("${Web.PATH}:${Web.PARAMETER}")
            .handler(
                DefaultRequestHandler(primaryKeyRequestHandler, logger)
            )
        router
            .route("${Web.PATH}:${Web.PARAMETER}/:${Web.SECONDARY_PARAMETER}")
            .handler(
                DefaultRequestHandler(secondaryKeyRequestHandler, logger)
            )
        router
            .route("/eventbus/*")
            .handler(
                SockJSHandler
                    .create(vertx)
                    .bridge(
                        BridgeOptions(
                            outboundPermitted = listOf(PermittedOptions(addressRegex = "sodium\\.out.*")),
                            inboundPermitted = listOf(PermittedOptions(address = "sodium.in.request"))
                        )
                    )
            )

        getVertx().createHttpServer().requestHandler {
            router.accept(it)
        }.exceptionHandler {
            logger.error("Connection error!", it)
        }.listen(
            config().getInteger(Web.PORT, 8080)
        )

        vertx.eventBus().consumer<String>("sodium.in.request", EventBusRequestHandler(dataService, logger))
    }
}
