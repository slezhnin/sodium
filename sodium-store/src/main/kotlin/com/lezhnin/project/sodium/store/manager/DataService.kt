package com.lezhnin.project.sodium.store.manager

import com.lezhnin.project.vertx.web.RequestResult
import io.vertx.core.Future

interface DataService {
    fun request(key: String): Future<RequestResult>
    fun chain(key: String): DataService
}