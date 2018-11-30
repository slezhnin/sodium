package com.lezhnin.project.vertx.web

import io.vertx.core.Future
import io.vertx.ext.web.RoutingContext

interface RequestHandler {
    fun handle(context: RoutingContext): Future<RequestResult>
}
