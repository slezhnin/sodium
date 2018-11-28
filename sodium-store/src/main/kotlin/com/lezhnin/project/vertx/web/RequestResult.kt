package com.lezhnin.project.vertx.web

import io.vertx.core.json.JsonObject

interface RequestResult {
    val headers: Map<String, String>
}

data class JsonRequestResult(
    val result: JsonObject,
    override val headers: Map<String, String> = emptyMap()
) : RequestResult


data class FailedRequestResult(
    val status: RequestStatus,
    override val headers: Map<String, String> = emptyMap()
) : RequestResult
