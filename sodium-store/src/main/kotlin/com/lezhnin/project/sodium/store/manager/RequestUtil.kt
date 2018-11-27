package com.lezhnin.project.sodium.store.manager

import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject

object RequestUtil {
    fun endWithValueFound(response: HttpServerResponse, value: JsonObject) {
        value.toBuffer().apply {
            response.putHeader("content-type", "application/json")
                .putHeader("content-length", length().toString())
                .write(this)
                .end()
        }
    }

    fun endWithValueNotFound(response: HttpServerResponse, key: String) {
        response.setStatusCode(404)
            .setStatusMessage("Error get value for: $key")
            .end()
    }
}