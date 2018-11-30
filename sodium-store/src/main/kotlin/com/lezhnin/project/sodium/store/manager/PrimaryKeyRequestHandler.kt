package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.sodium.store.Web
import com.lezhnin.project.vertx.web.FailedRequestResult
import com.lezhnin.project.vertx.web.RequestHandler
import com.lezhnin.project.vertx.web.RequestResult
import com.lezhnin.project.vertx.web.RequestStatus
import io.vertx.core.Future
import io.vertx.core.Future.succeededFuture
import io.vertx.core.logging.Logger
import io.vertx.ext.web.RoutingContext

class PrimaryKeyRequestHandler(private val dataService: DataService, val logger: Logger) : RequestHandler {
    override fun handle(context: RoutingContext): Future<RequestResult> {
        val primaryKey = context.request().getParam(Web.PARAMETER) ?: return succeededFuture(
            FailedRequestResult(
                status = RequestStatus(
                    404,
                    "Request parameter ${Web.PARAMETER} is not found!"
                )
            )
        )

        return dataService.request(primaryKey)
    }
}