package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.sodium.store.Web
import com.lezhnin.project.vertx.web.*
import io.vertx.core.Future
import io.vertx.core.Future.future
import io.vertx.core.Future.succeededFuture
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.ext.web.RoutingContext

class SecondaryKeyRequestHandler(private val primary: RequestHandler) : RequestHandler {
    override fun handle(context: RoutingContext, config: JsonObject, logger: Logger): Future<RequestResult> {
        val secondaryKey = context.request().getParam(Web.SECONDARY_PARAMETER) ?: return succeededFuture(
            FailedRequestResult(
                status = RequestStatus(
                    404,
                    "Request parameter ${Web.SECONDARY_PARAMETER} is not found!"
                )
            )
        )
        val primaryFuture = primary.handle(context, config, logger)
        val resultFuture = future<RequestResult>()

        primaryFuture.setHandler { primaryJson ->
            if (primaryJson.succeeded()) {
                when (val request = primaryJson.result()) {
                    is JsonRequestResult -> completeFuture(resultFuture, request.result, secondaryKey, logger)
                    is FailedRequestResult -> resultFuture.complete(request)
                }
            } else {
                resultFuture.fail(primaryJson.cause())
            }
        }

        return resultFuture
    }

    private fun completeFuture(future: Future<RequestResult>, result: JsonObject, key: String, logger: Logger) {
        if (result.containsKey(key)) {
            try {
                future.complete(
                    JsonRequestResult(result.getJsonObject(key))
                )
                logger.info("Found value for key: $key")
            } catch (e: ClassCastException) {
                future.complete(
                    FailedRequestResult(
                        status = RequestStatus(404, "The value for key: $key is not JSON")
                    )
                )
            }
        } else {
            future.complete(
                FailedRequestResult(
                    status = RequestStatus(404, "Found no value for key: $key")
                )
            )
        }
    }
}