package com.lezhnin.project.sodium.store.reader

import com.lezhnin.project.sodium.store.Sodium
import com.lezhnin.project.sodium.store.Store
import io.vertx.config.ConfigRetriever
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.kotlin.config.ConfigRetrieverOptions
import io.vertx.kotlin.config.ConfigStoreOptions

class MasterConfig(
    config: JsonObject,
    private val mainConfig: JsonObject,
    private val vertx: Vertx,
    private val logger: Logger
) {
    private var retriever: ConfigRetriever
    private val retrievers = mutableMapOf<String, ConfigRetriever>()

    init {
        retriever = configRetriever(config)

        retriever.getConfig {
            if (it.succeeded()) {
                reloadMasterConfig(it.result())
            } else {
                logger.error("Error reading master config!", it.cause())
            }
        }
        retriever.listen {
            logger.debug("Master config changed: $it")
            reloadMasterConfig(it.newConfiguration)
        }

    }

    fun close() {
        retriever.close()
        retrievers.forEach { _, retriever -> retriever.close() }
    }

    private fun reloadMasterConfig(json: JsonObject) {
        retrievers.forEach { _, retriever -> retriever.close() }
        retrievers.clear()

        json.fieldNames().forEach { key ->
            val retriever = configRetriever(json.getJsonObject(key))
            val readHandler = ReadHandler(
                vertx,
                mainConfig.getString(Sodium.MAP_NAME, Sodium.DEFAULT_MAP_NAME),
                key,
                logger
            )

            retriever.getConfig(readHandler)
            retriever.listen(ChangeHandler(readHandler))
            retrievers += key to retriever
        }
    }

    private fun configRetriever(json: JsonObject): ConfigRetriever {
        val stores = json.getJsonArray(Store.STORES).map { obj ->
            val store = obj as JsonObject
            ConfigStoreOptions(
                type = store.getString(Store.TYPE),
                format = store.getString(Store.FORMAT),
                config = store.getJsonObject(Store.CONFIG)
            )
        }
        val options = ConfigRetrieverOptions(
            scanPeriod = json.getLong(Store.SCAN_PERIOD, 5000),
            stores = stores
        )

        return ConfigRetriever.create(vertx, options)
    }
}