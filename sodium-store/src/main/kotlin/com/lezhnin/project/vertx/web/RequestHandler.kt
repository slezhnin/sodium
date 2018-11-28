package com.lezhnin.project.vertx.web

import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.ext.web.RoutingContext

interface RequestHandler {
    fun handle(context: RoutingContext, config: JsonObject, logger: Logger): Future<RequestResult>
}
