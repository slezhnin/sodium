package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.sodium.store.Sodium
import com.lezhnin.project.sodium.store.Web
import com.lezhnin.project.vertx.web.*
import io.vertx.core.Future
import io.vertx.core.Future.future
import io.vertx.core.Future.succeededFuture
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.ext.web.RoutingContext

class PrimaryKeyRequestHandler : RequestHandler {
    override fun handle(context: RoutingContext, config: JsonObject, logger: Logger): Future<RequestResult> {
        val mapName = config.getString(Sodium.MAP_NAME, Sodium.DEFAULT_MAP_NAME)
        val primaryKey = context.request().getParam(Web.PARAMETER) ?: return succeededFuture(
            FailedRequestResult(
                status = RequestStatus(
                    404,
                    "Request parameter ${Web.PARAMETER} is not found!"
                )
            )
        )

        val resultFuture = future<RequestResult>()

        context.vertx().sharedData().getAsyncMap<String, JsonObject>(mapName) { asyncMap ->
            if (asyncMap.succeeded()) {
                asyncMap.result().get(primaryKey) { json ->
                    if (json.succeeded() && json.result() != null) {
                        resultFuture.complete(
                            JsonRequestResult(json.result())
                        )
                        logger.info("Found value in map: $mapName for name: $primaryKey")
                    } else {
                        resultFuture.complete(
                            FailedRequestResult(
                                status = RequestStatus(
                                    404,
                                    "Found no value in map: $mapName for name: $primaryKey"
                                )
                            )
                        )
                    }
                }
            } else {
                resultFuture.fail(asyncMap.cause())
            }
        }

        return resultFuture
    }
}