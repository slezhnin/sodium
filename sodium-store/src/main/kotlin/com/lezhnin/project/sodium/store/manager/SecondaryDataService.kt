package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.vertx.web.FailedRequestResult
import com.lezhnin.project.vertx.web.JsonRequestResult
import com.lezhnin.project.vertx.web.RequestResult
import com.lezhnin.project.vertx.web.RequestStatus
import io.vertx.core.Future
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger

class SecondaryDataService(private val primaryResult: Future<RequestResult>, val logger: Logger) : DataService {
    override fun chain(key: String): DataService = SecondaryDataService(request(key), logger)

    override fun request(key: String): Future<RequestResult> {
        val resultFuture = Future.future<RequestResult>()

        primaryResult.setHandler { primaryJson ->
            if (primaryJson.succeeded()) {
                when (val request = primaryJson.result()) {
                    is JsonRequestResult -> completeFuture(resultFuture, request.result, key)
                    is FailedRequestResult -> resultFuture.complete(request)
                }
            } else {
                resultFuture.fail(primaryJson.cause())
            }
        }

        return resultFuture
    }

    private fun completeFuture(future: Future<RequestResult>, result: JsonObject, key: String) {
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