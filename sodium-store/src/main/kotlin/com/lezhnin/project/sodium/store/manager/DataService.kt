package com.lezhnin.project.sodium.store.manager

import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonObject
import io.vertx.core.shareddata.AsyncMap

interface DataService {
    fun serviceData(
        dataMap: AsyncMap<String, JsonObject>,
        request: HttpServerRequest,
        response: HttpServerResponse
    )
}