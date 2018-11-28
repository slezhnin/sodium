package com.lezhnin.project.vertx.web

import io.vertx.core.buffer.Buffer

data class RequestResult(
    val buffer: Buffer? = null,
    val status: RequestStatus? = null,
    val headers: Map<String, String> = emptyMap()
)
