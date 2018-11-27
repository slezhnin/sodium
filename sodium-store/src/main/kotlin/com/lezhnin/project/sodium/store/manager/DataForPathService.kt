package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.sodium.store.Web
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.shareddata.AsyncMap

class DataForPathService(private val parentService: DataService, val logger: Logger) : DataService {
    override fun serviceData(
        dataMap: AsyncMap<String, JsonObject>,
        request: HttpServerRequest,
        response: HttpServerResponse
    ) {
        request.getParam(Web.PARAMETER).let { key ->
            request.getParam(Web.VALUE_PATH).let { path ->
                val pathList = path
                    ?.split("/")
                    ?.filterNot(String::isBlank)
                    .orEmpty()

                if (key.isNullOrBlank() || pathList.isEmpty()) {
                    parentService.serviceData(dataMap, request, response)
                } else {
                    dataMap.get(key) { ar ->
                        if (ar.succeeded() && ar.result() != null) {
                            logger.debug("Success get key: $key")
                            endWithPath(response, ar.result(), key, pathList)
                        } else {
                            logger.error("Error get key: $key")
                            RequestUtil.endWithValueNotFound(response, key)
                        }
                    }
                }
            }
        }
    }


    private fun endWithPath(response: HttpServerResponse, value: JsonObject, key: String, path: List<String>) {
        val result = try {
            path.fold(value) { r: JsonObject?, k: String ->
                r?.getJsonObject(k)
            }
        } catch (x: ClassCastException) {
            null
        }

        if (result == null) {
            logger.error("Error get key: $key path: $path")
            RequestUtil.endWithValueNotFound(response, key)
        } else {
            RequestUtil.endWithValueFound(response, result)
        }
    }
}