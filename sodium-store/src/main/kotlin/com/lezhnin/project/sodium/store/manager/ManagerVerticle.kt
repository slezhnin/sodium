package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.sodium.store.Web
import io.vertx.core.AbstractVerticle
import io.vertx.ext.web.Router

class ManagerVerticle : AbstractVerticle() {
    override fun start() {
        val router = Router.router(getVertx())

        router
            .route("${Web.PATH}:${Web.PARAMETER}")
            .handler(ManagerDataRequestHandler())

        getVertx().createHttpServer().requestHandler {
            Router.router(getVertx()).accept(it)
        }.listen(
            config().getInteger(Web.PORT, 8080)
        )
    }
}
