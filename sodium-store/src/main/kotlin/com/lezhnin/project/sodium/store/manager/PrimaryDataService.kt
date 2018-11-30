package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.vertx.web.FailedRequestResult
import com.lezhnin.project.vertx.web.JsonRequestResult
import com.lezhnin.project.vertx.web.RequestResult
import com.lezhnin.project.vertx.web.RequestStatus
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger

class PrimaryDataService(val vertx: Vertx, private val mapName: String, val logger: Logger) : DataService {
    override fun chain(key: String) = SecondaryDataService(request(key), logger)

    override fun request(key: String): Future<RequestResult> {
        val resultFuture = Future.future<RequestResult>()

        vertx.sharedData().getAsyncMap<String, JsonObject>(mapName) { asyncMap ->
            if (asyncMap.succeeded()) {
                asyncMap.result().get(key) { json ->
                    if (json.succeeded() && json.result() != null) {
                        resultFuture.complete(
                            JsonRequestResult(json.result())
                        )
                        logger.info("Found value in map: $mapName for name: $key")
                    } else {
                        resultFuture.complete(
                            FailedRequestResult(
                                status = RequestStatus(
                                    404,
                                    "Found no value in map: $mapName for name: $key"
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