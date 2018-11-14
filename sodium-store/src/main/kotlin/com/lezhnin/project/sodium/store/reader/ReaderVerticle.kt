package com.lezhnin.project.sodium.store.reader

import com.lezhnin.project.sodium.store.*
import io.vertx.config.ConfigChange
import io.vertx.config.ConfigRetriever
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.config.ConfigRetrieverOptions
import io.vertx.kotlin.config.ConfigStoreOptions

class ReaderVerticle : AbstractVerticle() {
    companion object {
        private val logger = LoggerFactory.getLogger(ReaderVerticle::class.java)
    }

    private var masterRetriever: ConfigRetriever? = null
    private val retrievers = mutableMapOf<String, ConfigRetriever>()

    override fun start() {
        val options = ConfigRetrieverOptions(
                includeDefaultStores = true,
                scanPeriod = 10000
        )
        logger.debug("config(): {}", config().encodePrettily())
        val retriever = ConfigRetriever.create(getVertx(), options)

        retriever.getConfig(this::gotConfig)
        retriever.listen(this::configChanged)
    }

    private fun configChanged(change: ConfigChange) {
        logger.debug("Config changed: $change")
        readConfig(change.newConfiguration)
    }

    private fun gotConfig(config: AsyncResult<JsonObject>) {
        if (config.succeeded()) {
            readConfig(config.result())
        } else {
            logger.error("Error reading config!", config.cause())
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

        return ConfigRetriever.create(getVertx(), options)
    }

    private fun readConfig(json: JsonObject) {
        masterRetriever?.close()
        masterRetriever = configRetriever(json)

        masterRetriever?.getConfig(this::gotMasterConfig)
        masterRetriever?.listen(this::masterConfigChanged)
    }

    private fun masterConfigChanged(change: ConfigChange) {
        logger.debug("Master config changed: $change")
        readConfig(change.newConfiguration)
    }

    private fun gotMasterConfig(config: AsyncResult<JsonObject>) {
        if (config.succeeded()) {
            reloadMasterConfig(config.result())
        } else {
            logger.error("Error reading master config!", config.cause())
        }
    }

    private fun reloadMasterConfig(json: JsonObject) {
        retrievers.forEach { _, retriever -> retriever.close()  }
        retrievers.clear()

        json.fieldNames().forEach { name ->
            val retriever = configRetriever(json.getJsonObject(name))
            val readHandler = ReadHandler(getVertx(), name)

            retriever.getConfig(readHandler)
            retriever.listen(ChangeHandler(readHandler))
            retrievers += name to retriever
        }
    }
}
