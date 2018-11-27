package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.sodium.store.Web
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.shareddata.AsyncMap

class DataForKeyService(private val logger: Logger) : DataService {
    override fun serviceData(
        dataMap: AsyncMap<String, JsonObject>,
        request: HttpServerRequest,
        response: HttpServerResponse
    ) {
       request.getParam(Web.PARAMETER).let { key ->
            if (key.isNullOrBlank()) {
                logger.error("Error: empty key", this)
                RequestUtil.endWithValueNotFound(response, key)
            } else {
                dataMap.get(key) { ar ->
                    if (ar.succeeded() && ar.result() != null) {
                        logger.debug("Success get key: $key")
                        RequestUtil.endWithValueFound(response, ar.result())
                    } else {
                        logger.error("Error get key: $key")
                        RequestUtil.endWithValueNotFound(response, key)
                    }
                }
            }
        }
    }
}