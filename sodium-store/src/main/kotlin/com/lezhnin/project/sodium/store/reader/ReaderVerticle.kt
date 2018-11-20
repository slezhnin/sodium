package com.lezhnin.project.sodium.store.reader

import io.vertx.config.ConfigRetriever
import io.vertx.core.AbstractVerticle
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.kotlin.config.ConfigRetrieverOptions

class ReaderVerticle : AbstractVerticle() {
    private var masterConfig: MasterConfig? = null

    override fun start() {
        val logger = LoggerFactory.getLogger(ReaderVerticle::class.java)
        val options = ConfigRetrieverOptions(
            includeDefaultStores = true,
            scanPeriod = 5000
        )
        logger.debug("config(): {}", config().encodePrettily())
        val retriever = ConfigRetriever.create(getVertx(), options)

        retriever.getConfig {
            if (it.succeeded()) {
                replaceMaserConfig(it.result(), logger)
            } else {
                logger.error("Error reading config!", it.cause())
            }
        }
        retriever.listen {
            logger.debug("Config changed: $it")
            replaceMaserConfig(it.newConfiguration, logger)
        }
    }

    private fun replaceMaserConfig(config: JsonObject, logger: Logger) {
        masterConfig?.close()
        masterConfig = MasterConfig(config, config(), getVertx(), logger)
    }
}
